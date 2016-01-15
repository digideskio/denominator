package denominator.verisigndns;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.squareup.okhttp.mockwebserver.MockResponse;

import denominator.AllProfileResourceRecordSetApi;
import denominator.common.Util;
import denominator.model.ResourceRecordSet;

public class VerisignDnsResourceRecordSetApiMockTest {

  @Rule
  public final MockVerisignDnsServer server = new MockVerisignDnsServer();

  @Test
  public void iteratorWhenPresent() throws Exception {
    server
      .enqueue(new MockResponse()
      .setBody("<getResourceRecordListRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
          + "   <callSuccess>true</callSuccess>"
          + "   <totalCount>1</totalCount>"
          + "   <resourceRecord>"
          + "       <resourceRecordId>3194811</resourceRecordId>"
          + "       <owner>www.denominator.io.</owner>"
          + "       <type>A</type>"
          + "       <ttl>86400</ttl>"
          + "       <rData>127.0.0.1</rData>"
          + "   </resourceRecord>"
          + "</getResourceRecordListRes>"));
    
    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io");

    assertThat(recordSetsInZoneApi.iterator()).containsExactly(
        ResourceRecordSet.builder().name("www").type("A").ttl(86400)
            .add(Util.toMap("A", "127.0.0.1")).build());
  }

  @Test
  public void iteratorWhenAbsent() throws Exception {
    server.enqueue(new MockResponse()
        .setBody("<getResourceRecordListRes></getResourceRecordListRes>"));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io");
    assertThat(recordSetsInZoneApi.iterator()).isEmpty();
  }

  @Test
  public void iterateByNameWhenPresent() throws Exception {
    server
      .enqueue(new MockResponse()
      .setBody("<getResourceRecordListRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
          + "   <callSuccess>true</callSuccess>"
          + "   <totalCount>1</totalCount>"
          + "   <resourceRecord>"
          + "       <resourceRecordId>3194811</resourceRecordId>"
          + "       <owner>www.denominator.io.</owner>"
          + "       <type>A</type>"
          + "       <ttl>86400</ttl>"
          + "       <rData>127.0.0.1</rData>"
          + "   </resourceRecord>"
          + "</getResourceRecordListRes>"));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io");

    assertThat(recordSetsInZoneApi.iterateByName("www")).containsExactly(
        ResourceRecordSet.builder().name("www").type("A").ttl(86400)
            .add(Util.toMap("A", "127.0.0.1")).build());
  }

  @Test
  public void iterateByNameWhenAbsent() throws Exception {
    server.enqueue(new MockResponse()
        .setBody("<getResourceRecordListRes></getResourceRecordListRes>"));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io");
    assertThat(recordSetsInZoneApi.iterator()).isEmpty();
  }

  @Test
  public void putFirstRecordCreatesNewRRSet() throws Exception {
    server
    .enqueue(new MockResponse()
    .setBody("<getResourceRecordListRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
        + "   <callSuccess>true</callSuccess>"
        + "   <totalCount>0</totalCount>"
        + "</getResourceRecordListRes>"));
    
    server
      .enqueue(new MockResponse()
      .setBody("<getResourceRecordListRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
          + "   <callSuccess>true</callSuccess>"
          + "   <totalCount>1</totalCount>"
          + "   <resourceRecord>"
          + "       <resourceRecordId>3194811</resourceRecordId>"
          + "       <owner>www.denominator.io.</owner>"
          + "       <type>A</type>"
          + "       <ttl>86400</ttl>"
          + "       <rData>127.0.0.1</rData>"
          + "   </resourceRecord>"
          + "</getResourceRecordListRes>"));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io");
    assertThat(recordSetsInZoneApi.iterator()).isEmpty();

    recordSetsInZoneApi.put(ResourceRecordSet.builder().name("www").type("A").ttl(86400)
        .add(Util.toMap("A", "127.0.0.1")).build());
  }

