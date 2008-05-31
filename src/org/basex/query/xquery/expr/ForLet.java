package org.basex.query.xquery.expr;

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
  public final String color() {
    return "66FF66";
  }
}
