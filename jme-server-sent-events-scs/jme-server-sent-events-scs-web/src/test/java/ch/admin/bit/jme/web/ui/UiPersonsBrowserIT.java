package ch.admin.bit.jme.web.ui;

import ch.admin.bit.jme.domain.Person;
import ch.admin.bit.jme.domain.PersonService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.UUID;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledIfSystemProperty(
        named = "deployStage",
        matches = ".*"
)
class UiPersonsBrowserIT extends UiBrowserTestBase {

    @Autowired
    private PersonService personService;

    @Test
    void personsOverview_showsSeededPeople() {
        savePerson("Alice", "Anderson");
        savePerson("Bob", "Baker");

        openPageWithRoles(ALL_ROLES);
        page.navigate(APP_URL);

        assertThat(
                page.getByText("Alice")
        ).isVisible();

        assertThat(
                page.getByText("Anderson")
        ).isVisible();

        assertThat(
                page.getByText("Bob")
        ).isVisible();

        assertThat(
                page.getByText("Baker")
        ).isVisible();
    }

    @Test
    void personsOverview_showsForbiddenForUserWithoutReadRole() {
        openPageWithRoles(UNRELATED_ROLES);
        page.navigate(APP_URL);

        Locator forbiddenHeading = page.getByRole(
                AriaRole.HEADING,
                new Page.GetByRoleOptions()
                        .setName("Access forbidden")
        );

        waitUntilVisible(forbiddenHeading);

        assertThat(forbiddenHeading).isVisible();
    }

    @Test
    void personsOverview_readOnlyUserCanOpenOverview() {
        openPageWithRoles(VIEW_ONLY_ROLES);
        page.navigate(APP_URL);

        assertThat(personsTable()).isVisible();
    }

    @Test
    void personsOverview_createsAndDeletesPersonViaUi() {
        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, 8);

        String firstName = "E2EFirst" + suffix;
        String lastName = "E2ELast" + suffix;

        openPageWithRoles(ALL_ROLES);
        page.navigate(APP_URL);

        /*
         * Open the create mask through the same toolbar action a user uses.
         * This also verifies the Angular navigation from the overview.
         */
        openCreatePersonFormFromOverview();

        fillCreateForm(
                firstName,
                lastName
        );

        /*
         * Wait for the actual POST response. This produces a useful failure
         * message when the backend rejects or rolls back the create request.
         */
        Response createResponse = page.waitForResponse(
                response ->
                        "POST".equals(
                                response.request().method()
                        )
                                && response.url().contains(
                                "/api/persons"
                        ),
                this::submitCreateForm
        );

