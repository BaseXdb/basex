package org.basex.query.xquery.path;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Context;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Var;

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
  public Expr comp(final XQContext ctx) throws XQException {
    if(root != null) root = root.comp(ctx);
    if(root instanceof Context) root = null;
    return this;
  }

  @Override
  public abstract Iter iter(final XQContext ctx) throws XQException;
  
  /**
   * Position test.
   * @param step step array
   * @return result of check
   */
  protected boolean usesPos(final Expr[] step) {
    for(final Expr s : step) if(s.usesPos()) return true;
    return root != null && root.usesPos();
  }

  /**
   * Variables test.
   * @param v variable to be checked
   * @param step step array
   * @return result of check
   */
  protected boolean usesVar(final Var v, final Expr[] step) {
    for(final Expr s : step) if(s.usesVar(v)) return true;
    return root != null && root.usesVar(v);
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
