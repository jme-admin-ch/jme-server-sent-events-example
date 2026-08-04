package ch.admin.bit.jme.web;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@EnabledIfSystemProperty(named = AfterDeploymentSmokeTestIT.DEPLOY_STAGE_PROPERTY_NAME, matches = "dev|d")
public class AfterDeploymentSmokeTestIT {

    public static final String DEPLOY_STAGE_PROPERTY_NAME = "deployStage";
    private static final String DEPLOY_PLATFORM_PROPERTY_NAME = "deployPlatform";

    private static final String AWS = "aws";
    private static final String RHOS = "rhos";
    private static final String DEV = "dev";

    private static final Map<String, String> PLATFORM_BASE_URI_TEMPLATE_MAP = Map.of(
            RHOS, "https://bit-jme-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch",
            AWS, "https://jme-dev.ingress.nivel.bazg.admin.ch");

    private static final String AUTH_SERVER_TOKEN_PATH = "/jme-server-sent-events-auth-scs/oauth2/token";
    private static final String PERSONS_API_PATH = "/jme-server-sent-events-secure-scs/api/persons";
    private static final String SSE_API_PATH = "/jme-server-sent-events-secure-scs/ui-api/sse/events";

    private String authServerUrl;
    private RequestSpecification request;

    @Test
    void createAndDeletePerson() throws Exception {
        // Prepare to receive events
        String baseUrl = getBaseUri();
        final String suffix = UUID.randomUUID().toString();

        String accessToken = retrieveAccessToken();

        SSEClientTestSupport.SSEClient testClient = SSEClientTestSupport.createTestClient(baseUrl, SSE_API_PATH, 1, accessToken, true);

        // Get current count
        int initialPersonCount = getCurrentCount(accessToken);

        // Create person
        Response createPersonResponse = given()
                .spec(request)
                .contentType(ContentType.JSON)
                .auth().oauth2(accessToken)
                .when().post("%s?firstname=first%s&lastname=last%s".formatted(PERSONS_API_PATH, suffix, suffix));
        assertEquals(201, createPersonResponse.getStatusCode());

        JsonPath personResponseJson = createPersonResponse.jsonPath();
        String personId = personResponseJson.get("id");

        assertEquals(initialPersonCount + 1, getCurrentCount(accessToken));
        assertTrue(getPersons(accessToken).contains(personId));

        testClient.waitFor(5);
        // Assert event types
        testClient.expectEventTypes("RESOURCE_CREATED");
        // Assert payloads
        testClient.expectPayloads(Map.of("path", "persons"));

        testClient.close();


        Thread.sleep(500); // Wait a bit to stabilize test // NOSONAR

        // We make a new client to receive the delete event
        testClient = SSEClientTestSupport.createTestClient(baseUrl, SSE_API_PATH, 1, accessToken, true);

        // Clean up by deleting person
        Response deletePersonResponse = given()
                .spec(request)
                .auth().oauth2(accessToken)
                .when().delete("%s/%s".formatted(PERSONS_API_PATH, personId));
        assertEquals(200, deletePersonResponse.getStatusCode());
        assertEquals(initialPersonCount, getCurrentCount(accessToken));

        testClient.waitFor(5);
        // Assert event types
        testClient.expectEventTypes("RESOURCE_DELETED");
        // Assert payloads
        testClient.expectPayloads(Map.of("path", "persons"));

        testClient.close();
    }

    @BeforeEach
    void setUp() {
        String baseUri = getBaseUri();
        authServerUrl = baseUri + AUTH_SERVER_TOKEN_PATH;

        config.getLogConfig().blacklistHeader(HttpHeaders.AUTHORIZATION, HttpHeaders.SET_COOKIE);
        request = new RequestSpecBuilder()
                .addFilter(new ResponseLoggingFilter())
                .setBaseUri(baseUri).build();

        log.info("Running tests with baseUri={} and authServerUrl={}", baseUri, authServerUrl);
    }

    private int getCurrentCount(String accessToken) {
        Response response = given()
                .spec(request)
                .auth().oauth2(accessToken)
                .when().get(PERSONS_API_PATH);
        return response.getBody().as(List.class).size();
    }

    private String getPersons(String accessToken) {
        Response response = given()
                .spec(request)
                .auth().oauth2(accessToken)
                .when().get(PERSONS_API_PATH);
        return response.getBody().asString();
    }

    private String retrieveAccessToken() {
        // A client must be defined with the roles 'jme_@person_#read' and 'jme_@person_#write' in the OAuth-Mock-Server on DEV
        return given()
                .config(config().encoderConfig(encoderConfig()
                        .encodeContentTypeAs("x-www-form-urlencoded", ContentType.URLENC)))
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .formParam("grant_type", "client_credentials")
                .formParam("client_id", "microService")
                .formParam("client_secret", "secret")
                .post(authServerUrl)
                .jsonPath().get("access_token");
    }

    private static String getBaseUri() {
        String deployStage = getDeployStage();
        String deployPlatform = getDeployPlatform();
        log.info("Getting baseUri for deployStage {} on deployPlatform {}.", deployStage, deployPlatform);

        String baseUri = PLATFORM_BASE_URI_TEMPLATE_MAP.get(deployPlatform);
        log.info("baseUri is: {}.", baseUri);
        return baseUri;
    }

    private static String getDeployPlatform() {
        return System.getProperty(DEPLOY_PLATFORM_PROPERTY_NAME, RHOS);
    }

    private static String getDeployStage() {
        return System.getProperty(DEPLOY_STAGE_PROPERTY_NAME, DEV);
    }
}
