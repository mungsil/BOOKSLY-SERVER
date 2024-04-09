package kyonggi.bookslyserver.domain.shop.entity.BusinessSchedule;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BusinessHrs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isHoliday;

    private LocalTime openAt;

    private LocalTime closeAt;
}
