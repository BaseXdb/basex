package org.basex.query.func.crypto;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
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
  // BaseX 14.0: unit differs from crypto:derive, crypto:random-bytes, crypto:pbkdf2 (bytes)
  /** Length of generated secret keys in bits. */
  public static final NumberOption LENGTH = new NumberOption("length");
  /** Length of the modulus of generated RSA keys in bits. */
  public static final NumberOption MODULUS_LENGTH = new NumberOption("modulus-length");
  /** Curve of generated EC keys. */
  public static final StringOption NAMED_CURVE = new StringOption("named-curve");
  /** Initialization vector. */
  public static final ValueOption IV = new ValueOption("iv", Types.BINARY_ZO);
  /** Additional authenticated data. */
  public static final ValueOption ADDITIONAL_DATA =
      new ValueOption("additional-data", Types.STRING_OR_BINARY_ZO);
  /** Length of the authentication tag in bits. */
  public static final NumberOption TAG_LENGTH = new NumberOption("tag-length");
  /** Salt for key derivation. */
  public static final ValueOption SALT = new ValueOption("salt", Types.STRING_OR_BINARY_ZO);
  /** Context information for key derivation. */
  public static final ValueOption INFO = new ValueOption("info", Types.STRING_OR_BINARY_ZO);
  /** Public key of the other party for key agreement. */
  public static final ValueOption PUBLIC = new ValueOption("public",
      Types.STRING_OR_BINARY_OR_MAP_O.with(Occ.ZERO_OR_ONE));

  @Override
  protected StringOption stringOption() {
    return NAME;
  }

  /**
   * Returns the bytes of a string or binary option.
   * @param option option
   * @param info input info (can be {@code null})
   * @return bytes (can be {@code null})
   * @throws QueryException query exception
   */
  byte[] bytes(final ValueOption option, final InputInfo info) throws QueryException {
    final Value value = get(option);
    return value.isEmpty() ? null : CryptoKey.bytes((Item) value, info);
  }
}
