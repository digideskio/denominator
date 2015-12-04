package denominator.verisignbind;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.squareup.okhttp.mockwebserver.MockResponse;

import denominator.AllProfileResourceRecordSetApi;
import denominator.common.Util;
import denominator.model.ResourceRecordSet;

public class VerisignBindResourceRecordSetApiMockTest {

  @Rule
  public final MockVerisignBindServer server = new MockVerisignBindServer();

  @Test
  public void iteratorWhenPresent() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone(zoneName);

    assertThat(recordSetsInZoneApi.iterator()).containsExactly(
        ResourceRecordSet.builder().name("www.denominator.io.").type("A").ttl(86400)
            .add(Util.toMap("A", "127.0.0.1")).build());

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));

  }

  @Test
  public void iteratorWhenAbsent() throws Exception {
    server.enqueue(new MockResponse().setBody("{ \"records\": [] }"));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io.");
    assertThat(recordSetsInZoneApi.iterator()).isEmpty();

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @Test
  public void iterateByNameWhenPresent() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone(zoneName);

    assertThat(recordSetsInZoneApi.iterateByName("www")).containsExactly(
        ResourceRecordSet.builder().name("www.denominator.io.").type("A").ttl(86400)
            .add(Util.toMap("A", "127.0.0.1")).build());

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @Test
  public void iterateByNameWhenAbsent() throws Exception {
    server.enqueue(new MockResponse().setBody("{ \"records\": [] }"));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io.");
    assertThat(recordSetsInZoneApi.iterateByName("www.denominator.io.")).isEmpty();

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @Test
  public void iterateByNameAndTypeWhenPresent() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io.");

    assertThat(recordSetsInZoneApi.iterateByNameAndType("www", "A")).containsExactly(
        ResourceRecordSet.builder().name("www.denominator.io.").type("A").ttl(86400)
            .add(Util.toMap("A", "127.0.0.1")).build());

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @Test
  public void iterateByNameAndTypeWhenAbsent() throws Exception {
    server.enqueue(new MockResponse().setBody("{ \"records\": [] }"));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io.");

    assertThat(recordSetsInZoneApi.iterateByNameAndType("www.denominator.io.", "A")).isEmpty();

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @Test
  public void putCreatesRecord() throws Exception {
    server.enqueue(new MockResponse().setBody("{ \"records\": [] }"));
    server.enqueue(new MockResponse().setBody(recordResponse));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone(zoneName);

    recordSetsInZoneApi.put(ResourceRecordSet.builder().name("www.denominator.io.").type("A")
        .ttl(86400).add(Util.toMap("A", "127.0.0.1")).build());

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
    server.assertRequest().hasMethod("PUT")
        .hasPath(format("/zones/%s/records/%s", zoneName, "www.denominator.io."));
  }

  @Test
  public void putSameRecordNoOp() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse));
    server.enqueue(new MockResponse().setBody(recordsResponse));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io.");

    recordSetsInZoneApi.put(ResourceRecordSet.builder().name("www").type("A").ttl(86400)
        .add(Util.toMap("A", "127.0.0.1")).build());

    assertThat(recordSetsInZoneApi.iterator()).hasSize(1);

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @Test
  public void putOneRecordReplacesRRSet() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse2));
    server.enqueue(new MockResponse().setBody(recordsResponse));

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone(zoneName);
    assertThat(recordSetsInZoneApi.iterator()).hasSize(2);

    recordSetsInZoneApi.put(ResourceRecordSet.builder().name("www.denominator.io.").type("A")
        .ttl(86400).add(Util.toMap("A", "127.0.0.1")).build());

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @Test
  public void deleteByNameAndTypeWhenPresent() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse));
    server.enqueue(new MockResponse());

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io.");
    recordSetsInZoneApi.deleteByNameAndType("www.denominator.io.", "A");

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @Test
  public void deleteByNameAndTypeWhenAbsent() throws Exception {
    server.enqueue(new MockResponse().setBody(recordResponse));
    server.enqueue(new MockResponse());

    AllProfileResourceRecordSetApi recordSetsInZoneApi =
        server.connect().api().recordSetsInZone("denominator.io.");
    recordSetsInZoneApi.deleteByNameAndType("www", "A");

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  static String zoneName = "denominator.io.";

  static String recordResponse = "{\n" 
      + "   \"name\": \"www.denominator.io.\",\n"
      + "   \"type\": \"A\",\n" 
      + "   \"ttl\": 86400,\n" 
      + "   \"rdata\": \"127.0.0.1\"\n" 
      + "}";

  static String recordResponse2 = "{\n" 
      + "   \"name\": \"www2.denominator.io.\",\n"
      + "   \"type\": \"A\",\n" 
      + "   \"ttl\": 86400,\n" 
      + "   \"rdata\": \"127.0.0.2\"\n" 
      + "}";

  static String recordsResponse = "{\n" 
      + "  \"records\": [\n" 
      + recordResponse + "  ]\n" 
      + "}";

  static String recordsResponse2 = "{\n" 
      + "  \"records\": [\n" 
      + recordResponse + ",\n"
      + recordResponse2 + " ]\n" + "}";
}
