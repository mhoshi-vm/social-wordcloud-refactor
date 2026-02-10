package jp.broadcom.tanzu.mhoshi.social.analytics;

import org.springframework.boot.SpringApplication;

public class TestSocialAnalyticsApplication {

	public static void main(String[] args) {
		SpringApplication.from(AnalyticsApplication::main).with(TestContainersConfiguration.class).run(args);
	}

}
