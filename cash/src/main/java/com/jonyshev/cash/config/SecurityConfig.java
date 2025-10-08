package com.jonyshev.cash.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()); // для POST из сервиса во внутренней сети
        http.authorizeHttpRequests(reg -> reg
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/**").permitAll()    // открой свои cash-эндпоинты
                .anyRequest().denyAll()
        );
        http.formLogin(form -> form.disable());
        http.httpBasic(b -> b.disable());
        http.logout(l -> l.disable());
        // НЕ включаем oauth2ResourceServer в cash, если он не должен принимать токены
        return http.build();
    }
}