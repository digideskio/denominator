package denominator.verisignbind;

import static denominator.common.Util.peekingIterator;
import static denominator.verisignbind.VerisignBindResourceRecordSetApi.getRRTypeAndRdata;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import denominator.common.PeekingIterator;
import denominator.model.ResourceRecordSet;
import denominator.model.ResourceRecordSet.Builder;
import denominator.verisignbind.VerisignBindAdapters.ResourceRecord;

class RecordIterator implements Iterator<ResourceRecordSet<?>> {

  private final PeekingIterator<ResourceRecord> peekingIterator;

  public RecordIterator(Iterator<ResourceRecord> iterator) {
    this.peekingIterator = peekingIterator(iterator);
  }

  @Override
  public boolean hasNext() {
    return peekingIterator.hasNext();
  }

  @Override
  public ResourceRecordSet<?> next() {
    ResourceRecord record = peekingIterator.next();
    Builder<Map<String, Object>> builder =
        ResourceRecordSet.builder().name(record.getName()).type(record.getType())
            .ttl(record.getTtl());

    if (record.getType().equalsIgnoreCase("TLSA")) {
      String[] rdata = record.getRdata().split(" ");
      
      Map<String, Object> tlsaData = new LinkedHashMap<String, Object>();            
      tlsaData.put("certUsage", rdata[0]);
      tlsaData.put("selector", rdata[1]);
      tlsaData.put("matchingType", rdata[2]);
      tlsaData.put("certificateAssociationData", rdata[3]);
      builder.add(tlsaData);
    } else if (record.getType().equalsIgnoreCase("SMIMEA")) {
      String[] rdata = record.getRdata().split(" ");
      
      Map<String, Object> smimeaData = new LinkedHashMap<String, Object>();
      smimeaData.put("certUsage", rdata[0]);
      smimeaData.put("selector", rdata[1]);
      smimeaData.put("matchingType", rdata[2]);
      smimeaData.put("certificateAssociationData",rdata[3]);
      builder.add(smimeaData);
    } else {
      builder.add(getRRTypeAndRdata(record.getType(), record.getRdata()));
    }
        
    while (hasNext()) {
      ResourceRecord next = peekingIterator.peek();
      if (next == null) {
        break;
      }

      if (nameAndTypeEquals(next, record)) {
        next = peekingIterator.next();
        if (record.getType().equalsIgnoreCase("TLSA")) {
          String[] rdata = next.getRdata().split(" ");
          
          Map<String, Object> tlsaData = new LinkedHashMap<String, Object>();            
          tlsaData.put("certUsage", rdata[0]);
          tlsaData.put("selector", rdata[1]);
          tlsaData.put("matchingType", rdata[2]);
          tlsaData.put("certificateAssociationData", rdata[3]);
          builder.add(tlsaData);
        } else if (record.getType().equalsIgnoreCase("SMIMEA")) {
          String[] rdata = next.getRdata().split(" ");
          
          Map<String, Object> smimeaData = new LinkedHashMap<String, Object>();
          smimeaData.put("certUsage", rdata[0]);
          smimeaData.put("selector", rdata[1]);
          smimeaData.put("matchingType", rdata[2]);
          smimeaData.put("certificateAssociationData",rdata[3]);
          builder.add(smimeaData);
        } else {
          builder.add(getRRTypeAndRdata(record.getType(), next.getRdata()));
        }                
      } else {
        break;
      }
    }

    return builder.build();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  private static boolean nameAndTypeEquals(ResourceRecord actual, ResourceRecord expected) {
    return actual.getName().equals(expected.getName())
        && actual.getType().equals(expected.getType());
  }
}
