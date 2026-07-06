package org.basex.core.users;

import static org.basex.util.Strings.*;
import static org.basex.util.Token.*;

import java.security.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.basex.util.*;

/**
 * Password algorithms.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum Algorithm {
  /** Digest (legacy; required for client/server and HTTP digest authentication). */
  DIGEST(Code.HASH) {
    @Override
    void prepare(final EnumMap<Code, String> codes) { }
    @Override
    String hash(final String name, final String password, final EnumMap<Code, String> codes) {
      return md5(name + ':' + Prop.NAME + ':' + password);
    }
    @Override
    boolean current(final EnumMap<Code, String> codes) {
      return true;
    }
  },
  /** Salted SHA-256. */
  SALTED_SHA256(Code.SALT, Code.HASH) {
    @Override
    void prepare(final EnumMap<Code, String> codes) {
      codes.put(Code.SALT, salt());
    }
    @Override
    String hash(final String name, final String password, final EnumMap<Code, String> codes) {
      return sha256(codes.get(Code.SALT) + password);
    }
    @Override
    boolean current(final EnumMap<Code, String> codes) {
      return true;
    }
  },
  /** PBKDF2 (HMAC-SHA256). */
  PBKDF2(Code.SALT, Code.ITERATIONS, Code.HASH) {
    @Override
    void prepare(final EnumMap<Code, String> codes) {
      codes.put(Code.SALT, salt());
      codes.put(Code.ITERATIONS, Integer.toString(PBKDF2_ITERATIONS));
    }
    @Override
    String hash(final String name, final String password, final EnumMap<Code, String> codes) {
      return pbkdf2(password, codes.get(Code.SALT), toInt(codes.get(Code.ITERATIONS)));
    }
    @Override
    boolean current(final EnumMap<Code, String> codes) {
      return toInt(codes.get(Code.ITERATIONS)) >= PBKDF2_ITERATIONS;
    }
  };

  /** Salt generation. */
  private static final SecureRandom RANDOM = new SecureRandom();
  /** Iteration count for PBKDF2. */
  private static final int PBKDF2_ITERATIONS = 600_000;

  /** Code types used by this algorithm. */
  final Code[] codeTypes;

  /**
   * Constructor.
   * @param codeTypes used code types
   */
  Algorithm(final Code... codeTypes) {
    this.codeTypes = codeTypes;
  }

  /**
   * Computes the password codes for a user.
   * @param name username
   * @param password password (plain text)
   * @return codes
   */
  final EnumMap<Code, String> create(final String name, final String password) {
    final EnumMap<Code, String> codes = new EnumMap<>(Code.class);
    prepare(codes);
    codes.put(Code.HASH, hash(name, password, codes));
    return codes;
  }

  /**
   * Checks if a password matches the stored codes.
   * @param name username
   * @param password password (plain text)
   * @param codes stored codes
   * @return result of check
   */
  final boolean verify(final String name, final String password,
      final EnumMap<Code, String> codes) {
    // constant-time comparison of the recomputed and the stored hash
    return MessageDigest.isEqual(token(hash(name, password, codes)), token(codes.get(Code.HASH)));
  }

  /**
   * Stores the parameters required by this algorithm.
   * @param codes codes to populate
   */
  abstract void prepare(EnumMap<Code, String> codes);

  /**
   * Computes a password hash.
   * @param name username
   * @param password password (plain text)
   * @param codes algorithm parameters
   * @return hash (hex string)
   */
  abstract String hash(String name, String password, EnumMap<Code, String> codes);

  /**
   * Checks if stored codes were computed with the current algorithm parameters.
   * @param codes stored codes
   * @return result of check
   */
  abstract boolean current(EnumMap<Code, String> codes);

  /**
   * Returns a new random salt.
   * @return salt (hex string)
   */
  private static String salt() {
    final byte[] bytes = new byte[16];
    RANDOM.nextBytes(bytes);
    return string(hex(bytes, false));
  }

  /**
   * Computes a PBKDF2 (HMAC-SHA256) hash.
   * @param password password
   * @param salt salt
   * @param iterations iteration count
   * @return hash (hex string)
   */
  private static String pbkdf2(final String password, final String salt, final int iterations) {
    try {
      final PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), token(salt), iterations, 256);
      final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      return string(hex(factory.generateSecret(spec).getEncoded(), false));
    } catch(final GeneralSecurityException ex) {
      throw Util.notExpected(ex);
    }
  }

  @Override
  public String toString() {
    return Enums.string(this);
  }
}
