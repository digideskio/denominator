package denominator.verisignbind;

import feign.FeignException;

public class VerisignBindException extends FeignException {

  static final int SYSTEM_ERROR = -1;
  static final int ZONE_NOT_FOUND = 1;
  static final int ZONE_ALREADY_EXISTS = 2;

  private static final long serialVersionUID = 1L;
  private final int code;
  private final String description;

  VerisignBindException(String message, int code, String description) {
    super(message);
    this.code = code;
    this.description = description;
  }

  public int code() {
    return code;
  }

  public String description() {
    return description;
  }

}
