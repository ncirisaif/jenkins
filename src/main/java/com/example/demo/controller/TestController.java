package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {


    @RequestMapping("/")
    public Mono<ResponseEntity<String>> test(){
        return Mono.just("sayf").map(ResponseEntity::ok);
    }
}
