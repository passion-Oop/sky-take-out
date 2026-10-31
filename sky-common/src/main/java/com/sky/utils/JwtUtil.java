package com.sky.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
     * @param secretKey jwt秘钥，长度不得少于 32 字节（HS256要求256位密钥）
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     * @return jwt令牌
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // JJWT 0.12+ 要求HS256的密钥不少于 32 字节，长度不足会直接抛 WeakKeyException
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        // 生成JWT的时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 设置jwt的body
        JwtBuilder builder = Jwts.builder()
                // 如果有私有声明，一定要先设置这个自己创建的私有的声明，一旦写在标准声明赋值之后，就会覆盖那些标准声明
                .claims(claims)
                // 设置签名使用的秘钥和签名算法
                .signWith(key, Jwts.SIG.HS256)
                // 设置过期时间
                .expiration(exp);

        return builder.compact();
    }

    /**
     * Token解密
     *
     * @param secretKey jwt秘钥 此秘钥一定要保留好在服务端, 不能暴露出去, 否则sign就可以被伪造, 如果对接多个客户端建议改造成多个
     * @param token     加密后的token
     * @return 解密后的token
     */
    public static Claims parseJWT(String secretKey, String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        // 设置签名的秘钥，解析并校验签名，返回载荷
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
