package jp.broadcom.tanzu.mhoshi.socialcollector.shared;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackageClasses = OffsetStoreRepository.class)
class OffsetStoreConfig {

}
