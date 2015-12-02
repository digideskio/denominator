package denominator.verisigndns;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.squareup.okhttp.mockwebserver.MockResponse;

import denominator.ZoneApi;
import denominator.model.Zone;

public class VerisignDnsZoneApiMockTest {

  @Rule
  public final MockVerisignDnsServer server = new MockVerisignDnsServer();

  @Test
  public void iteratorWhenPresent() throws Exception {
    server
    .enqueue(new MockResponse()
        .setBody("<getZoneListRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
            + "     <callSuccess>true</callSuccess>"
            + "     <totalCount>1</totalCount>"
            + "     <zoneInfo>"
            + "         <domainName>denominator.io</domainName>"
            + "         <type>DNS Hosting</type>"
            + "         <status>ACTIVE</status>"
            + "         <createTimestamp>2015-09-29T01:55:39.000Z</createTimestamp>"
            + "         <updateTimestamp>2015-09-30T00:25:53.000Z</updateTimestamp>"
            + "         <geoLocationEnabled>No</geoLocationEnabled>"
            + "     </zoneInfo>" 
            + "</getZoneListRes>"));    

    server
        .enqueue(new MockResponse()
            .setBody("<getZoneInfoRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
                + "     <callSuccess>true</callSuccess>"
                + "     <primaryZoneInfo>"
                + "         <domainName>denominator.io</domainName>"
                + "         <type>DNS Hosting</type>"
                + "         <status>ACTIVE</status>"
                + "         <createTimestamp>2015-09-29T13:58:53.000Z</createTimestamp>"
                + "         <updateTimestamp>2015-09-29T14:41:11.000Z</updateTimestamp>"
                + "         <zoneSOAInfo>"
                + "             <email>nil@denominator.io</email>"
                + "             <retry>7400</retry>"
                + "             <ttl>86400</ttl>"
                + "             <refresh>30000</refresh>"
                + "             <expire>1234567</expire>"
                + "             <serial>1443535137</serial>"
                + "         </zoneSOAInfo>"
                + "         <serviceLevel>COMPLETE</serviceLevel>"
                + "         <webParking>"
                + "             <parkingEnabled>false</parkingEnabled>"
                + "         </webParking>"
                + "         <verisignNSInfo>"
                + "             <virtualNameServerId>10</virtualNameServerId>"
                + "             <name>a1.verisigndns.com</name>"
                + "             <ipAddress>209.112.113.33</ipAddress>"
                + "             <ipv6Address>2001:500:7967::2:33</ipv6Address>"
                + "             <location>Anycast Global</location>"
                + "         </verisignNSInfo>"
                + "         <verisignNSInfo>"
                + "             <virtualNameServerId>11</virtualNameServerId>"
                + "             <name>a2.verisigndns.com</name>"
                + "             <ipAddress>209.112.114.33</ipAddress>"
                + "             <ipv6Address>2620:74:19::33</ipv6Address>"
                + "             <location>Anycast 1</location>"
                + "         </verisignNSInfo>"
                + "         <verisignNSInfo>"
                + "             <virtualNameServerId>12</virtualNameServerId>"
                + "             <name>a3.verisigndns.com</name>"
                + "             <ipAddress>69.36.145.33</ipAddress>"
                + "             <ipv6Address>2001:502:cbe4::33</ipv6Address>"
                + "             <location>Anycast 2</location>"
                + "         </verisignNSInfo>"
                + "     </primaryZoneInfo>" 
                + "</getZoneInfoRes>"));
    
    ZoneApi api = server.connect().api().zones();

    assertThat(api.iterator()).containsExactly(
        Zone.create("denominator.io", "denominator.io", 86400, "nil@denominator.io"));
  }

  @Test
  public void iteratorWhenAbsent() throws Exception {
    server.enqueue(new MockResponse().setBody("<api1:getZoneList></api1:getZoneList>"));

    ZoneApi api = server.connect().api().zones();
    assertThat(api.iterator()).isEmpty();
  }

