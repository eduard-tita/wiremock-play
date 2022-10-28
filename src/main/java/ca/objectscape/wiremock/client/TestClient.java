package ca.objectscape.wiremock.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClient
{
  final Logger logger = LoggerFactory.getLogger(TestClient.class);

  CloseableHttpClient httpClient = HttpClients.createDefault();

  int port;

  public TestClient(int port) {
    this.port = port;
    logger.info("Port set to: {}", port);
  }

  public TestResponse get(String url) throws IOException {
    HttpGet request = new HttpGet(adjustPort(url));
    logger.info("GET: {}", url);
    CloseableHttpResponse httpResponse = httpClient.execute(request);
    String content = convertResponseToString(httpResponse);
    return new TestResponse(httpResponse.getCode(), content);
  }

  private String adjustPort(final String url) {
    return port != 8080 ? url.replace(":8080", ":" + port) : url;
  }

  private String convertResponseToString(CloseableHttpResponse response) throws IOException {
    InputStream responseStream = response.getEntity().getContent();
    Scanner scanner = new Scanner(responseStream, "UTF-8");
    String responseString = scanner.useDelimiter("\\Z").next();
    scanner.close();
    return responseString;
  }

  public TestResponse post(final String url, final String body) throws IOException {
    HttpPost request = new HttpPost(adjustPort(url));
    logger.info("POST: {}", url);
    HttpEntity entity = new StringEntity(body, ContentType.APPLICATION_JSON);
    request.setEntity(entity);
    CloseableHttpResponse httpResponse = httpClient.execute(request);
    return new TestResponse(httpResponse.getCode(), null);
  }

  public static class TestResponse {
    public int code;

    public String content;

    public TestResponse(final int code, final String content) {
      this.code = code;
      this.content = content;
    }

    @Override
    public String toString() {
      return "{" +
          "code=" + code +
          ", content='" + content + '\'' +
          '}';
    }
  }
}
