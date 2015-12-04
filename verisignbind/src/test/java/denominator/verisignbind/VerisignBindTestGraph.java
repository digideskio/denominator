package denominator.verisignbind;

import static feign.Util.emptyToNull;
import static java.lang.System.getProperty;
import denominator.DNSApiManagerFactory;

public class VerisignBindTestGraph extends denominator.TestGraph {

  private static final String url = emptyToNull(getProperty("verisignbind.url"));
  private static final String zone = emptyToNull(getProperty("verisignbind.zone"));

  public VerisignBindTestGraph() {
    super(DNSApiManagerFactory.create(new VerisignBindProvider(url)), zone);
  }
}
