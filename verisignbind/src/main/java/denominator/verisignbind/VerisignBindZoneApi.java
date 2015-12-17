package denominator.verisignbind;

import static denominator.common.Util.singletonIterator;

import java.util.Iterator;

import javax.inject.Inject;

import denominator.model.Zone;
import feign.FeignException;

final class VerisignBindZoneApi implements denominator.ZoneApi {

  static final String ZONE_NOT_FOUND = "404";
  static final String ZONE_ALREADY_EXISTS = "409";

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
    try {
     Zone zone =  api.getZone(name);
     String zoneName = strip(zone.name(), ".");
     String email = strip(zone.email(), ".");
     
     Zone updatedZone = Zone.create(zone.id(), zoneName, zone.ttl(), email);     
      return singletonIterator(updatedZone);
    } catch (FeignException e) {
      if (e.getMessage().indexOf(ZONE_NOT_FOUND) == -1) {
        throw e;
      }
    }
    
    return singletonIterator(null);
  }

  @Override
  public String put(Zone zone) {

      try {
        zone = api.createZone(zone.name(), zone.ttl(), zone.email());
      } catch (FeignException e) {
        if (e.getMessage().indexOf(ZONE_ALREADY_EXISTS) == -1) {
          throw e;
        }
      }
    
    api.updateZone(zone.name(), zone.name(), zone.ttl(), zone.email());
    return zone.name();
  }

  @Override
  public void delete(String name) {
    try {
      api.deleteZone(name);
    } catch (FeignException e) {
      if (e.getMessage().indexOf(ZONE_NOT_FOUND) == -1) {
        throw e;
      }
    }
  }
  
  private String strip(String data, String replace) {
    if(data != null) {
      if(data.endsWith(replace)) {
        data = data.substring(0, data.length()-1);
      }
    }
    return data;
  }
  
}
