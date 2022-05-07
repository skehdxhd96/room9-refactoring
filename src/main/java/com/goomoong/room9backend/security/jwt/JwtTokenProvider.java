//package com.goomoong.room9backend.security.jwt;
//
//import com.goomoong.room9backend.domain.user.User;
//import com.goomoong.room9backend.security.userdetails.CustomUserDetailsService;
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.util.*;
//
//@Slf4j
//@Component
//public class JwtTokenProvider {
//
//    @Autowired
//    private CustomUserDetailsService customUserDetailsService;
//
//    private final String SECRET;
//    private final long ACCESS_TOKEN_EXPIRED_TIME;
//    private final SecretKey secretKey;
//    private final JwtParser jwtParser;
//
//    public JwtTokenProvider(
//            @Value("${jwt.secret}") String secret,
//            @Value("${jwt.access-token-expired-time}") long accessTokenExpiredTime
//    ) {
//        this.SECRET = secret;
//        this.ACCESS_TOKEN_EXPIRED_TIME = accessTokenExpiredTime;
//        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
//        this.jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();
//    }
//
//    public String createToken(User user) {
//
//        return Jwts.builder()
//                .setSubject(user.getId().toString())
//                .claim("auth", user.getRole().getKey())
//                .signWith(secretKey, SignatureAlgorithm.HS256)
//                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRED_TIME))
//                .compact();
//    }
//
//    public Authentication getAuthentication(String token) {
//
//        Claims claims = jwtParser.parseClaimsJws(token).getBody();
//        UserDetails userDetails = customUserDetailsService.loadUserById(Long.parseLong(claims.getSubject()));
//
//        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//    }
//
//    public boolean isValidToken(String token) {
//        try {
//            jwtParser.parseClaimsJws(token);
//            return true;
//        } catch (ExpiredJwtException e) {
//            log.error("토큰이 만료되었습니다.");
//        } catch (IllegalArgumentException e) {
//            log.error("잘못된 토큰입니다.");
//        } catch (Exception e) {
//            log.error("토큰 파싱 오류 발생");
//        }
//        return false;
//    }
//}
