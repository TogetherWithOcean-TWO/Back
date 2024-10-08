package com.togetherwithocean.TWO.Stat.Domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.togetherwithocean.TWO.Member.Domain.Member;
import com.togetherwithocean.TWO.StatLoc.Domain.StatLoc;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "stat")
@NoArgsConstructor
public class Stat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_number")
    private Long statNumber;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "attend")
    private Boolean attend;

    @Column(name = "step")
    private Long step;

    @Column(name = "achieve_step")
    private Boolean achieveStep;

    @Column(name = "plogging")
    private Long plogging;

    @Column(name = "trash_bag")
    private Long trashBag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_member_number")
    @JsonBackReference
    private Member member;

    @OneToMany(mappedBy = "stat", cascade = CascadeType.REMOVE)
    @JsonManagedReference
    private List<StatLoc> locationsList = new ArrayList<>();

    @Builder
    public Stat(LocalDate date, Member member) {
        this.member = member;
        this.attend = false;
        this.date = date;
        this.step = 0L;
        this.achieveStep = false;
        this.plogging = 0L;
        this.trashBag = 0L;
    }
}
