package tmtd.event.user;

public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException(String email) {
        super("Email đã tồn tại: " + email);
    }
}