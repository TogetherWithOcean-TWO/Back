package com.togetherwithocean.TWO.OAuth.Controller;


import com.togetherwithocean.TWO.Jwt.TokenDto;
import com.togetherwithocean.TWO.Member.DTO.MemberRes;
import com.togetherwithocean.TWO.Member.Domain.Member;
import com.togetherwithocean.TWO.Member.Repository.MemberRepository;
import com.togetherwithocean.TWO.Member.Service.MemberService;
import com.togetherwithocean.TWO.OAuth.DTO.KakaoUserInfo;
import com.togetherwithocean.TWO.OAuth.Service.KakaoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
@PropertySource("classpath:/application.properties")
public class AuthController {
    @Value("${redirectURI}")
    private String redirectUri;
    @Value("${clientID}")
    private String clientId;
    private final KakaoService kakaoService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @GetMapping("/kakao/login")
    public ResponseEntity<String> kakaoLogin() {
        System.out.println("kakao login");
        String kakaoLoginUrl =
                "https://kauth.kakao.com/oauth/authorize?client_id=" + clientId
                        + "&redirect_uri=" + redirectUri + "&response_type=code";
        return ResponseEntity.status(HttpStatus.OK).body(kakaoLoginUrl);
    }

    @GetMapping("/kakao/callback")
    public @ResponseBody ResponseEntity<MemberRes> kakaoCallback(@RequestParam String code, HttpServletResponse response) {
        System.out.println("kakao callback");
        String accessToken = kakaoService.getAccessToken(code);
        KakaoUserInfo kakaoUserInfo = kakaoService.getUserInfoByAccessToken(accessToken);
        System.out.println(kakaoUserInfo.getName());
        System.out.println(kakaoUserInfo.getEmail());
        System.out.println(kakaoUserInfo.getPhoneNumber());

        // 카카오에서 가져온 정보 바탕으로 사용자 찾기
        Member loginMember = memberRepository.findMemberByEmail(kakaoUserInfo.getEmail());

        if (loginMember == null)
            return ResponseEntity.status(HttpStatus.OK).body(null);

        // 토큰 생성 및 헤더에 토큰 정보 추가
        TokenDto token = memberService.setTokenInHeader(loginMember, response);

        MemberRes memberRes = MemberRes.builder()
                .realName(loginMember.getRealName())
                .nickname(loginMember.getNickname())
                .email(loginMember.getEmail())
                .passwd(loginMember.getPasswd())
                .phoneNumber(loginMember.getPhoneNumber())
                .postalCode(loginMember.getPostalCode())
                .address(loginMember.getAddress())
                .detailAddress(loginMember.getDetailAddress())
                .charId(loginMember.getCharId())
                .charName(loginMember.getCharName())
                .stepGoal(loginMember.getStepGoal())
                .availTrashBag(loginMember.getAvailTrashBag())
                .totalPlog(loginMember.getTotalPlog())
                .point(loginMember.getPoint())
                .build();

        // 로그인 성공시 사용자 반환
        return ResponseEntity.status(HttpStatus.OK).body(memberRes);
    }
}
