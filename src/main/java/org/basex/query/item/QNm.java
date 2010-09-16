package org.basex.query.item;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import javax.xml.namespace.QName;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.XMLToken;

/**
 * QName item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class QNm extends Item {
  /** URI. */
  private Uri uri;
  /** String data. */
  private byte[] val;
  /** Namespace index. */
  private int ns;

  /**
   * Empty constructor.
   */
  public QNm() {
    super(Type.QNM);
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
  void name(final byte[] nm) {
    val = nm;
    ns = indexOf(val, ':');
  }

  @Override
  public byte[] atom() {
    return val;
  }

  @Override
  public boolean bool(final InputInfo ii) throws QueryException {
    CONDTYPE.thrw(ii, type, this);
    return false;
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
    Err.diff(ii, it, this);
    return 0;
  }

  /**
   * Checks if the name contains a namespace.
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

  @Override
  public boolean equals(final Object o) {
    return o instanceof QNm && eq((QNm) o);
  }

  @Override
  public SeqType type() {
    return SeqType.QNM;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public String toString() {
    return "\"" + Token.string(val) + "\"";
  }
}
