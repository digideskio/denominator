package denominator.verisignbind;

import static denominator.common.Preconditions.checkArgument;
import static denominator.common.Preconditions.checkNotNull;
import static denominator.common.Util.join;
import static denominator.common.Util.equal;
import static denominator.verisignbind.VerisignBindResourceRecordSetApi.getRRTypeAndRdata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import denominator.ResourceRecordSetApi;
import denominator.common.Util;
import denominator.model.ResourceRecordSet;
import denominator.model.ResourceRecordSet.Builder;
import denominator.verisignbind.VerisignBindAdapters.ResourceRecord;

final class VerisignBindResourceRecordSetApi implements ResourceRecordSetApi {

  private final VerisignBind api;
  private final String zoneName;

  VerisignBindResourceRecordSetApi(VerisignBind api, String zoneName) {
    this.api = api;
    this.zoneName = zoneName;
  }

  @Override
  public Iterator<ResourceRecordSet<?>> iterator() {
    return new RecordIterator(api.getResourceRecords(zoneName).iterator());
  }

  @Override
  public Iterator<ResourceRecordSet<?>> iterateByName(String name) {
    List<ResourceRecord> records = api.getResourceRecord(zoneName, name, null);
    if (records == null) {
      records = new ArrayList<VerisignBindAdapters.ResourceRecord>();
    }

    return new RecordIterator(records.iterator());
  }

  @Override
  public ResourceRecordSet<?> getByNameAndType(String name, String type) {
    List<ResourceRecord> records = api.getResourceRecord(zoneName, name, type);
    if (records != null && !records.isEmpty()) {
      ResourceRecord record = records.get(0);
      Builder<Map<String, Object>> builder =
          ResourceRecordSet.builder().name(name).type(record.getType()).ttl(record.getTtl());

      for (ResourceRecord resourceRecord : records) {
        if (record.getType().equalsIgnoreCase("TLSA")) {
          String[] rdata = resourceRecord.getRdata().split(" ");
          
          Map<String, Object> tlsaData = new LinkedHashMap<String, Object>();            
          tlsaData.put("certUsage", rdata[0]);
          tlsaData.put("selector", rdata[1]);
          tlsaData.put("matchingType", rdata[2]);
          tlsaData.put("certificateAssociationData", rdata[3]);
          builder.add(tlsaData);
        } else if (record.getType().equalsIgnoreCase("SMIMEA")) {
          String[] rdata = resourceRecord.getRdata().split(" ");
          
          Map<String, Object> smimeaData = new LinkedHashMap<String, Object>();
          smimeaData.put("certUsage", rdata[0]);
          smimeaData.put("selector", rdata[1]);
          smimeaData.put("matchingType", rdata[2]);
          smimeaData.put("certificateAssociationData",rdata[3]);
          builder.add(smimeaData);
        } else {
          builder.add(getRRTypeAndRdata(record.getType(), resourceRecord.getRdata()));
        }        
      }
      return builder.build();
    }

    return null;
  }

  @Override
  public void put(ResourceRecordSet<?> rrset) {
    checkNotNull(rrset, "rrset was null");
    checkArgument(!rrset.records().isEmpty(), "rrset was empty %s", rrset);
    
    List<Map<String, Object>> recordsToCreate = new ArrayList<Map<String, Object>>(rrset.records());
    
    ResourceRecordSet<?> oldRRSet = getByNameAndType(rrset.name(), rrset.type());
    if(oldRRSet != null) {
      api.deleteResourceRecord(zoneName, rrset.name(), rrset.type());
    }

    ResourceRecord record = new ResourceRecord();
    record.setName(rrset.name());
    record.setType(rrset.type());

    if (rrset.ttl() != null) {
      record.setTtl(rrset.ttl());
    } else {
      record.setTtl(86400);
    }

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

  public static Map<String, Object> getRRTypeAndRdata(String type, String rdata) {
    rdata = rdata.replace("\"", "");
    if ("AAAA".equals(type)) {
      rdata = rdata.toUpperCase();
    }
    return Util.toMap(type, rdata);
  }
}
