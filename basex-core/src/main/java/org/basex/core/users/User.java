package org.basex.core.users;

import static org.basex.core.users.UserText.*;
import static org.basex.util.Strings.*;
import static org.basex.util.Token.*;
import static org.basex.util.XMLAccess.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * This class contains information on a single user.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class User {
  /** Stored password codes. */
  private final EnumMap<Algorithm, EnumMap<Code, String>> passwords =
      new EnumMap<>(Algorithm.class);
  /** Local permissions, using database names or glob patterns as key. */
  private final LinkedHashMap<String, Perm> locals = new LinkedHashMap<>();
  /** User name. */
  private String name;
  /** Permission. */
  private Perm perm;

  /**
   * Constructor.
   * @param name user name
   * @param password password
   * @param perm rights
   */
  User(final String name, final String password, final Perm perm) {
    this.name = name;
    this.perm = perm;
    for(final Algorithm algo : Algorithm.values()) {
      passwords.put(algo, new EnumMap<Code, String>(Code.class));
    }
    password(password);
  }

  /**
   * Parses a single user from the specified node.
   * @param user user node
   * @throws BaseXException database exception
   */
  User(final ANode user) throws BaseXException {
    name = string(attribute("Root", user, NAME));
    perm = attribute(name, user, PERMISSION, Perm.values());

    for(final ANode password : children(user, PASSWORD)) {
      final EnumMap<Code, String> ec = new EnumMap<>(Code.class);
      final Algorithm algo = attribute(name, password, ALGORITHM, Algorithm.values());
      if(passwords.containsKey(algo)) throw new BaseXException(
          name + ": Algorithm \"" + algo + "\" supplied more than once.");
      passwords.put(algo, ec);

      for(final ANode code : children(password, null)) {
        final Code cd = value(name, code.qname().id(), algo.codes);
        if(ec.containsKey(cd)) throw new BaseXException(
            name + ", " + algo + ": Code \"" + code + "\" supplied more than once.");
        ec.put(cd, string(code.string()));
      }
      for(final Code code : algo.codes) {
        if(ec.get(code) == null)
          throw new BaseXException(name + ", " + algo + ": Code \"" + code + "\" missing.");
      }
    }

    // create missing entries
    for(final Algorithm algo : Algorithm.values()) {
      if(passwords.get(algo) == null) throw new BaseXException(
          name + ": Algorithm \"" + algo + "\" missing.");
    }

    // parse local permissions
    for(final ANode database : children(user, DATABASE)) {
      final String nm = string(attribute(name, database, PATTERN));
      final Perm prm = attribute(name, database, PERMISSION, Perm.values());
      locals.put(nm, prm);
    }
  }

  /**
   * Writes permissions to the specified XML file.
   * @param xml xml builder
   */
  synchronized void write(final XMLBuilder xml) {
    xml.open(USER, NAME, name, PERMISSION, perm);
    if(passwords.size() != 0) {
      for(final Entry<Algorithm, EnumMap<Code, String>> algo : passwords.entrySet()) {
        xml.open(PASSWORD, ALGORITHM, algo.getKey());
        for(final Entry<Code, String> code : algo.getValue().entrySet()) {
          final String v = code.getValue();
          if(!v.isEmpty()) xml.open(code.getKey()).text(v).close();
        }
        xml.close();
      }
      for(final Entry<String, Perm> local : locals.entrySet()) {
        xml.open(DATABASE, PATTERN, local.getKey(), PERMISSION, local.getValue());
        xml.close();
      }
    }
    xml.close();
  }

  /**
   * Sets the user name.
   * @param nm name
   */
  void name(final String nm) {
    name = nm;
  }

  /**
   * Removes local permissions.
   * @param pattern database pattern
   */
  void remove(final String pattern) {
    locals.remove(pattern);
  }

  /**
   * Returns the local permissions.
   * @return local permissions
   */
  public Map<String, Perm> locals() {
    return locals;
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
  void password(final String password) {
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
   * Returns algorithms.
   * @return algorithms
   */
  public EnumMap<Algorithm, EnumMap<Code, String>> alg() {
    return passwords;
  }

  /**
   * Returns the global permission, or the permission for the specified database.
   * @param db database (can be {@code null})
   * @return permission
   */
  public Perm perm(final String db) {
    if(db != null) {
      final Entry<String, Perm> entry = find(db);
      if(entry != null) return entry.getValue();
    }
    return perm;
  }

  /**
   * Returns the first entry for the specified database.
   * @param db database
   * @return entry, or {@code null}
   */
  Entry<String, Perm> find(final String db) {
    for(final Entry<String, Perm> entry : locals.entrySet()) {
      if(Databases.regex(entry.getKey()).matcher(db).matches()) return entry;
    }
    return null;
  }

  /**
   * Sets the permission.
   * @param prm permission
   * @param pattern database pattern (can be {@code null})
   */
  public void perm(final Perm prm, final String pattern) {
    if(pattern == null) {
      perm = prm;
    } else {
      locals.put(pattern, prm);
    }
  }


  /**
   * Tests if the user has the specified permission.
   * @param prm permission to be checked
   * @return result of check
   */
  public boolean has(final Perm prm) {
    return has(prm, null);
  }

  /**
   * Tests if the user has the specified permission.
   * @param prm permission to be checked
   * @param db database (can be {@code null})
   * @return result of check
   */
  public boolean has(final Perm prm, final String db) {
    return perm(db).ordinal() >= prm.ordinal();
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
  static String digest(final String name, final String password) {
    return md5(name + ':' + Prop.NAME + ':' + password);
  }

  @Override
  public String toString() {
    final XMLBuilder xml = new XMLBuilder().indent();
    write(xml);
    return xml.toString();
  }
}
