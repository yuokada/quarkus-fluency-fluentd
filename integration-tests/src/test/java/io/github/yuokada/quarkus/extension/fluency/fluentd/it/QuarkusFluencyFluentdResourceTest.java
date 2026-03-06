package io.github.yuokada.quarkus.extension.fluency.fluentd.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class QuarkusFluencyFluentdResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/quarkus-fluency-fluentd")
                .then()
                .statusCode(200)
                .body(is("Hello quarkus-fluency-fluentd"));
    }

    @Test
    public void testStatusEndpoint() {
        // No real Fluentd in tests — expect 200 (connected) or 503 (disconnected), not a 5xx crash
        int status = given()
                .when().get("/quarkus-fluency-fluentd/status")
                .then()
                .extract().statusCode();
        assert status == 200 || status == 503;
    }

    @Test
    public void testEmitEndpoint() {
        // No real Fluentd in tests — 200 or 503 both acceptable
        int status = given()
                .when().post("/quarkus-fluency-fluentd/emit?tag=test.tag&message=hello")
                .then()
                .extract().statusCode();
        assert status == 200 || status == 503;
    }

    @Test
    public void testValidatedEmitWithValidInput() {
        // No real Fluentd — 200 or 503 acceptable, but no 400
        int status = given()
                .when().post("/quarkus-fluency-fluentd/validated-emit?tag=myapp.events&message=hello")
                .then()
                .extract().statusCode();
        assert status == 200 || status == 503;
    }

    @Test
    public void testValidatedEmitWithNullTagReturnsBadRequest() {
        given()
                .when().post("/quarkus-fluency-fluentd/validated-emit?message=hello")
                .then()
                .statusCode(400)
                .body(containsString("tag must not be null or blank"));
    }

    @Test
    public void testValidatedEmitWithInvalidTagFormatReturnsBadRequest() {
        given()
                .when().post("/quarkus-fluency-fluentd/validated-emit?tag=.invalid&message=hello")
                .then()
                .statusCode(400)
                .body(containsString("invalid tag format"));
    }
}
