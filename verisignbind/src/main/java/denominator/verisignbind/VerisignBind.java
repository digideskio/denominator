package denominator.verisignbind;

import java.util.List;

import denominator.model.Zone;
import denominator.verisignbind.VerisignBindAdapters.ResourceRecord;
import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Headers({ "Content-Type: application/json" })
interface VerisignBind {

  @RequestLine("POST /zones")
  @Body("%7B\"name\":\"{name}\",\"ttl\":{ttl},\"email\":\"{email}\"%7D")
  Zone createZone(@Param("name") String name, @Param("ttl") int ttl, @Param("email") String email);

  @RequestLine("PUT /zones/{id}")
  @Body("%7B\"id\":\"{id}\",\"name\":\"{name}\",\"ttl\":{ttl},\"email\":\"{email}\"%7D")
  Zone updateZone(@Param("id") String id, @Param("name") String name, @Param("ttl") int ttl,
      @Param("email") String email);

  @RequestLine("GET /zones")
  List<Zone> getZones();

  @RequestLine("GET /zones/{id}")
  Zone getZone(@Param("id") String id);

  @RequestLine("DELETE /zones/{id}")
  void deleteZone(@Param("id") String id);

  @RequestLine("POST /zones/{zone_id}/records")
  @Body("%7B\"name\":\"{name}\",\"type\":\"{type}\",\"ttl\":\"{ttl}\",\"rdata\":\"{rdata}\"%7D")
  void createResourceRecord(@Param("zone_id") String zoneId, @Param("name") String name,
      @Param("type") String type, @Param("ttl") int ttl, @Param("rdata") String rdata);

  @RequestLine("GET /zones/{zone_id}/records")
  List<ResourceRecord> getResourceRecords(@Param("zone_id") String zoneId);

  @RequestLine("GET /zones/{zone_id}/records/{name}?type={type}")
  List<ResourceRecord> getResourceRecord(@Param("zone_id") String zoneId,
      @Param("name") String name, @Param("type") String type);

  @RequestLine("DELETE /zones/{zone_id}/records/{name}?type={type}")
  void deleteResourceRecord(@Param("zone_id") String zoneId, @Param("name") String name,
      @Param("type") String type);
}
