package jp.broadcom.tanzu.mhoshi.social.collector;

import org.springframework.boot.SpringApplication;

public class TestSocialCollectorApplication {

    public static void main(String[] args) {
        SpringApplication.from(SocialCollectorApplication::main).with(TestContainersConfiguration.class).run(args);
    }

}
