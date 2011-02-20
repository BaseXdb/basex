package org.basex.query.path;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Context;
import org.basex.query.expr.Expr;
import org.basex.query.expr.ParseExpr;
import org.basex.query.expr.Root;
import org.basex.query.item.Item;
import org.basex.query.item.Type;
import org.basex.query.item.Value;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Path expression.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Path extends ParseExpr {
  /** Top expression. */
  public Expr root;

  /**
   * Constructor.
   * @param ii input info
   * @param r root expression; can be null
   */
  protected Path(final InputInfo ii, final Expr r) {
    super(ii);
    root = r;
  }

  @Override
  public final Expr comp(final QueryContext ctx) throws QueryException {
    if(root != null) {
      root = checkUp(root, ctx).comp(ctx);
      if(root instanceof Context) root = null;
    }

    final Value vi = ctx.value;
    ctx.value = root(ctx);
    final Expr e = compPath(ctx);
    ctx.value = vi;
    if(root instanceof Context) root = null;
    return e;
  }

  /**
   * Compiles the location path.
   * @param ctx query context
   * @return optimized Expression
   * @throws QueryException query exception
   */
  protected abstract Expr compPath(final QueryContext ctx)
    throws QueryException;

  /**
   * Returns the root of the current context or {@code null}.
   * @param ctx query context
   * @return root
   */
  protected final Value root(final QueryContext ctx) {
    final Value v = ctx != null ? ctx.value : null;
    // no root specified: return context, if it does not reference a document
    // as e.g. happens in //a(b|c)
    if(root == null) return v == null || v.type != Type.DOC ? v : null;
    // root is value: return root
    if(root.value()) return (Value) root;
    // no root reference, no/dummy context: return null
    if(!(root instanceof Root) || v == null || v == Item.DUMMY) return null;
    // return context sequence or root of current context
    return v.size() != 1 ? v : ((Root) root).root(v);
  }

  /**
   * Position test.
   * @param step step array
   * @param use use type
   * @return result of check
   */
  protected final boolean uses(final Expr[] step, final Use use) {
    // initial context will be used as input
    if(use == Use.CTX) return root == null || root.uses(use);
    for(final Expr s : step) if(s.uses(use)) return true;
    return root != null && root.uses(use);
  }

  @Override
  public int count(final Var v) {
    return root != null ? root.count(v) : 0;
  }

  @Override
  public boolean removable(final Var v) {
    return root == null || root.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    if(root != null) root = root.remove(v);
    if(root instanceof Context) root = null;
    return this;
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
  protected final String toString(final Expr[] step) {
    final StringBuilder sb = new StringBuilder();
    if(root != null) sb.append(root);
    for(final Expr s : step) sb.append((sb.length() != 0 ? "/" : "") + s);
    return sb.toString();
  }
}
