package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.item.FAttr;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.iter.Iter;
import static org.basex.query.util.Err.*;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Attribute fragment.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class CAttr extends CFrag {
  /** Generated namespace. */
  private static final byte[] NS0 = token("ns0:");
  /** Tag name. */
  private Expr atn;
  /** Computed constructor. */
  private final boolean comp;

  /**
   * Constructor.
   * @param ii input info
   * @param c computed construction flag
   * @param n name
   * @param v attribute values
   */
  public CAttr(final InputInfo ii, final boolean c, final Expr n,
      final Expr... v) {
    super(ii, v);
    atn = n;
    comp = c;
  }

  @Override
  public CAttr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    atn = checkUp(atn, ctx).comp(ctx);
    return this;
  }

  @Override
  public FAttr item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    QNm name = qname(ctx, checkItem(atn, ctx), true, ii);
    final byte[] pre = name.pref();
    final byte[] ln = name.ln();

    if(comp) {
      final byte[] uri = name.uri().string();
      if(eq(pre, XMLNS) || eq(ln, XMLNS) || eq(uri, XMLNSURI) ||
          eq(pre, XML) ^ eq(uri, XMLURI)) CAINS.thrw(input, uri, ln);

      if(eq(pre, EMPTY) && !eq(uri, EMPTY)) {
        // create new standard namespace to cover most frequent cases
        name = new QNm(concat(NS0, name.string()), uri);
      }
    }

    final TokenBuilder tb = new TokenBuilder();
    for(final Expr e : expr) add(tb, ctx.iter(e), ii);
    byte[] val = tb.finish();
    if(eq(pre, XML) && eq(ln, ID)) val = norm(val);

    return new FAttr(name, val);
  }

  /**
   * Adds the atomized value of an item to a token builder.
   * @param tb token builder
   * @param ir iterator
   * @param ii input info
   * @throws QueryException query exception
   */
  static void add(final TokenBuilder tb, final Iter ir, final InputInfo ii)
      throws QueryException {
    boolean m = false;
    for(Item it; (it = ir.next()) != null;) {
      if(m) tb.add(' ');
      tb.add(it.string(ii));
      m = true;
    }
  }

  @Override
  public Expr remove(final Var v) {
    atn = atn.remove(v);
    return super.remove(v);
  }

  @Override
  public boolean uses(final Use u) {
    return atn.uses(u) || super.uses(u);
  }

  @Override
  public int count(final Var v) {
    return atn.count(v) + super.count(v);
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
    return info(QueryText.ATTRIBUTE);
  }

  @Override
  public String toString() {
    return toString(Token.string(NodeType.ATT.string()) + " { " + atn + " }");
  }
}
