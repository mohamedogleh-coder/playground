package com.hammi.playground.modules.stadium;

import com.hammi.playground.modules.fields.Field;
import com.hammi.playground.modules.managers.StadiumManager;
import com.hammi.playground.modules.merchants.StadiumMerchant;
import com.hammi.playground.modules.working_days.StadiumWorkingDay;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "stadiums")
public class Stadium {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "stadium_name", unique = true, length = 100, nullable = false)
    private String stadiumName;


    @Column(name = "extra_time", nullable = false)
    private Short extraTime;

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "half_booking")
    private boolean halfBooking;

    @Column(columnDefinition = "geography(Point,4326)", name = "location")
    private Point location;

    public Double getLatitude() {
        return location != null ? location.getY() : null;
    }

    public Double getLongitude() {
        return location != null ? location.getX() : null;
    }

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "stadium")
    private List<Field> fields = new ArrayList<>();

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "stadium")
    private List<StadiumWorkingDay> workingDays = new ArrayList<>();


    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "stadium")
    private List<StadiumMerchant> merchants = new ArrayList<>();

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "stadium")
    private List<StadiumManager> managers = new ArrayList<>();
}