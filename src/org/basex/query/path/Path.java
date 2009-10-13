package org.basex.query.path;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Context;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Root;
import org.basex.query.item.Item;
import org.basex.query.util.Var;

/**
 * Path expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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

  /**
   * Position test.
   * @param step step array
   * @param use use type
   * @param ctx query context
   * @return result of check
   */
  protected final boolean uses(final Expr[] step, final Use use,
      final QueryContext ctx) {

    if(use == Use.CTX || use == Use.ELM)
      return root == null || root.uses(use, ctx);

    for(final Expr s : step) if(s.uses(use, ctx)) return true;
    return root != null && root.uses(use, ctx);
  }

  /**
   * Returns the root of the current context or null.
   * @param ctx query context
   * @return root
   */
  protected final Item root(final QueryContext ctx) {
    final Item it = ctx != null ? ctx.item : null;
    if(root == null) return it;
    if(root.i()) return (Item) root;
    if(!(root instanceof Root) || it == null) return null;
    return it.size(ctx) != 1 ? it : ((Root) root).root(it);
  }

  @Override
  public Expr remove(final Var v) {
    if(root != null) root = root.remove(v);
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
   * @throws IOException I/O exception
   */
  final void plan(final Serializer ser, final Expr[] step) throws IOException {
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
  public final String toString(final Expr[] step) {
    final StringBuilder sb = new StringBuilder();
    if(root != null) sb.append(root);
    for(final Expr s : step) sb.append((sb.length() != 0 ? "/" : "") + s);
    return sb.toString();
  }
}
