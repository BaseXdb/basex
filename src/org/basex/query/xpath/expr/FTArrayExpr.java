package org.basex.query.xpath.expr;

import java.io.IOException;
import org.basex.data.MetaData;
import org.basex.data.Serializer;
import org.basex.index.FTNode;
import org.basex.index.FTNodeIter;
import org.basex.query.FTOpt;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.path.Step;
import org.basex.util.TokenBuilder;

/**
 * This is an abstract class for fulltext array expressions.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class FTArrayExpr extends Expr {
  /** Array with arguments. */
  protected FTArrayExpr[] exprs;
  /** Fulltext option. */
  public FTOpt fto;
  
  /**
   * Optimizes the expression.
   * @param ctx expression context
   * @return optimized Expression
   * @throws QueryException evaluation exception
   */
  @Override
  public FTArrayExpr comp(final XPContext ctx) throws QueryException {
    for(int i = 0; i < exprs.length; i++) exprs[i] = exprs[i].comp(ctx);
    return this;
  }

  /**
   * Verifies if the fulltext query options comply with the index defaults.
   * Check where this method is overwritten to get more info.
   * @param meta meta data
   * @return result of check
   */
  public boolean indexOptions(final MetaData meta) {
    for(final FTArrayExpr e : exprs) if(!e.indexOptions(meta)) return false;
    return true;
  }
  
  /**
   * Checks for more results.
   * @return boolean 
   */
  public boolean more() {
    return false;
  }
  
  /**
   * Checks whether the result of the expression is negative.
   * @return boolean
   */
  public boolean pos() {
    return true;
  }

  /**
   * Returns the next ftquery result.
   * @param ctx current context
   * @return FTNode container for the next result
   */
  @SuppressWarnings("unused")
  public FTNode next(final QueryContext ctx) {
    return new FTNode();
  }

  
  /**
   * Returns an itr for the results.
   * @param ctx current context
   * @return itr container for the results
   */
  @SuppressWarnings("unused")
  public FTNodeIter iter(final QueryContext ctx) {
    return FTNodeIter.EMPTY;
  }
  
  @Override
  public final boolean usesPos() {
    return false;
  }

  @Override
  public final boolean usesSize() {
    return false;
  }

  @Override
  @SuppressWarnings("unused")
  public FTArrayExpr indexEquivalent(final XPContext ctx, final Step step, 
      final boolean seq)
      throws QueryException {
    return null;
  }

  @Override
  public String color() {
    return "66FF66";
  }

  @Override
  public String toString() {
    final TokenBuilder sb = new TokenBuilder(name());
    sb.add('(');
    for(int i = 0; i != exprs.length; i++) {
      if(i != 0) sb.add(", ");
      sb.add(exprs[i]);
    }
    sb.add(')');
    return sb.toString();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final Expr expr : exprs) expr.plan(ser);
    ser.closeElement();
  }
}
