/*
 * This Java source file was generated by the Gradle 'init' task.
 */
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.vavr.collection.Seq;
import io.vavr.control.Try;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;



public class AppTest {
    @Test public void testAppHasAGreeting() throws Exception{
        RateLimiterRegistry registry = setup();
        RateLimiter rateLimiter = registry.rateLimiter("backendService", "backendServiceConfig");

        // Decorate your call to BackendService.doSomething()
        Supplier<String> restrictedSupplier = RateLimiter
                .decorateSupplier(rateLimiter, Service::sayHello);

        // First call is successful
        Try<String> firstTry = Try.ofSupplier(restrictedSupplier);
        Assertions.assertThat(firstTry.get()).isEqualTo("Hello partner");
        Assertions.assertThat(firstTry.isSuccess()).isTrue();

        // Second call fails, because the call was not permitted
        Try<String> secondTry = Try.ofSupplier(restrictedSupplier);
        Assertions.assertThat(secondTry.isFailure()).isTrue();
        Assertions.assertThat(secondTry.getCause()).isInstanceOf(RequestNotPermitted.class);


        // Create a RateLimiter
        RateLimiter anotherRateLimiter = registry.rateLimiter("anotherService", "anotherServiceConfig");
        Supplier<String> anotherRestrictedSupplier = RateLimiter
                .decorateSupplier(anotherRateLimiter, AnotherService::sayHelloFriendly);

        trySomething(anotherRestrictedSupplier);
        anotherRateLimiter.changeLimitForPeriod(3);

        Thread.sleep(1000);

        trySomething(anotherRestrictedSupplier);



    }

    private RateLimiterRegistry setup() {
        Map configs = new HashMap<>();
        RateLimiterConfig config = RateLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(100))
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(1)
                .build();
        
        configs.put("backendServiceConfig", config);

        RateLimiterConfig anotherConfig = RateLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(100))
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(2)
                .build();
        configs.put("anotherServiceConfig", anotherConfig);

        return RateLimiterRegistry.of(configs);

    }



    private void trySomething(Supplier<String> anotherRestrictedSupplier ){

        Try<String> anotherTry = Try.ofSupplier(anotherRestrictedSupplier);
        Assertions.assertThat(anotherTry.get()).isEqualTo("Hi partner!");
        Assertions.assertThat(anotherTry.isSuccess()).isTrue();

        Try<String> secondTryAgain = Try.ofSupplier(anotherRestrictedSupplier);
        Assertions.assertThat(secondTryAgain.get()).isEqualTo("Hi partner!");
        Assertions.assertThat(secondTryAgain.isSuccess()).isTrue();

        Try<String> thirdTry = Try.ofSupplier(anotherRestrictedSupplier);
        Assertions.assertThat(thirdTry.isFailure()).isTrue();
        Assertions.assertThat(thirdTry.getCause()).isInstanceOf(RequestNotPermitted.class);

    }
}
