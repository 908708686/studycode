package cn.unis.ad;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class AdApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdApplication.class, args);
    }
}
