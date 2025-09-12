// tmtd/event/user/dto/UserRegisterRequest.java
package tmtd.event.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 2, max = 150) String fullName,
        @NotBlank @Size(min = 6, max = 100) String password
) {}
