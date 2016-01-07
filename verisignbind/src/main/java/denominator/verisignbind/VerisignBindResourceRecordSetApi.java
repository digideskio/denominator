package denominator.verisignbind;

import static denominator.common.Preconditions.checkArgument;
import static denominator.common.Preconditions.checkNotNull;
import static denominator.common.Util.join;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import denominator.ResourceRecordSetApi;
import denominator.common.Util;
import denominator.model.ResourceRecordSet;
import denominator.verisignbind.VerisignBindAdapters.ResourceRecord;

final class VerisignBindResourceRecordSetApi implements ResourceRecordSetApi {
  private static final ArrayList<ResourceRecord> EMPTY_RRLIST = new ArrayList<ResourceRecord>();
  private final VerisignBind api;
  private final String zoneName;

  VerisignBindResourceRecordSetApi(VerisignBind api, String zoneName) {
    this.api = api;
    this.zoneName = zoneName;
  }

  private static List<ResourceRecord> nonNull(List<ResourceRecord> records) {
    return records != null ? records : EMPTY_RRLIST;
  }

  @Override
  public Iterator<ResourceRecordSet<?>> iterator() {
    List<ResourceRecord> records = api.getResourceRecords(zoneName);

    return new RecordIterator(nonNull(records).iterator());
  }

  @Override
  public Iterator<ResourceRecordSet<?>> iterateByName(String name) {
    List<ResourceRecord> records = api.getResourceRecord(zoneName, name, null);

    return new RecordIterator(nonNull(records).iterator());
  }

  @Override
  public ResourceRecordSet<?> getByNameAndType(String name, String type) {
    List<ResourceRecord> records = api.getResourceRecord(zoneName, name, type);
    RecordIterator i = new RecordIterator(nonNull(records).iterator());

    return i.hasNext() ? i.next() : null;
  }

  @Override
  public void put(ResourceRecordSet<?> rrset) {
    checkNotNull(rrset, "rrset was null");
    checkArgument(!rrset.records().isEmpty(), "rrset was empty %s", rrset);

    List<Map<String, Object>> recordsToCreate = new ArrayList<Map<String, Object>>(rrset.records());

    ResourceRecordSet<?> oldRRSet = getByNameAndType(rrset.name(), rrset.type());
    if (oldRRSet != null) {
      if (oldRRSet.equals(rrset)) {
        return;
      }
      if (!rrset.type().equalsIgnoreCase("NS")) {
        api.deleteResourceRecord(zoneName, rrset.name(), rrset.type());
      }
    }

    ResourceRecord record = new ResourceRecord();
    record.setName(rrset.name());
    record.setType(rrset.type());
    record.setTtl(rrset.ttl() != null ? rrset.ttl() : 86400);

    for (Map<String, Object> rdata : recordsToCreate) {
      LinkedHashMap<String, Object> mutable = new LinkedHashMap<String, Object>(rdata);
      record.setRdata(join(' ', mutable.values().toArray()));
      api.createResourceRecord(zoneName, record.getName(), record.getType(), record.getTtl(),
          record.getRdata());
    }
  }

  @Override
  public void deleteByNameAndType(String name, String type) {
    checkNotNull(name, "name");
    checkNotNull(type, "type");

    api.deleteResourceRecord(zoneName, name, type);
  }

  static final class Factory implements denominator.ResourceRecordSetApi.Factory {
    private final VerisignBind api;

    @Inject
    Factory(VerisignBind api) {
      this.api = checkNotNull(api, "api");
    }

    @Override
    public ResourceRecordSetApi create(String name) {
      return new VerisignBindResourceRecordSetApi(api, name);
    }
  }

  interface ByTypeMapper {
    Map<String, Object> map(String type, String rdata);
  }

  private static HashMap<String, ByTypeMapper> mappers = new HashMap<String, ByTypeMapper>();

  static {
    mappers.put("tlsa", new ByTypeMapper() {
      // Util.toMap() does not know TLSA records
      public Map<String, Object> map(String type, String rdata) {
        Map<String, Object> tlsaData = new LinkedHashMap<String, Object>();
        String[] parts = rdata.split(" ");

        tlsaData.put("certUsage", parts[0]);
        tlsaData.put("selector", parts[1]);
        tlsaData.put("matchingType", parts[2]);
        tlsaData.put("certificateAssociationData", parts[3]);

        return tlsaData;
      }
    });
    mappers.put("smimea", new ByTypeMapper() {
      // Util.toMap() does not know SMIMEA records
      public Map<String, Object> map(String type, String rdata) {
        Map<String, Object> smimeaData = new LinkedHashMap<String, Object>();
        String[] parts = rdata.split(" ");

        smimeaData.put("certUsage", parts[0]);
        smimeaData.put("selector", parts[1]);
        smimeaData.put("matchingType", parts[2]);
        smimeaData.put("certificateAssociationData", parts[3]);

        return smimeaData;
      }
    });
    mappers.put("txt", new ByTypeMapper() {
      public Map<String, Object> map(String type, String rdata) {
        return Util.toMap(type, rdata.replace("\"", ""));
      }
    });
    mappers.put("naptr", new ByTypeMapper() {
      public Map<String, Object> map(String type, String rdata) {
        return Util.toMap(type, rdata.replace("\"", ""));
      }
    });
    mappers.put("aaaa", new ByTypeMapper() {
      public Map<String, Object> map(String type, String rdata) {
        return Util.toMap(type, rdata.toUpperCase());
      }
    });
  }

  public static Map<String, Object> getRRTypeAndRdata(String type, String rdata) {
    ByTypeMapper mapper = mappers.get(type.toLowerCase());

    return mapper != null ? mapper.map(type, rdata) : Util.toMap(type, rdata);
  }
}
