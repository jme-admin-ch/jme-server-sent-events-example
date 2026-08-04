package ch.admin.bit.jme.web.ui;

import ch.admin.bit.jeap.security.test.mock.OidcAuthorizationMockServer;
import ch.admin.bit.jeap.security.test.resource.configuration.DisableJeapPermitAllSecurityConfiguration;
import ch.admin.bit.jeap.messaging.kafka.test.KafkaIntegrationTestBase;
import ch.admin.bit.jeap.server.sent.events.messaging.NotifyClientTopicValidator;
import ch.admin.bit.jeap.server.sent.events.web.NotifyClientHeartbeatSender;
import ch.admin.bit.jme.domain.Person;
import ch.admin.bit.jme.domain.PersonRepository;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@SpringBootTest(
        webEnvironment = DEFINED_PORT,
        properties = {
                "server.shutdown=immediate",
                "jeap.messaging.kafka.embedded=true",
                "jeap.messaging.kafka.cluster.default.bootstrapServers=${spring.embedded.kafka.brokers}"
        }
)
@ActiveProfiles("test")
@Import(DisableJeapPermitAllSecurityConfiguration.class)
@MockitoBean(types = {
        NotifyClientHeartbeatSender.class,
        NotifyClientTopicValidator.class
})
abstract class UiBrowserTestBase extends KafkaIntegrationTestBase {

    private static final int APP_PORT = 8311;

    private static final String APP_ORIGIN =
            "http://localhost:" + APP_PORT;

    private static final String CONTEXT_PATH =
            "/jme-server-sent-events-scs";

    protected static final String APP_URL =
            APP_ORIGIN + CONTEXT_PATH + "/";

    protected static final String PERSON_READ_ROLE =
            "jme_@person_#read";

    protected static final String PERSON_WRITE_ROLE =
            "jme_@person_#write";

    protected static final String JME_SSE_READ =
            "jme_@sse_#read";

    protected static final List<String> ALL_ROLES =
            List.of(
                    PERSON_READ_ROLE,
                    PERSON_WRITE_ROLE,
                    JME_SSE_READ
            );

    protected static final List<String> VIEW_ONLY_ROLES =
            List.of(PERSON_READ_ROLE);

    protected static final List<String> UNRELATED_ROLES =
            List.of(JME_SSE_READ);

    private static final String CLIENT_ID =
            "jme-server-sent-events-scs";

    private static final String SUBJECT =
            "77AE502D-04D1-43AE-95AE-8AF54EAF5EA3";

    private static final int OAUTH_MOCK_PORT = 8312;

    private static final String OAUTH_MOCK_BASE_PATH =
            "/oidc-mock";

    private static final String DEFAULT_PROFILE =
            "default";

    private static final String VIEW_ONLY_PROFILE =
            "view-only";

    private static final String UNRELATED_PROFILE =
            "unrelated";

    private static final Map<List<String>, String>
            PROFILE_NAMES_BY_ROLES = Map.of(
            ALL_ROLES,
            DEFAULT_PROFILE,

            VIEW_ONLY_ROLES,
            VIEW_ONLY_PROFILE,

            UNRELATED_ROLES,
            UNRELATED_PROFILE
    );

    private static OidcAuthorizationMockServer oauthMockServer;
    private static Playwright playwright;
    private static Browser browser;

    @Autowired
    private PersonRepository personRepository;

    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void startBrowser() {
        playwright = Playwright.create(
                new Playwright.CreateOptions()
                        .setEnv(Map.of(
                                "PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1"
                        ))
        );

        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setChannel("chrome")
                        .setHeadless(true)
        );
    }

    @AfterAll
    static void stopBrowser() {
        if (browser != null) {
            browser.close();
            browser = null;
        }

        if (oauthMockServer != null) {
            oauthMockServer.stop();
            oauthMockServer = null;
        }

        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

    @BeforeEach
    void prepareBrowserTest() {
        personRepository.deleteAll();
    }

    @AfterEach
    void closeBrowserContext() {
        closeCurrentContext();
    }

    protected void openPageWithRoles(
            List<String> roles
    ) {
        String profileName =
                PROFILE_NAMES_BY_ROLES.get(roles);

        if (profileName == null) {
            throw new IllegalArgumentException(
                    "No OIDC mock profile configured for roles: "
                            + roles
            );
        }

        ensureMockServerStarted();

        oauthMockServer.reset();
        oauthMockServer.setActiveProfile(profileName);

        closeCurrentContext();

        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setLocale("de-CH")
        );

        page = context.newPage();
    }

    protected void savePerson(
            String firstName,
            String lastName
    ) {
        personRepository.save(
                new Person(
                        UUID.randomUUID(),
                        firstName,
                        lastName
                )
        );
    }

    protected boolean personExists(
            String firstName,
            String lastName
    ) {
        return personRepository.findAll()
                .stream()
                .anyMatch(person ->
                        firstName.equals(
                                person.getFirstname()
                        )
                                && lastName.equals(
                                person.getLastname()
                        )
                );
    }

    private static synchronized void ensureMockServerStarted() {
        if (oauthMockServer != null) {
            return;
        }

        OidcAuthorizationMockServer mockServer =
                OidcAuthorizationMockServer
                        .builder(
                                OAUTH_MOCK_PORT,
                                OAUTH_MOCK_BASE_PATH,
                                APP_ORIGIN
                        )
                        .withDefaultClientId(CLIENT_ID)
                        .withSubject(SUBJECT)
                        .withGivenName("E2E")
                        .withFamilyName("Testuser")
                        .withName("E2E Testuser")
                        .withUserRoles(ALL_ROLES)
                        .withRoleProfile(
                                VIEW_ONLY_PROFILE,
                                VIEW_ONLY_ROLES
                        )
                        .withRoleProfile(
                                UNRELATED_PROFILE,
                                UNRELATED_ROLES
                        )
                        .build();

        mockServer.start();
        oauthMockServer = mockServer;
    }

    private void closeCurrentContext() {
        if (context == null) {
            return;
        }

        context.close();
        context = null;
        page = null;
    }
}