        assertResponseStatus(
                createResponse,
                201,
                "Create person"
        );

        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertTrue(
                        personExists(firstName, lastName),
                        "The created person was not persisted"
                ));

        navigateToPersonsOverview();

        Locator createdPersonRow =
                personTableRow(
                        firstName,
                        lastName
                );

        waitUntilVisible(createdPersonRow);

        assertThat(createdPersonRow).isVisible();

        assertThat(
                createdPersonRow.getByText(firstName)
        ).isVisible();

        assertThat(
                createdPersonRow.getByText(lastName)
        ).isVisible();

        /*
         * Wait for the actual DELETE response before checking the repository.
         */
        Response deleteResponse = page.waitForResponse(
                response ->
                        "DELETE".equals(
                                response.request().method()
                        )
                                && response.url().contains(
                                "/api/persons/"
                        ),
                () -> deletePersonViaUi(
                        firstName,
                        lastName
                )
        );

        assertResponseStatus(
                deleteResponse,
                200,
                "Delete person"
        );

        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertFalse(
                        personExists(firstName, lastName),
                        "The deleted person still exists in the repository"
                ));

        navigateToPersonsOverview();

        assertThat(personTableRow(firstName, lastName)).hasCount(0);
    }

    @Test
    void personsOverview_receivesCreatedEventThroughSseTransport() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String firstName = "SseFirst" + suffix;
        String lastName = "SseLast" + suffix;
        UUID personId = UUID.randomUUID();

        openPageWithRoles(ALL_ROLES);
        page.waitForRequest(
                request -> request.url().contains("/ui-api/sse/events"),
                () -> page.navigate(APP_URL)
        );
        waitForPersonsOverview();

        Response sseResponse = page.waitForResponse(
                response -> response.url().contains("/ui-api/sse/events")
                        && response.status() == 200,
                () -> personService.save(
                        new Person(personId, firstName, lastName)
                )
        );

        assertResponseStatus(sseResponse, 200, "SSE connection");

        personService.deleteById(personId);
    }

    private void navigateToPersonsOverview() {
        page.navigate(APP_URL);
        waitForPersonsOverview();
    }

    private void waitForPersonsOverview() {
        waitUntilVisible(personsTable());
    }

    private Locator personsTable() {
        return page.locator(
                "[data-test-id='table-persons']:visible,"
                        + " qd-table:visible"
        ).first();
    }

    private void openCreatePersonFormFromOverview() {
        Locator createPersonButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Neue Person erfassen")
        );

        waitUntilVisible(createPersonButton);
        assertThat(createPersonButton).isEnabled();

        createPersonButton.click();

        page.waitForURL(
                "**/create",
                new Page.WaitForURLOptions()
        );

        waitUntilVisible(
                page.getByText(
                        "Personalien",
                        new Page.GetByTextOptions()
                                .setExact(true)
                )
        );

        waitUntilVisible(firstNameInput());
        waitUntilVisible(lastNameInput());
    }

    private void fillCreateForm(
            String firstName,
            String lastName
    ) {
        Locator firstNameField = firstNameInput();
        Locator lastNameField = lastNameInput();

        waitUntilVisible(firstNameField);
        waitUntilVisible(lastNameField);

        firstNameField.fill(firstName);
        lastNameField.fill(lastName);

        /*
         * Verify that the values were written into the native inputs.
         */
        assertThat(firstNameField)
                .hasValue(firstName);

        assertThat(lastNameField)
                .hasValue(lastName);
    }

    private Locator firstNameInput() {
        return page.locator(
                "qd-input[formcontrolname='firstName'] "
                        + "input[data-test-id='text-input-input']"
        ).first();
    }

    private Locator lastNameInput() {
        return page.locator(
                "qd-input[formcontrolname='lastName'] "
                        + "input[data-test-id='text-input-input']"
        ).first();
    }

    private void submitCreateForm() {
        Locator submitButton = page.locator(
                "[data-test-id='footer-primary-button-submit']"
        );

        waitUntilVisible(submitButton);

        submitButton.click();
    }

    private void deletePersonViaUi(
            String firstName,
            String lastName
    ) {
        Locator row = personTableRow(
                firstName,
                lastName
        );

        waitUntilVisible(row);

        Locator rowActionsButton = waitForFirstVisible(
                "Actions button for person '%s %s' was not found"
                        .formatted(
                                firstName,
                                lastName
                        ),

                row.locator(
                        "button[data-test-id$="
                                + "'secondary-actions-toggler'"
                                + "]"
                ),

                row.locator(
                        "button.menu-button"
                )
        );

        rowActionsButton.click();

        Locator deleteButton = waitForFirstVisible(
                "Delete action for person '%s %s' was not found"
                        .formatted(
                                firstName,
                                lastName
                        ),

                page.locator(
                        "button[data-test-id$="
                                + "'secondary-actions-0'"
                                + "]"
                ),

                page.getByRole(
                        AriaRole.BUTTON,
                        new Page.GetByRoleOptions()
                                .setName("Löschen")
                                .setExact(true)
                ),

                page.locator(
                        "button[data-test-id*="
                                + "'secondary-actions-'"
                                + "]"
                                + ":not("
                                + "[data-test-id$='toggler']"
                                + ")"
                                + ":has-text('Löschen')"
                ),

                page.locator(
                        "button:has-text('Löschen')"
                )
        );

        deleteButton.click();
    }

    private Locator personTableRow(
            String firstName,
            String lastName
    ) {
        /*
         * Restrict the lookup to rows inside the persons table.
         */
        return personsTable()
                .locator(
                        "[data-test-id^='table-row-'],"
                                + " tbody tr"
                )
                .filter(
                        new Locator.FilterOptions()
                                .setHasText(firstName)
                )
                .filter(
                        new Locator.FilterOptions()
                                .setHasText(lastName)
                )
                .first();
    }

    private void assertResponseStatus(
            Response response,
            int expectedStatus,
            String operation
    ) {
        if (response.status() == expectedStatus) {
            return;
        }

        String message = """
                %s returned HTTP %d instead of HTTP %d.
                Method: %s
                URL: %s
                Response body: %s
                """.formatted(
                operation,
                response.status(),
                expectedStatus,
                response.request().method(),
                response.url(),
                readResponseBodySafely(response)
        );

        throw new AssertionError(message);
    }

    private String readResponseBodySafely(
            Response response
    ) {
        try {
            String body = response.text();

            if (body == null || body.isBlank()) {
                return "<empty>";
            }

            return body;
        } catch (PlaywrightException exception) {
            return "<response body unavailable: "
                    + exception.getMessage()
                    + ">";
        }
    }

    private Locator waitForFirstVisible(
            String errorMessage,
            Locator... locators
    ) {
        PlaywrightException lastException = null;

        for (Locator locator : locators) {
            Locator first = locator.first();

            try {
                first.waitFor(
                        new Locator.WaitForOptions()
                                .setState(
                                        WaitForSelectorState.VISIBLE
                                )
                );

                return first;
            } catch (PlaywrightException exception) {
                lastException = exception;
            }
        }

        throw new IllegalStateException(
                errorMessage,
                lastException
        );
    }

    private void waitUntilVisible(
            Locator locator
    ) {
        locator.first().waitFor(
                new Locator.WaitForOptions()
                        .setState(
                                WaitForSelectorState.VISIBLE
                        )

        );
    }
}
