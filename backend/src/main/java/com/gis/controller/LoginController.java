package com.gis.controller;

import com.gis.entity.User;
import com.gis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @NonNull LoginRequest loginRequest) {
        try {
            // 检查用户名是否为空
            String username = loginRequest.getUsername();
            if (username == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "用户名不能为空");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
            // 根据用户名获取用户
            User user = userService.getUserByUsername(username);
            
            // 验证用户是否存在且密码是否正确
            if (user == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "用户名或密码错误");
                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
            }
            
            // 检查密码是否已经加密
            boolean passwordMatch = false;
            if (user.getPassword().length() >= 60) {
                // 密码已经加密，使用passwordEncoder.matches验证
                passwordMatch = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
            } else {
                // 密码未加密，直接比较明文
                passwordMatch = loginRequest.getPassword().equals(user.getPassword());
            }
            
            if (!passwordMatch) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "用户名或密码错误");
                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
            }

            // 登录成功，返回用户信息和token（这里使用简单的token生成方式）
            Map<String, Object> response = new HashMap<>();
            response.put("token", "Bearer " + user.getId() + "-" + System.currentTimeMillis());
            response.put("user", user);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "登录失败：" + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 登录请求参数
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
