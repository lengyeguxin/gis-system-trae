package com.gis.service;

import com.gis.entity.User;
import com.gis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 获取所有用户
    public List<User> getAllUsers() {
        // 暂时返回模拟数据
        List<User> users = new ArrayList<>();
        User admin = new User("admin", "admin123", "ADMIN", "管理员", "admin@example.com", true);
        admin.setId(1L);
        User user1 = new User("user1", "password1", "USER", "用户1", "user1@example.com", true);
        user1.setId(2L);
        users.add(admin);
        users.add(user1);
        return users;
    }

    // 根据ID获取用户
    public User getUserById(@NonNull Long id) {
        // 暂时返回模拟数据
        List<User> users = getAllUsers();
        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    // 根据用户名获取用户
    public User getUserByUsername(@NonNull String username) {
        // 暂时返回模拟数据
        List<User> users = getAllUsers();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    // 保存用户
    public User saveUser(@NonNull User user) {
        // 暂时返回模拟数据
        return user;
    }

    // 删除用户
    public void deleteUser(@NonNull Long id) {
        // 暂时不做任何操作
    }

    // 初始化默认用户
    public void initDefaultUser() {
        // 暂时不做任何操作
    }
}
