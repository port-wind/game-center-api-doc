package com.pw.game.components.tools;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.pw.game.components.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Description jwt工具类
 * @Author gavin
 * @Date Created in 2022/10/18
 */
@Slf4j
public class JWTUtil {

    /**
     * access token
     */
    private static final String ACCESS_TOKEN_FLAG = "atf";
    /**
     * Refresh token
     */
    private static final String REFRESH_TOKEN_FLAG = "rtf";

    /**
     * 生成token
     *
     * @param secret    秘钥
     * @param timestamp 过期时间
     * @param params    加密参数
     */
    public static String generateToken(String secret, long timestamp, Map<String, String> params) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(timestamp);
        JWTCreator.Builder builder = JWT.create().withSubject(ACCESS_TOKEN_FLAG);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.withClaim(entry.getKey(), entry.getValue());
        }
        return builder.withExpiresAt(instance.getTime())
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * 生成token
     *
     * @param secret    秘钥
     * @param timestamp 过期时间
     * @param params    加密参数
     */
    public static String generateRefreshToken(String secret, long timestamp, Map<String, String> params) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(timestamp);
        JWTCreator.Builder builder = JWT.create().withSubject(REFRESH_TOKEN_FLAG);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.withClaim(entry.getKey(), entry.getValue());
        }
        return builder.withExpiresAt(instance.getTime())
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * token校验
     *
     * @param secret   密钥
     * @param token    token
     * @param username 用户名
     */
    public static boolean verify(String secret, String token, String username) {
        JWTVerifier build = JWT.require(Algorithm.HMAC256(secret)).build();
        try {
            if (Objects.equals(username, build.verify(token).getClaims().get("username").asString())) {
                return true;
            }
        } catch (Exception e) {
            log.error("解析token失败, errorMsg:{}", e.getMessage());
            throw BizException.E10006;
        }
        return false;
    }


    /**
     * 解析token获取用户登录上下文
     *
     * @param secret 密钥
     * @param token  token
     */
    public static Map<String, String> resolveToken(String secret, String token) {
        JWTVerifier build = JWT.require(Algorithm.HMAC256(secret)).withSubject(ACCESS_TOKEN_FLAG).build();
        try {
            Map<String, String> map = new HashMap<>();
            build.verify(token).getClaims().forEach((key, value) -> {
                map.put(key, value.asString());
            });
            return map;
        } catch (Exception e) {
            if (e instanceof TokenExpiredException) {
                log.warn("token过期了, {}", e.getMessage());
                throw BizException.E10006;
            }
            log.error("解析token失败, errorMsg:{}", e.getMessage());
            throw BizException.E10006;
        }
    }

    /**
     * 解析token获取用户登录上下文
     *
     * @param secret 密钥
     * @param token  token
     */
    public static Map<String, String> resolveRefreshToken(String secret, String token) {
        JWTVerifier build = JWT.require(Algorithm.HMAC256(secret)).withSubject(REFRESH_TOKEN_FLAG).build();
        try {
            Map<String, String> map = new HashMap<>();
            build.verify(token).getClaims().forEach((key, value) -> {
                map.put(key, value.asString());
            });
            return map;
        } catch (Exception e) {
            log.error("解析refresh token失败, errorMsg:{}", e.getMessage());
            throw BizException.E10006;
        }
    }

    public static void main(String[] args) {
        //Map<String, String> stringStringMap = resolveToken("fengyuda123456@@", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhdGYiLCJsb2dpblRpbWUiOiIxNzI1MDAzMDI2OTEwIiwia29sVXNlcklkIjoiMTAyNDAxMDQyMTI5MiIsImlwIjoiMTkyLjE2OC4xMC4xMSIsImlkIjoiMTAyNDAxMDQyMTI5MiIsInVzZXJuYW1lIjoicm9jazAwMDAiLCJjaWQiOiJjNzM0YTk1ZDlmOWI2ZDkzNjJjYjU0ZDA4ZWYxZTI5YzZkYTdmOGQ3ZWUiLCJleHAiOjE3MjU2MDc4MjZ9.CfK6PFUnkecnphb4ZvmuBR5ChV1DgEQ-XOLYGyYQbdM");
        //System.out.println(stringStringMap);
        Map<String,String> map = new HashMap<>();
        map.put("service","game-proxy");
        map.put("env","dev");
        System.out.println(generateToken("game-proxy-10086",System.currentTimeMillis()+100000000,map));
        System.out.println(resolveToken("game-proxy-10086","eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI4MDllODZkNS02NjVkLTQ0MGMtYTdiYy1kZWVlMDJjZTA4NGMiLCJpYXQiOjE3NTY4NzE2NDksImV4cCI6MTc1Njg3MzQ0OX0.kagUqwzp_-QoMHliuUmVqsreAppbp5cGHH_cvsEofU0"));
    }

    /**
     * 解析token获取用户名
     *
     * @param secret 密钥
     * @param token  token
     */
    public static String getUsername(String secret, String token) {
        JWTVerifier build = JWT.require(Algorithm.HMAC256(secret)).withSubject(ACCESS_TOKEN_FLAG).build();
        try {
            return build.verify(token).getClaims().get("username").asString();
        } catch (Exception e) {
            log.error("解析token失败, errorMsg:{}", e.getMessage());
            throw BizException.E10006;
        }
    }

    /**
     * 解析token获取用户编号
     *
     * @param secret 密钥
     * @param token  token
     */
    public static Long jwtGetUserId(String secret, String token) {
        JWTVerifier build = JWT.require(Algorithm.HMAC256(secret)).withSubject(ACCESS_TOKEN_FLAG).build();
        try {
            Map<String, Claim> claims = build.verify(token).getClaims();
            return Long.parseLong(claims.get("id").asString());
        } catch (Exception e) {
            log.error("解析token失败, errorMsg:{}", e.getMessage());
            throw BizException.E10006;
        }
    }

    /**
     * 生成一个16位的随机数
     *
     * @return
     */
    public static String getGUID() {
        return String.valueOf(ThreadLocalRandom.current().nextLong(10000000L, 99999999));
    }

}
