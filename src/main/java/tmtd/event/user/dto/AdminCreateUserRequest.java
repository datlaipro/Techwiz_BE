package tmtd.event.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminCreateUserRequest(
    @Email @NotBlank String email,
    @NotBlank String fullName,
    @NotBlank String password
) {}
