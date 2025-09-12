package tmtd.event.notifications;

public interface RegistrationEmailPort {
    void sendRegistrationSuccess(String to, String eventTitle, Long eventId, Long registrationId);
}
