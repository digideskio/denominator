package denominator.verisignbind;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.squareup.okhttp.mockwebserver.MockResponse;

import denominator.ZoneApi;
import denominator.model.Zone;

public class VerisignBindZoneApiMockTest {

  @Rule
  public final MockVerisignBindServer server = new MockVerisignBindServer();

  @Test
  public void iteratorWhenPresent() throws Exception {
    server.enqueue(new MockResponse().setBody(zonesResponse));
    ZoneApi api = server.connect().api().zones();
    assertThat(api.iterator()).containsExactly(
        Zone.create("denominator.io", "denominator.io", 86400, "nil@denominator.io"));

    server.assertRequest().hasMethod("GET").hasPath("/zones");
  }

  @Test
  public void iteratorWhenAbsent() throws Exception {
    server.enqueue(new MockResponse().setBody("[]"));
    ZoneApi api = server.connect().api().zones();
    assertThat(api.iterator()).isEmpty();

    server.assertRequest().hasMethod("GET").hasPath("/zones");
  }

  @Test
  public void iterateByNameWhenPresent() throws Exception {
    server.enqueue(new MockResponse().setBody(zoneResponse));
    ZoneApi api = server.connect().api().zones();
    assertThat(api.iterateByName("denominator.io")).containsExactly(
        Zone.create("denominator.io", "denominator.io", 86400, "nil@denominator.io"));

    server.assertRequest().hasMethod("GET").hasPath("/zones/denominator.io");
  }

  @Test
  public void iterateByNameWhenAbsent() throws Exception {
    server.enqueue(new MockResponse());
    ZoneApi api = server.connect().api().zones();
    assertThat(api.iterateByName("denominator.io")).isEmpty();

    server.assertRequest().hasMethod("GET").hasPath("/zones/denominator.io");
  }

  @Test
  public void putWhenPresent() throws Exception {
    server.enqueue(new MockResponse().setBody(zoneResponse));
    server.enqueue(new MockResponse().setBody(zoneResponse));

    ZoneApi api = server.connect().api().zones();

    Zone zone = Zone.create("denominator.io", "denominator.io", 86400, "nil@denominator.io");
    assertThat(api.put(zone)).isEqualTo(zone.name());

    server.assertRequest().hasMethod("POST").hasPath("/zones")
        .hasBody("{\"name\":\"denominator.io\",\"ttl\":86400,\"email\":\"nil@denominator.io\"}");

    server.assertRequest().hasMethod("PUT").hasPath("/zones/denominator.io").hasBody(
        "{\"id\":\"denominator.io\",\"name\":\"denominator.io\",\"ttl\":86400,\"email\":\"nil@denominator.io\"}");
  }

  @Test
  public void putWhenAbsent() throws Exception {
    server.enqueue(new MockResponse().setBody(zoneResponse));
    server.enqueue(new MockResponse().setBody(zoneResponse));
    ZoneApi api = server.connect().api().zones();

    Zone zone = Zone.create(null, "denominator.io", 86400, "nil@denominator.io");
    assertThat(api.put(zone)).isEqualTo(zone.name());

    server.assertRequest().hasMethod("POST").hasPath("/zones")
        .hasBody("{\"name\":\"denominator.io\",\"ttl\":86400,\"email\":\"nil@denominator.io\"}");

    server.assertRequest().hasMethod("PUT").hasPath("/zones/denominator.io").hasBody(
        "{\"id\":\"denominator.io\",\"name\":\"denominator.io\",\"ttl\":86400,\"email\":\"nil@denominator.io\"}");

  }

  @Test
  public void deleteWhenPresent() throws Exception {
    server.enqueue(new MockResponse());

    ZoneApi api = server.connect().api().zones();
    api.delete(zoneName);

    server.assertRequest().hasMethod("DELETE").hasPath("/zones/" + zoneName);
  }

  @Test
  public void deleteWhenAbsent() throws Exception {
    server.enqueue(new MockResponse().setResponseCode(404)
        .setBody("{\"code\": 404, \"reason\": \"zone not found\"}"));

    ZoneApi api = server.connect().api().zones();
    api.delete(zoneName);

    server.assertRequest().hasMethod("DELETE").hasPath("/zones/" + zoneName);
  }

  /* @formatter:off */
  
  static String zoneName = "denominator.io.";

  static String zoneResponse = "{\n" 
      + "  \"id\": \"denominator.io\",\n"
      + "  \"name\": \"denominator.io\",\n" 
      + "  \"ttl\": 86400,\n"
      + "  \"email\": \"nil@denominator.io\"\n" 
      + "}\n";

  static String zonesResponse = "[\n" + zoneResponse + "]\n";
  
  /* @formatter:on */

}
