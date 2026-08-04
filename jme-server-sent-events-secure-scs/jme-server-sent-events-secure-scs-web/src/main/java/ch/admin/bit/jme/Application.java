package ch.admin.bit.jme;

import ch.admin.bit.jeap.command.notify.client.NotifyClientCommand;
import ch.admin.bit.jeap.messaging.annotations.JeapMessageConsumerContract;
import ch.admin.bit.jeap.messaging.annotations.JeapMessageProducerContract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.time.Clock;

@JeapMessageProducerContract(value = NotifyClientCommand.TypeRef.class, topic = "jme-server-sent-events-secure-scs-notifyclient")
@JeapMessageConsumerContract(value = NotifyClientCommand.TypeRef.class, topic = "jme-server-sent-events-secure-scs-notifyclient")
@SpringBootApplication
@Slf4j
public class Application {

    public static void main(String[] args) {

        Environment env = SpringApplication.run(Application.class, args).getEnvironment();

        log.info("The time now is {}", java.time.LocalDateTime.now());
        log.info("The current time zone for clock is {}", Clock.systemDefaultZone());
        log.info("""
                        ----------------------------------------------------------
                        \t\
                        {} is running! &#9825;\s
                        \t\
                        
                        \tFrontend (npm): \t\thttp://localhost:4201
                        \tFrontend (bundled): \thttp://localhost:{}{}
                        \tSwaggerUI: \t\t\t\thttp://localhost:{}{}/swagger-ui.html?urls.primaryName=Service%20API
                        \t\
                        Profile(s): \t\t\t{}\
                        
                        ----------------------------------------------------------""",
                env.getProperty("spring.application.name"),
                env.getProperty("server.port"),
                env.getProperty("server.servlet.context-path"),
                env.getProperty("server.port"),
                env.getProperty("server.servlet.context-path"),
                env.getActiveProfiles());

    }
}
