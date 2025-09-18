


// package tmtd.event.notifications;

// import java.util.Optional;
// import java.util.concurrent.Executor;

// import org.springframework.beans.factory.ObjectProvider;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.scheduling.annotation.EnableAsync;
// import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

// import tmtd.event.notifications.template.EmailTemplateRenderer;

// /**
//  * Autoconfig cho email: quyết định tạo bean thật / NO-OP / listener / executor.
//  */
// @Configuration
// @EnableAsync
// public class RegistrationEmailAutoConfig {

//     /**
//      * Bean Port "thật" khi bật mail và có JavaMailSender.
//      */
//     @Bean
//     @ConditionalOnProperty(name = "mail.enabled", havingValue = "true", matchIfMissing = false)
//     @ConditionalOnBean(JavaMailSender.class)
//     RegistrationEmailPort registrationEmailService(
//             JavaMailSender mailSender,
//             ObjectProvider<EmailTemplateRenderer> rendererProvider
//     ) {
//         return new RegistrationEmailService(
//             mailSender,
//             Optional.ofNullable(rendererProvider.getIfAvailable())
//         );
//     }

//     /**
//      * Fallback NO-OP khi chưa có bean Port nào khác.
//      */
//     @Bean
//     @ConditionalOnMissingBean(RegistrationEmailPort.class)
//     RegistrationEmailPort registrationEmailServiceNoOp() {
//         return new RegistrationEmailServiceNoOp();
//     }

//     /**
//      * Listener chỉ tạo khi đã có 1 bean Port (thật hoặc NO-OP).
//      */
//     @Bean
//     @ConditionalOnBean(RegistrationEmailPort.class)
//     RegistrationEmailListener registrationEmailListener(RegistrationEmailPort port) {
//         return new RegistrationEmailListener(port);
//     }

//     /**
//      * Executor cho tác vụ gửi mail async (nếu bạn chưa define bean "emailExecutor" ở nơi khác).
//      */
//     @Bean(name = "emailExecutor")
//     @ConditionalOnMissingBean(name = "emailExecutor")
//     public Executor emailExecutor() {
//         ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
//         ex.setCorePoolSize(2);
//         ex.setMaxPoolSize(4);
//         ex.setQueueCapacity(100);
//         ex.setThreadNamePrefix("email-");
//         ex.initialize();
//         return ex;
//     }
// }



package tmtd.event.notifications;

import java.util.Optional;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import tmtd.event.notifications.template.EmailTemplateRenderer;

/**
 * Autoconfig cho email: quyết định tạo bean thật / NO-OP / listener / executor.
 */
@Configuration
@EnableAsync
public class RegistrationEmailAutoConfig {

    /**
     * Bean Port "thật" khi bật mail và có JavaMailSender.
     */
    @Bean
    @ConditionalOnProperty(name = "mail.enabled", havingValue = "true", matchIfMissing = false)
    @ConditionalOnBean(JavaMailSender.class)
    RegistrationEmailPort registrationEmailService(
            JavaMailSender mailSender,
            ObjectProvider<EmailTemplateRenderer> rendererProvider
    ) {
        return new RegistrationEmailService(
            mailSender,
            Optional.ofNullable(rendererProvider.getIfAvailable())
        );
    }

    /**
     * Fallback NO-OP khi chưa có bean Port nào khác.
     */
    @Bean
    @ConditionalOnMissingBean(RegistrationEmailPort.class)
    RegistrationEmailPort registrationEmailServiceNoOp() {
        return new RegistrationEmailServiceNoOp();
    }

    /**
     * Listener chỉ tạo khi đã có 1 bean Port (thật hoặc NO-OP).
     */
    @Bean
    @ConditionalOnBean(RegistrationEmailPort.class)
    RegistrationEmailListener registrationEmailListener(RegistrationEmailPort port) {
        return new RegistrationEmailListener(port);
    }

    /**
     * Executor cho tác vụ gửi mail async (nếu bạn chưa define bean "emailExecutor" ở nơi khác).
     */
    @Bean(name = "emailExecutor")
    @ConditionalOnMissingBean(name = "emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(2);
        ex.setMaxPoolSize(4);
        ex.setQueueCapacity(100);
        ex.setThreadNamePrefix("email-");
        ex.initialize();
        return ex;
    }
}
