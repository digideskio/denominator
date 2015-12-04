package denominator.verisignbind;

import static denominator.common.Preconditions.checkNotNull;
import static feign.Util.UTF_8;
import static java.lang.String.format;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import denominator.Credentials;
import denominator.Provider;
import feign.Request;
import feign.RequestTemplate;
import feign.Target;

final class VerisignBindTarget implements Target<VerisignBind> {

  private final Provider provider;
  private final javax.inject.Provider<Credentials> credentials;

  @Inject
  VerisignBindTarget(Provider provider, javax.inject.Provider<Credentials> credentials) {
    this.provider = provider;
    this.credentials = credentials;
  }

  @Override
  public Class<VerisignBind> type() {
    return VerisignBind.class;
  }

  @Override
  public String name() {
    return provider.name();
  }

  @Override
  public String url() {
    return provider.url();
  }

  @Override
  public Request apply(RequestTemplate in) {
    String username;
    String password;

    Credentials creds = credentials.get();
    if (creds instanceof List) {
      @SuppressWarnings("unchecked")
      List<Object> listCreds = (List<Object>) creds;
      username = listCreds.get(0).toString();
      password = listCreds.get(1).toString();
    } else if (creds instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> mapCreds = (Map<String, Object>) creds;
      username = checkNotNull(mapCreds.get("username"), "username").toString();
      password = checkNotNull(mapCreds.get("password"), "password").toString();
    } else {
      throw new IllegalArgumentException("Unsupported credential type: " + creds);
    }

    in.insert(0, url());
    in.header("Host", URI.create(in.url()).getHost());
    // in.header("X-Auth-Token", urlAndToken.tokenId);
    in.header("Content-Type", "application/json");
    return in.request();
  }

}
