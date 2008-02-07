package org.basex.query.xquery.path;

import static org.basex.query.xquery.XQText.*;
import static org.basex.util.Token.*;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Node;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.item.Uri;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;

/**
 * XQuery Node Tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Test {
  /** Static node test. */
  public static final byte[] WILD = { '*' };
  /** Static node test. */
  public static final Test NODE = new Test(Type.NOD);
  /** Name test. */
  public QNm name;
  /** Local name. */
  public byte[] ln;
  /** Prefix flag. */
  public boolean pre;
  /** Node test. */
  public Type type;
  /** Wildcard flag. */
  public boolean wild;

  /**
   * Empty Constructor ("*" test).
   */
  public Test() { }

  /**
   * Constructor.
   * @param t node type
   */
  public Test(final Type t) {
    type = t;
  }

  /**
   * Constructor.
   * @param t node type
   * @param ext type extension
   * @param ctx xquery context
   * @throws XQException evaluation exception
   */
  public Test(final Type t, final byte[] ext, final XQContext ctx)
      throws XQException {
    
    type = t;
    if(ext.length != 0) {
      if(type == Type.ITEM || type == Type.NOD || type == Type.COM ||
          type == Type.TXT || type == Type.DOC) Err.or(TESTINVALID, type, ext);

      if((type == Type.ELM || type == Type.ATT) && eq(ext, WILD)) return;

      byte[] nm = delete(delete(ext, '\''), '"');
      final int i = indexOf(nm, ',');
      if(i != -1) {
        final QNm test = new QNm(trim(substring(nm, i + 1)), ctx);
        // <CG> XQuery/Node Test: check extended info
        if(Type.find(test, false) == null) Err.or(TESTINVALID, type, test);
        nm = substring(nm, 0, i);
      }
      name = new QNm(nm, ctx);
    }
  }

  /**
   * Constructor.
   * @param n name
   * @param ctx xquery context
   * @param w wildcard flag
   * @throws XQException evaluation exception
   */
  public Test(final byte[] n, final boolean w, final XQContext ctx)
      throws XQException {
    
    name = new QNm(n);
    name.check(ctx);
    //if(!name.scan()) Err.value(Type.QNM, name);
    ln = name.ln();
    pre = !Token.eq(ln, name.str());
    wild = w;
  }
  
  /** Temporary QName. */
  private QNm qname = new QNm(Token.EMPTY);

  /**
   * Tests the specified node.
   * @param tmp temporary node
   * @param ctx xquery context
   * @return result of check
   * @throws XQException evaluation exception
   */
  public boolean e(final Node tmp, final XQContext ctx) throws XQException {
    if(this == NODE) return true;

    if(type == null) {
      if(tmp.type != Type.ELM && tmp.type != Type.ATT) return false;
      if(name == null) return true;

      if(wild) {
        final byte[] nm = tmp.nname();
        return Token.eq(ln, Token.substring(nm, Token.indexOf(nm, ':') + 1));
      }

      final QNm nm = tmp.qname(qname);
      if(!pre && !nm.ns()) return Token.eq(nm.str(), ln);
      ctx.ns.uri(nm);
      return Token.eq(ln, nm.ln()) && (name.uri.eq(nm.uri) ||
        name.uri == Uri.EMPTY && nm.uri == Uri.XMLNS);
    }

    return tmp.type != type ? false : name == null ||
        tmp.qname(qname).eq(name);
  }

  @Override
  public String toString() {
    return type != null ? type.toString() + "()" : name != null ?
        Token.string(name.str()) : "*";
  }
}
