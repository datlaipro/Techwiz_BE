package tmtd.event.registrations.dto;

import tmtd.event.registrations.RegistrationStatus;

public class RegistrationStatusUpdateRequest {
    private RegistrationStatus status;
    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }
}
