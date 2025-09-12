package tmtd.event.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUser extends JpaRepository<EntityUser, Integer>
// Cùng cấp thì ko cần import file
{
// Bạn có thể định nghĩa các phương thức truy vấn tùy chỉnh tại đây
Optional<EntityUser> findByEmail(String email); // Thêm phương thức tìm kiếm theo email
List<EntityUser> findByRolesContaining(String roles);  // Tìm User theo vai trò
}
