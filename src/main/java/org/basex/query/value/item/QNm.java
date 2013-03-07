package org.basex.query.value.item;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import javax.xml.namespace.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * QName item ({@code xs:QName}).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class QNm extends Item {
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
   * @param n name
   */
  public QNm(final byte[] n) {
    super(AtomType.QNM);
    name = n;
    pref = indexOf(n, ':');
  }

  /**
   * Constructor.
   * @param n name
   */
  public QNm(final String n) {
    this(token(n));
  }

  /**
   * Constructor.
   * @param n name
   * @param u namespace URI
   */
  public QNm(final byte[] n, final byte[] u) {
    this(n);
    uri(u);
  }

  /**
   * Constructor.
   * @param n name
   * @param u namespace URI
   */
  public QNm(final String n, final byte[] u) {
    this(token(n), u);
  }

  /**
   * Constructor.
   * @param n name
   * @param u namespace URI
   */
  public QNm(final String n, final String u) {
    this(token(n), token(u));
  }

  /**
   * Constructor, binding a statically known namespace.
   * If no namespace is found, the namespace uri is set to {@code null}.
   * @param n name
   * @param ctx query context
   */
  public QNm(final byte[] n, final QueryContext ctx) {
    this(n);
    uri(ctx.sc.ns.uri(prefix()));
  }

  /**
   * Constructor for converting a Java QName.
   * @param qn qname
   */
  public QNm(final QName qn) {
    this(token(qn.getPrefix().isEmpty() ? qn.getLocalPart() :
      qn.getPrefix() + ':' + qn.getLocalPart()), token(qn.getNamespaceURI()));
  }

  /**
   * Sets the URI of this QName.
   * @param u the uri to be set
   */
  public void uri(final byte[] u) {
    uri = u == null ? null : norm(u);
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
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    if(it instanceof QNm) return eq((QNm) it);
    throw XPTYPECMP.thrw(ii, it.type, type);
  }

  /**
   * Compares the specified item.
   * @param n name to be compared
   * @return result of check
   */
  public boolean eq(final QNm n) {
    return n == this || Token.eq(local(), n.local()) && Token.eq(uri(), n.uri());
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    throw Err.diff(ii, it, this);
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
  public void update(final byte[] n, final byte[] u) {
    name = n;
    pref = indexOf(name, ':');
    uri = u;
  }

  /**
   * Returns a unique representation of the QName:
   * <ul>
   * <li> if a URI exists, the EQName notation is used.</li>
   * <li> otherwise, if a prefix exists, the prefix and local name is returned.</li>
   * <li> otherwise, the local name is returned.</li>
   * </ul>
   * @return unique representation
   */
  public byte[] id() {
    final byte[] u = uri();
    final byte[] p = prefix();
    final byte[] l = local();
    return u.length == 0 ? p.length == 0 ? l : name :
        new TokenBuilder().add('Q').add('{').add(u).add('}').add(local()).finish();
  }

  @Override
  public QName toJava() {
    return new QName(Token.string(uri()), Token.string(local()), Token.string(prefix()));
  }

  @Override
  public int hash(final InputInfo ii) throws QueryException {
    return Token.hash(local());
  }

  @Override
  public byte[] xdmInfo() {
    return new ByteList().add(typeId().asByte()).add(uri()).add(0).toArray();
  }

  @Override
  public String toString() {
    return Token.string(id());
  }

  @Override
  public boolean equals(final Object obj) {
    return obj.getClass() == QNm.class && eq((QNm) obj);
  }

  @Override
  public int hashCode() {
    return Token.hash(id());
  }
}
