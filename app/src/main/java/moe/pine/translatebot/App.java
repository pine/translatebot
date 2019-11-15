package moe.pine.translatebot;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
public class App {
    public static void main(String... args) {
        AppInitializer.run();
        SpringApplication.run(App.class, args);
    }
}