  @Test
  public void putSameRecordNoOp() throws Exception {
    server
      .enqueue(new MockResponse()
      .setBody("<getResourceRecordListRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
          + "   <callSuccess>true</callSuccess>"
          + "   <totalCount>1</totalCount>"
          + "   <resourceRecord>"
          + "       <resourceRecordId>3194811</resourceRecordId>"
          + "       <owner>www.denominator.io.</owner>"
          + "       <type>A</type>"
          + "       <ttl>86400</ttl>"
          + "       <rData>127.0.0.1</rData>"
          + "   </resourceRecord>"
          + "</getResourceRecordListRes>"));
    server
      .enqueue(new MockResponse()
      .setBody("<getResourceRecordListRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
          + "   <callSuccess>true</callSuccess>"
          + "   <totalCount>1</totalCount>"
          + "   <resourceRecord>"
          + "       <resourceRecordId>3194811</resourceRecordId>"
          + "       <owner>www.denominator.io.</owner>"
          + "       <type>A</type>"
          + "       <ttl>86400</ttl>"
          + "       <rData>127.0.0.1</rData>"
          + "   </resourceRecord>"
          + "</getResourceRecordListRes>"));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io");

    recordSetsInZoneApi.put(ResourceRecordSet.builder().name("www").type("A").ttl(86400)
        .add(Util.toMap("A", "127.0.0.1")).build());

    assertThat(recordSetsInZoneApi.iterator()).hasSize(1);
  }

  @Test
  public void putOneRecordReplacesRRSet() throws Exception {
    server
    .enqueue(new MockResponse()
        .setBody("<getResourceRecordListRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
            + "   <callSuccess>true</callSuccess>"
            + "   <totalCount>2</totalCount>"
            + "   <resourceRecord>"
            + "       <resourceRecordId>3194811</resourceRecordId>"
            + "       <owner>www.denominator.io.</owner>"
            + "       <type>A</type>"
            + "       <ttl>86400</ttl>"
            + "       <rData>127.0.0.1</rData>"
            + "   </resourceRecord>"
            + "   <resourceRecord>"
            + "       <resourceRecordId>3194812</resourceRecordId>"
            + "       <owner>www1.denominator.io.</owner>"
            + "       <type>A</type>"
            + "       <ttl>86400</ttl>"
            + "       <rData>127.0.0.2</rData>"
            + "   </resourceRecord>"            
            + "</getResourceRecordListRes>"));
    
    server
      .enqueue(new MockResponse()
        .setBody("<getResourceRecordListRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
            + "   <callSuccess>true</callSuccess>"
            + "   <totalCount>1</totalCount>"
            + "   <resourceRecord>"
            + "       <resourceRecordId>3194811</resourceRecordId>"
            + "       <owner>www.denominator.io.</owner>"
            + "       <type>A</type>"
            + "       <ttl>86400</ttl>"
            + "       <rData>127.0.0.1</rData>"
            + "   </resourceRecord>"
            + "</getResourceRecordListRes>"));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io");
    assertThat(recordSetsInZoneApi.iterator()).hasSize(2);

    recordSetsInZoneApi.put(ResourceRecordSet.builder().name("www").type("A").ttl(86400)
        .add(Util.toMap("A", "127.0.0.1")).build());
  }

  @Test
  public void deleteWhenPresent() throws Exception {
    server
      .enqueue(new MockResponse()
        .setBody("<getResourceRecordListRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
            + "   <callSuccess>true</callSuccess>"
            + "   <totalCount>1</totalCount>"
            + "   <resourceRecord>"
            + "       <resourceRecordId>3194811</resourceRecordId>"
            + "       <owner>www.denominator.io.</owner>"
            + "       <type>A</type>"
            + "       <ttl>86400</ttl>"
            + "       <rData>127.0.0.1</rData>"
            + "   </resourceRecord>"
            + "</getResourceRecordListRes>"));
    server
      .enqueue(new MockResponse()
        .setBody("<dnsaWSRes:dnsaWSRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:dnsaWSRes=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
            + "     <callSuccess>false</callSuccess>" 
            + "</dnsaWSRes:dnsaWSRes>"));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io");
    recordSetsInZoneApi.deleteByNameAndType("www.denominator.io.", "A");
  }

