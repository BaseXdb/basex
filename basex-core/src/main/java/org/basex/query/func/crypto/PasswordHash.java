package org.basex.query.func.crypto;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.crypto.PasswordOptions.*;
import static org.basex.util.Token.*;

import java.security.*;
import java.util.*;
import java.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.basex.query.*;
import org.basex.query.func.crypto.CryptoAlgorithm.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Hashes and verifies passwords with Argon2id or PBKDF2 (PHC string format).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class PasswordHash {
  /** Base64 encoder (without padding). */
  private static final Base64.Encoder ENCODER = Base64.getEncoder().withoutPadding();
  /** Base64 decoder. */
  private static final Base64.Decoder DECODER = Base64.getDecoder();
  /** Maximum length of salts and hashes in bytes. */
  private static final int MAX_LENGTH = 1024;

  /** Query context. */
  private final QueryContext qc;
  /** Input info (can be {@code null}). */
  private final InputInfo info;

  /**
   * Constructor.
   * @param qc query context
   * @param info input info (can be {@code null})
   */
  PasswordHash(final QueryContext qc, final InputInfo info) {
    this.qc = qc;
    this.info = info;
  }

  /**
   * Hashes a password.
   * @param password password
   * @param options options
   * @return PHC string
   * @throws QueryException query exception
   */
  String hash(final String password, final PasswordOptions options) throws QueryException {
    final Scheme scheme = options.get(SCHEME);
    final boolean argon = scheme == Scheme.ARGON2ID;
    // options of the other scheme (which have no default values)
    for(final Option<?> option : argon ? List.of(HASH) : List.of(MEMORY, PARALLELISM)) {
      if(options.contains(option)) throw invalid(scheme, option, options.get(option));
    }

    final int iterations = options.contains(ITERATIONS) ? options.get(ITERATIONS) :
      argon ? Argon2.ITERATIONS : 600000;
    final int saltLength = options.get(SALT_LENGTH), length = options.get(LENGTH);
    if(saltLength < 1 || saltLength > MAX_LENGTH) throw invalid(scheme, SALT_LENGTH, saltLength);
    if(length > MAX_LENGTH) throw invalid(scheme, LENGTH, length);

    final byte[] salt = CryptoRandomBytes.get(saltLength);
    if(argon) {
      final int memory = options.contains(MEMORY) ? options.get(MEMORY) : Argon2.MEMORY;
      final int parallelism = options.contains(PARALLELISM) ? options.get(PARALLELISM) :
        Argon2.PARALLELISM;
      final byte[] key;
      try {
        key = new Argon2(qc).derive(token(password), salt, iterations, memory, parallelism,
            length);
      } catch(final IllegalArgumentException ex) {
        throw CX_INVALGO_X.get(info, scheme + " with " + ex.getMessage());
      }
      return "$argon2id$v=" + Argon2.VERSION + "$m=" + memory + ",t=" + iterations + ",p=" +
        parallelism + '$' + ENCODER.encodeToString(salt) + '$' + ENCODER.encodeToString(key);
    }
    if(iterations < 1) throw invalid(scheme, ITERATIONS, iterations);
    if(length < 1) throw invalid(scheme, LENGTH, length);
    final Hash hash = options.contains(HASH) ? Hash.get(options.get(HASH), info) : Hash.SHA_256;
    final byte[] key = derive(password, salt, iterations, length, hash.compact);
    return "$pbkdf2-" + hash.compact.toLowerCase(Locale.ENGLISH) + "$i=" + iterations + '$' +
      ENCODER.encodeToString(salt) + '$' + ENCODER.encodeToString(key);
  }

  /**
   * Verifies a password.
   * @param password password
   * @param phc PHC string
   * @return result of check
   * @throws QueryException query exception
   */
  boolean verify(final String password, final String phc) throws QueryException {
    final String[] parts = Strings.split(phc, '$');
    try {
      if(parts.length == 6 && parts[0].isEmpty() && parts[1].equals("argon2id") &&
          parts[2].equals("v=" + Argon2.VERSION) && parts[3].matches("m=\\d+,t=\\d+,p=\\d+")) {
        // $argon2id$v=19$m=<memory>,t=<iterations>,p=<parallelism>$<salt>$<key>
        final String[] params = parts[3].split("[=,]");
        final byte[] key = DECODER.decode(parts[5]);
        return MessageDigest.isEqual(new Argon2(qc).derive(token(password),
            DECODER.decode(parts[4]), Integer.parseInt(params[3]), Integer.parseInt(params[1]),
            Integer.parseInt(params[5]), key.length), key);
      }
      if(parts.length == 5 && parts[0].isEmpty() && parts[1].startsWith("pbkdf2-") &&
          parts[2].startsWith("i=")) {
        // $pbkdf2-<hash>$i=<iterations>$<salt>$<key>
        final Hash hash = Hash.get(parts[1].substring(7));
        final int iterations = Integer.parseInt(parts[2].substring(2));
        final byte[] salt = DECODER.decode(parts[3]);
        final byte[] key = DECODER.decode(parts[4]);
        if(hash != null && iterations >= 1 && salt.length > 0 && key.length > 0) {
          return MessageDigest.isEqual(derive(password, salt, iterations, key.length,
              hash.compact), key);
        }
      }
    } catch(final IllegalArgumentException ex) {
      Util.debug(ex);
    }
    // do not include salt and key in the error message
    throw CX_PWHASH_X.get(info, parts.length > 1 && parts[0].isEmpty() ?
      "$" + parts[1] + "$…" : "unknown format");
  }

  /**
   * Returns an error for an invalid option.
   * @param scheme hashing scheme
   * @param option option
   * @param value value
   * @return query exception
   */
  private QueryException invalid(final Scheme scheme, final Option<?> option,
      final Object value) {
    return CX_INVALGO_X.get(info, scheme + " with " + option.name() + ' ' + value);
  }

  /**
   * Derives a key from a password with PBKDF2 (RFC 8018).
   * @param password password
   * @param salt salt
   * @param iterations number of iterations
   * @param length length of the derived key in bytes
   * @param hash compact name of the hash algorithm ({@code SHA256})
   * @return derived key
   * @throws QueryException query exception
   */
  byte[] derive(final String password, final byte[] salt, final int iterations,
      final int length, final String hash) throws QueryException {
    if(salt.length == 0) throw CX_KEYINV_X.get(info, "empty salt");
    try {
      final String name = "Hmac" + hash;
      final Mac mac = Mac.getInstance(name);
      // HMAC pads keys with zeros: an empty key (rejected by SecretKeySpec) equals a zero byte
      final byte[] pw = token(password);
      mac.init(new SecretKeySpec(pw.length == 0 ? new byte[1] : pw, name));
      final int hl = mac.getMacLength();
      final byte[] key = new byte[length], u = new byte[hl];
      // the loops may run for a long time: allow timeouts and interruptions
      long macs = 0;
      for(int b = 1, p = 0; p < length; b++) {
        mac.update(salt);
        mac.update(new byte[] { (byte) (b >>> 24), (byte) (b >>> 16), (byte) (b >>> 8), (byte) b });
        mac.doFinal(u, 0);
        if((++macs & 0xFFFF) == 0) qc.checkStop();
        final byte[] t = u.clone();
        for(int i = 1; i < iterations; i++) {
          if((++macs & 0xFFFF) == 0) qc.checkStop();
          mac.update(u);
          mac.doFinal(u, 0);
          for(int j = 0; j < hl; j++) t[j] ^= u[j];
        }
        final int n = Math.min(hl, length - p);
        System.arraycopy(t, 0, key, p, n);
        p += n;
      }
      return key;
    } catch(final NoSuchAlgorithmException ex) {
      throw CX_INVHASH_X.get(info, hash).cause(ex);
    } catch(final GeneralSecurityException | IllegalArgumentException ex) {
      throw CX_KEYINV_X.get(info, ex);
    }
  }
}
