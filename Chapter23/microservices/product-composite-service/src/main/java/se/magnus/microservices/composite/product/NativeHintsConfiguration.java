package se.magnus.microservices.composite.product;

import static org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_METHODS;

import com.fasterxml.jackson.databind.BeanDescription;
import io.swagger.v3.core.jackson.mixin.Schema31Mixin;
import io.swagger.v3.oas.models.media.JsonSchema;
import java.lang.invoke.MethodHandles;
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

    LOG.info("Will register native hints for the product composite service");

    // Due to missing native hint in springdoc-openapi v2.8.3
    // See https://github.com/swagger-api/swagger-ui/issues/10220
    hints.reflection().registerType(BeanDescription.class, INVOKE_DECLARED_METHODS);

    hints.reflection().registerType(MethodHandles.Lookup.class);
    hints.reflection().registerType(Schema31Mixin.TypeSerializer.class, INVOKE_DECLARED_CONSTRUCTORS);
    hints.reflection().registerType(JsonSchema.class, INVOKE_DECLARED_CONSTRUCTORS);

    // Workaround for the error: "java.io.IOException: Resource not found: "org/joda/time/tz/data/ZoneInfoMap"
    hints.resources().registerPattern("org/joda/time/tz/data/**");
  }
}
