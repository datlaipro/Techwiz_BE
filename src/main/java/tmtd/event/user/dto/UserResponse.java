package tmtd.event.user.dto;

public record UserResponse(
    Long userId,
    String email,
    String fullName,
    String roles
) {}
