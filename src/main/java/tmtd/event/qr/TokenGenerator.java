package tmtd.event.qr;

import java.security.SecureRandom;
import java.util.Base64;

public final class TokenGenerator {
  private static final SecureRandom RNG = new SecureRandom();
  private static final int BYTES = 16; // 128-bit

  private TokenGenerator(){}

  public static String randomToken() {
    byte[] buf = new byte[BYTES];
    RNG.nextBytes(buf);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
  }
}
