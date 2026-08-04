package ch.admin.bit.jme.web.config;

import ch.admin.bit.jme.web.filter.SpaWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;


@Configuration
public class WebSecurityConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 11)
    public SecurityFilterChain uiSecurityFilterChain(HttpSecurity http) throws Exception {
        // Protect the API AND SSE endpoints
        // Allow public access to frontend resources (i.e. non-/api and non-SSE routes)

        RequestMatcher apiMatcher = PathPatternRequestMatcher.pathPattern("/api/**");
        RequestMatcher sseMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/ui-api/sse/**");

        // Combine matchers: match if it's NOT api AND NOT sse
        RequestMatcher publicMatcher = new AndRequestMatcher(
                new NegatedRequestMatcher(apiMatcher),
                new NegatedRequestMatcher(sseMatcher)
        );

        http.securityMatcher(publicMatcher)
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests.anyRequest().permitAll())
                .addFilterAfter(new SpaWebFilter(), AnonymousAuthenticationFilter.class);

        http.headers(headers ->
                headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        return http.build();
    }

}
