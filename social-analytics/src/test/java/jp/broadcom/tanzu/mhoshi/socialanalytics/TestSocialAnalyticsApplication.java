package jp.broadcom.tanzu.mhoshi.socialanalytics;

import org.springframework.boot.SpringApplication;

public class TestSocialAnalyticsApplication {

    public static void main(String[] args) {
        String[] specificArgs = new String[args.length + 1];
        System.arraycopy(args, 0, specificArgs, 0, args.length);
        specificArgs[args.length] = "--analytics.database=postgres";
        SpringApplication.from(SocialAnalyticsApplication::main).with(TestContainersConfiguration.class).run(specificArgs);
    }

}
