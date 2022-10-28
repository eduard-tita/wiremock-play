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

public class TestClient
{
  CloseableHttpClient httpClient = HttpClients.createDefault();

  public TestResponse get(String url) throws IOException {
    HttpGet request = new HttpGet(url);
    CloseableHttpResponse httpResponse = httpClient.execute(request);
    String content = convertResponseToString(httpResponse);
    return new TestResponse(httpResponse.getCode(), content);
  }

  private String convertResponseToString(CloseableHttpResponse response) throws IOException {
    InputStream responseStream = response.getEntity().getContent();
    Scanner scanner = new Scanner(responseStream, "UTF-8");
    String responseString = scanner.useDelimiter("\\Z").next();
    scanner.close();
    return responseString;
  }

  public TestResponse post(final String url, final String body) throws IOException {
    HttpPost request = new HttpPost(url);
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
