// package tmtd.event.registrations;

// import java.util.List;

// import tmtd.event.registrations.dto.RegistrationCreateRequest;
// import tmtd.event.registrations.dto.RegistrationResponse;

// public interface ServiceRegistrations {

//     RegistrationResponse register(RegistrationCreateRequest req);

//     EntityRegistrations getById(Long id);

//     List<EntityRegistrations> listByEvent(Long eventId);

//     List<EntityRegistrations> listByStudent(Integer studentId);

//     void cancel(Long id);

//     void updateStatus(Long id, RegistrationStatus status);

//     long countConfirmed(Long eventId);
// }


package tmtd.event.registrations;

import java.util.List;

import tmtd.event.registrations.dto.RegistrationCreateRequest;
import tmtd.event.registrations.dto.RegistrationResponse;

public interface ServiceRegistrations {

    RegistrationResponse register(RegistrationCreateRequest req);

    EntityRegistrations getById(Long id);

    List<EntityRegistrations> listByEvent(Long eventId);

    List<EntityRegistrations> listByStudent(Integer studentId);

    void cancel(Long id);

    void updateStatus(Long id, RegistrationStatus status);

    long countConfirmed(Long eventId);

    // === Moderation ===
    RegistrationResponse approveRegistration(Long eventId, Long registrationId, Long actorId);

    RegistrationResponse rejectRegistration(Long eventId, Long registrationId, Long actorId);
}
