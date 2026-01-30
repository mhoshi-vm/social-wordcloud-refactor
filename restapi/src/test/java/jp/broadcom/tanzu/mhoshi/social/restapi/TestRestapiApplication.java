package jp.broadcom.tanzu.mhoshi.social.restapi;

import org.springframework.boot.SpringApplication;

public class TestRestapiApplication {

    public static void main(String[] args) {
        SpringApplication.from(RestApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
