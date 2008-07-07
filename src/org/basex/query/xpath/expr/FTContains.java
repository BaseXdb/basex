package org.basex.query.xpath.expr;

import static org.basex.query.xpath.XPText.*;
import org.basex.data.MetaData;
import org.basex.data.Serializer;
import org.basex.query.FTOpt;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.XPOptimizer;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.LocPathRel;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.locpath.TestNode;
import org.basex.query.xpath.values.Bool;
import org.basex.query.xpath.values.Item;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.FTTokenizer;

/**
 * FTContains Expression; used for fulltext operations.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTContains extends DualExpr {
  /** Fulltext parser. */
  private final FTTokenizer ft = new FTTokenizer();
  /** FullText options (currently not used). */
  FTOpt option;
 
  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   */
  public FTContains(final Expr e1, final FTArrayExpr e2) {
    super(e1, e2);
  }
  
  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   * @param opt fulltext options
   */
  public FTContains(final Expr e1, final Expr e2, final FTOpt opt) {
    super(e1, e2);
    option = opt;
  }

  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    expr1 = expr1.compile(ctx);
    expr2 = expr2.compile(ctx);
    XPOptimizer.addText(expr1, ctx);
    return this;
  }

  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    final FTTokenizer tmp = ctx.ftitem;
    ctx.ftitem = ft;
    
    final Item v1 = ctx.eval(expr1);
    if(v1.size() == 0) return Bool.FALSE;

    boolean res = false;
    if(v1 instanceof NodeSet) {
      NodeSet ns = (NodeSet) v1;
      for(int i = 0; i < ns.size; i++) {
        ft.init(ctx.local.data.atom(ns.nodes[i]));
        res = ctx.eval(expr2).bool();
        if(res) break;
      }
    } else {
      ft.init(v1.str());
      res = ctx.eval(expr2).bool();
    }
    
    ctx.ftitem = tmp;
    return Bool.get(res);

    /*
    for(int i = 0; i < size; i++) {
      if(Token.ftcontains(data.atom(nodes[i]), qu)) return true;
    }
    
    // don't evaluate empty node sets
    return Bool.get(v1.size() != 0 && v2.size() != 0 && type.eval(v1, v2));
    */
  }

  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr)
      throws QueryException {

    final LocPath path = (LocPath) expr1;
    ctx.compInfo(OPTFTINDEX);
    
    // all FTWords are recursively converted to FTIndex requests
    final Expr expr = expr2.indexEquivalent(ctx, curr);
    
    // <SG> ideally, this method should never return null
    // and only be called if an index exists...
    if(expr == null) return this;
    
    return new Path(expr, path.invertPath(curr));
  } 

  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {

    // check if first expression is a location path and if fulltext index exists
    final MetaData meta = ctx.local.data.meta;
    if(!(expr1 instanceof LocPathRel && meta.ftxindex))
      return Integer.MAX_VALUE;
    
    // check if index can be applied
    final LocPath path = (LocPathRel) expr1;
    final Step step = path.steps.last();
    final boolean text = step.test == TestNode.TEXT && step.preds.size() == 0;
    if(!text || !path.checkAxes()) return Integer.MAX_VALUE;

    if(expr2 instanceof FTUnaryNot) return Integer.MAX_VALUE;

    // check all ftcontains options recursively if they comply
    // to the index options..
    if(!((FTArrayExpr) expr2).indexOptions(meta)) return Integer.MAX_VALUE;
 
    // TODO... check number of index results
    //final byte[] token = expr2.str();
    //final int nrIDs = ctx.local.data.nrFTIDs(token);
    
    final int nrIDs = expr2.indexSizes(ctx, curr, min);
    return nrIDs == -1 ? Integer.MAX_VALUE : nrIDs;
  }
  
  @Override
  public String toString() {
    return expr1 + " ftcontains " + expr2;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    expr1.plan(ser);
    expr2.plan(ser);
    ser.closeElement(this);
  }
}
