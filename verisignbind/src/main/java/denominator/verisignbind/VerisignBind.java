package denominator.verisignbind;

import java.util.List;

import denominator.model.Zone;
import denominator.verisignbind.VerisignBindAdapters.ResourceRecord;
import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Headers({"Content-Type: application/json"})
interface VerisignBind {

  @RequestLine("POST /zones")
  @Body("%7B\"name\":\"{zone_name}\",\"ttl\":{ttl},\"email\":\"{email}\"%7D")
  void createZone(@Param("zone_name") String name, @Param("ttl") int ttl,
      @Param("email") String email);

  @RequestLine("PUT /zones")
  @Body("%7B\"name\":\"{zone_name}\",\"ttl\":{ttl},\"email\":\"{email}\"%7D")
  void updateZone(@Param("zone_name") String name, @Param("ttl") int ttl,
      @Param("email") String email);

  @RequestLine("GET /zones")
  List<Zone> getZones();

  @RequestLine("GET /zones/{zone_name}")
  Zone getZone(@Param("zone_name") String name);

  @RequestLine("DELETE /zones/{zone_name}")
  void deleteZone(@Param("zone_name") String name);

  @RequestLine("POST /zones/{zone_name}/records")
  @Body("%7B\"records\":[%7B\"name\":\"{name}\",\"type\":\"{type}\",\"ttl\":\"{ttl}\",\"rdata\":\"{rdata}\"%7D]%7D")
  void createResourceRecord(@Param("zone_name") String zoneName, @Param("name") String name,
      @Param("type") String type, @Param("ttl") int ttl, @Param("rdata") String rdata);

  @RequestLine("PUT /zones/{zone_name}/records/{name}")
  @Body("%7B\"ttl\":\"{ttl}\"%7D")
  void updateResourceRecord(@Param("zone_name") String zoneName, @Param("name") String name,
      @Param("ttl") int ttl);

  @RequestLine("GET /zones/{zone_name}/records")
  List<ResourceRecord> getResourceRecords(@Param("zone_name") String zoneName);

  @RequestLine("GET /zones/{zone_name}/records/?name={name}&type={type}")
  ResourceRecord getResourceRecord(@Param("zone_name") String zoneName, @Param("name") String name,
      @Param("type") String type);

  @RequestLine("DELETE /zones/{zone_name}/records/{name}")
  void deleteResourceRecord(@Param("zone_name") String zoneName, @Param("name") String name);
}
