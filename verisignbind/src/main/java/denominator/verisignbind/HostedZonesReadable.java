package denominator.verisignbind;

import javax.inject.Inject;

import denominator.CheckConnection;

public class HostedZonesReadable implements CheckConnection {

  private final VerisignBind api;

  @Inject
  HostedZonesReadable(VerisignBind api) {
    this.api = api;
  }

  @Override
  public boolean ok() {
    try {
       return api.getZones() != null;
    } catch (RuntimeException e) {
      return false;
    }
  }

  @Override
  public String toString() {
    return "HostedZonesReadable";
  }
}
