package jp.broadcom.tanzu.mhoshi.social.graphql.grpc.cf;

import io.grpc.NameResolverRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
class PreferIPNameResolverConfig {

    @PostConstruct
    void init() {
        // Register the custom provider globally
        NameResolverRegistry.getDefaultRegistry().register(new PreferIPNameResolverProvider());
    }
}
