package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import java.io.IOException;
import org.basex.data.Serializer;
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
import org.basex.query.xquery.util.Var;

/**
 * FTContains expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class FTContains extends Expr {
  /** Expression. */
  public Expr expr;
  /** Fulltext expression. */
  public FTExpr ftexpr;
  /** Fulltext parser. */
  public FTTokenizer ft = new FTTokenizer();
  
  /**
   * Constructor.
   * @param e expression
   * @param fte fulltext expression
   */
  public FTContains(final Expr e, final FTExpr fte) {
    expr = e;
    ftexpr = fte;
  }
  
  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    expr = expr.comp(ctx);
    ftexpr = ftexpr.comp(ctx);
    if(expr instanceof AxisPath) ((AxisPath) expr).addText(ctx);
    
    Expr e = this;
    if(expr.e()) e = Bln.FALSE;
    if(e != this) ctx.compInfo(OPTPRE, this);
    return e;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {    
    final Iter iter = expr.iter(ctx);
    final FTTokenizer tmp = ctx.ftitem;

    double d = 0;
    Item i;
    ctx.ftitem = ft;
    while((i = iter.next()) != null) {
      ft.init(i.str());
      d = Scoring.and(d, ftexpr.iter(ctx).next().score());
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
    ftexpr.indexAccessible(ctx, ic);
  }
  
  @Override
  public Expr indexEquivalent(final XQContext ctx, final IndexContext ic)
    throws XQException {

    final FTExpr ae = ftexpr.indexEquivalent(ctx, ic);
    // sequential evaluation with index access
    if(ic.seq) return new FTContainsSIndex(expr, ae);

    // standard index evaluation; first expression will always be an axis path
    ctx.compInfo(OPTFTXINDEX);
    final Expr root = new FTContainsIndex(ae, ft);
    return ((AxisPath) expr).invertPath(root, ic.step);
  }
  
  @Override
  public boolean usesPos() {
    return expr.usesPos() || ftexpr.usesPos();
  }

  @Override
  public boolean usesVar(final Var v) {
    return expr.usesVar(v) || ftexpr.usesVar(v);
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
    return expr + " ftcontains " + ftexpr;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr.plan(ser);
    ftexpr.plan(ser);
    ser.closeElement();
  }
}
