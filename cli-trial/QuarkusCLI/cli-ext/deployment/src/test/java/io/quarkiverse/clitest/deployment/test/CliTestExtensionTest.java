package io.quarkiverse.clitest.deployment.test;

import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;

public class CliTestExtensionTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().withEmptyApplication();

    @Test
    public void testDoit() {
        RestAssured.when().get("/doit").then().statusCode(200).body(containsString("Hello"));
    }
}
