package com.togetherwithocean.TWO.Stat.Controller;

import com.togetherwithocean.TWO.Stat.DTO.*;
import com.togetherwithocean.TWO.Stat.Domain.Stat;
import com.togetherwithocean.TWO.Stat.Service.StatService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("stat")
public class StatController {
    private final StatService statService;

    // 줍깅 api
    @PostMapping("/plog")
    ResponseEntity<StatRes> savePlog(@RequestBody PostStatSaveReq postStatSaveReq, Authentication principal) {
        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        StatRes statRes = statService.savePlog(principal.getName(), postStatSaveReq);
        return ResponseEntity.status(HttpStatus.OK).body(statRes);
    }

    // 걸음수 갱신 api
    @PostMapping("/walk")
    ResponseEntity<StatRes> saveStep(@RequestBody PatchStatWalkReq patchStatWalkReq, Authentication principal) {
        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        StatRes statRes = statService.saveStep(patchStatWalkReq, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).body(statRes);
    }

    // 일자별 캘린더 조회
    @GetMapping("/daily")
    ResponseEntity<StatRes> getPlogList(@RequestParam LocalDate date, Authentication principal) {
        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        StatRes dailyStat = statService.getDailyPlog(principal.getName(), date);
        return ResponseEntity.status(HttpStatus.OK).body(dailyStat);
    }

    // 월별 캘린더 및 줍깅 수, 스코어 조회
    @GetMapping("/monthly")
    public ResponseEntity<GetMonthlyStatRes> getMonthlyStat(@RequestParam int year, @RequestParam int month, Authentication principal) {
        if (principal == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        GetMonthlyStatRes monthlyStat = statService.getMonthlyStat(year, month, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).body(monthlyStat);
    }
}