  @Test
  public void deleteTxtRecordWhenPresent() throws Exception {
    server
      .enqueue(new MockResponse()
        .setBody("<getResourceRecordListRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
            + "   <callSuccess>true</callSuccess>"
            + "   <totalCount>1</totalCount>"
            + "   <resourceRecord>"
            + "       <resourceRecordId>3194811</resourceRecordId>"
            + "       <owner>www.denominator.io.</owner>"
            + "       <type>TXT</type>"
            + "       <ttl>86400</ttl>"
            + "       <rData>Sample TXT record</rData>"
            + "   </resourceRecord>"
            + "</getResourceRecordListRes>"));
    server
      .enqueue(new MockResponse()
        .setBody("<dnsaWSRes:dnsaWSRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:dnsaWSRes=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
            + "     <callSuccess>false</callSuccess>" 
            + "</dnsaWSRes:dnsaWSRes>"));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io");
    recordSetsInZoneApi.deleteByNameAndType("www.denominator.io.", "A");
  }
  
  @Test
  public void deleteTxtRecordWithEmbeddedQuotesWhenPresent() throws Exception {
    server
      .enqueue(new MockResponse()
        .setBody("<getResourceRecordListRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
            + "   <callSuccess>true</callSuccess>"
            + "   <totalCount>1</totalCount>"
            + "   <resourceRecord>"
            + "       <resourceRecordId>3194811</resourceRecordId>"
            + "       <owner>www.denominator.io.</owner>"
            + "       <type>TXT</type>"
            + "       <ttl>86400</ttl>"
            + "       <rData>test string 1\" \"test string 2</rData>"
            + "   </resourceRecord>"
            + "</getResourceRecordListRes>"));
    server
      .enqueue(new MockResponse()
        .setBody("<dnsaWSRes:dnsaWSRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:dnsaWSRes=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
            + "     <callSuccess>false</callSuccess>" 
            + "</dnsaWSRes:dnsaWSRes>"));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io");
    recordSetsInZoneApi.deleteByNameAndType("www.denominator.io.", "A");
  }  
  
  @Test
  public void deleteTxtRecordWithQuotesWhenPresent() throws Exception {
    server
      .enqueue(new MockResponse()
        .setBody("<getResourceRecordListRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
            + "   <callSuccess>true</callSuccess>"
            + "   <totalCount>1</totalCount>"
            + "   <resourceRecord>"
            + "       <resourceRecordId>3194811</resourceRecordId>"
            + "       <owner>www.denominator.io.</owner>"
            + "       <type>TXT</type>"
            + "       <ttl>86400</ttl>"
            + "       <rData>\"test string 1\" \"test string 2\"</rData>"
            + "   </resourceRecord>"
            + "</getResourceRecordListRes>"));
    server
      .enqueue(new MockResponse()
        .setBody("<dnsaWSRes:dnsaWSRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:dnsaWSRes=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
            + "     <callSuccess>false</callSuccess>" 
            + "</dnsaWSRes:dnsaWSRes>"));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io");
    recordSetsInZoneApi.deleteByNameAndType("www.denominator.io.", "A");
  } 
  
  @Test
  public void deleteWhenAbsent() throws Exception {
    server
        .enqueue(new MockResponse()
            .setBody("<getResourceRecordListRes xmlns=\"urn:com:verisign:dnsa:api:schema:1\" xmlns:ns2=\"urn:com:verisign:dnsa:auth:schema:1\" xmlns:ns3=\"urn:com:verisign:dnsa:api:schema:2\" xmlns:ns4=\"urn:com:verisign:dnsa:messaging:schema:1\">"
                + "   <callSuccess>true</callSuccess>"
                + "   <totalCount>1</totalCount>"
                + "   <resourceRecord>"
                + "       <resourceRecordId>3194811</resourceRecordId>"
                + "       <owner>www.denominator.io.</owner>"
                + "       <type>A</type>"
                + "       <ttl>86400</ttl>"
                + "       <rData>127.0.0.1</rData>"
                + "   </resourceRecord>"
                + "</getResourceRecordListRes>"));
    
    server.enqueue(new MockResponse());

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io");
    recordSetsInZoneApi.deleteByNameAndType("www", "A");
  }
}
