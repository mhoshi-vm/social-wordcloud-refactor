package jp.broadcom.tanzu.mhoshi.socialanalytics;

import io.grpc.StatusException;
import org.jspecify.annotations.Nullable;
import org.mybatis.scripting.thymeleaf.SqlGenerator;
import org.mybatis.scripting.thymeleaf.SqlGeneratorConfig;
import org.mybatis.scripting.thymeleaf.processor.BindVariableRender;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.function.Consumer;

@Configuration
@EnableScheduling
@EnableAsync
@EnableWebSecurity
@EnableConfigurationProperties(AnalyticsConfigProperties.class)
class AnalyticsConfig {

    @Bean
    SqlGenerator sqlGenerator() {
        final SqlGeneratorConfig config = SqlGeneratorConfig.newInstanceWithCustomizer(c ->
                c.getDialect().setBindVariableRenderInstance(BindVariableRender.BuiltIn.SPRING_NAMED_PARAMETER));
        return new SqlGenerator(config);
    }

    @Bean
    Consumer<List<SocialMessage>> messageConsumer(AnalyticsComponent analyticsComponent) {
        return (in) -> {
            if (!in.isEmpty()) {
                analyticsComponent.insertSocialMessages(in);
                analyticsComponent.updateTsvector();
                analyticsComponent.updateVaderSentiment();
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        return http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/h2-console/**").permitAll() // Allow access to H2 console
                        .requestMatchers("/actuator/**").permitAll()  // Allow access to all Actuator endpoints
                        .anyRequest().authenticated()                 // Secure all other requests
                )
                .csrf(AbstractHttpConfigurer::disable // Disable CSRF for simplified H2 console access
                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable) // Enable frames for H2 console
                ).build();
    }

}
