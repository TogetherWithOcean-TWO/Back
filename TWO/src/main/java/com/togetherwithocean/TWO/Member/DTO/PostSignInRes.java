package com.togetherwithocean.TWO.Member.DTO;

import com.togetherwithocean.TWO.Jwt.TokenDto;
import com.togetherwithocean.TWO.Member.Domain.Member;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostSignInRes {
    MemberRes memberRes;
    TokenDto token;

    @Builder
    public PostSignInRes(MemberRes memberRes, TokenDto token) {
        this.memberRes = memberRes;
        this.token = token;
    }
}
