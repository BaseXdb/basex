package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.iter.ResetIter;
import org.basex.query.xquery.util.Var;

/**
 * Abstract For/Let Clause.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class ForLet extends Expr {
  /** Variable inputs. */
  protected Expr expr;
  /** Variable. */
  protected Var var;

  @Override
  public boolean usesVar(final Var v) {
    return v == null || !v.eq(var);
  }
  
  @Override
  public final String color() {
    return "66CC66";
  }
  
  @Override
  public abstract ResetIter iter(final XQContext ctx);
}
