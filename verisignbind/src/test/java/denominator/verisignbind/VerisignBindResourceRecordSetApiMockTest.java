package denominator.verisignbind;

import static denominator.assertj.ModelAssertions.assertThat;
import static denominator.model.ResourceRecordSets.a;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.squareup.okhttp.mockwebserver.MockResponse;

import denominator.ResourceRecordSetApi;
import denominator.common.Util;
import denominator.model.ResourceRecordSet;
import denominator.model.rdata.AAAAData;
import denominator.model.rdata.AData;

public class VerisignBindResourceRecordSetApiMockTest {

  @Rule
  public final MockVerisignBindServer server = new MockVerisignBindServer();

  @Test
  public void iteratorWhenPresent() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse));

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone(zoneName);

    assertThat(api.iterator()).containsExactly(
        ResourceRecordSet.builder().name("www.denominator.io.").type("A").ttl(86400)
            .add(Util.toMap("A", "127.0.0.1")).build());

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @Test
  public void iteratorWhenAbsent() throws Exception {
    server.enqueue(new MockResponse().setBody("[]"));

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone("denominator.io.");
    assertThat(api.iterator()).isEmpty();

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void iterateByNameWhenPresent() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse));
    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone(zoneName);

    assertThat(api.iterateByName("www.denominator.io.").next()).hasName("www.denominator.io.")
        .hasType("A").hasTtl(86400).containsExactlyRecords(AData.create("127.0.0.1"));

    server.assertRequest().hasMethod("GET")
        .hasPath(format("/zones/%s/records/%s", zoneName, "www.denominator.io."));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void iterateByNameWhenPresentMultiple() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponseMixed));
    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone(zoneName);

    Iterator<ResourceRecordSet<?>> rrsets = api.iterateByName("www.denominator.io.");
    assertThat(rrsets.next()).hasName("www.denominator.io.").hasType("A").hasTtl(86400)
        .containsExactlyRecords(AData.create("127.0.0.1"));

    assertThat(rrsets.next()).hasName("www.denominator.io.").hasType("A").hasTtl(86400)
        .containsExactlyRecords(AData.create("127.0.0.10"));

    assertThat(rrsets.next()).hasName("www.denominator.io.").hasType("AAAA").hasTtl(86400)
        .containsExactlyRecords(AAAAData.create("2001:db8::3"));

    server.assertRequest().hasMethod("GET")
        .hasPath(format("/zones/%s/records/%s", zoneName, "www.denominator.io."));
  }

  @Test
  public void iterateByNameWhenAbsent() throws Exception {
    server.enqueue(new MockResponse().setBody("[]"));

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone("denominator.io.");
    assertThat(api.iterateByName("www.denominator.io.")).isEmpty();

    server.assertRequest().hasMethod("GET")
        .hasPath(format("/zones/%s/records/%s", zoneName, "www.denominator.io."));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getByNameAndTypeWhenPresent() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse));

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone("denominator.io.");

    assertThat(api.getByNameAndType("www.denominator.io.", "A")).hasName("www.denominator.io.")
        .hasType("A").hasTtl(86400).containsExactlyRecords(AData.create("127.0.0.1"));

    server.assertRequest().hasMethod("GET")
        .hasPath(format("/zones/%s/records/%s?type=A", zoneName, "www.denominator.io."));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getByNameAndTypeWhenPresentMultiple() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponseMultiple));

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone("denominator.io.");
    assertThat(api.getByNameAndType("www.denominator.io.", "A")).hasName("www.denominator.io.")
        .hasType("A").hasTtl(86400)
        .containsExactlyRecords(AData.create("127.0.0.1"), AData.create("127.0.0.10"));

    server.assertRequest().hasMethod("GET")
        .hasPath(format("/zones/%s/records/%s?type=A", zoneName, "www.denominator.io."));
  }


  @Test
  public void getByNameAndTypeWhenAbsent() throws Exception {
    server.enqueue(new MockResponse());
    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone("denominator.io.");

    assertThat(api.getByNameAndType("www.denominator.io.", "A")).isNull();

    server.assertRequest().hasMethod("GET")
        .hasPath(format("/zones/%s/records/%s?type=A", zoneName, "www.denominator.io."));
  }

  @Ignore
  public void putFirstRecord() throws Exception {
    server.enqueue(new MockResponse().setBody("[{}]"));
    server.enqueue(new MockResponse().setBody(recordResponse));
    server.enqueue(new MockResponse().setBody(recordResponse));

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone(zoneName);
    api.put(a("www.denominator.io.", 86400, "127.0.0.1"));

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
    server.assertRequest().hasMethod("POST").hasPath(format("/zones/%s/records", zoneName));
  }

  @Ignore
  public void putFirstMultiRecord() throws Exception {
    server.enqueue(new MockResponse().setBody("[{}]"));
    server.enqueue(new MockResponse().setBody(recordResponse));
    server.enqueue(new MockResponse().setBody(recordResponse));

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone(zoneName);
    api.put(a("www.denominator.io.", 86400, Arrays.asList("127.0.0.1", "127.0.0.2")));

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
    server.assertRequest().hasMethod("POST").hasPath(format("/zones/%s/records", zoneName));
    server.assertRequest().hasMethod("POST").hasPath(format("/zones/%s/records", zoneName));
  }

  @Ignore
  public void putSameRecord() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse));
    server.enqueue(new MockResponse().setBody(recordsResponse));

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone("denominator.io.");
    api.put(a("www.denominator.io.", 86400, "127.0.0.1"));

    assertThat(api.iterator()).hasSize(1);
    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @Ignore
  public void putSameRecordWithDifferentRdata() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse));
    server.enqueue(new MockResponse().setBody("[{}]"));
    server.enqueue(new MockResponse().setBody(recordsResponse));
    server.enqueue(new MockResponse().setBody(recordsResponse));

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone("denominator.io.");
    api.put(a("www.denominator.io.", 86400, "127.0.0.2"));

    assertThat(api.iterator()).hasSize(1);
    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
    server.assertRequest().hasMethod("DELETE")
        .hasPath(format("/zones/%s/records/%s?type=%s", zoneName, "www.denominator.io.", "A"));
    server.assertRequest().hasMethod("POST").hasPath(format("/zones/%s/records", zoneName));
    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @Ignore
  public void putSameRecordWithDifferentTtl() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse));
    server.enqueue(new MockResponse().setBody("[{}]"));
    server.enqueue(new MockResponse().setBody(recordsResponse));

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone("denominator.io.");
    api.put(a("www.denominator.io.", 1000, "127.0.0.1"));

    assertThat(api.iterator()).hasSize(1);

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
    server.assertRequest().hasMethod("PUT")
        .hasPath(format("/zones/%s/records/%s", zoneName, "www.denominator.io."));
    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }


  @Ignore
  public void putOneRecordReplacesRRSet() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse2));
    server.enqueue(new MockResponse().setBody(recordsResponse2));
    server.enqueue(new MockResponse().setBody("[{}]"));
    server.enqueue(new MockResponse().setBody(recordsResponse));

    System.out.println(recordsResponse2);

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone(zoneName);
    assertThat(api.iterator()).hasSize(2);

    api.put(a("www.denominator.io.", 1000, "127.0.0.10"));

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
    server.assertRequest().hasMethod("DELETE")
        .hasPath(format("/zones/%s/records/%s?type=%s", zoneName, "www.denominator.io.", "A"));

  }

  @Test
  public void deleteByNameAndTypeWhenPresent() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse));
    server.enqueue(new MockResponse());

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone("denominator.io.");
    api.deleteByNameAndType("www.denominator.io.", "A");

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @Test
  public void deleteByNameAndTypeWhenAbsent() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse));
    server.enqueue(new MockResponse());

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone("denominator.io.");
    api.deleteByNameAndType("www.denominator.io.", "A");

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  /* @formatter:off */
  
  static String zoneName = "denominator.io.";

  static String recordResponse = "{\n" 
      + "   \"name\": \"www.denominator.io.\",\n"
      + "   \"type\": \"A\",\n" 
      + "   \"ttl\": 86400,\n" 
      + "   \"rdata\": \"127.0.0.1\"\n" + "}";

  static String recordResponse2 = "{\n" 
      + "   \"name\": \"www2.denominator.io.\",\n"
      + "   \"type\": \"A\",\n" 
      + "   \"ttl\": 86400,\n" 
      + "   \"rdata\": \"127.0.0.2\"\n" 
      + "}";

  static String recordsResponse = "[\n" 
      + recordResponse 
      + "  ]\n";

  static String recordsResponse2 = "[\n" 
      + recordResponse 
      + ",\n" 
      + recordResponse2 
      + " ]\n";
  
  static String recordsResponseMultiple = "[\n" 
      + "{\n" 
      + "   \"name\": \"www.denominator.io.\",\n"
      + "   \"type\": \"A\",\n" 
      + "   \"ttl\": 86400,\n" 
      + "   \"rdata\": \"127.0.0.1\"\n" + "}" 
      + ",\n" 
      + "{\n" 
      + "   \"name\": \"www.denominator.io.\",\n"
      + "   \"type\": \"A\",\n" 
      + "   \"ttl\": 86400,\n" 
      + "   \"rdata\": \"127.0.0.10\"\n" + "}" 
      + " ]\n";  
  
  static String recordsResponseMixed = "[\n" 
      + "{\n" 
      + "   \"name\": \"www.denominator.io.\",\n"
      + "   \"type\": \"A\",\n" 
      + "   \"ttl\": 86400,\n" 
      + "   \"rdata\": \"127.0.0.1\"\n" + "}" 
      + ",\n" 
      + "{\n" 
      + "   \"name\": \"www.denominator.io.\",\n"
      + "   \"type\": \"A\",\n" 
      + "   \"ttl\": 86400,\n" 
      + "   \"rdata\": \"127.0.0.10\"\n" + "}" 
      + ",\n"      
      + "{\n" 
      + "   \"name\": \"www.denominator.io.\",\n"
      + "   \"type\": \"AAAA\",\n" 
      + "   \"ttl\": 86400,\n" 
      + "   \"rdata\": \"2001:db8::3\"\n" + "}" 
      + " ]\n"; 
  
  /* @formatter:on */

}
