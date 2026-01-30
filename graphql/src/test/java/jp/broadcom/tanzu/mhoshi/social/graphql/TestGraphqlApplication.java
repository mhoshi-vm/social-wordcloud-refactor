package jp.broadcom.tanzu.mhoshi.social.graphql;

import org.springframework.boot.SpringApplication;

public class TestGraphqlApplication {

    public static void main(String[] args) {
        SpringApplication.from(GraphqlApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
