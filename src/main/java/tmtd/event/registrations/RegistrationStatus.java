package tmtd.event.registrations;

public enum RegistrationStatus {
    PENDING,     // chờ duyệt (nếu bạn cần workflow)
    CONFIRMED,   // đã xác nhận chỗ
    CANCELLED    // hủy đăng ký
}
