package com.goomoong.room9backend.callback;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goomoong.room9backend.callback.dto.KakaoOAuth2ResponseDto;
import com.goomoong.room9backend.domain.user.Role;
import com.goomoong.room9backend.domain.user.User;
import com.goomoong.room9backend.repository.user.UserRepository;
import com.goomoong.room9backend.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/oauth2/callback")
public class OAuth2CallbackController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/kakao")
    public RedirectView kakao(@RequestParam String code, @RequestParam String state) {
        Map<String, String> res = new HashMap<>();
        res.put("code", code);
        res.put("state", state);

        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add("grant_type", "authorization_code");
        tokenRequest.add("client_id", "64dfaa62a542bcefe16d09bd77b6ca8c");
        tokenRequest.add("redirect_uri", "http://localhost:8080/oauth2/callback/kakao");
        tokenRequest.add("code", code);

        try {
            RestTemplate getTokenRequest = new RestTemplate();
            Map<String, String> tokenResponse = getTokenRequest.postForObject("https://kauth.kakao.com/oauth/token", tokenRequest, Map.class);
            res.put("access_token", tokenResponse.get("access_token"));

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + tokenResponse.get("access_token"));
            MultiValueMap<String, String> userInfoRequest = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity(userInfoRequest, headers);
            RestTemplate getUserInfoRequest = new RestTemplate();
            ResponseEntity<String> userInfo = getUserInfoRequest.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.GET, entity, String.class);
            res.put("userInfo", userInfo.getBody());

            // 1. DB에 사용자가 있는지 확인
            // 2. 만약 없다면 데이터베이스에 삽입 (회원가입)
            // 3. 만약 있다면 패스
            // 4. 사용자 정보를 데이터베이스 가져옴
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            KakaoOAuth2ResponseDto responseDto = objectMapper.readValue(userInfo.getBody(), KakaoOAuth2ResponseDto.class);

            User verifyUser = userRepository.findByAccountId(responseDto.getId()).orElseGet(() ->
                    userRepository.save(User.toEntity(responseDto.getId(), responseDto.getKakaoAccount().getProfile().getNickname(),
                            responseDto.getKakaoAccount().getProfile().getNickname()+ UUID.randomUUID().toString().substring(25),
                            Role.GUEST, responseDto.getKakaoAccount().getProfile().getThumbnailImageUrl(),
                            responseDto.getKakaoAccount().getEmail(), responseDto.getKakaoAccount().getBirthday(),
                            responseDto.getKakaoAccount().getGender()))
            );

            Map<String, String> requestState = objectMapper.readValue(state, Map.class);
            String redirectUri = requestState.get("redirectUri");
            res.put("redirectUri", redirectUri);

            String jwtToken = jwtTokenProvider.createToken(verifyUser);
            res.put("jwtToken", jwtToken);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String rrredirectUrl = res.get("redirectUri") + "?token=" + res.get("jwtToken");
        RedirectView rv = new RedirectView(rrredirectUrl);
        rv.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        return rv;
    }

}
