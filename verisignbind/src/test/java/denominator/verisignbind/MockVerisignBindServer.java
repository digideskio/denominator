package denominator.verisignbind;

import static denominator.assertj.MockWebServerAssertions.assertThat;

import java.io.IOException;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import denominator.Credentials;
import denominator.Credentials.ListCredentials;
import denominator.CredentialsConfiguration;
import denominator.DNSApiManager;
import denominator.Denominator;
import denominator.assertj.RecordedRequestAssert;

public class MockVerisignBindServer extends VerisignBindProvider implements TestRule {

  private final MockWebServer delegate = new MockWebServer();
  private String username;
  private String password;

  MockVerisignBindServer() {
    credentials("testuser", "password");
  }

  @Override
  public String url() {
    return "http://localhost:" + delegate.getPort();
  }

  DNSApiManager connect() {
    return Denominator.create(this, CredentialsConfiguration.credentials(credentials()));
  }

  Credentials credentials() {
    return ListCredentials.from(username, password);
  }

  MockVerisignBindServer credentials(String username, String password) {
    this.username = username;
    this.password = password;
    return this;
  }

  void enqueue(MockResponse mockResponse) {
    delegate.enqueue(mockResponse);
  }

  RecordedRequestAssert assertRequest() throws InterruptedException {
    return assertThat(delegate.takeRequest());
  }

  void shutdown() throws IOException {
    delegate.shutdown();
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return delegate.apply(base, description);
  }

  @dagger.Module(injects = DNSApiManager.class, complete = false,
      includes = VerisignBindProvider.Module.class)
  static final class Module {

  }
}
