package org.basex.query.xpath.expr;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.XPOptimizer;
import org.basex.query.xpath.internal.FTIndex;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.LocPathRel;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Literal;
import static org.basex.query.xpath.XPText.*;

/**
 * FTFuzzy Expression; used for fuzzy search in fulltext.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTFuzzy extends Comparison {
  /** Result of index request - currently only pre values are saved. */
  int[] ids;
  /** Flag for FuzzyIndex use. */
  boolean fi = false;
  /** Number errors allowed. */
  int ne;
 
  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   * @param numError number of errors
   */
  public FTFuzzy(final Expr e1, final Expr e2, final int numError) {
    super(e1, e2);
    ne = numError;
  }
  
  
  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    

    if (expr2 instanceof Literal && expr1 instanceof LocPathRel) {
      final byte[] lit = ((Literal)  expr2).str();

      ctx.compInfo(OPTFTINDEX);
      return new FTIndex(lit, ne);
 /*     if (fi)
        return new FTFuzzyIndex(lit, ne);
      return new FTIndex(lit, ne);
  */  
      }

    expr1 = expr1.compile(ctx);
    expr2 = expr2.compile(ctx);

    XPOptimizer.addText(expr1, ctx);
    XPOptimizer.addText(expr2, ctx);

   return this;
  }

  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr)
    throws QueryException {
    LocPath path = (LocPath) expr1;;
    
    //indexExprs = new FTIndex(lit, option, false, false); 
    //return new Path(new FTIndex(lit, option, true, false), 
    //    path.invertPath(curr));
    return new Path(expr2.indexEquivalent(ctx, curr), path.invertPath(curr));
    
  } 

  @Override
  public int indexSizes(final XPContext ctx, final Step curr,
      final int min) {
    // high selectivity - always create index access
    return 1;
    
  }
}
