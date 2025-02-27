package org.basex.core.users;

import static org.basex.core.users.UserText.*;
import static org.basex.util.Strings.*;
import static org.basex.util.Token.*;
import static org.basex.util.XMLAccess.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * This class contains information on a single user.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class User {
  /** Stored password codes. */
  private EnumMap<Algorithm, EnumMap<Code, String>> passwords =
      new EnumMap<>(Algorithm.class);
  /** Database patterns for local permissions. */
  private LinkedHashMap<String, Perm> patterns = new LinkedHashMap<>();
  /** Permission. */
  private Perm permission = Perm.NONE;
  /** Name. */
  private String name;
  /** Info node (can be {@code null}). */
  private ANode info;

  /**
   * Constructor.
   * @param name username
   */
  public User(final String name) {
    this.name = name;
  }

  /**
   * Constructor with password.
   * @param name username
   * @param password password
   */
  public User(final String name, final String password) {
    this(name);
    password(password);
  }

  /**
   * Copy constructor.
   * @param user parent user
   */
  public User(final User user) {
    passwords = user.passwords;
    patterns = user.patterns;
    permission = user.permission;
    name = user.name;
    info = user.info;
  }


  /**
   * Indicates if passwords are stored for a user.
   * @return result of check
   */
  public boolean enabled() {
    return !passwords.isEmpty();
  }

  /**
   * Parses a single user from the specified node.
   * @param user user node
   * @param file input file
   * @throws BaseXException database exception
   */
  User(final ANode user, final IOFile file) throws BaseXException {
    name = string(attribute(user, Q_NAME, "Root"));
    permission = attribute(name, user, Q_PERMISSION, Perm.values());

    for(final ANode child : children(user)) {
      final QNm qname = child.qname();
      if(qname.eq(Q_PASSWORD)) {
        final EnumMap<Code, String> ec = new EnumMap<>(Code.class);
        final Algorithm algorithm = attribute(name, child, Q_ALGORITHM, Algorithm.values());
        if(passwords.containsKey(algorithm)) throw new BaseXException(
            "%: Algorithm % supplied more than once.", name, algorithm);
        passwords.put(algorithm, ec);

        for(final ANode code : children(child)) {
          final Code cd = value(name, code.qname().unique(), algorithm.codes);
          if(ec.containsKey(cd)) throw new BaseXException(
              "%, %: Code % supplied more than once.", name, algorithm, code);
          ec.put(cd, string(code.string()));
        }
        for(final Code code : algorithm.codes) {
          if(ec.get(code) == null)
            throw new BaseXException("%, %: Code '%' missing.", name, algorithm, code);
        }
      } else if(qname.eq(Q_DATABASE)) {
        // parse local permissions
        final String nm = string(attribute(child, Q_PATTERN, name));
        final Perm perm = attribute(name, child, Q_PERMISSION, Perm.values());
        patterns.put(nm, perm);
      } else if(qname.eq(Q_INFO)) {
        if(info != null) throw new BaseXException("%: <%/> occurs more than once.", file, qname);
        info = child.finish();
      } else {
        throw new BaseXException("%: invalid element <%/>.", file, qname);
      }
    }
  }

  /**
   * Returns user information as XML.
   * @param qc query context ({@code null} if element will only be created for serialization)
   * @param ii input info (can be {@code null})
   * @return user element
   * @throws QueryException query exception
   */
  public synchronized FNode toXml(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FBuilder user = FElem.build(Q_USER).add(Q_NAME, name).add(Q_PERMISSION, permission);
    passwords.forEach((key, value) -> {
      final FBuilder pw = FElem.build(Q_PASSWORD).add(Q_ALGORITHM, key);
      value.forEach((k, v) -> {
        if(!v.isEmpty()) pw.add(FElem.build(new QNm(k.toString())).add(v));
      });
      user.add(pw.finish());
    });
    patterns.forEach((key, value) -> user.add(FElem.build(Q_DATABASE).add(Q_PATTERN, key).
        add(Q_PERMISSION, value).finish()));
    if(info != null) {
      if(qc != null) {
        // create copy of the info node if query context is available
        user.add(info.materialize(n -> false, ii, qc));
      } else {
        // otherwise, referenced original info node and invalidate parent reference
        user.add(info);
        info.parent(null);
      }
    }
    return user.finish();
  }

  /**
   * Sets the username.
   * @param nm name
   */
  public synchronized void name(final String nm) {
    name = nm;
  }

  /**
   * Drops the specified database pattern.
   * @param pattern database pattern
   */
  public synchronized void drop(final String pattern) {
    patterns.remove(pattern);
  }

  /**
   * Returns the username.
   * @return name
   */
  public synchronized String name() {
    return name;
  }

  /**
   * Computes new password hashes.
   * @param password password (plain text)
   */
  public synchronized void password(final String password) {
    for(final Algorithm algorithm : Algorithm.values()) {
      final EnumMap<Code, String> codes = passwords.computeIfAbsent(algorithm,
          k -> new EnumMap<>(Code.class));
      if(algorithm == Algorithm.SALTED_SHA256) {
        final String salt = Long.toString(System.nanoTime());
        codes.put(Code.SALT, salt);
        codes.put(Code.HASH, sha256(salt + password));
      } else {
        codes.put(Code.HASH, digest(name, password));
      }
    }
  }

  /**
   * Returns the specified code.
   * @param algorithm used algorithm
   * @param code code to be returned
   * @return code, or {@code null} if code does not exist
   */
  public synchronized String code(final Algorithm algorithm, final Code code) {
    return passwords.get(algorithm).get(code);
  }

  /**
   * Returns the global permission, or the permission for the specified database.
   * @param db database pattern (can be {@code null})
   * @return permission
   */
  public synchronized Perm perm(final String db) {
    if(db != null) {
      final Entry<String, Perm> entry = find(db);
      if(entry != null) return entry.getValue();
    }
    return permission;
  }

  /**
   * Returns the first entry for the specified database.
   * @param pattern database pattern
   * @return entry, or {@code null} if no entry exists
   */
  synchronized Entry<String, Perm> find(final String pattern) {
    for(final Entry<String, Perm> entry : patterns.entrySet()) {
      if(Databases.regex(entry.getKey()).matcher(pattern).matches()) return entry;
    }
    return null;
  }

  /**
   * Sets a global permission.
   * @param perm permission
   * @return self reference
   */
  public synchronized User permission(final Perm perm) {
    permission = perm;
    return this;
  }

  /**
   * Sets a permission for a specific pattern.
   * @param perm permission
   * @param pattern database pattern (if empty, a global permission is set)
   * @return self reference
   */
  public synchronized User permission(final Perm perm, final String pattern) {
    if(pattern.isEmpty()) {
      permission(perm);
    } else {
      patterns.put(pattern, perm);
    }
    return this;
  }

  /**
   * Tests if the user has the specified permission.
   * @param perm permission to be checked
   * @return result of check
   */
  public synchronized boolean has(final Perm perm) {
    return has(perm, null);
  }

  /**
   * Tests if the user has the specified permission.
   * @param perm permission to be checked
   * @param db database pattern (can be {@code null})
   * @return result of check
   */
  public synchronized boolean has(final Perm perm, final String db) {
    return perm(db).ordinal() >= perm.ordinal();
  }

  /**
   * Computes the hash from the specified password and checks if it is correct.
   * @param password (plain text)
   * @return name
   */
  public synchronized boolean matches(final String password) {
    if(!enabled()) return false;
    final EnumMap<Code, String> algorithm = passwords.get(Algorithm.SALTED_SHA256);
    return sha256(algorithm.get(Code.SALT) + password).equals(algorithm.get(Code.HASH));
  }

  /**
   * Returns the info element.
   * @return info element (can be {@code null})
   */
  public synchronized ANode info() {
    return info;
  }

  /**
   * Sets the info element.
   * @param elem info element
   */
  public synchronized void info(final ANode elem) {
    info = elem.hasChildren() || elem.hasAttributes() ? elem : null;
  }

  /**
   * Returns the digest hash value.
   * @param name username
   * @param password password
   * @return digest digest hash
   */
  private static String digest(final String name, final String password) {
    return md5(name + ':' + Prop.NAME + ':' + password);
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + name + '/' + permission + ']';
  }
}
