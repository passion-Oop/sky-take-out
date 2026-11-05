package com.sky.controller.user;


import com.sky.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("userShopController")
@RequestMapping("/user/shop")
@Tag(name = "店铺状态", description = "店铺状态相关接口")
public class ShopController {

    public static final String KEY = "SHOP_STATUS";
    @Autowired
    RedisTemplate redisTemplate;

    @GetMapping("/status")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("获取店铺运营状态: {}", status == 1 ? "营业中" : "打烊中");
        return  Result.success(status);
    }
}
