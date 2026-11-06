package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    JwtProperties jwtProperties;

    @Autowired
    UserMapper userMapper;

    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {

        String openid = getOpenid(userLoginDTO);

        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        User user = userMapper.getByOpenid(openid);

        if (user == null) {
            user = User.builder()
                     .openid(openid)
                     .createTime(LocalDateTime.now())
                     .build();
            userMapper.insert(user);
        }

        return user;
    }

    private String getOpenid(UserLoginDTO userLoginDTO) {

        Map<String, String> map = new HashMap<>();
        map.put("appid", jwtProperties.getUserSecretKey());
        map.put("secret", jwtProperties.getUserSecretKey());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type", "authorization_code");

        String response = HttpClientUtil.doGet(WX_LOGIN, map);
        JSONObject jsonObject = JSON.parseObject(response);

        return jsonObject.getString("openid");
    }
}
