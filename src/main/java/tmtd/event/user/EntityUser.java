package tmtd.event.user;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "entity_user",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_entity_user_email", columnNames = "email")
    }
)
@Getter
@Setter
public class EntityUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long user_id;

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 180) // UNIQUE ở @Table phía trên
    private String email;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(nullable = false)
    private String password;

    private Date dateJoin;
    private Date birthDay;
    private String address;

    @Column(nullable = false, length = 100)
    private String roles;

    private String gender;
    private String avatar;

    public EntityUser() {
        this.fullName = "(chưa xác định)";
        this.roles = "ROLE_USER";
        this.gender = null;
        this.avatar = null;
    }

    public Boolean InputError() {
        boolean ipe = false;
        if (this.fullName == null || this.fullName.length() < 2) ipe = true;
        if (this.fullName != null && this.fullName.length() > 22) ipe = true;
        return ipe;
    }

    public List<String> getRoleList() {
        if (this.roles == null || this.roles.isEmpty()) return new ArrayList<>();
        return List.of(this.roles.split(","));
    }

    // alias
    public Long getId() { return this.user_id; }
    public void setId(Long id) { this.user_id = id; }
}
