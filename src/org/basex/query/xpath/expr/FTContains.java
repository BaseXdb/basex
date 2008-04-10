package org.basex.query.xpath.expr;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.XPOptimizer;
import org.basex.query.xpath.internal.FTIndex;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.LocPathRel;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.locpath.TestNode;
import org.basex.query.xpath.values.Bool;
import org.basex.query.xpath.values.Comp;
import org.basex.query.xpath.values.Literal;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Token;
import static org.basex.query.xpath.XPText.*;

/**
 * FTContains Expression; used for fulltext operations.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTContains extends Comparison {
  /** FullText options. */
  private FTOption option;
  /** Flag for FTPosFilter queries.*/
  protected boolean ftpos = false;
 
  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   */
  public FTContains(final Expr e1, final Expr e2) {
    super(e1, e2);
    type = Comp.FTCONTAINS;
  }
  
  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   * @param opt fulltext options
   */
  public FTContains(final Expr e1, final Expr e2, final FTOption opt) {
    super(e1, e2);
    option = opt;
    type = Comp.FTCONTAINS;
  }

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   * @param opt fulltext options
   * @param ftposfilter flag to be set if query contains an ft pososition filter
   */
  public FTContains(final Expr e1, final Expr e2, final FTOption opt, 
      final boolean ftposfilter) {
    super(e1, e2);
    expr1 = e1;
    expr2 = e2;
    option = opt;
    type = Comp.FTCONTAINS;
    ftpos = ftposfilter;
  }

  
  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    if (expr2 instanceof Literal && expr1 instanceof Literal) {
      final byte[] lit = ((Literal)  expr2).str();

      ctx.compInfo(OPTFTINDEX);
      
//    <SG> to be change if ftnot is combined with further options like order...
      if (Token.indexOf(lit, ' ') > 0) {
        return new FTIndex(lit, option, false, false);
      }
        
      return new FTIndex(lit, option, false, true);
    }

    expr1 = expr1.compile(ctx);
    expr2 = expr2.compile(ctx);

    XPOptimizer.addText(expr1, ctx);
    XPOptimizer.addText(expr2, ctx);

   return this;
  }

  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    
    if(expr1 instanceof LocPath && expr2 instanceof FTUnaryNot) {
      final NodeSet ns = ctx.local;
      ctx.local = (NodeSet) expr1.eval(ctx); 
    
      Bool bool = Bool.get(expr2.eval(ctx).bool());
      ctx.local = ns;
      return bool;

      /*
      FTIndex f = (FTIndex) ((FTUnaryNot) expr2).exprs[0];
      NodeSet n = f.eval(ctx);
      // evaluate location path to get resulting nodes
      ctx.local = (NodeSet) ctx.eval(expr1);

      // check if index is fulltext index exists
      final Data data = ctx.local.data;
      if(data.meta.ftxindex) {
        if(ctx.local.ftids == null) {
          ctx.local.ftids = Array.extractIDsFromData(data.ftIDs(l, f.option));
        }
        return Bool.get(expr2.eval(ctx).bool());
      }
      */
    }
   
    // use conventional check for other cases..
    
    return super.eval(ctx);
  }

  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr)
    throws QueryException {
    LocPath path = (LocPath) expr1;;
    
    
    return new Path(expr2.indexEquivalent(ctx, curr), path.invertPath(curr));
    
    /*
    // simple ftcontains invert path for index access
    if (expr1 instanceof LocPath && expr2 instanceof Literal) {
      path = (LocPath) expr1;
      final byte[] lit = ((Literal)  expr2).str();

      ctx.compInfo(OPTFTINDEX);
      
      
      if (Token.indexOf(lit, ' ') > 0 || option.ftContent != null) {
        indexExprs = new FTIndex(lit, option, false, false); 
        //return new Path(new FTIndex(lit, option, true, false), 
        //    path.invertPath(curr));
        
      }
        
      ///return new Path(new FTIndex(lit, option, true, true), 
      //   path.invertPath(curr));
         
      indexExprs = new FTIndex(lit, option, false, true);
      
    } else if ((expr1 instanceof LocPath) && 
        (expr2 instanceof FTAnd || expr2 instanceof FTMildNot)) {
      // find index equivalents
      indexExprs = expr2.indexEquivalent(ctx, curr);
      
      if(indexExprs == null) 
        return null;
   
      path = (LocPath) expr1;
      //ctx.compInfo(OPTAND4);
      //return new Path(indexExprs, path.invertPath(curr));
      //return indexExprs;
    } else if (expr1 instanceof LocPath && expr2 instanceof FTOr) {
      // find index equivalents
      indexExprs = expr2.indexEquivalent(ctx, curr);

      if(indexExprs == null) 
        return null;
   
      path = (LocPath) expr1;
      //ctx.compInfo(OPTOR4);
      //return new Path(indexExprs, path.invertPath(curr));
      //return indexExprs;
    } else if ((expr1 instanceof Literal || expr1 instanceof FTOr ||
        expr1 instanceof FTAnd || expr1 instanceof FTMildNot) 
        && expr2 instanceof Literal) {
      final byte[] lit = ((Literal)  expr2).str();
      
      ctx.compInfo(OPTFTINDEX);

      if (Token.indexOf(lit, ' ') > 0) {
        return new FTIndex(lit, option, false, false);
      }
      return new FTIndex(lit, option, false, true);
    } else if (expr1 instanceof LocPath && expr2 instanceof FTUnaryNot) {
      // should not happen
      return ((FTUnaryNot) expr2).indexEquivalent(ctx, curr);
    }

    if (ftpos && path != null && indexExprs != null) {
      // add ftposfilter to queryplan on top of all other ftoperands
      FTPosFilter ftposf = new FTPosFilter(new Expr[]{indexExprs}, option);
      return new Path(ftposf, path.invertPath(curr));
    } else if (path != null && indexExprs != null) {
      return new Path(indexExprs, path.invertPath(curr));
    }
    return null; */
  } 

  @Override
  public int indexSizes(final XPContext ctx, final Step curr,
      final int min) {
    
    // check which expression is a location path
    if(!(ctx.local.data.meta.ftxindex && expr1 instanceof LocPathRel
        )) return Integer.MAX_VALUE;
    // (expr2 instanceof Literal)
    
    // check if index can be applied
    final LocPath path = (LocPathRel) expr1;
    final Step step = path.steps.last();
    final boolean text = step.test == TestNode.TEXT && step.preds.size() == 0;
    if(!text || !path.checkAxes()) return Integer.MAX_VALUE;

    if (expr1 instanceof FTUnaryNot || expr2 instanceof FTUnaryNot) 
      return Integer.MAX_VALUE; 
    
    // high selectivity - always create index access
    return 1;
    
  }
}
