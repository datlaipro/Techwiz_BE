package tmtd.event.qr.dto;

public record QrRedeemResponse(
    boolean success,
    String message,
    Long eventId,       // ✅ mới
    Integer studentId   // ✅ mới
) {}
