package com.paulcartagena.skillmatchapi.security;

import com.paulcartagena.skillmatchapi.exception.ErrorMessages;
import com.paulcartagena.skillmatchapi.exception.ErrorResponseWriter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate limiter for the public auth endpoints (login/register), keyed by
 * client IP + endpoint. Mitigates brute-force / credential-stuffing attempts.
 * <p>
 * Note: this is per-instance state. If the app is ever deployed across multiple
 * instances, this should move to a shared store (e.g. Bucket4j + Redis) so the
 * limit is enforced globally instead of per-instance.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of(
            "/auth/login", "/auth/register", "/auth/register-recruiter"
    );

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ErrorResponseWriter errorResponseWriter;

    @Value("${rate-limit.login.capacity:5}")
    private int capacity;

    @Value("${rate-limit.login.refill-minutes:1}")
    private int refillMinutes;

    public RateLimitFilter(ErrorResponseWriter errorResponseWriter) {
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!LIMITED_PATHS.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Behind the load balancer, Tomcat already resolves the real client IP from X-Forwarded-For
        // (server.forward-headers-strategy: native, trusting only private-network proxies).
        // Reading the header here instead would let clients bypass the limit by sending a fake one.
        String key = request.getRequestURI() + ":" + request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1, (long) Math.ceil(probe.getNanosToWaitForRefill() / 1_000_000_000.0));

        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        errorResponseWriter.write(response, HttpStatus.TOO_MANY_REQUESTS, ErrorMessages.TOO_MANY_REQUESTS);
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, Duration.ofMinutes(refillMinutes)));
        return Bucket.builder().addLimit(limit).build();
    }
}
