package org.basex.query.func.crypto;

import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Options for cryptographic algorithms (W3C Web Cryptography API names).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class AlgorithmOptions extends Options {
  /** Signature formats. */
  public enum SignatureFormat {
    /** Concatenated r and s values. */ P1363,
    /** ASN.1 DER. */ DER;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }

  /** Algorithm name. */
  public static final StringOption NAME = new StringOption("name");
  /** Hash algorithm. */
  public static final StringOption HASH = new StringOption("hash");
  /** Salt length. */
  public static final NumberOption SALT_LENGTH = new NumberOption("salt-length");
  /** Signature format. */
  public static final EnumOption<SignatureFormat> SIGNATURE_FORMAT =
      new EnumOption<>("signature-format", SignatureFormat.class);

  @Override
  protected StringOption stringOption() {
    return NAME;
  }
}
