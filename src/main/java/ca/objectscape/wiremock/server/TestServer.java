package ca.objectscape.wiremock.server;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

public class TestServer
    extends WireMockServer
{
  public TestServer() {
    super(WireMockConfiguration.wireMockConfig()
        .port(8080)
        .extensions(new ResponseTemplateTransformer(false)));
  }

  public void initialSetup() {
    // GET : exact URL matching
    givenThat(get(urlEqualTo("/wiremock"))
        .willReturn(aResponse()
            .withBody("Welcome to wiremock!")
        ));

    // GET : regex URL matching : capture path segment : response templating
    givenThat(get(urlMatching("/wiremock/.*"))
        .willReturn(aResponse()
            .withBody("Welcome to wiremock # {{request.pathSegments.[1]}}")
            .withTransformers("response-template")
        ));

    // GET : exact URL path : response from file
    givenThat(get(urlPathEqualTo("/apps"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json;charset=UTF-8")
            .withBodyFile("json/app-list.json")
        ));

    // GET : exact URL path : query param
    givenThat(get(urlPathEqualTo("/apps"))
        .withQueryParam("orgId", equalTo("org-1"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json;charset=UTF-8")
            .withBody("[{\"id\": \"app-1\", \"name\": \"app one\", \"orgId\": \"org-1\"}]")
        ));

    // GET : exact URL path : not found
    givenThat(get(urlPathEqualTo("/apps/app-5"))
        .willReturn(aResponse()
            .withStatus(404)
            .withBody("Invalid application ID: app-5")
        ));

    scenarioSetup();
  }

  public void scenarioSetup() {
    // mock initial org list
    givenThat(get(urlEqualTo("/orgs")).inScenario("Org list")
        .whenScenarioStateIs(STARTED)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json;charset=UTF-8")
            .withBody("[{\"id\": \"org-1\"}]")
        ));

    // mock new org addition
    givenThat(post(urlEqualTo("/orgs")).inScenario("Org list")
        .whenScenarioStateIs(STARTED)
        .willSetStateTo("Org 2 added")
        .withRequestBody(containing("org-2"))
        .willReturn(aResponse()
            .withStatus(201)
        ));

    // mock final org list
    givenThat(get(urlEqualTo("/orgs")).inScenario("Org list")
        .whenScenarioStateIs("Org 2 added")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json;charset=UTF-8")
            .withBody("[{\"id\": \"org-1\"},{\"id\": \"org-2\"}]")
        ));
  }
}