  @Test
  public void iterateByNameWhenPresent() throws Exception {

    server
    .enqueue(new MockResponse()
        .setBody("<getZoneInfoRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
            + "     <callSuccess>true</callSuccess>"
            + "     <primaryZoneInfo>"
            + "         <domainName>denominator.io</domainName>"
            + "         <type>DNS Hosting</type>"
            + "         <status>ACTIVE</status>"
            + "         <createTimestamp>2015-09-29T13:58:53.000Z</createTimestamp>"
            + "         <updateTimestamp>2015-09-29T14:41:11.000Z</updateTimestamp>"
            + "         <zoneSOAInfo>"
            + "             <email>nil@denominator.io</email>"
            + "             <retry>7400</retry>"
            + "             <ttl>86400</ttl>"
            + "             <refresh>30000</refresh>"
            + "             <expire>1234567</expire>"
            + "             <serial>1443535137</serial>"
            + "         </zoneSOAInfo>"
            + "         <serviceLevel>COMPLETE</serviceLevel>"
            + "         <webParking>"
            + "             <parkingEnabled>false</parkingEnabled>"
            + "         </webParking>"
            + "         <verisignNSInfo>"
            + "             <virtualNameServerId>10</virtualNameServerId>"
            + "             <name>a1.verisigndns.com</name>"
            + "             <ipAddress>209.112.113.33</ipAddress>"
            + "             <ipv6Address>2001:500:7967::2:33</ipv6Address>"
            + "             <location>Anycast Global</location>"
            + "         </verisignNSInfo>"
            + "         <verisignNSInfo>"
            + "             <virtualNameServerId>11</virtualNameServerId>"
            + "             <name>a2.verisigndns.com</name>"
            + "             <ipAddress>209.112.114.33</ipAddress>"
            + "             <ipv6Address>2620:74:19::33</ipv6Address>"
            + "             <location>Anycast 1</location>"
            + "         </verisignNSInfo>"
            + "         <verisignNSInfo>"
            + "             <virtualNameServerId>12</virtualNameServerId>"
            + "             <name>a3.verisigndns.com</name>"
            + "             <ipAddress>69.36.145.33</ipAddress>"
            + "             <ipv6Address>2001:502:cbe4::33</ipv6Address>"
            + "             <location>Anycast 2</location>"
            + "         </verisignNSInfo>"
            + "     </primaryZoneInfo>" 
            + "</getZoneInfoRes>"));
    
    ZoneApi api = server.connect().api().zones();

    assertThat(api.iterateByName("denominator.io")).containsExactly(
        Zone.create("denominator.io", "denominator.io", 86400, "nil@denominator.io"));
  }

  @Test
  public void iterateByNameWhenAbsent() throws Exception {
    server.enqueue(new MockResponse().setBody("<getZoneInfoRes></getZoneInfoRes>"));

    ZoneApi api = server.connect().api().zones();
    assertThat(api.iterateByName("denominator.io.")).isEmpty();
  }

  @Test
  public void putWhenPresent() throws Exception {
    server.enqueueError("ERROR_OPERATION_FAILURE",
        "Domain already exists. Please verify your domain name.");
    server.enqueue(new MockResponse());

    ZoneApi api = server.connect().api().zones();

    Zone zone = Zone.create("denominator.io", "denominator.io", 86400, "nil@denominator.io");
    api.put(zone);
  }

  @Test
  public void putWhenAbsent() throws Exception {
    server.enqueue(new MockResponse());
    server.enqueue(new MockResponse());
    ZoneApi api = server.connect().api().zones();

    Zone zone = Zone.create("denominator.io", "denominator.io", 86400, "nil@denominator.io");
    assertThat(api.put(zone)).isEqualTo(zone.name());
  }

  @Test
  public void deleteWhenPresent() throws Exception {
    server
        .enqueue(new MockResponse()
            .setBody("<dnsaWSRes:dnsaWSRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:dnsaWSRes=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
                + "     <callSuccess>false</callSuccess>" 
                + "</dnsaWSRes:dnsaWSRes>"));

    ZoneApi api = server.connect().api().zones();
    api.delete("denominator.io.");
  }

  @Test
  public void deleteWhenAbsent() throws Exception {
    server.enqueue(new MockResponse());

    ZoneApi api = server.connect().api().zones();
    api.delete("test.io");
  }
}
