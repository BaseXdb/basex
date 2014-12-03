package org.basex.core;

import static org.basex.util.Token.*;
import static org.basex.util.Strings.*;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class contains information on a single user.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class User {
  /** Codes. */
  public enum Code {
    /** MD5 (used for cram-md5). */ MD5,
    /** Salt (standard authentication). */ SALT,
    /** Digest (HTTP digest authentication). */ DIGEST,
    /** Salted SHA256 hash (standard authentication). */ SALT256
  };

  /** User name. */
  private final String name;

  /** Stored codes. */
  private EnumMap<Code, String> codes;
  /** Permission. */
  private Perm perm;

  /**
   * Constructor.
   * @param name user name
   * @param password password
   * @param perm rights
   */
  public User(final String name, final String password, final Perm perm) {
    this(name, codes(name, password), perm);
  }

  /**
   * Constructor.
   * @param name user name
   * @param codes password codes
   * @param perm rights
   */
  public User(final String name, final EnumMap<Code, String> codes, final Perm perm) {
    this.name = name;
    this.codes = codes;
    this.perm = perm;
  }

  /**
   * Reads users from disk.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public User(final DataInput in) throws IOException {
    this.name = string(in.readToken());
    codes = new EnumMap<>(Code.class);
    final StringList inputs = split(string(in.readToken()), ',');
    if(inputs.size() == 1) {
      // legacy: only md5 is stored
      codes.put(Code.MD5, inputs.get(0));
    } else {
      for(final String input : inputs) {
        final String[] kv = split(input, '=').finish();
        codes.put(Code.valueOf(kv[0]), kv[1]);
      }
    }
    this.perm = Perm.get(in.readNum());
  }

  /**
   * Writes permissions to disk.
   * @param out output stream; if set to null, the global rights are written
   * @throws IOException I/O exception
   */
  public synchronized void write(final DataOutput out) throws IOException {
    out.writeToken(token(name));
    out.writeToken(token(codes.get(Code.MD5)));
    out.writeNum(perm.num);
  }

  /**
   * Returns the user name.
   * @return name
   */
  public String name() {
    return name;
  }

  /**
   * Sets the password.
   * @param password password (plain text)
   */
  public void password(final String password) {
    codes = codes(name, password);
  }

  /**
   * Returns the specified code.
   * @param code code to be returned
   * @return code, or {@code null} if code does not exist
   */
  public String code(final Code code) {
    return codes.get(code);
  }

  /**
   * Returns the permission.
   * @return permission
   */
  public Perm perm() {
    return perm;
  }

  /**
   * Sets the permission.
   * @param prm permission
   */
  public void perm(final Perm prm) {
    perm = prm;
  }

  /**
   * Tests if the user has the specified permission.
   * @param prm permission to be checked
   * @return result of check
   */
  public boolean has(final Perm prm) {
    return perm.num >= prm.num;
  }

  /**
   * Computes the salted SHA256 hash from the specified password and checks if it is correct.
   * @param password (plain text)
   * @return name
   */
  public boolean matches(final String password) {
    // use salted authentication or fallback (md5)
    final String salt = codes.get(Code.SALT);
    return salt != null ? sha256(salt + password).equals(codes.get(Code.SALT256)) :
      md5(password).equals(codes.get(Code.MD5));
  }

  /**
   * Returns a local copy of this user.
   * @return user copy
   */
  public User copy() {
    return new User(name, codes, perm.min(Perm.WRITE));
  }

  /**
   * Returns different codes for a password.
   * @param name user name
   * @param password password (plain text)
   * @return codes
   */
  private static EnumMap<Code, String> codes(final String name, final String password) {
    final EnumMap<Code, String> codes = new EnumMap<>(Code.class);
    final String salt = Long.toString(System.nanoTime());
    codes.put(Code.MD5, md5(password));
    codes.put(Code.DIGEST, md5(name + ':' + Prop.NAME + ':' + password));
    codes.put(Code.SALT, salt);
    codes.put(Code.SALT256, sha256(salt + password));
    return codes;
  }
}
