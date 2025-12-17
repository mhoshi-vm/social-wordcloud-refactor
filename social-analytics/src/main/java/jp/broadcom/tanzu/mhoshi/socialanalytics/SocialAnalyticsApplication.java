package jp.broadcom.tanzu.mhoshi.socialanalytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AnalyticsConfigProperties.class)
public class SocialAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialAnalyticsApplication.class, args);
    }

}
