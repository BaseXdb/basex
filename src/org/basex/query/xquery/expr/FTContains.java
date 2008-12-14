package org.basex.query.xquery.expr;

import org.basex.index.FTTokenizer;
import org.basex.query.xquery.FTIndexAcsbl;
import org.basex.query.xquery.FTIndexEq;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQOptimizer;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.path.AxisPath;
import org.basex.query.xquery.path.Step;
import org.basex.query.xquery.util.Scoring;

/**
 * FTContains expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class FTContains extends Arr {
  /** Fulltext parser. */
  public FTTokenizer ft = new FTTokenizer();
  
  /**
   * Constructor.
   * @param ex contains, select and optional ignore expression
   */
  public FTContains(final Expr... ex) {
    super(ex);
  }
  
  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);
    expr[0] = XQOptimizer.addText(expr[0], ctx);
    return this;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {    
    final Iter iter = ctx.iter(expr[0]);
    final FTTokenizer tmp = ctx.ftitem;

    double d = 0;
    Item i;
    ctx.ftitem = ft;
    while((i = iter.next()) != null) {
      ft.init(i.str());
      final Item it = ctx.iter(expr[1]).next();
      d = Scoring.and(d, it.dbl());
    }
    ctx.ftitem = tmp;
    return Bln.get(d).iter();
  }

  /*
   * Checks if there is anything to sum up.
   * @param d data
   * @return boolean sum up
  public boolean sumUp(final Data d) {
    return expr[0] instanceof AxisPath && ((AxisPath) expr[0]).sumUp(d);
  }
   */

  @Override
  public void indexAccessible(final XQContext ctx, final FTIndexAcsbl ia)
      throws XQException {

    // return if step is no text node, or if no index is available
    final Step s = CmpG.indexStep(expr);
    if(s == null || s.test.type != Type.TXT || !ia.data.meta.ftxindex) return;
    
    expr[1].indexAccessible(ctx, ia);
  }
  
  @Override
  public Expr indexEquivalent(final XQContext ctx, final FTIndexEq ieq)
    throws XQException {

    final Expr ae = expr[1].indexEquivalent(ctx, ieq);
    // sequential evaluation with index access
    if(ieq.seq) return new FTContainsSIndex(expr[0], ae);

    // standard index evaluation; first expression will always be an axis path
    final Expr ex = new FTContainsIndex(ae, ft);
    return ((AxisPath) expr[0]).invertPath(ex, ieq.curr);
  }
  
  @Override
  public Type returned() {
    return Type.BLN;
  }

  @Override
  public String color() {
    return "33CC33";
  }

  @Override
  public String toString() {
    return toString(" ftcontains ");
  }
}
