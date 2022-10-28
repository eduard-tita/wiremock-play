package ca.objectscape.wiremock;

import java.io.IOException;

import ca.objectscape.wiremock.client.TestClient;
import ca.objectscape.wiremock.client.TestClient.TestResponse;
import ca.objectscape.wiremock.server.TestServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestOne {

  final Logger logger = LoggerFactory.getLogger(TestOne.class);

  static TestServer testServer;

  static TestClient testClient;

  @BeforeClass
  public static void setup() {
    testServer = new TestServer();
    testServer.start();
    testServer.initialSetup();
    testClient = new TestClient(testServer.port());
  }

  @AfterClass
  public static void tearDown() {
    testServer.stop();
  }

  @Test
  public void testExactUrlMatching() throws IOException {
    TestResponse response = testClient.get("http://localhost:8080/wiremock");
    logger.info("Response: {}", response);

    assertThat(response.code, is(200));
    assertThat(response.content, is("Welcome to wiremock!"));

    verify(getRequestedFor(urlEqualTo("/wiremock")));
  }

  @Test
  public void testRegextUrlMatching() throws IOException {
    TestResponse response = testClient.get("http://localhost:8080/wiremock/123");
    logger.info("Response: {}", response);

    assertThat(response.code, is(200));
    assertThat(response.content, is("Welcome to wiremock # 123"));
  }

  @Test
  public void testExactUrlPathQueryParam() throws IOException {
    // get app list
    TestResponse response = testClient.get("http://localhost:8080/apps");
    logger.info("Response: {}", response);

    assertThat(response.code, is(200));
    assertThat(response.content, is("[{\"id\": \"app-1\", \"name\": \"app one\", \"orgId\": \"org-1\"}, " +
        "{\"id\": \"app-2\", \"name\": \"app two\", \"orgId\": \"org-2\"}]"));

    // get app list for org 1 only
    response = testClient.get("http://localhost:8080/apps?dummy=3&orgId=org-1");
    logger.info("Response: {}", response);

    assertThat(response.code, is(200));
    assertThat(response.content, is("[{\"id\": \"app-1\", \"name\": \"app one\", \"orgId\": \"org-1\"}]"));
  }

  @Test
  public void testBadRequest() throws IOException {
    TestResponse response = testClient.get("http://localhost:8080/apps/app-5");
    logger.info("Response: {}", response);

    assertThat(response.code, is(404));
    assertThat(response.content, is("Invalid application ID: app-5"));
  }

  @Test
  public void testOrgAdditionFlow() throws IOException {
    // initially there's only org-1
    TestResponse response = testClient.get("http://localhost:8080/orgs");
    logger.info("Response: {}", response);

    assertThat(response.code, is(200));
    assertThat(response.content, is("[{\"id\": \"org-1\"}]"));

    // add org-2
    response = testClient.post("http://localhost:8080/orgs", "{\"id\": \"org-2\"}");
    assertThat(response.code, is(201));

    // now there are two orgs
    response = testClient.get("http://localhost:8080/orgs");
    logger.info("Response: {}", response);

    assertThat(response.code, is(200));
    assertThat(response.content, is("[{\"id\": \"org-1\"},{\"id\": \"org-2\"}]"));

    testServer.resetScenario("Org list");
  }

  @Test
  public void testOrgAdditionFlow2() throws IOException {
    // initially there's only org-1
    TestResponse response = testClient.get("http://localhost:8080/orgs");
    logger.info("Response: {}", response);

    assertThat(response.code, is(200));
    assertThat(response.content, is("[{\"id\": \"org-1\"}]"));

    // add org-2 by changing scenario's state
    testServer.setScenarioState("Org list", "Org 2 added");

    // now there are two orgs
    response = testClient.get("http://localhost:8080/orgs");
    logger.info("Response: {}", response);

    assertThat(response.code, is(200));
    assertThat(response.content, is("[{\"id\": \"org-1\"},{\"id\": \"org-2\"}]"));

    testServer.resetScenario("Org list");
  }
}
