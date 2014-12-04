package org.basex.core.users;

import static org.basex.core.users.UserText.*;
import static org.basex.util.Strings.*;
import static org.basex.util.XMLAccess.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * This class contains information on a single user.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class User {
  /** Stored password codes. */
  private final EnumMap<Algorithm, EnumMap<Code, String>> passwords =
      new EnumMap<>(Algorithm.class);
  /** User name. */
  private final String name;
  /** Permission. */
  private Perm perm;

  /**
   * Constructor.
   * @param name user name
   * @param password password
   * @param perm rights
   */
  public User(final String name, final String password, final Perm perm) {
    this(name, perm);
    for(final Algorithm algo : Algorithm.values()) {
      passwords.put(algo, new EnumMap<Code, String>(Code.class));
    }
    password(password);
  }

  /**
   * Constructor.
   * @param name user name
   * @param perm rights
   */
  public User(final String name, final Perm perm) {
    this.name = name;
    this.perm = perm;
  }

  /**
   * Parses a single user from the specified node.
   * @param user user node
   * @param global global permissions
   * @throws BaseXException database exception
   */
  public User(final ANode user, final boolean global) throws BaseXException {
    name = string(attribute("Root", user, NAME));
    perm = attribute(name, user, PERM, Perm.values());
    if(!global) return;

    for(final ANode password : children(user, PASSWORD)) {
      final Algorithm algo = attribute(name, password, ALGORITHM, Algorithm.values());
      final EnumMap<Code, String> ec = new EnumMap<>(Code.class);
      for(final ANode code : children(password, null)) {
        ec.put(value(name, code.qname().id(), algo.codes), string(code.string()));
      }
      for(final Code code : algo.codes) {
        if(ec.get(code) == null)
          throw new BaseXException(name + ", " + algo + ": " + code + " missing.");
      }
      passwords.put(algo, ec);
    }

    // create missing entries
    for(final Algorithm algo : Algorithm.values()) {
      if(passwords.get(algo) == null) throw new BaseXException(name + ": " + algo + " missing.");
    }
  }

  /**
   * Writes permissions to the specified XML file.
   * @param xml xml builder
   */
  public synchronized void write(final XMLBuilder xml) {
    xml.open(USER, NAME, name, PERM, perm);
    if(passwords != null) {
      for(final Entry<Algorithm, EnumMap<Code, String>> algo : passwords.entrySet()) {
        xml.open(PASSWORD, ALGORITHM, algo.getKey());
        for(final Entry<Code, String> code : algo.getValue().entrySet()) {
          final String v = code.getValue();
          if(!v.isEmpty()) xml.open(code.getKey()).text(v).close();
        }
        xml.close();
      }
    }
    xml.close();
  }

  /**
   * Returns the user name.
   * @return name
   */
  public String name() {
    return name;
  }

  /**
   * Computes new password hashes.
   * @param password password (plain text)
   */
  public void password(final String password) {
    EnumMap<Code, String> codes = passwords.get(Algorithm.DIGEST);
    codes.put(Code.HASH, digest(name, password));

    codes = passwords.get(Algorithm.SALTED_SHA256);
    final String salt = Long.toString(System.nanoTime());
    codes.put(Code.SALT, salt);
    codes.put(Code.HASH, sha256(salt + password));
  }

  /**
   * Returns the specified code.
   * @param alg used algorithm
   * @param code code to be returned
   * @return code, or {@code null} if code does not exist
   */
  public String code(final Algorithm alg, final Code code) {
    return passwords.get(alg).get(code);
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
   * Computes the hash from the specified password and checks if it is correct.
   * @param password (plain text)
   * @return name
   */
  public boolean matches(final String password) {
    final EnumMap<Code, String> alg = passwords.get(Algorithm.SALTED_SHA256);
    return sha256(alg.get(Code.SALT) + password).equals(alg.get(Code.HASH));
  }

  /**
   * Returns the digest hash value.
   * @param name user name
   * @param password password
   * @return digest digest hash
   */
  public static String digest(final String name, final String password) {
    return md5(name + ':' + Prop.NAME + ':' + password);
  }
}
