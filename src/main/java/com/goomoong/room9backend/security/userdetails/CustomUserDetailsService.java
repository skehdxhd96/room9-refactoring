//package com.goomoong.room9backend.security.userdetails;
//
//import com.goomoong.room9backend.domain.user.User;
//import com.goomoong.room9backend.exception.NoSuchUserException;
//import com.goomoong.room9backend.repository.user.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//@RequiredArgsConstructor
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String nickname) throws UsernameNotFoundException {
//        User user = userRepository.findByNickname(nickname).orElseThrow(() -> new NoSuchUserException("해당 유저가 존재하지 않습니다."));
//        return CustomUserDetails.create(user);
//    }
//
//    public UserDetails loadUserById(Long id) {
//        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchUserException("해당 유저가 존재하지 않습니다."));
//        return CustomUserDetails.create(user);
//    }
//}
