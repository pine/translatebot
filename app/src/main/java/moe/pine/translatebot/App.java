package moe.pine.translatebot;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class App {
    public static void main(String... args) {
        AppInitializer.run();
        SpringApplication.run(App.class, args);
    }
}
