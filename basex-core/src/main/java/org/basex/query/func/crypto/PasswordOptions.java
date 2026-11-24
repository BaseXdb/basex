package org.basex.query.func.crypto;

import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Options for hashing passwords.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class PasswordOptions extends Options {
  /** Hashing scheme. */
  public static final EnumOption<Scheme> SCHEME = new EnumOption<>("scheme", Scheme.ARGON2ID);
  /** Hash algorithm (PBKDF2, default: SHA-256). */
  public static final StringOption HASH = new StringOption("hash");
  /** Number of iterations (default depends on the scheme). */
  public static final NumberOption ITERATIONS = new NumberOption("iterations");
  /** Memory size in KiB (Argon2id, default: {@link Argon2#MEMORY}). */
  public static final NumberOption MEMORY = new NumberOption("memory");
  /** Number of lanes (Argon2id, default: {@link Argon2#PARALLELISM}). */
  public static final NumberOption PARALLELISM = new NumberOption("parallelism");
  /** Length of the salt in bytes. */
  public static final NumberOption SALT_LENGTH = new NumberOption("salt-length", 16);
  /** Length of the derived key in bytes. */
  public static final NumberOption LENGTH = new NumberOption("length", 32);

  /** Hashing schemes. */
  public enum Scheme {
    /** Argon2id. */ ARGON2ID,
    /** PBKDF2. */ PBKDF2;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }
}
