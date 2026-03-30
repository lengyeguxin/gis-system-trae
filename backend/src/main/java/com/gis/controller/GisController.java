package com.gis.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "http://localhost:8082")
public class GisController {

    @GetMapping
    public Map<String, String> getMessage() {
        Map<String, String> map = new HashMap<>();
        map.put("message", "GIS Application Backend");
        return map;
    }

}