package com.gis.service;

import com.gis.entity.User;
import com.gis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 获取所有用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 根据ID获取用户
    public User getUserById(@NonNull Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // 根据用户名获取用户
    public User getUserByUsername(@NonNull String username) {
        return userRepository.findByUsername(username);
    }

    // 保存用户
    public User saveUser(@NonNull User user) {
        // 密码加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    // 删除用户
    public void deleteUser(@NonNull Long id) {
        userRepository.deleteById(id);
    }

    // 初始化默认用户
    public void initDefaultUser() {
        // 检查是否已存在用户
        if (userRepository.count() == 0) {
            // 创建默认管理员用户
            User admin = new User("admin", "admin123", "admin", "系统管理员", null);
            saveUser(admin);
        }
    }
}
