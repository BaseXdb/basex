package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import org.basex.index.FTTokenizer;
import org.basex.query.xquery.IndexContext;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
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
    
    final Expr e1 = expr[0];
    final Expr e2 = expr[1];
    if(e1 instanceof AxisPath) ((AxisPath) e1).addText(ctx);
    
    if(e1.e() || e2.e()) {
      ctx.compInfo(OPTSIMPLE, this, Bln.FALSE);
      return Bln.FALSE;
    }

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

  @Override
  public void indexAccessible(final XQContext ctx, final IndexContext ic)
      throws XQException {

    // return if step is no text node, or if no index is available
    final Step s = CmpG.indexStep(expr);
    if(s == null || s.test.type != Type.TXT || !ic.data.meta.ftxindex) return;
    
    ic.iu = false;
    expr[1].indexAccessible(ctx, ic);
  }
  
  @Override
  public Expr indexEquivalent(final XQContext ctx, final IndexContext ic)
    throws XQException {

    final Expr ae = expr[1].indexEquivalent(ctx, ic);
    // sequential evaluation with index access
    if(ic.seq) return new FTContainsSIndex(expr[0], ae);

    // standard index evaluation; first expression will always be an axis path
    ctx.compInfo(OPTFTXINDEX);
    final Expr root = new FTContainsIndex(ae, ft);
    return ((AxisPath) expr[0]).invertPath(root, ic.step);
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
