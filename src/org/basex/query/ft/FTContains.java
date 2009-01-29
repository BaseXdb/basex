package org.basex.query.ft;

import static org.basex.query.QueryText.*;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.index.FTTokenizer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.CmpG;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Return;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.path.AxisPath;
import org.basex.query.path.Step;
import org.basex.query.util.Scoring;
import org.basex.query.util.Var;
import org.basex.util.IntList;

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
  /** Flag for first evaluation.*/
  private int div = 0;
  
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
  public Expr comp(final QueryContext ctx) throws QueryException {
    expr = expr.comp(ctx).addText(ctx);
    ftexpr = ftexpr.comp(ctx);
    
    Expr e = this;
    if(expr.e()) e = Bln.FALSE;
    if(e != this) ctx.compInfo(OPTPRE, this);
    return e;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {    
    if (div == 0) 
      div = ++ctx.ftcount;

    final Iter iter = expr.iter(ctx);
    final FTTokenizer tmp = ctx.ftitem;
    final IntList[] ftd = ctx.ftd;
    int pre = -1;
    double d = 0;
    Item i;
    ctx.ftitem = ft;
    while((i = iter.next()) != null) {
      ft.init(i.str());
      d = Scoring.and(d, ftexpr.iter(ctx).next().score());
      if(i instanceof DBNode) pre = ((DBNode) i).pre;
    }
    
    if (Bln.get(d).bool() && pre > -1 && ctx.ftd != null && ctx.ftdata != null) 
      ctx.ftdata.addConvSeqData(ctx.ftd, pre, div);      
    
    ctx.ftitem = tmp;
    ctx.ftd = ftd;
    return Bln.get(d).iter();
  }

  @Override
  public void indexAccessible(final QueryContext ctx, final IndexContext ic)
      throws QueryException {

    // return if step is no text node, or if no index is available
    ic.iu = false;
    final Step s = CmpG.indexStep(expr);
    if(s == null || s.test.type != Type.TXT || !ic.data.meta.ftxindex) return;
    
    ftexpr.indexAccessible(ctx, ic);
  }
  
  @Override
  public Expr indexEquivalent(final QueryContext ctx, final IndexContext ic)
      throws QueryException {

    if(!ic.data.meta.ftxindex) return this;
    
    final FTExpr ae = ftexpr.indexEquivalent(ctx, ic);
    // sequential evaluation with index access
    if(ic.seq) return new FTContainsSIndex(expr, ae);

    // standard index evaluation; first expression will always be an axis path
    ctx.compInfo(OPTFTXINDEX);
    final Expr root = new FTContainsIndex(ae, ft);
    return ((AxisPath) expr).invertPath(root, ic.step);
  }
  
  @Override
  public boolean usesPos(final QueryContext ctx) {
    return expr.usesPos(ctx) || ftexpr.usesPos(ctx);
  }

  @Override
  public int countVar(final Var v) {
    return expr.countVar(v) + ftexpr.countVar(v);
  }

  @Override
  public Expr removeVar(final Var v) {
    expr = expr.removeVar(v);
    ftexpr = ftexpr.removeVar(v);
    return this;
  }

  @Override
  public Return returned(final QueryContext ctx) {
    return Return.BLN;
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
