//package com.goomoong.room9backend.security.jwt;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.GenericFilterBean;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//
//@Slf4j
//@RequiredArgsConstructor
//@Component
//public class JwtAuthenticationFilter extends GenericFilterBean {
//
//    private final JwtTokenProvider jwtTokenProvider;
//    private final String AUTHORIZATION_HEADER = "Authorization";
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        String accessToken = resolveToken(httpRequest);
//        if (accessToken != null && jwtTokenProvider.isValidToken(accessToken)) {
//            Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }
//        chain.doFilter(request, response);
//    }
//
//    private String resolveToken(HttpServletRequest request) {
//        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
//        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
//            return bearerToken.substring(7);
//        }
//        return null;
//    }
//}
