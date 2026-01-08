package jp.broadcom.tanzu.mhoshi.socialwebapi;

import org.springframework.boot.SpringApplication;

public class TestSocialWebapiApplication {

    public static void main(String[] args) {
        SpringApplication.from(SocialWebapiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
