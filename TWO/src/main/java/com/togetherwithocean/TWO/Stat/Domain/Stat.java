package com.togetherwithocean.TWO.Stat.Domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.togetherwithocean.TWO.Location.Domain.Location;
import com.togetherwithocean.TWO.Member.Domain.Member;
import com.togetherwithocean.TWO.StatLoc.Domain.StatLoc;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "stat")
@NoArgsConstructor
public class Stat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_number")
    private Long StatNumber;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "step")
    private Long step;

    @Column(name = "achieve_step")
    private boolean achieveStep;

    @Column(name = "plogging")
    private Long plogging;

    @Column(name = "trash_bag")
    private Long trashBag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_number")
    @JsonBackReference
    private Member member;

    @OneToMany(mappedBy = "stat")
    @JsonManagedReference
    private List<StatLoc> locationsList = new ArrayList<>();

    @Builder
    public Stat(LocalDate date, Member member) {
        this.member = member;
        this.date = date;
        this.step = 0L;
        this.achieveStep = false;
        this.plogging = 0L;
        this.trashBag = 0L;
    }
}
