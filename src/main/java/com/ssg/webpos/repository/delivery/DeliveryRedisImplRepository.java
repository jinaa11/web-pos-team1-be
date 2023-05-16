package com.ssg.webpos.repository.delivery;

import com.ssg.webpos.domain.PosStoreCompositeId;
import com.ssg.webpos.domain.enums.DeliveryType;
import com.ssg.webpos.dto.DeliveryAddDTO;
import com.ssg.webpos.dto.PointDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DeliveryRedisImplRepository implements DeliveryRedisRepository {
  private RedisTemplate<String, Map<String, List<Object>>> redisTemplate;
  private HashOperations hashOperations;

  public DeliveryRedisImplRepository(RedisTemplate<String, Map<String, List<Object>>> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.hashOperations = redisTemplate.opsForHash();
  }

  public void saveDelivery(@NotNull DeliveryAddDTO deliveryDTO) {
    String storeId = String.valueOf(deliveryDTO.getStoreId());
    String posId = String.valueOf(deliveryDTO.getPosId());
    String compositeId = posId + "-" + storeId;

    System.out.println("compositeId = " + compositeId);

    Map<String, List<Object>> posData = (Map<String, List<Object>>) hashOperations.get("CART", compositeId);
    if (posData == null) {
      posData = new HashMap<>();
    }

    List<Object> deliveryList = new ArrayList<>();
    deliveryDTO.setDeliveryType(DeliveryType.DELIVERY);
    deliveryList.add(deliveryDTO);
    posData.put("delivery", deliveryList);
    System.out.println("posData = " + posData);
    hashOperations.put("CART", compositeId, posData);
  }

  @Override
  public Map<String, Map<String, List<Object>>> findAll() throws Exception {
    Map<String, Map<String, List<Object>>> result = new HashMap<>();
    Map<String, Map<String, List<Object>>> posData = hashOperations.entries("CART");
    for (Map.Entry<String, Map<String, List<Object>>> entry : posData.entrySet()) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  @Override
  public Map<String, List<Object>> findById(String id) {
    return (Map<String, List<Object>>) hashOperations.get("CART", id);
  }

  @Override
  public void delete(String id) {
    hashOperations.delete("CART", id);
  }

  @Override
  public void deleteAll() {
    redisTemplate.delete("CART");
  }

  @Override
  public List<String> findByUserId() {
    List<String> user = new ArrayList<>();
    Map<String, Map<String, List<Object>>> posDataMap = hashOperations.entries("CART");
    System.out.println("posDataMap = " + posDataMap);
    for (Map.Entry<String, Map<String, List<Object>>> entry : posDataMap.entrySet()) {
      Map<String, List<Object>> posData = entry.getValue();
      if (posData != null) {
        List<Object> userIdList = posData.get("userId");
        if (userIdList != null && !userIdList.isEmpty()) {
          Long userId = (Long) userIdList.get(0); // userId 값을 Long으로 가져옴
          user.add(String.valueOf(userId));
        }
      }
    }
    return user;
  }
}