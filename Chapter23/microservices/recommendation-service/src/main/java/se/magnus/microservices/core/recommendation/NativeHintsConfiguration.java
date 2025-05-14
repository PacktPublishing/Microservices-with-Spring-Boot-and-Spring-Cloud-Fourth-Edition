package se.magnus.microservices.core.recommendation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(NativeHintsConfiguration.class)
public class NativeHintsConfiguration implements RuntimeHintsRegistrar {

  private static final Logger LOG = LoggerFactory.getLogger(NativeHintsConfiguration.class);

  @Override
  public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
    LOG.info("Will register native hints for the recommendation service");

    // Workaround for the error: "java.io.IOException: Resource not found: "org/joda/time/tz/data/ZoneInfoMap"
    hints.resources().registerPattern("org/joda/time/tz/data/**");
  }
}
