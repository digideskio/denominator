package denominator.verisignbind;

import static denominator.common.Util.filter;
import static denominator.model.Zones.nameEqualTo;

import java.util.Iterator;

import javax.inject.Inject;

import denominator.model.Zone;
import feign.FeignException;

final class VerisignBindZoneApi implements denominator.ZoneApi {

  private final VerisignBind api;

  @Inject
  VerisignBindZoneApi(VerisignBind api) {
    this.api = api;
  }

  @Override
  public Iterator<Zone> iterator() {
    return api.getZones().iterator();
  }

  @Override
  public Iterator<Zone> iterateByName(String name) {
    return filter(iterator(), nameEqualTo(name));
  }

  @Override
  public String put(Zone zone) {
    try {
      zone = api.createZone(zone.name(), zone.ttl(), zone.email());
    } catch (FeignException e) {
      if (e.getMessage().indexOf(" 409 ") == -1) {
        throw e;
      }
    }

    api.updateZone(zone.id(), zone.name(), zone.ttl(), zone.email());
    return zone.name();
  }

  @Override
  public void delete(String name) {
    try {
      api.deleteZone(name);
    } catch (FeignException e) {
      if (e.getMessage().indexOf(" 404 ") == -1) {
        throw e;
      }
    }
  }
}
