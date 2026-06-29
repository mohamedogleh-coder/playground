import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "stadiums")
public class Stadium {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "stadium_name", unique = true, length = 100, nullable = false)
    private String stadiumName;

    @Column(columnDefinition = "geography(Point,4326)",name = "location")
    private Point location;
}