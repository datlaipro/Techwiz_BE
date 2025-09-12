// package tmtd.event.registrations;

// import java.util.List;
// import java.util.Optional;

// import org.springframework.data.jpa.repository.JpaRepository;

// public interface JpaRegistrations extends JpaRepository<EntityRegistrations, Long> {

//     boolean existsByEventIdAndStudentId(Long eventId, Integer studentId);

//     Optional<EntityRegistrations> findByEventIdAndStudentId(Long eventId, Integer studentId);

//     long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

//     List<EntityRegistrations> findAllByEventId(Long eventId);

//     List<EntityRegistrations> findAllByStudentId(Integer studentId);
// }



package tmtd.event.registrations;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRegistrations extends JpaRepository<EntityRegistrations, Long> {

    // Kiểm tra học viên đã đăng ký sự kiện chưa
    boolean existsByEventIdAndStudentId(Integer eventId, Integer studentId);

    // Tìm bản đăng ký theo eventId và studentId (trả Optional)
    Optional<EntityRegistrations> findByEventIdAndStudentId(Long eventId, Integer studentId);
    
    long countByEventId(Long eventId);

    // Đếm số bản đăng ký theo trạng thái
    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    // Lấy tất cả bản đăng ký của một sự kiện
    List<EntityRegistrations> findAllByEventId(Long eventId);

    // Lấy tất cả bản đăng ký của một học viên
    List<EntityRegistrations> findAllByStudentId(Integer studentId);

    // [ADD] Tìm bản đăng ký với studentId kiểu Long (để đồng nhất với nơi khác trong code)
    Optional<EntityRegistrations> findByEventIdAndStudentId(Long eventId, Long studentId);

    // [ADD] Kiểm tra bản đăng ký có đúng trạng thái (với studentId kiểu Long)
    boolean existsByEventIdAndStudentIdAndStatus(Long eventId, Long studentId, RegistrationStatus status);
}
