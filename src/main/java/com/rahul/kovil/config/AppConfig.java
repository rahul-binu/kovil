package com.rahul.kovil.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class AppConfig {

	@Value("${app.crossorigin.url}")
	private String corssOrigin;

	private final JwtValidator jwtValidator;

	public AppConfig(JwtValidator jwtValidator) {
		this.jwtValidator = jwtValidator;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

	    http
	        .csrf(csrf -> csrf.disable())
	        .headers(headers -> headers
	                .frameOptions(frame -> frame.disable())
	            )
	        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers(
	                    "/web/**",
	                    "/assets/**",
	                    "/dynamic/**",
	                    "/auth/**"
	            ).permitAll()
	            .anyRequest().authenticated()
	        )

	        // Enable Session (Redis will store it)
	        .sessionManagement(sess ->
	            sess.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
	        )

//	        .formLogin(form -> form
//	            .loginPage("/web/auth/login")
//	            .loginProcessingUrl("/web/auth/login")
//	            .defaultSuccessUrl("/web/home", true)
//	            .permitAll()
//	        )
	        // Apply JWT only to API calls
	        .addFilterBefore(jwtValidator, BasicAuthenticationFilter.class);
//	    .addFilterBefore(new JwtValidator(Arrays.asList("/api/")), BasicAuthenticationFilter.class)


	    return http.build();
	}

	private CorsConfigurationSource corsConfigurationSource() {
		return new CorsConfigurationSource() {
			@Override
			public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

				CorsConfiguration cfg = new CorsConfiguration();
				cfg.setAllowedOrigins(Arrays.asList(corssOrigin));
				cfg.setAllowedMethods(Collections.singletonList("*"));
				cfg.setAllowCredentials(true);
				cfg.setAllowedHeaders(Collections.singletonList("*"));
				cfg.setExposedHeaders(Arrays.asList("Authorization"));
				cfg.setMaxAge(3600L);
				return cfg;
			}
		};
	}


	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
}
