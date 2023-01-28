package com.superdog.music_site.common;

import com.superdog.music_site.config.JwtConfig;
import com.superdog.music_site.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;

public class JwtUtil {

    public static String createToken(User user){
        String token = Jwts.builder()
                .setId(user.getId().toString())
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setIssuer(JwtConfig.issuer)
                .signWith(SignatureAlgorithm.HS512,JwtConfig.secret)
                .claim("msg",encrypt(user.getUsername(), user.getId().toString()))
                .compact();
        return JwtConfig.prefix+token;
    }

    private static String encrypt(String content,String key){
        try{
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(key.getBytes());
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(secureRandom);

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, generator.generateKey());
            byte[] bytes = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(bytes);
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    public static boolean parseAccessToken(String token){
        if(StringUtils.isNoneEmpty(token)){
            token = token.substring(JwtConfig.prefix.length());

            Claims claims = Jwts.parser().setSigningKey(JwtConfig.secret).parseClaimsJws(token).getBody();

            String id = claims.getId();
            String username = claims.getSubject();
            String msg = claims.get("msg").toString();
            return msg != null && msg.equals(encrypt(username, id));
        }
        return false;
    }
}
