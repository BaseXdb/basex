package org.basex.query.item;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import javax.xml.namespace.QName;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.Token;
import org.basex.util.XMLToken;

/**
 * QName item.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class QNm extends Item {
  /** URI. */
  public Uri uri = Uri.EMPTY;
  /** String data. */
  private byte[] val;
  /** Namespace index. */
  private int ns;
  
  /**
   * Empty Constructor.
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
   * @throws QueryException query exception
   */
  public QNm(final byte[] n, final QueryContext ctx) throws QueryException {
    this(n);
    if(!XMLToken.isQName(val)) Err.value(type, val);
    if(ns()) uri = Uri.uri(ctx.ns.uri(pre()));
  }

  /**
   * Constructor.
   * @param n name
   * @param u uri
   */
  public QNm(final byte[] n, final Uri u) {
    this(n);
    uri = u;
  }
  
  /**
   * Convenient method for converting a Java QName to a project specific one.
   * @param qn qname
   */
  public QNm(final QName qn) {
    this(token(qname(qn)), Uri.uri(token(qn.getNamespaceURI())));
  }
  
  /**
   * Converts the specified QName to a string.
   * @param qn qname
   * @return string
   */
  private static String qname(final QName qn) {
    final String name = qn.getLocalPart();
    final String pre = qn.getPrefix();
    return pre.length() != 0 ?  pre + ":" + name : name;
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
  public byte[] str() {
    return val;
  }

  @Override
  public boolean bool() throws QueryException {
    Err.or(CONDTYPE, type, this);
    return false;
  }

  @Override
  public boolean eq(final Item it) throws QueryException {
    if(it.type != type) Err.cmp(this, it);
    return eq((QNm) it);
  }

  /**
   * Compares the specified item.
   * @param n name to be compared
   * @return result of check
   */
  public boolean eq(final QNm n) {
    return n == this || Token.eq(ln(), n.ln()) && uri.eq(n.uri);
  }

  @Override
  public int diff(final Item it) throws QueryException {
    Err.cmp(it, this);
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
  public byte[] pre() {
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
  public QName java() {
    return new QName(string(uri.str()), string(ln()), string(pre()));
  }

  @Override
  public String toString() {
    return "\"" + string(val) + "\"";
  }
}
