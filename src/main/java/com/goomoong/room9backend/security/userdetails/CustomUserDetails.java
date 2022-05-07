//package com.goomoong.room9backend.security.userdetails;
//
//import com.goomoong.room9backend.domain.user.User;
//import lombok.Getter;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//
//@Getter
//public class CustomUserDetails implements UserDetails {
//
//    private Long id;
//    private String name;
//    private String nickname;
//    private String accountId;
//    private Collection<? extends GrantedAuthority> authorities;
//    private User user;
//
//    public CustomUserDetails(Long id, String name, String nickname, String accountId, Collection<? extends GrantedAuthority> authorities, User user) {
//        this.id = id;
//        this.name = name;
//        this.nickname = nickname;
//        this.accountId = accountId;
//        this.authorities = authorities;
//        this.user = user;
//    }
//
//    public static CustomUserDetails create(User user) {
//        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getKey()));
//
//        return new CustomUserDetails(
//                user.getId(),
//                user.getName(),
//                user.getNickname(),
//                user.getAccountId(),
//                authorities,
//                user
//        );
//    }
//
//    @Override
//    public String getUsername() {
//        return nickname;
//    }
//
//    @Override
//    public String getPassword() {
//        return "";
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }
//}
