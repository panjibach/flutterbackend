package com.example.flutterbackend.config;

// DISABLED SESSION CONFIG - USING STATELESS JWT ONLY

/*
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@EnableJdbcHttpSession(
    maxInactiveIntervalInSeconds = 3600, // 1 hour
    tableName = "SPRING_SESSION",
    cleanupCron = "0 * * * * *" // Clean expired sessions every minute
)
public class SessionConfig extends AbstractHttpSessionApplicationInitializer {

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("FLUTTER_SESSION");
        serializer.setCookiePath("/");
        serializer.setUseSecureCookie(false); // Set to true in production with HTTPS
        serializer.setUseHttpOnlyCookie(true);
        serializer.setSameSite("Lax");
        serializer.setCookieMaxAge(3600); // 1 hour
        return serializer;
    }
}
*/

// SESSION CONFIG IS DISABLED - USING STATELESS JWT AUTHENTICATION ONLY
public class SessionConfig {
    // This class is intentionally empty
    // Session management is disabled in favor of stateless JWT authentication
}
