package denominator.verisignbind;

import static denominator.assertj.ModelAssertions.assertThat;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.squareup.okhttp.mockwebserver.MockResponse;

import denominator.ResourceRecordSetApi;
import denominator.common.Util;
import denominator.model.ResourceRecordSet;
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
    server.enqueue(new MockResponse().setBody("{ \"records\": [] }"));

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

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @Test
  public void iterateByNameWhenAbsent() throws Exception {
    server.enqueue(new MockResponse().setBody("{ \"records\": [] }"));

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone("denominator.io.");
    assertThat(api.iterateByName("www.denominator.io.")).isEmpty();

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getByNameAndTypeWhenPresent() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse));

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone("denominator.io.");

    assertThat(api.getByNameAndType("www.denominator.io.", "A")).hasName("www.denominator.io.")
        .hasType("A").hasTtl(86400).containsExactlyRecords(AData.create("127.0.0.1"));

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @Test
  public void getByNameAndTypeWhenAbsent() throws Exception {
    server.enqueue(new MockResponse().setBody("{ \"records\": [] }"));

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone("denominator.io.");

    assertThat(api.getByNameAndType("www.denominator.io.", "A")).isNull();

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @Test
  public void putCreatesRecord() throws Exception {
    server.enqueue(new MockResponse().setBody("{ \"records\": [] }"));
    server.enqueue(new MockResponse().setBody(recordResponse));

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone(zoneName);

    api.put(ResourceRecordSet.builder().name("www.denominator.io.").type("A").ttl(86400)
        .add(Util.toMap("A", "127.0.0.1")).build());

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
    server.assertRequest().hasMethod("POST").hasPath(format("/zones/%s/records", zoneName));
  }

  @Test
  public void putSameRecordNoOp() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse));
    server.enqueue(new MockResponse().setBody(recordsResponse));

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone("denominator.io.");

    api.put(ResourceRecordSet.builder().name("www.denominator.io.").type("A").ttl(86400)
        .add(Util.toMap("A", "127.0.0.1")).build());

    assertThat(api.iterator()).hasSize(1);

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
  }

  @Test
  public void putOneRecordReplacesRRSet() throws Exception {
    server.enqueue(new MockResponse().setBody(recordsResponse2));
    server.enqueue(new MockResponse().setBody(recordsResponse));

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone(zoneName);
    assertThat(api.iterator()).hasSize(2);

    api.put(ResourceRecordSet.builder().name("www.denominator.io.").type("A").ttl(86400)
        .add(Util.toMap("A", "127.0.0.1")).build());

    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
    server.assertRequest().hasMethod("GET").hasPath(format("/zones/%s/records", zoneName));
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
    server.enqueue(new MockResponse().setBody(recordResponse));
    server.enqueue(new MockResponse());

    ResourceRecordSetApi api = server.connect().api().basicRecordSetsInZone("denominator.io.");
    api.deleteByNameAndType("www", "A");

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
      + recordResponse 
      + "  ]\n" 
      + "}";

  static String recordsResponse2 = "{\n" 
      + "  \"records\": [\n" 
      + recordResponse 
      + ",\n"
      + recordResponse2 + " ]\n" 
      + "}";
}
