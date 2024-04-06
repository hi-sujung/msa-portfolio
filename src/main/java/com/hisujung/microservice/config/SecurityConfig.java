package com.hisujung.microservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final String[] AUTH_WHITELIST = {
            "/portfolio/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //CSRF, CORS
        http.csrf((csrf) -> csrf.disable());
        http.cors(Customizer.withDefaults());

        //세션 관리 상태 없음으로 구성, Spring Security가 세션 생성 or 사용 X
//        http.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(
//                SessionCreationPolicy.STATELESS));

        //http.addFilterBefore(new JwtTokenFilter(loginService, secretKey), UsernamePasswordAuthenticationFilter.class);

        //FormLogin, BasicHttp 비활성화
        http.formLogin((form) -> form.disable());
        http.httpBasic(AbstractHttpConfigurer::disable);




        // 권한 규칙 작성
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        //@PreAuthrization을 사용할 것이기 때문에 모든 경로에 대한 인증처리는 Pass
                        .anyRequest().permitAll()
//                        .anyRequest().authenticated()
        );

        return http.build();
    }

}