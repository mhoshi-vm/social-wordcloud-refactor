package jp.broadcom.tanzu.mhoshi.socialanalytics;

import org.springframework.boot.SpringApplication;

public class TestSocialAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.from(SocialAnalyticsApplication::main).with(TestContainersConfiguration.class).run(args);
    }

}
