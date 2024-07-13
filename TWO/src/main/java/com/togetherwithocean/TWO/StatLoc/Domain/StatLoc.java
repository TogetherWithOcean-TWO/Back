package com.togetherwithocean.TWO.StatLoc.Domain;

import com.togetherwithocean.TWO.Location.Domain.Location;
import com.togetherwithocean.TWO.Stat.Domain.Stat;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "stat_loc")
@NoArgsConstructor
public class StatLoc {
    @Id
    @Column(name = "stat_loc_number")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statLocNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stat_stat_number")
    private Stat stat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_location_number")
    private Location location;
}
