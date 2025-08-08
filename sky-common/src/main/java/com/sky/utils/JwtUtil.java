package com.sky.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    /**
     * 创建JWT
     * @param secretKey JWT密钥
     * @param ttlMillis 有效期（毫秒）
     * @param claims    私有声明
     * @return 生成的JWT字符串
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 1. 定义签名算法（Header）
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        // 2. 构建JWT的Payload部分
        // 计算过期时间
        long nowMillis = System.currentTimeMillis();
        Date exp = new Date(nowMillis + ttlMillis);

        JwtBuilder builder = Jwts.builder()
                // 设置自定义声明。此方法会覆盖所有已有声明，必须最先调用。
                .setClaims(claims)
                // 设置签发时间 (iat)
                .setIssuedAt(new Date(nowMillis))
                // 设置过期时间 (exp)
                .setExpiration(exp);

        // 3. 构建Signature部分，使用指定的算法和密钥对Header和Payload进行签名
        builder.signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8));

        // 返回字符串
        return builder.compact();
    }

    /**
     * Token解密
     *
     * @param secretKey jwt秘钥 此秘钥一定要保留好在服务端, 不能暴露出去, 否则sign就可以被伪造,
     *                  如果对接多个客户端建议改造成多个
     * @param token     加密后的token
     * @return
     */
    public static Claims parseJWT(String secretKey, String token) {
        // 得到DefaultJwtParser
        Claims claims = Jwts.parser()
                // 设置签名的秘钥
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                // 设置需要解析的jwt
                .parseClaimsJws(token).getBody();
        return claims;
    }

}
