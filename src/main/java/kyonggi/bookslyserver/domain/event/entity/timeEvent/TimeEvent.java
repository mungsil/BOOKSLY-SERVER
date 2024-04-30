package kyonggi.bookslyserver.domain.event.entity.timeEvent;

import jakarta.persistence.*;
import kyonggi.bookslyserver.global.common.BaseTimeEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TimeEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private int discountRate;

    private boolean repetitionStatus;

    private boolean isDayRepeat; // 요일 반복

    private boolean isDateRepeat; // 날짜 반복

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "timeEventSchedule_id")
    private TimeEventSchedule timeEventSchedule;

    @OneToMany(mappedBy = "timeEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepeatDayOfWeek> repeatDayOfWeeks = new ArrayList<>();

    @OneToMany(mappedBy = "timeEvent", cascade = CascadeType.ALL)
    private List<EmployeeTimeEvent> employeeTimeEvents = new ArrayList<>();
}
