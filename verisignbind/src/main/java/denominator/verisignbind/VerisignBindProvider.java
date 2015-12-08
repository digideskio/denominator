package denominator.verisignbind;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import com.google.gson.TypeAdapter;

import dagger.Provides;
import denominator.BasicProvider;
import denominator.CheckConnection;
import denominator.DNSApiManager;
import denominator.ResourceRecordSetApi;
import denominator.ZoneApi;
import denominator.config.GeoUnsupported;
import denominator.config.NothingToClose;
import denominator.config.OnlyBasicResourceRecordSets;
import denominator.config.WeightedUnsupported;
import denominator.verisignbind.VerisignBindAdapters.ResourceRecordListAdapter;
import denominator.verisignbind.VerisignBindAdapters.ZoneListAdapter;
import feign.Feign;
import feign.Logger;
import feign.Request.Options;
import feign.codec.Decoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

public class VerisignBindProvider extends BasicProvider {
  // private static final String DEFAULT_URL = "http://10.239.30.206:8001/api/v1/";
  private static final String DEFAULT_URL = "http://127.0.0.1:8080/api/v1/";

  private final String url;

  public VerisignBindProvider() {
    this(null);
  }

  /**
   * Construct a new provider for the Verisign Bind service.
   *
   * @param url if empty or null use default
   */
  public VerisignBindProvider(String url) {
    this.url = url == null || url.isEmpty() ? DEFAULT_URL : url;
  }

  @Override
  public String url() {
    return url;
  }

  @Override
  public Set<String> basicRecordTypes() {
    Set<String> types = new LinkedHashSet<String>();
    types.addAll(Arrays.asList("A", "AAAA", "CNAME", "MX", "NAPTR", "NS", "PTR", "SRV", "TXT"));
    return types;
  }

  @Override
  public Map<String, Collection<String>> credentialTypeToParameterNames() {
    Map<String, Collection<String>> options = new LinkedHashMap<String, Collection<String>>();
    options.put("password", Arrays.asList("username", "password"));
    return options;
  }

  @dagger.Module(injects = {DNSApiManager.class}, complete = false,  overrides = true, includes = {
      NothingToClose.class, WeightedUnsupported.class, GeoUnsupported.class, OnlyBasicResourceRecordSets.class, FeignModule.class})
  public static final class Module {

    @Provides
    CheckConnection checkConnection(HostedZonesReadable checkConnection) {
      return checkConnection;
    }

    @Provides
    @Singleton
    ZoneApi provideZoneApi(VerisignBindZoneApi api) {
      return api;
    }


    @Provides
    @Singleton
    ResourceRecordSetApi.Factory provideResourceRecordSetApiFactory(
        VerisignBindResourceRecordSetApi.Factory factory) {
      return factory;
    }
  }

  @dagger.Module(injects = VerisignBindResourceRecordSetApi.Factory.class, complete = false)
  public static final class FeignModule {

    @Provides
    @Singleton
    VerisignBind verisignBind(Feign feign, VerisignBindTarget target) {
      return feign.newInstance(target);
    }

    @Provides
    Logger logger() {
      return new Logger.NoOpLogger();
    }

    @Provides
    Logger.Level logLevel() {
      return Logger.Level.NONE;
    }

    @Provides
    @Singleton
    Feign feign(Logger logger, Logger.Level logLevel) {

      Options options = new Options(10 * 1000, 10 * 60 * 1000);
      Decoder decoder = decoder();

      return Feign.builder()
          .logger(logger)
          .logLevel(logLevel)
          .options(options)
          .encoder(new GsonEncoder())
          .decoder(decoder)
          .build();
    }

    static Decoder decoder() {
      return new GsonDecoder(Arrays.<TypeAdapter<?>>asList(
          new ZoneListAdapter(),
          new ResourceRecordListAdapter()));
    }
  }
}
