package io.github.yuokada.quarkus.extension.fluency.fluentd.it;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@QuarkusTestResource(FluentdTestResource.class)
public class QuarkusFluencyFluentdResourceTest {

    @Test
    public void testHelloEndpoint() {
        given().when()
                .get("/quarkus-fluency-fluentd")
                .then()
                .statusCode(200)
                .body(is("Hello quarkus-fluency-fluentd"));
    }

    @Test
    public void testStatusEndpoint() {
        // No real Fluentd in tests — expect 200 (connected) or 503 (disconnected), not a 5xx crash
        int status =
                given().when().get("/quarkus-fluency-fluentd/status").then().extract().statusCode();
        Assertions.assertTrue(
                status == 200 || status == 503, "Expected 200 or 503 but got: " + status);
    }

    @Test
    public void testEmitEndpoint() {
        // No real Fluentd in tests — 200 or 503 both acceptable
        int status =
                given().when()
                        .post("/quarkus-fluency-fluentd/emit?tag=test.tag&message=hello")
                        .then()
                        .extract()
                        .statusCode();
        Assertions.assertTrue(
                status == 200 || status == 503, "Expected 200 or 503 but got: " + status);
    }

    @Test
    public void testValidatedEmitWithValidInput() {
        // No real Fluentd — 200 or 503 acceptable, but no 400
        int status =
                given().when()
                        .post(
                                "/quarkus-fluency-fluentd/validated-emit?tag=myapp.events&message=hello")
                        .then()
                        .extract()
                        .statusCode();
        Assertions.assertTrue(
                status == 200 || status == 503, "Expected 200 or 503 but got: " + status);
    }

    @Test
    public void testValidatedEmitWithNullTagReturnsBadRequest() {
        given().when()
                .post("/quarkus-fluency-fluentd/validated-emit?message=hello")
                .then()
                .statusCode(400)
                .body(containsString("tag must not be null or blank"));
    }

    @Test
    public void testValidatedEmitWithInvalidTagFormatReturnsBadRequest() {
        given().when()
                .post("/quarkus-fluency-fluentd/validated-emit?tag=.invalid&message=hello")
                .then()
                .statusCode(400)
                .body(containsString("invalid tag format"));
    }

    @Test
    public void testReadinessHealthCheckIsRegistered() {
        // The health check must be present in the readiness endpoint. 200 (UP) or 503 (DOWN) both
        // indicate the check ran. Asserting on the JSON structure (checks[].name == "fluentd")
        // rather than a raw substring avoids false positives from other components that happen to
        // include the word "fluentd" in their output.
        given().when()
                .get("/q/health/ready")
                .then()
                .statusCode(anyOf(is(200), is(503)))
                .body("checks.name", hasItem("fluentd"));
    }
}
