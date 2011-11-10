package org.basex.query.item;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import javax.xml.namespace.QName;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.XMLToken;

/**
 * QName item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class QNm extends Item {
  /** URI. */
  private Uri uri;
  /** Local part and optional prefix. */
  private byte[] val;
  /** Namespace index. */
  private int ns;

  /**
   * Empty constructor.
   */
  public QNm() {
    super(AtomType.QNM);
    val = EMPTY;
  }

  /**
   * Constructor.
   * @param n name
   */
  public QNm(final byte[] n) {
    this();
    name(n);
  }

  /**
   * Constructor.
   * @param n name
   * @param ctx query context
   * @param ii input info
   * @throws QueryException query exception
   */
  public QNm(final byte[] n, final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    this(n);
    if(!XMLToken.isQName(val)) Err.value(ii, type, val);
    if(ns()) uri(ctx.ns.uri(pref(), false, ii));
  }

  /**
   * Constructor.
   * @param n name
   * @param u uri
   */
  public QNm(final byte[] n, final Uri u) {
    this(n);
    uri(u);
  }

  /**
   * Constructor.
   * @param n name
   * @param u uri
   */
  public QNm(final byte[] n, final byte[] u) {
    this(n);
    uri(u);
  }

  /**
   * Convenience method for converting a Java QName.
   * @param qn qname
   */
  public QNm(final QName qn) {
    this(token(qname(qn)), token(qn.getNamespaceURI()));
  }

  /**
   * Sets the URI of this QName.
   * @param u the uri to set
   */
  public void uri(final Uri u) {
    uri = u;
  }

  /**
   * Sets the URI of this QName.
   * @param u the uri to set
   */
  public void uri(final byte[] u) {
    uri(Uri.uri(u));
  }

  /**
   * Returns the URI of this QName.
   * @return the uri
   */
  public Uri uri() {
    return uri == null ? Uri.EMPTY : uri;
  }

  /**
   * Checks if the URI of this QName has been explicitly set.
   * @return {@code true} if it has been set, {@code false} otherwise
   */
  public boolean hasUri() {
    return uri != null;
  }

  /**
   * Converts the specified QName to a string.
   * @param qn qname
   * @return string
   */
  private static String qname(final QName qn) {
    final String name = qn.getLocalPart();
    final String pre = qn.getPrefix();
    return !pre.isEmpty() ? pre + ":" + name : name;
  }

  /**
   * Sets the name.
   * @param nm name
   */
  public void name(final byte[] nm) {
    val = nm;
    ns = indexOf(val, ':');
  }

  @Override
  public byte[] atom(final InputInfo ii) {
    return val;
  }

  /**
   * Returns an atomized string.
   * @return Returns an atomized string.
   */
  public byte[] atom() {
    return val;
  }

  @Override
  public boolean bool(final InputInfo ii) throws QueryException {
    throw CONDTYPE.thrw(ii, type, this);
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) {
    // at this stage, item will always be of the same type
    return eq((QNm) it);
  }

  /**
   * Compares the specified item.
   * @param n name to be compared
   * @return result of check
   */
  public boolean eq(final QNm n) {
    return n == this || Token.eq(ln(), n.ln()) && uri().eq(n.uri());
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    throw Err.diff(ii, it, this);
  }

  /**
   * Checks if the name contains a prefix.
   * @return result of check
   */
  public boolean ns() {
    return ns != -1;
  }

  /**
   * Returns the prefix.
   * @return prefix
   */
  public byte[] pref() {
    return ns == -1 ? EMPTY : substring(val, 0, ns);
  }

  /**
   * Returns the local name.
   * @return local name
   */
  public byte[] ln() {
    return ns == -1 ? val : substring(val, ns + 1);
  }

  @Override
  public QName toJava() {
    return new QName(Token.string(uri().atom()), Token.string(ln()),
        Token.string(pref()));
  }

  /**
   * Returns the Clark notation, represented by the URI in curly braces
   * and the local name.
   * @return full name
   */
  public byte[] full() {
    return new TokenBuilder().add('{').add(uri().atom()).add('}').
      add(ln()).finish();
  }

  @Override
  public int hash(final InputInfo ii) throws QueryException {
    return Token.hash(ln());
  }

  @Override
  public String toString() {
    return Util.info("\"%\"", val);
  }
}
