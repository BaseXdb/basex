package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.FAttr;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.item.Uri;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.util.TokenBuilder;
import org.basex.util.XMLToken;

/**
 * Attribute fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CAttr extends Arr {
  /** Tag name. */
  private Expr atn;
  /** Computed constructor. */
  private final boolean comp;

  /**
   * Constructor.
   * @param n name
   * @param v attribute values
   * @param c computed construction flag
   */
  public CAttr(final Expr n, final Expr[] v, final boolean c) {
    super(v);
    atn = n;
    comp = c;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);
    atn = ctx.comp(atn);
    return this;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final QNm name = name(ctx, ctx.atomic(atn, this, false));
    final byte[] pre = name.pre();
    final byte[] ln = name.ln();
    if(comp && (eq(name.str(), XMLNS) || eq(pre, XMLNS))) Err.or(NSATTCONS);

    final TokenBuilder tb = new TokenBuilder();
    for(final Expr e : expr) CText.add(tb, ctx.iter(e));
    byte[] val = tb.finish();
    if(eq(pre, XML) && eq(ln, ID)) val = norm(val);

    return new FAttr(name, val, null).iter();
  }

  /**
   * Returns an updated name expression.
   * @param ctx query context
   * @param i item
   * @return result
   * @throws XQException query exception
   */
  public static QNm name(final XQContext ctx, final Item i) throws XQException {
    QNm name = null;
    if(i.type == Type.QNM) {
      name = (QNm) i;
    } else {
      final byte[] nm = i.str();
      if(contains(nm, ' ')) Err.or(INVAL, nm);
      if(!XMLToken.isQName(nm)) Err.or(NAMEWRONG, nm);
      name = new QNm(nm);
    }

    if(name.uri == Uri.EMPTY) name.uri = Uri.uri(ctx.ns.uri(name.pre()));
    return name;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NS, timer());
    ser.openElement(NAME);
    atn.plan(ser);
    ser.closeElement();
    ser.openElement(VALUE);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
    ser.closeElement();
  }

  @Override
  public String color() {
    return "FF3333";
  }

  @Override
  public String info() {
    return "Attribute constructor";
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("attribute " + atn + " { ");
    sb.append(toString(", "));
    return sb.append(" }").toString();
  }
}
