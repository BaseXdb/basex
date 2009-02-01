package org.basex.query.path;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Context;
import org.basex.query.expr.Expr;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;

/**
 * Path expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Path extends Expr {
  /** Top expression. */
  public Expr root;

  /**
   * Constructor.
   * @param r root expression; can be null
   */
  public Path(final Expr r) {
    root = r;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    if(root != null) root = root.comp(ctx);
    return this;
  }

  @Override
  public abstract Iter iter(final QueryContext ctx) throws QueryException;
  
  /**
   * Position test.
   * @param step step array
   * @param ctx query context
   * @return result of check
   */
  protected boolean usesPos(final Expr[] step, final QueryContext ctx) {
    for(final Expr s : step) if(s.usesPos(ctx)) return true;
    return root != null && root.usesPos(ctx);
  }

  /**
   * Variables test.
   * @param v variable to be checked
   * @param step step array
   * @return result of check
   */
  protected int usesVar(final Var v, final Expr[] step) {
    int c = 0;
    for(final Expr s : step) c += s.countVar(v);
    return c + (root != null ? root.countVar(v) : 0);
  }

  @Override
  public Expr removeVar(final Var v) {
    if(root != null) root = root.removeVar(v);
    if(root instanceof Context) root = null;
    return this;
  }

  @Override
  public final String color() {
    return "FFCC33";
  }

  /**
   * Prints the query plan.
   * @param ser serializer
   * @param step step array
   * @throws IOException exception
   */
  void plan(final Serializer ser, final Expr[] step) throws IOException {
    ser.openElement(this);
    if(root != null) root.plan(ser);
    for(final Expr s : step) s.plan(ser);
    ser.closeElement();
  }

  /**
   * Returns a string representation.
   * @param step step array
   * @return string representation
   */
  public String toString(final Expr[] step) {
    final StringBuilder sb = new StringBuilder();
    if(root != null) sb.append(root);
    for(final Expr s : step) sb.append((sb.length() != 0 ? "/" : "") + s);
    return sb.toString();
  }
}
