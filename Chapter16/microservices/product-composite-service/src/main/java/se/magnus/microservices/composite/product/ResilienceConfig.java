package se.magnus.microservices.composite.product;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

  private static final Logger LOG = LoggerFactory.getLogger(ResilienceConfig.class);

  public ResilienceConfig(
    @Value("${app.resilience.enabled:true}") String resilienceEnabledStr,
    CircuitBreakerRegistry circuitBreakerRegistry,
    TimeLimiterRegistry timeLimiterRegistry,
    RetryRegistry retryRegistry
  ) {

    // Convert resilienceEnabledStr to boolean, only set to false if set to "false" (i.e. null or a value other than "false" will be true)
    boolean resilienceEnabled = !("false".equalsIgnoreCase(resilienceEnabledStr));
    LOG.debug("Resilience4j is enabled: {} ({})", resilienceEnabled, resilienceEnabledStr);
    
    // Disable use of Resilience4j if "app.resilience.enabled = false"
    if (!resilienceEnabled) {
      LOG.warn("Resilience4j is disabled (\"app.resilience.enabled = {}\"). CircuitBreaker, TimeLimiter and Retry are not used.", resilienceEnabledStr);
      circuitBreakerRegistry.remove("product");
      // timeLimiterRegistry.remove("product");
      retryRegistry.remove("product");
      timeLimiterRegistry.replace("product", io.github.resilience4j.timelimiter.TimeLimiter.of(Duration.ofHours(24)));
      LOG.warn("Instead of disabling the timelimiter, a loooong time out of {} will be used", timeLimiterRegistry.find("product").get().getTimeLimiterConfig().getTimeoutDuration());
    }

  }

}
