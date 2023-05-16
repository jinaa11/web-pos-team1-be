package com.ssg.webpos.controller;

import com.ssg.webpos.domain.PosStoreCompositeId;
import com.ssg.webpos.domain.User;
import com.ssg.webpos.dto.PointDTO;
import com.ssg.webpos.dto.PointRequestDTO;
import com.ssg.webpos.repository.CartRedisRepository;
import com.ssg.webpos.repository.UserRepository;
import com.ssg.webpos.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/point")
public class PointController {

  @Autowired
  CartRedisRepository cartRedisRepository;

  @Autowired
  UserService userService;

  @GetMapping("")
  public ResponseEntity<List<User>> getPointList() throws Exception {
    Map<String, Map<String, List<Object>>> all = cartRedisRepository.findAll();
    System.out.println("all = " + all);
    return new ResponseEntity(all, HttpStatus.OK);
  }

  @PostMapping("/add")
  public ResponseEntity addPoint(@RequestBody @Valid PointRequestDTO requestDTO, BindingResult bindingResult) throws Exception {
    if (bindingResult.hasErrors()) {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    String phoneNumbers = requestDTO.getPhoneNumber();
    String pointMethod = requestDTO.getPointMethod();

    // 요청한 회원이 존재하는지 여부
    boolean isMemberExist = userService.checkMemberExist(phoneNumbers);

    if (isMemberExist) {
      PointDTO pointDTO = new PointDTO();
      pointDTO.setPointMethod(pointMethod);
      pointDTO.setPhoneNumber(phoneNumbers);
      pointDTO.setPosId(requestDTO.getPosId());
      pointDTO.setStoreId(requestDTO.getStoreId());
      cartRedisRepository.savePoint(pointDTO);

      return new ResponseEntity(HttpStatus.OK);
    } else {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    }
  }
}