package com.jonyshev.transfer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()); // иначе POST от веб-клиента/сервисов будут резаться
        http.authorizeHttpRequests(reg -> reg
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/**").permitAll()      // открой API transfer
                .anyRequest().denyAll()
        );
        http.formLogin(f -> f.disable());
        http.httpBasic(b -> b.disable());
        http.logout(l -> l.disable());
        // НЕ включаем oauth2ResourceServer(jwt), если transfer не должен проверять токены
        return http.build();
    }
}