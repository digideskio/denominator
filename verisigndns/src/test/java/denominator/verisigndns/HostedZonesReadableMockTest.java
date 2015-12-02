package denominator.verisigndns;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import com.squareup.okhttp.mockwebserver.MockResponse;

import denominator.DNSApiManager;

public class HostedZonesReadableMockTest {

  @Rule
  public final MockVerisignDnsServer server = new MockVerisignDnsServer();

  @Test
  public void singleRequestOnSuccess() throws Exception {
    server
        .enqueue(new MockResponse()
            .setBody("<getZoneListRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
                + "   <callSuccess>true</callSuccess>"
                + "   <totalCount>1</totalCount>"
                + "     <zoneInfo>"
                + "       <domainName>denominator.io</domainName>"
                + "       <type>DNS Hosting</type>"
                + "       <status>ACTIVE</status>"
                + "       <createTimestamp>2015-09-29T01:55:39.000Z</createTimestamp>"
                + "       <updateTimestamp>2015-09-30T00:25:53.000Z</updateTimestamp>"
                + "       <geoLocationEnabled>No</geoLocationEnabled>"
                + "   </zoneInfo>" 
                + "</getZoneListRes>"));

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
