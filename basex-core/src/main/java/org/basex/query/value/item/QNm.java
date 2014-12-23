package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import javax.xml.namespace.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * QName item ({@code xs:QName}).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class QNm extends Item {
  /** URL pattern (matching Clark and EQName notation). */
  private static final Pattern BIND = Pattern.compile("^((\"|')(.*?)\\2:|Q?(\\{(.*?)\\}))(.+)$");
  /** Singleton instance. */
  private static final QNmMap CACHE = new QNmMap();

  /** Namespace URI. */
  private byte[] uri;
  /** Name with optional prefix. */
  private byte[] name;
  /** Prefix index. */
  private int pref;

  /**
   * Empty constructor.
   */
  public QNm() {
    super(AtomType.QNM);
    name = EMPTY;
  }

  /**
   * Constructor.
   * @param name name
   */
  public QNm(final byte[] name) {
    super(AtomType.QNM);
    this.name = name;
    pref = indexOf(name, ':');
  }

  /**
   * Constructor.
   * @param name name
   */
  public QNm(final String name) {
    this(token(name));
  }

  /**
   * Constructor.
   * @param name name
   * @param uri namespace URI
   */
  public QNm(final byte[] name, final byte[] uri) {
    this(name);
    uri(uri);
  }

  /**
   * Constructor.
   * @param name name
   * @param uri namespace URI
   */
  public QNm(final String name, final byte[] uri) {
    this(token(name), uri);
  }

  /**
   * Constructor.
   * @param name name
   * @param uri namespace URI
   */
  public QNm(final String name, final String uri) {
    this(token(name), uri == null ? null : token(uri));
  }

  /**
   * Constructor, binding a statically known namespace.
   * If no namespace is found, the namespace uri is set to {@code null}.
   * @param name name
   * @param sc static context
   */
  public QNm(final byte[] name, final StaticContext sc) {
    this(name);
    uri(sc.ns.uri(prefix()));
  }

  /**
   * Constructor for converting a Java QName.
   * @param name qname
   */
  public QNm(final QName name) {
    this(token(name.getPrefix().isEmpty() ? name.getLocalPart() :
      name.getPrefix() + ':' + name.getLocalPart()), token(name.getNamespaceURI()));
  }

  /**
   * Resolves a QName string.
   * @param name name to resolve
   * @param def default namespace (can be {@code null})
   * @param sc static context
   * @param info input info
   * @return string
   * @throws QueryException query exception
   */
  public static QNm resolve(final byte[] name, final byte[] def, final StaticContext sc,
      final InputInfo info) throws QueryException {

    // check for namespace declaration
    final Matcher m = BIND.matcher(Token.string(name));
    byte[] uri = EMPTY, nm = name;
    if(m.find()) {
      final String u = m.group(3);
      uri = token(u == null ? m.group(5) : u);
      nm = token(m.group(6));
    } else {
      final int i = indexOf(nm, ':');
      if(i != -1) {
        uri = sc.ns.uri(substring(nm, 0, i));
        if(uri == null) throw NOURI_X.get(info, name);
      } else {
        uri = def;
      }
    }
    if(!XMLToken.isQName(nm)) throw BINDNAME_X.get(info, name);
    return new QNm(nm, uri);
  }

  /**
   * Sets the URI of this QName.
   * @param u the uri to be set
   */
  public void uri(final byte[] u) {
    uri = u == null ? null : normalize(u);
  }

  /**
   * Returns the URI of this QName.
   * @return uri
   */
  public byte[] uri() {
    return uri == null ? EMPTY : uri;
  }

  /**
   * Checks if the URI of this QName has been explicitly set.
   * @return result of check
   */
  public boolean hasURI() {
    return uri != null;
  }

  @Override
  public byte[] string(final InputInfo ii) {
    return name;
  }

  /**
   * Returns the string value.
   * @return string value
   */
  public byte[] string() {
    return name;
  }

  @Override
  public boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {

    final QNm nm;
    if(item instanceof QNm) {
      nm = (QNm) item;
    } else if(item.type.isUntyped() && sc != null) {
      nm = new QNm(item.string(ii), sc);
      if(!nm.hasURI() && nm.hasPrefix()) throw NSDECL_X.get(ii, nm.string());
    } else {
      throw diffError(ii, this, item);
    }
    return eq(nm);
  }

  /**
   * Compares the specified item.
   * @param n name to be compared
   * @return result of check
   */
  public boolean eq(final QNm n) {
    return n == this || Token.eq(uri(), n.uri()) && Token.eq(local(), n.local());
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
    throw diffError(ii, it, this);
  }

  /**
   * Checks if the name contains a prefix.
   * @return result of check
   */
  public boolean hasPrefix() {
    return pref != -1;
  }

  /**
   * Returns the prefix.
   * @return prefix
   */
  public byte[] prefix() {
    return pref == -1 ? EMPTY : substring(name, 0, pref);
  }

  /**
   * Returns the local name.
   * @return local name
   */
  public byte[] local() {
    return pref == -1 ? name : substring(name, pref + 1);
  }

  /**
   * Updates the values of this QName. This method is only called
   * to speed up internal operations.
   * @param n name
   * @param u URI
   */
  public void set(final byte[] n, final byte[] u) {
    name = n;
    pref = indexOf(name, ':');
    uri = u;
  }

  /**
   * Returns a unique representation of the QName.
   * <ul>
   * <li> If a URI exists, the EQName notation is used.</li>
   * <li> Otherwise, if a prefix exists, the prefix and local name is returned.</li>
   * <li> Otherwise, the local name is returned.</li>
   * </ul>
   * @return unique representation
   */
  public byte[] id() {
    return uri == null ? name : internal(null, local(), uri);
  }

  /**
   * Returns a unique representation of the QName.
   * @return unique representation
   */
  public byte[] prefixId() {
    return prefixId(null);
  }

  /**
   * Returns a unique representation of the QName.
   * <ul>
   *   <li> Skips the prefix if the namespace of the QName equals the specified one.</li>
   *   <li> Uses a prefix if its namespace URI is statically known.</li>
   *   <li> Otherwise, {@link #id()} is called.</li>
   * </ul>
   * @param ns default uri (can be {@code null})
   * @return unique representation
   */
  public byte[] prefixId(final byte[] ns) {
    if(ns != null && Token.eq(uri(), ns)) return local();
    final byte[] p = NSGlobal.prefix(uri());
    return p.length == 0 ? id() : concat(p, token(":"), local());
  }

  @Override
  public QName toJava() {
    return new QName(Token.string(uri()), Token.string(local()), Token.string(prefix()));
  }

  @Override
  public int hash(final InputInfo ii) {
    return Token.hash(local());
  }

  @Override
  public byte[] xdmInfo() {
    return new ByteList().add(typeId().bytes()).add(uri()).add(0).finish();
  }

  @Override
  public String toString() {
    return Token.string(id());
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof QNm && eq((QNm) obj);
  }

  @Override
  public int hashCode() {
    return Token.hash(id());
  }

  /**
   * Returns a cached QName with the specified local name.
   * @param local local name
   * @return instance
   */
  public static QNm get(final String local) {
    return CACHE.index(null, token(local), null);
  }

  /**
   * Returns a cached QName with the specified local name.
   * @param local local name
   * @return instance
   */
  public static QNm get(final byte[] local) {
    return CACHE.index(null, local, null);
  }

  /**
   * Returns a cached QName with the specified local name and uri.
   * @param local local name
   * @param uri namespace uri
   * @return instance
   */
  public static QNm get(final String local, final byte[] uri) {
    return CACHE.index(null, token(local), uri);
  }

  /**
   * Returns a cached QName with the specified local name and uri.
   * @param local local name
   * @param uri namespace uri
   * @return instance
   */
  public static QNm get(final byte[] local, final byte[] uri) {
    return CACHE.index(null, local, uri);
  }

  /**
   * Returns a cached QName with the specified prefix, local name and uri.
   * @param prefix prefix
   * @param local local name
   * @param uri namespace uri
   * @return instance
   */
  public static QNm get(final byte[] prefix, final String local, final byte[] uri) {
    return CACHE.index(prefix, token(local), uri);
  }

  /**
   * Returns a cached QName with the specified prefix, local name and uri.
   * @param prefix prefix
   * @param local local name
   * @param uri namespace uri
   * @return instance
   */
  public static QNm get(final String prefix, final String local, final String uri) {
    return CACHE.index(token(prefix), token(local), token(uri));
  }

  /**
   * Returns a cached QName with the specified prefix, local name and uri.
   * @param prefix prefix
   * @param local local name
   * @param uri namespace uri
   * @return instance
   */
  public static QNm get(final byte[] prefix, final byte[] local, final byte[] uri) {
    return CACHE.index(prefix, local, uri);
  }

  /**
   * Constructs an internal string representation for the components of a QName.
   * @param prefix prefix
   * @param local name
   * @param uri uri
   * @return EQName representation
   */
  public static byte[] internal(final byte[] prefix, final byte[] local, final byte[] uri) {
    // optimized for speed, as it is called quite frequently
    final int ul = uri == null ? 0 : uri.length;
    final int pl = prefix == null ? 0 : prefix.length;
    // return local name if no prefix and uri exist
    if(ul == 0 && pl == 0) return local;

    final int l = (ul == 0 ? 0 : ul + 3) + (pl == 0 ? 0 : pl + 1) + local.length;
    final byte[] key = new byte[l];
    int i = 0;
    if(ul != 0) {
      key[i++] = 'Q';
      key[i++] = '{';
      System.arraycopy(uri, 0, key, i, ul);
      key[i + ul] = '}';
      i += ul + 1;
    }
    if(pl != 0) {
      System.arraycopy(prefix, 0, key, i, pl);
      key[i + pl] = ':';
      i += pl + 1;
    }
    System.arraycopy(local, 0, key, i, local.length);
    return key;
  }
}
