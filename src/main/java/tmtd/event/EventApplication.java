// package tmtd.event;

// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.boot.autoconfigure.domain.EntityScan;
// import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// @SpringBootApplication(scanBasePackages = "tmtd.event")
// @EntityScan(basePackages = {
//   "tmtd.event.events",
//   "tmtd.event.events.image",
//   "tmtd.event.registrations",
//   "tmtd.event.registrations.details",
//   "tmtd.event.user",
//   "tmtd.event.feedback"
// })
// @EnableJpaRepositories(basePackages = {
//   "tmtd.event.events",
//   "tmtd.event.registrations",
//   "tmtd.event.user",
//   "tmtd.event.feedback"
 
// })

// public class EventApplication {
//   public static void main(String[] args) {
//     SpringApplication.run(EventApplication.class, args);
//   }
// }



package tmtd.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "tmtd.event")
@EntityScan(basePackages = {
  "tmtd.event.events",
  "tmtd.event.events.image",
  "tmtd.event.registrations",
  "tmtd.event.user",
  "tmtd.event.feedback",
  "tmtd.event.attendance",   // thêm
  "tmtd.event.qr"            // thêm
})
@EnableJpaRepositories(basePackages = {
  "tmtd.event.events",
  "tmtd.event.registrations",
  "tmtd.event.user",
  "tmtd.event.feedback",
  "tmtd.event.attendance",   // thêm
  "tmtd.event.qr"            // thêm
})
public class EventApplication {
  public static void main(String[] args) {
    SpringApplication.run(EventApplication.class, args);
  }
}
