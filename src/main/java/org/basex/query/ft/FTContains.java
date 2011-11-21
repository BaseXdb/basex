package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.CmpG;
import org.basex.query.expr.Context;
import org.basex.query.expr.Expr;
import org.basex.query.expr.ParseExpr;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.FTNode;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.path.AxisPath;
import org.basex.query.path.AxisStep;
import org.basex.query.util.IndexContext;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.FTOpt;
import org.basex.util.ft.Scoring;

/**
 * FTContains expression.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class FTContains extends ParseExpr {
  /** Expression. */
  Expr expr;
  /** Full-text expression. */
  FTExpr ftexpr;
  /** Full-text parser. */
  FTLexer lex;

  /**
   * Constructor.
   * @param e expression
   * @param fte full-text expression
   * @param ii input info
   */
  public FTContains(final Expr e, final FTExpr fte, final InputInfo ii) {
    super(ii);
    expr = e;
    ftexpr = fte;
    type = SeqType.BLN;
  }

  @Override
  public final Expr comp(final QueryContext ctx) throws QueryException {
    expr = checkUp(expr, ctx).comp(ctx).addText(ctx);
    lex = new FTLexer(new FTOpt());
    ftexpr = ftexpr.comp(ctx);
    return expr.empty() ? optPre(Bln.FALSE, ctx) : this;
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Iter iter = expr.iter(ctx);
    final FTLexer tmp = ctx.fttoken;
    double s = 0;

    ctx.fttoken = lex;
    for(Item it; (it = iter.next()) != null;) {
      lex.init(it.string(input));
      final FTNode item = ftexpr.item(ctx, input);
      double d = 0;
      if(item.all.matches()) {
        d = item.score();
        // no scoring found - use default value
        if(d == 0) d = 1;
      }
      s = Scoring.and(s, d);

      // add entry to visualization
      if(d > 0 && ctx.ftpos != null && it instanceof DBNode) {
        final DBNode node = (DBNode) it;
        ctx.ftpos.add(node.data, node.pre, item.all);
      }
    }

    ctx.fttoken = tmp;
    return Bln.get(s);
  }

  @Override
  public final boolean indexAccessible(final IndexContext ic)
      throws QueryException {

    // return if step is no text node, or if no index is available
    final AxisStep s = expr instanceof Context ? ic.step : CmpG.indexStep(expr);
    final boolean ok = s != null && ic.data.meta.ftindex &&
      s.test.type == NodeType.TXT && ftexpr.indexAccessible(ic);
    ic.seq |= ic.not;
    return ok;
  }

  @Override
  public final Expr indexEquivalent(final IndexContext ic)
      throws QueryException {

    ic.ctx.compInfo(OPTFTXINDEX);

    // sequential evaluation with index access
    final FTExpr ie = ftexpr.indexEquivalent(ic);
    if(ic.seq) return new FTContainsIndex(input, expr, ie, ic);

    // standard index evaluation; first expression will always be an axis path
    final FTIndexAccess root = new FTIndexAccess(input, ie, ic);
    return expr instanceof Context ? root :
      ((AxisPath) expr).invertPath(root, ic.step);
  }

  @Override
  public final boolean uses(final Use u) {
    return expr.uses(u) || ftexpr.uses(u);
  }

  @Override
  public final int count(final Var v) {
    return expr.count(v) + ftexpr.count(v);
  }

  @Override
  public final boolean removable(final Var v) {
    return expr.removable(v) && ftexpr.removable(v);
  }

  @Override
  public final Expr remove(final Var v) {
    expr = expr.remove(v);
    ftexpr = ftexpr.remove(v);
    return this;
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr.plan(ser);
    ftexpr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return expr + " " + CONTAINS + " " + TEXT + " " + ftexpr;
  }
}
