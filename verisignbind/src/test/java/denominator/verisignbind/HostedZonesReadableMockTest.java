package denominator.verisignbind;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import com.squareup.okhttp.mockwebserver.MockResponse;

import denominator.DNSApiManager;

public class HostedZonesReadableMockTest {

  @Rule
  public final MockVerisignBindServer server = new MockVerisignBindServer();

  @Test
  public void singleRequestOnSuccess() throws Exception {
    server.enqueue(new MockResponse().setBody("[]"));

    DNSApiManager api = server.connect();
    assertTrue(api.checkConnection());

    server.assertRequest();
  }

  @Test
  public void singleRequestOnFailure() throws Exception {
    server.enqueue(new MockResponse().setResponseCode(500));

    DNSApiManager api = server.connect();
    assertFalse(api.checkConnection());

    server.assertRequest();
  }
}
