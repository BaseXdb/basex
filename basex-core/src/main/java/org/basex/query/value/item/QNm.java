package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import javax.xml.namespace.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * QName item ({@code xs:QName}).
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class QNm extends Item {
  /** QName: empty. */
  public static final QNm EMPTY = new QNm(Token.EMPTY);
  /** QName: xml:base. */
  public static final QNm XML_BASE = new QNm(BASE, XML_URI);
  /** QName: rest:error. */
  public static final QNm REST_ERROR = new QNm(ERROR, REST_URI);

  /** URL pattern (matching Clark and EQName notation). */
  public static final Pattern EQNAME = Pattern.compile("^Q?\\{(.*?)}(.+)$");

  /** Name with optional prefix. */
  private final byte[] name;
  /** Prefix index. */
  private final int pref;
  /** Namespace URI (can be {@code null}). */
  private byte[] uri;

  /**
   * Constructor.
   * @param name name
   */
  public QNm(final byte[] name) {
    super(AtomType.QNAME);
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
      concat(name.getPrefix(), COLON, name.getLocalPart())), token(name.getNamespaceURI()));
  }

  /**
   * Constructor.
   * @param prefix prefix
   * @param local local name
   * @param uri namespace uri
   */
  public QNm(final byte[] prefix, final String local, final byte[] uri) {
    this(prefix, token(local), uri);
  }

  /**
   * Constructor.
   * @param prefix prefix
   * @param local local name
   * @param uri namespace uri
   */
  public QNm(final byte[] prefix, final byte[] local, final byte[] uri) {
    this(name(prefix, local), uri);
  }

  /**
   * Creates the name string.
   * @param prefix prefix
   * @param local local name
   * @return name
   */
  private static byte[] name(final byte[] prefix, final byte[] local) {
    return prefix.length == 0 ? local : concat(prefix, COLON, local);
  }

  /**
   * Resolves a QName string.
   * @param name name to resolve
   * @param sc static context (can be {@code null})
   * @return string
   * @throws QueryException query exception
   */
  public static QNm resolve(final byte[] name, final StaticContext sc) throws QueryException {
    return resolve(name, null, sc, null);
  }

  /**
   * Resolves a QName string.
   * @param name name to resolve
   * @param def default namespace (can be {@code null})
   * @param sc static context (can be {@code null})
   * @param ii input info
   * @return string
   * @throws QueryException query exception
   */
  public static QNm resolve(final byte[] name, final byte[] def, final StaticContext sc,
      final InputInfo ii) throws QueryException {

    // check for namespace declaration
    final Matcher m = EQNAME.matcher(Token.string(name));
    byte[] uri = null, nm = name;
    if(m.find()) {
      uri = token(m.group(1));
      nm = token(m.group(2));
    } else {
      final int i = indexOf(nm, ':');
      if(i == -1) {
        uri = def;
      } else {
        if(sc != null) uri = sc.ns.uri(substring(nm, 0, i));
        if(uri == null) throw NOURI_X.get(ii, name);
      }
    }
    if(!XMLToken.isQName(nm)) throw BINDNAME_X.get(ii, name);
    return new QNm(nm, uri);
  }

  /**
   * Sets the URI of this QName.
   * @param u the uri to be set (can be {@code null}, or an empty or non-empty string)
   */
  public void uri(final byte[] u) {
    uri = u == null ? null : normalize(u);
  }

  /**
   * Returns the URI of this QName.
   * @return uri
   */
  public byte[] uri() {
    return uri == null ? Token.EMPTY : uri;
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
      throw diffError(this, item, ii);
    }
    return eq(nm);
  }

  /**
   * Compares the specified name.
   * @param qnm name to be compared
   * @return result of check
   */
  public boolean eq(final QNm qnm) {
    return qnm == this || Token.eq(uri(), qnm.uri()) && Token.eq(local(), qnm.local());
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo ii) throws QueryException {
    throw diffError(item, this, ii);
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
    return pref == -1 ? Token.EMPTY : substring(name, 0, pref);
  }

  /**
   * Returns the local name.
   * @return local name
   */
  public byte[] local() {
    return pref == -1 ? name : substring(name, pref + 1);
  }

  /**
   * Returns a unique representation of the QName.
   * <ul>
   * <li> If a URI exists, the EQName notation is used.</li>
   * <li> Otherwise, if a prefix exists, the prefix and local name is returned.</li>
   * <li> Otherwise, the local name is returned.</li>
   * </ul>
   * @return QName as token
   */
  public byte[] id() {
    return uri == null ? name : internal(null, local(), uri);
  }

  /**
   * Returns an EQName representation.
   * @return QName as token
   */
  public byte[] eqName() {
    return eqName(uri(), local());
  }

  /**
   * Returns a unique representation of the QName.
   * @return QName as token
   */
  public byte[] prefixId() {
    return prefixId(null);
  }

  /**
   * Returns a unique representation of the QName.
   * <ul>
   *   <li> Skips the prefix if the namespace of the QName equals the specified one.</li>
   *   <li> Returns a prefixed name if the namespace URI is statically known.</li>
   *   <li> Otherwise, {@link #id()} is called.</li>
   * </ul>
   * @param ns default uri (can be {@code null})
   * @return QName as token
   */
  public byte[] prefixId(final byte[] ns) {
    final byte[] u = uri();
    if(ns != null && Token.eq(u, ns)) return local();
    final byte[] p = NSGlobal.prefix(u);
    return p.length != 0 ? concat(p, token(COL), local()) : id();
  }

  /**
   * Returns the QName string if it has a prefix, or {@link #prefixId()} otherwise.
   * @return QName as token
   */
  public byte[] prefixString() {
    return hasPrefix() ? string() : prefixId();
  }

  @Override
  public QName toJava() {
    return new QName(Token.string(uri()), Token.string(local()), Token.string(prefix()));
  }

  @Override
  public int hash(final InputInfo ii) {
    return Token.hash(id());
  }

  @Override
  public byte[] xdmInfo() {
    return new ByteList().add(typeId().asByte()).add(uri()).add(0).finish();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof QNm)) return false;
    final QNm qnm = (QNm) obj;
    return Token.eq(uri(), qnm.uri()) && Token.eq(name, qnm.name);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(id());
  }

  // STATIC METHODS ===============================================================================

  /**
   * Returns an EQName representation.
   * @param uri URI
   * @param local local name
   * @return QName as token
   */
  public static byte[] eqName(final byte[] uri, final byte[] local) {
    return concat(QueryText.EQNAME, uri, CURLY2, local);
  }

  /**
   * Returns an EQName representation.
   * @param uri URI
   * @param local local name
   * @return QName as token
   */
  public static String eqName(final String uri, final String local) {
    return Strings.concat(QueryText.EQNAME, uri, CURLY2, local);
  }

  /**
   * Constructs an internal string representation for the components of a QName.
   * @param prefix prefix
   * @param local name (can be {@code null})
   * @param uri uri (can be {@code null})
   * @return EQName representation
   */
  private static byte[] internal(final byte[] prefix, final byte[] local, final byte[] uri) {
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
      Array.copyFromStart(uri, ul, key, i);
      key[i + ul] = '}';
      i += ul + 1;
    }
    if(pl != 0) {
      Array.copyFromStart(prefix, pl, key, i);
      key[i + pl] = ':';
      i += pl + 1;
    }
    Array.copyFromStart(local, local.length, key, i);
    return key;
  }
}
