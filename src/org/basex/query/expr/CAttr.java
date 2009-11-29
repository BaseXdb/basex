package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FAttr;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.util.Err;
import org.basex.query.util.Var;
import org.basex.util.TokenBuilder;
import org.basex.util.XMLToken;

/**
 * Attribute fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class CAttr extends CFrag {
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
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    atn = checkUp(atn, ctx).comp(ctx);
    return this;
  }

  @Override
  public FAttr atomic(final QueryContext ctx) throws QueryException {
    final Item it = atn.atomic(ctx);
    if(it == null) Err.empty(this);
    final QNm name = name(ctx, it);
    final byte[] pre = name.pref();
    final byte[] ln = name.ln();
    if(comp && (eq(name.str(), XMLNS) || eq(pre, XMLNS))) Err.or(NSATTCONS);

    final TokenBuilder tb = new TokenBuilder();
    for(final Expr e : expr) CText.add(tb, ctx.iter(e));
    byte[] val = tb.finish();
    if(eq(pre, XML) && eq(ln, ID)) val = norm(val);

    return new FAttr(name, val, null);
  }

  /**
   * Returns an updated name expression.
   * @param ctx query context
   * @param i item
   * @return result
   * @throws QueryException query exception
   */
  static QNm name(final QueryContext ctx, final Item i) throws QueryException {
    QNm name = null;
    if(i.type == Type.QNM) {
      name = (QNm) i;
    } else {
      final byte[] nm = i.str();
      if(contains(nm, ' ')) Err.or(INVAL, nm);
      if(!XMLToken.isQName(nm)) Err.or(NAMEWRONG, nm);
      name = new QNm(nm);
    }

    if(name.uri == Uri.EMPTY) name.uri = Uri.uri(ctx.ns.uri(name.pref(),
        name != i));
    return name;
  }

  @Override
  public Expr remove(final Var v) {
    atn = atn.remove(v);
    return super.remove(v);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    ser.openElement(NAME);
    atn.plan(ser);
    ser.closeElement();
    ser.openElement(VALUE);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
    ser.closeElement();
  }

  @Override
  public String info() {
    return "attribute constructor";
  }

  @Override
  public String toString() {
    return toString(Type.ATT + " " + atn);
  }
}
