package jp.broadcom.tanzu.mhoshi.socialcollector;

import org.springframework.boot.SpringApplication;

public class TestSocialCollectorApplication {

    public static void main(String[] args) {
        SpringApplication.from(SocialCollectorApplication::main).with(TestContainersConfiguration.class).run(args);
    }

}
