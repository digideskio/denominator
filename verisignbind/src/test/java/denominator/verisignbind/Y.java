package denominator.verisignbind;

import static denominator.CredentialsConfiguration.credentials;
import static denominator.model.ResourceRecordSets.a;
import static denominator.model.ResourceRecordSets.aaaa;
import static denominator.model.ResourceRecordSets.txt;
import static denominator.model.ResourceRecordSets.mx;
import static denominator.model.ResourceRecordSets.srv;
import static denominator.model.ResourceRecordSets.spf;
import static denominator.model.ResourceRecordSets.ptr;
import static denominator.model.ResourceRecordSets.naptr;
import static denominator.model.ResourceRecordSets.ns;
import static denominator.model.ResourceRecordSets.cname;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import denominator.DNSApiManager;
import denominator.Denominator;
import denominator.ZoneApi;
import denominator.model.ResourceRecordSet;
import denominator.model.ResourceRecordSet.Builder;
import denominator.model.Zone;

public class Y {

  public static void mainx(String[] args) {
    DNSApiManager manager =
        Denominator.create(new VerisignBindProvider("http://10.239.30.207:8080/api/v1/"),
            credentials("denomuser", "letmein1!"));

    ZoneApi zoneApi = manager.api().zones();

    // Setup test data
    String zoneName = "verisign-test" + System.currentTimeMillis() + ".io.";
    int ttl = 86400;
    String email = "user@" + zoneName;

    // Create zone
    System.out.println("\nCreating zone...");
    String zoneId = zoneApi.put(Zone.create(null, zoneName, ttl, email));
    System.out.println("\t" + zoneId);

    // Query zones
    System.out.println("\nQuerying zones...");
    Iterator<Zone> zoneIterator = zoneApi.iterator();
    while (zoneIterator.hasNext()) {
      System.out.printf("\t%s", zoneIterator.next());
      System.out.println();
    }

    // Query zone by name
    System.out.println("\nQuerying zone by name...");
    zoneIterator = zoneApi.iterateByName(zoneName);
    while (zoneIterator.hasNext()) {
      System.out.printf("\t%s", zoneIterator.next());
      System.out.println();
    }

    VerisignBindResourceRecordSetApi recordSetsInZoneApi =
        (VerisignBindResourceRecordSetApi) manager.api().basicRecordSetsInZone(zoneName);

    // Add A resourceRecord
    System.out.println("\nAdding A resource record...");
    recordSetsInZoneApi.put(a("www." + zoneName, 86400, asList("127.0.0.1", "198.51.100.1")));
    // recordSetsInZoneApi.put(a("www." + zoneName, 86400, "127.0.0.1"));

    // Add A resourceRecord
    System.out.println("\nAdding AAAA resource record...");
    recordSetsInZoneApi.put(aaaa("www." + zoneName, 86400, "2001:db8::3"));

    // Query resourceRecords by name
    System.out.println("\nQuerying resourceRecord by name...");
    ResourceRecordSet<?> rr = recordSetsInZoneApi.getByNameAndType("www." + zoneName, "A");
    System.out.println(rr);

    System.out.println("\nUpdating A resource record...");
    recordSetsInZoneApi.put(a("www." + zoneName, 1234, asList("127.0.0.1", "198.51.100.1")));

    System.out.println("\nQuerying resourceRecord by name...");
    rr = recordSetsInZoneApi.getByNameAndType("www." + zoneName, "A");
    System.out.println(rr);

    // Add TXT resourceRecord
    System.out.println("\nAdding TXT resource record...");
    recordSetsInZoneApi.put(txt(zoneName, 86400, "Sample TXT record"));
    
    // Add SRV resourceRecord
    System.out.println("\nAdding SRV resource record...");
    recordSetsInZoneApi.put(srv(zoneName, 86400, "0 1 80 www.example.io."));
    
    // Add MX resourceRecord
    System.out.println("\nAdding MX resource record...");
    recordSetsInZoneApi.put(a("mx1." + zoneName, 86400, "127.0.0.20"));
    recordSetsInZoneApi.put(mx(zoneName, 86400, "10 mx1." + zoneName));
    
    // Add SPF resourceRecord
    System.out.println("\nAdding SPF resource record...");
    recordSetsInZoneApi.put(spf(zoneName, 86400, "v=spf1 a mx -all"));
    
    // Add PTR resourceRecord
    System.out.println("\nAdding PTR resource record...");
    recordSetsInZoneApi.put(ptr(zoneName, 86400, "ptr1." + zoneName));
    
    // Add NAPTR resourceRecord
    System.out.println("\nAdding NAPTR resource record...");
    recordSetsInZoneApi.put(naptr(zoneName, 86400, "1 1 U E2U+sip !^.*$!sip:service@example.io! ."));
    
    // Add CNAME resourceRecord
    System.out.println("\nAdding CNAME resource record...");
    recordSetsInZoneApi.put(cname(zoneName, 86400, "www." + zoneName));
    
    // Add NS resourceRecord
    System.out.println("\nAdding NS resource record...");
    recordSetsInZoneApi.put(a("ns1." + zoneName, 86400, "127.0.0.30"));
    recordSetsInZoneApi.put(ns(zoneName, 86400, "ns1." + zoneName));
    
    // Add TLSA record
    System.out.println("\nAdding TLSA resource record...");
    Map<String, Object> tlsaData = new LinkedHashMap<String, Object>();
    tlsaData.put("certUsage", "3");
    tlsaData.put("selector", "1");
    tlsaData.put("matchingType", "1");
    tlsaData.put("certificateAssociationData",
        "b760c12119c388736da724df1224d21dfd23bf03366c286de1a4125369ef7de0");

    recordSetsInZoneApi.put(ResourceRecordSet.builder().name("_443._tcp.www." + zoneName)
        .type("TLSA").add(tlsaData).build());

    // Add SMIMEA record
    System.out.println("\nAdding SMIMEA resource record...");
    Map<String, Object> smimeaData = new LinkedHashMap<String, Object>();
    smimeaData.put("certUsage", "3");
    smimeaData.put("selector", "1");
    smimeaData.put("matchingType", "1");
    smimeaData.put("certificateAssociationData",
        "b760c12119c388736da724df1224d21dfd23bf03366c286de1a4125369ef7de0");

    recordSetsInZoneApi.put(ResourceRecordSet.builder()
        .name("8812f49354927f48679bba15b670_smimecert." + zoneName).type("SMIMEA").add(smimeaData)
        .build());

    // Add multiple SMIMEA records
    System.out.println("\nAdding multiple SMIMEA resource records...");

    Builder<Map<String, Object>> builder =
        ResourceRecordSet.builder().name("8812f49354927f48679bba15b670._smimecert." + zoneName)
            .type("SMIMEA");

    Map<String, Object> smimeaData1 = new LinkedHashMap<String, Object>();
    smimeaData1.put("certUsage", "2");
    smimeaData1.put("selector", "0");
    smimeaData1.put("matchingType", "0");
    smimeaData1.put("certificateAssociationData",
        "b760c12119c388736da724df1224d21dfd23bf03366c286de1a4125369ef7de0");
    builder.add(smimeaData1);

    Map<String, Object> smimeaData2 = new LinkedHashMap<String, Object>();
    smimeaData2.put("certUsage", "3");
    smimeaData2.put("selector", "0");
    smimeaData2.put("matchingType", "0");
    smimeaData2.put("certificateAssociationData",
        "b760c12119c388736da724df1224d21dfd23bf03366c286de1a4125369ef7de0");
    builder.add(smimeaData2);

    recordSetsInZoneApi.put(builder.build());

//    recordSetsInZoneApi.deleteByNameAndType("ns1." + zoneName, "NS");
    
    System.out.println("\nQuerying resourceRecords...");
    Iterator<ResourceRecordSet<?>> rrsIterator = recordSetsInZoneApi.iterator();
    while (rrsIterator.hasNext()) {
      ResourceRecordSet<?> rrs = rrsIterator.next();
      System.out.printf("\t%s", rrs.toString());
      System.out.println();
    }

    // Delete A resourceRecord
    System.out.println("\nDeleting A resource record...");
    recordSetsInZoneApi.deleteByNameAndType("www." + zoneName, "A");
    

    // Deleting zone
    System.out.println("\nDeleting zone...");
    zoneApi.delete(zoneName);

  }
}
