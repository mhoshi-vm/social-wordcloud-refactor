package jp.broadcom.tanzu.mhoshi.socialanalytics;

import org.mybatis.scripting.thymeleaf.SqlGenerator;
import org.mybatis.scripting.thymeleaf.SqlGeneratorConfig;
import org.mybatis.scripting.thymeleaf.processor.BindVariableRender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
class AnalyticsConfig {

    @Bean
    SqlGenerator sqlGenerator() {
        final SqlGeneratorConfig config = SqlGeneratorConfig.newInstanceWithCustomizer(c ->
                c.getDialect().setBindVariableRenderInstance(BindVariableRender.BuiltIn.SPRING_NAMED_PARAMETER));
        return new SqlGenerator(config);
    }

}
