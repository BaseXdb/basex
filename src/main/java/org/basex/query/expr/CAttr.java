package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.item.FAttr;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.util.Err;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Attribute fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class CAttr extends CFrag {
  /** Tag name. */
  private Expr atn;
  /** Computed constructor. */
  private final boolean comp;

  /**
   * Constructor.
   * @param ii input info
   * @param n name
   * @param v attribute values
   * @param c computed construction flag
   */
  public CAttr(final InputInfo ii, final Expr n, final Expr[] v,
      final boolean c) {
    super(ii, v);
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
    final QNm name = qname(ctx, checkItem(atn, ctx));
    if(!name.ns()) name.uri = Uri.EMPTY;
    final byte[] pre = name.pref();
    final byte[] ln = name.ln();
    if(comp && (eq(name.atom(), XMLNS) || eq(pre, XMLNS)))
      Err.or(input, NSATTCONS);

    final TokenBuilder tb = new TokenBuilder();
    for(final Expr e : expr) CText.add(tb, ctx.iter(e));
    byte[] val = tb.finish();
    if(eq(pre, XML) && eq(ln, ID)) val = norm(val);

    return new FAttr(name, val, null);
  }

  @Override
  public Expr remove(final Var v) {
    atn = atn.remove(v);
    return super.remove(v);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    atn.plan(ser);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String desc() {
    return info(QueryTokens.ATTRIBUTE);
  }

  @Override
  public String toString() {
    return toString(Type.ATT.name + " { " + atn + " }");
  }
}
