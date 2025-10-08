package com.jonyshev.front.config;

import com.jonyshev.front.client.AccountsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AccountsClient accounts;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.authorizeHttpRequests(reg -> reg
                .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**",
                        "/actuator/health", "/actuator/info").permitAll()
                .anyRequest().authenticated()
        );
        http.formLogin(form -> form
                .permitAll()
                .defaultSuccessUrl("/main", true)
        );
        http.logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login?logout").permitAll());
        http.authenticationProvider(authProvider());
        return http.build();
    }

    @Bean
    AuthenticationProvider authProvider() {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String login = authentication.getName();
                String password = String.valueOf(authentication.getCredentials());
                if (accounts.auth(login, password)) {
                    return new UsernamePasswordAuthenticationToken(login, password,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")));
                }
                throw new BadCredentialsException("Bad credentials");
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }
}
