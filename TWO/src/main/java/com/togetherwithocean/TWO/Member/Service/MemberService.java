package com.togetherwithocean.TWO.Member.Service;

import org.springframework.data.redis.core.StringRedisTemplate;
import com.togetherwithocean.TWO.Badge.Service.BadgeService;
import com.togetherwithocean.TWO.Item.Service.ItemSerivce;
import com.togetherwithocean.TWO.Jwt.JwtProvider;
import com.togetherwithocean.TWO.Jwt.TokenDto;
import com.togetherwithocean.TWO.Member.DTO.*;
import com.togetherwithocean.TWO.Member.Domain.Member;
import com.togetherwithocean.TWO.Member.Repository.MemberRepository;
import com.togetherwithocean.TWO.Ranking.Domain.Ranking;
import com.togetherwithocean.TWO.Ranking.Repository.RankingRepository;
import com.togetherwithocean.TWO.Stat.Domain.Stat;
import com.togetherwithocean.TWO.Stat.Repository.StatRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final StatRepository statRepository;
    private final RankingRepository rankingRepository;
    private final BadgeService badgeService;
    private final ItemSerivce itemSerivce;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    private final String PREFIX_LOGOUT = "LOGOUT:";
    private final String PREFIX_LOGOUT_REFRESH = "LOGOUT_REFRESH:";

    private final Logger log = LoggerFactory.getLogger(getClass());


    // 유저명으로 유저 찾기
    @Transactional
    public Member getMemberByRealName(String realName) {
        return memberRepository.findByRealName(realName);
    }

    public boolean isNicknameDuplicate(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    public boolean isEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    public boolean equalEncodePassword(String encodePassword, String password) {
        return encodePassword.equals(passwordEncoder.encode(password));
    }

    @Transactional
    public MemberRes save(MemberJoinReq memberSave) {
        System.out.println(memberSave.getPasswd() + " "+ memberSave.getCheckPasswd());

        // 비밀번호 일치 여부 예외처리?
        if (!memberSave.getPasswd().equals(memberSave.getCheckPasswd()))
            return null; // 비밀번호가 일치하지 않습니다

        Ranking ranking = Ranking.builder().build();

        Member member = Member.builder()
                .realName(memberSave.getRealName())
                .nickname(memberSave.getNickname())
                .email(memberSave.getEmail())
                .passwd(passwordEncoder.encode(memberSave.getPasswd()))
                .phoneNumber(memberSave.getPhoneNumber())
                .postalCode(memberSave.getPostalCode())
                .address(memberSave.getAddress())
                .detailAddress(memberSave.getDetailAddress())
                .charId(memberSave.getCharId())
                .charName(memberSave.getCharName())
                .stepGoal(memberSave.getStepGoal())
                .ranking(ranking)
                .build();

        memberRepository.save(member);

        ranking.setMember(member);
        rankingRepository.save(ranking);

        // 바키타 돌고래 배지 지급
        badgeService.successJoin(member);

        MemberRes memberRes = MemberRes.builder()
                .realName(member.getRealName())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .passwd(member.getPasswd())
                .phoneNumber(member.getPhoneNumber())
                .postalCode(member.getPostalCode())
                .address(member.getAddress())
                .detailAddress(member.getDetailAddress())
                .charId(member.getCharId())
                .charName(member.getCharName())
                .stepGoal(member.getStepGoal())
                .availTrashBag(member.getAvailTrashBag())
                .totalPlog(member.getTotalPlog())
                .point(member.getPoint())
                .build();

        return memberRes;
    }
 
    @Transactional
    public void updatePasswd(Member newMember) { memberRepository.save(newMember); }

    @Transactional
    public TokenDto setTokenInHeader(Member loginMember, HttpServletResponse response) {
        // 토큰 생성
        TokenDto jwtToken = jwtProvider.generateToken(loginMember);

        // 액세스 토큰 발급
        log.info("request email = {}, password = {}", loginMember.getEmail(), loginMember.getPasswd());
        log.info("jwtToken accessToken = {}, refreshToken = {}", jwtToken.getAccessToken(), jwtToken.getRefreshToken());

        // Header에 정보 넘겨주기
        response.setHeader("MemberEmail", loginMember.getEmail());
        response.setHeader("TokenType", "Bearer");
        response.setHeader("AccessToken", jwtToken.getAccessToken());
        response.setHeader("RefreshToken", jwtToken.getRefreshToken());

        return jwtToken;
    }

    public PostSignInRes setSignInInfo(MemberRes memberRes, TokenDto token) {
        return new PostSignInRes(memberRes, token);
    }

    public void logoutMember(HttpServletRequest request, String email) {
        String accessToken = jwtProvider.resolveAccessToken(request);
        String refreshToken = jwtProvider.resolveRefreshToken(request);
        Date accessExpiration = jwtProvider.parseClaims(accessToken).getExpiration();
        Date refreshExpiration = jwtProvider.parseClaims(refreshToken).getExpiration();

        redisTemplate.opsForValue()
                .set(PREFIX_LOGOUT + email, accessToken, Duration.ofSeconds(accessExpiration.getTime() - new Date().getTime()));
        redisTemplate.opsForValue()
                .set(PREFIX_LOGOUT_REFRESH + email, refreshToken, Duration.ofSeconds(refreshExpiration.getTime() - new Date().getTime()));
    }

    public MainInfoRes getMainInfo(String email) {
        Member member = memberRepository.findMemberByEmail(email);

        LocalDate today = LocalDate.now();
        System.out.println("today : " + today.getYear() + " " + today.getMonthValue() + " \n");

        Stat stat = statRepository.findStatByMemberAndDate(member, today);

        Long monthlyPlogging = statRepository.getMonthlyPlogging(member, today.getYear(), today.getMonthValue());

        MainInfoRes mainInfo =MainInfoRes.builder()
                .nickname(member.getNickname())
                .charId(member.getCharId())
                .charName(member.getCharName())
                .stepGoal(member.getStepGoal())
                .dailyStep(stat.getStep())
                .monthlyPlog(monthlyPlogging)
                .equipItemList(itemSerivce.getEquipItemList(email))
                .build();
        return mainInfo;
    }

    public MemberRes changeAddress(Member member, PatchChangeAddress patchChangeAddress) {
        member.setPostalCode(patchChangeAddress.getPostalCode());
        member.setAddress(patchChangeAddress.getAddress());
        member.setDetailAddress(patchChangeAddress.getDetailAddress());

        memberRepository.save(member);

        MemberRes memberRes = MemberRes.builder()
                .realName(member.getRealName())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .passwd(member.getPasswd())
                .phoneNumber(member.getPhoneNumber())
                .postalCode(member.getPostalCode())
                .address(member.getAddress())
                .detailAddress(member.getDetailAddress())
                .charId(member.getCharId())
                .charName(member.getCharName())
                .stepGoal(member.getStepGoal())
                .availTrashBag(member.getAvailTrashBag())
                .totalPlog(member.getTotalPlog())
                .point(member.getPoint())
                .build();

        return memberRes;
    }

    public MemberRes changeStepGoal(Member member, Long stepGoal) {
        member.setStepGoal(stepGoal);
        memberRepository.save(member);

        MemberRes memberRes = MemberRes.builder()
                .realName(member.getRealName())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .passwd(member.getPasswd())
                .phoneNumber(member.getPhoneNumber())
                .postalCode(member.getPostalCode())
                .address(member.getAddress())
                .detailAddress(member.getDetailAddress())
                .charId(member.getCharId())
                .charName(member.getCharName())
                .stepGoal(member.getStepGoal())
                .availTrashBag(member.getAvailTrashBag())
                .totalPlog(member.getTotalPlog())
                .point(member.getPoint())
                .build();

        return memberRes;
    }

    public MemberRes changeCharacter(Member member, Long charId) {
        member.setCharId(charId);
        memberRepository.save(member);

        MemberRes memberRes = MemberRes.builder()
                .realName(member.getRealName())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .passwd(member.getPasswd())
                .phoneNumber(member.getPhoneNumber())
                .postalCode(member.getPostalCode())
                .address(member.getAddress())
                .detailAddress(member.getDetailAddress())
                .charId(member.getCharId())
                .charName(member.getCharName())
                .stepGoal(member.getStepGoal())
                .availTrashBag(member.getAvailTrashBag())
                .totalPlog(member.getTotalPlog())
                .point(member.getPoint())
                .build();

        return memberRes;
    }

    public MemberRes changeMypage(Member member, PatchChangeMypage patchChangeMypage) {
        member.setNickname(patchChangeMypage.getNickname());
        member.setPasswd(passwordEncoder.encode(patchChangeMypage.getPasswd()));
        member.setPhoneNumber(patchChangeMypage.getPhoneNumber());
        memberRepository.save(member);

        MemberRes memberRes = MemberRes.builder()
                .realName(member.getRealName())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .passwd(member.getPasswd())
                .phoneNumber(member.getPhoneNumber())
                .postalCode(member.getPostalCode())
                .address(member.getAddress())
                .detailAddress(member.getDetailAddress())
                .charId(member.getCharId())
                .charName(member.getCharName())
                .stepGoal(member.getStepGoal())
                .availTrashBag(member.getAvailTrashBag())
                .totalPlog(member.getTotalPlog())
                .point(member.getPoint())
                .build();

        return memberRes;
    }

    public void deleteMember(Member member) {
        memberRepository.deleteById(member.getMemberNumber());
    }

    public MyPageRes getMyPageInfo(String email) {
        Member member = memberRepository.findMemberByEmail(email);

        MyPageRes myPageRes = MyPageRes.builder()
                .nickname(member.getNickname())
                .point(member.getPoint())
                .score(member.getRanking().getScore())
                .build();
        return myPageRes;
    }
      
    public void checkAttend(String email) {
        Member member = memberRepository.findMemberByEmail(email);
        Stat todayStat = statRepository.findStatByMemberAndDate(member, LocalDate.now());
        if (!todayStat.getAttend()) {
            todayStat.setAttend(true);
        }
        statRepository.save(todayStat);
    }
    public Long getPoint(String email) {
        Member member = memberRepository.findMemberByEmail(email);
        return member.getPoint();
    }
}
