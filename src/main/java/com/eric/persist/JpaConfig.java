package com.eric.persist;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {"com.eric.persist"})
@Configuration
public class JpaConfig {


}
