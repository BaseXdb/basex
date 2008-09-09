package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import javax.xml.namespace.QName;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;
import org.basex.util.XMLToken;

/**
 * QName item.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
   * Constructor.
   * @param n name
   * @param ctx xquery context
   * @throws XQException query exception
   */
  public QNm(final byte[] n, final XQContext ctx) throws XQException {
    this(n);
    if(!XMLToken.isQName(val)) Err.value(type, val);
    if(ns()) uri = ctx.ns.uri(pre());
  }

  /**
   * Constructor.
   * @param q quick name
   * @param u uri
   */
  public QNm(final byte[] q, final Uri u) {
    this(q);
    uri = u;
  }
  
  /**
   * Constructor.
   * @param q quick name
   */
  public QNm(final byte[] q) {
    super(Type.QNM);
    name(q);
  }
  
  /**
   * Sets the name.
   * @param nm name
   */
  public void name(final byte[] nm) {
    val = nm;
    ns = Token.indexOf(val, ':');
  }

  @Override
  public byte[] str() {
    return val;
  }

  @Override
  public boolean bool() throws XQException {
    Err.or(CONDTYPE, type, this);
    return false;
  }

  @Override
  public boolean eq(final Item it) throws XQException {
    if(it.type != type) Err.cmp(this, it);
    return eq((QNm) it);
  }

  /**
   * Compares the specified item.
   * @param n name to be compared
   * @return result of check
   */
  public boolean eq(final QNm n) {
    return Token.eq(ln(), n.ln()) && uri.eq(n.uri);
  }

  @Override
  public int diff(final Item it) throws XQException {
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
    return ns == -1 ? Token.EMPTY : Token.substring(val, 0, ns);
  }
  
  /**
   * Returns the local name.
   * @return local name
   */
  public byte[] ln() {
    return ns == -1 ? val : Token.substring(val, ns + 1);
  }

  /**
   * Checks the validity of the QName.
   * @param ctx xquery context
   * @throws XQException query exception
   */
  public void check(final XQContext ctx) throws XQException {
    if(ns()) uri = ctx.ns.uri(pre());
  }

  @Override
  public String toString() {
    return "\"" + Token.string(val) + "\"";
  }
  
  /**
   * Converts this qname to a Java QName.
   * @return QName
   */
  public QName toQName() {
    return new QName(Token.string(uri.str()),
        Token.string(ln()), Token.string(pre()));
  }
}
