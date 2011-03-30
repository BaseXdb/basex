package org.basex.query.path;

import static org.basex.query.QueryText.*;
import static org.basex.query.path.Axis.*;

import java.io.IOException;
import java.util.Arrays;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Context;
import org.basex.query.expr.Expr;
import org.basex.query.expr.ParseExpr;
import org.basex.query.expr.Root;
import org.basex.query.item.NodeType;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.path.Test.Name;
import org.basex.query.util.Var;
import org.basex.util.Array;
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

    final Value v = ctx.value;
    ctx.value = root(ctx);
    final Expr e = compPath(ctx);
    ctx.value = v;
    return e;
  }

  /**
   * Compiles the location path.
   * @param ctx query context
   * @return optimized expression
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
    if(root == null) return v == null || v.type != NodeType.DOC ? v : null;
    // root is value: return root
    if(root.value()) return (Value) root;
    // no root reference, no context: return null
    if(!(root instanceof Root) || v == null) return null;
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
  /**
   * Optimizes descendant-or-self steps and static types.
   * @param steps step array
   * @param ctx query context
   * @return optimized steps
   */
  protected Expr[] optSteps(final Expr[] steps, final QueryContext ctx) {
    boolean opt = false;
    Expr[] step = steps;
    for(int l = 1; l < step.length; ++l) {
      if(!(step[l - 1] instanceof AxisStep &&
           step[l] instanceof AxisStep)) continue;

      final AxisStep last = (AxisStep) step[l - 1];
      final AxisStep curr = (AxisStep) step[l];
      if(!last.simple(DESCORSELF, false)) continue;

      if(curr.axis == CHILD && !curr.uses(Use.POS)) {
        // descendant-or-self::node()/child::X -> descendant::X
        Array.move(step, l, -1, step.length - l);
        step = Arrays.copyOf(step, step.length - 1);
        curr.axis = DESC;
        opt = true;
      } else if(curr.axis == ATTR && !curr.uses(Use.POS)) {
        // descendant-or-self::node()/@X -> descendant-or-self::*/@X
        last.test = new NameTest(false, last.input);
        opt = true;
      }
    }
    if(opt) ctx.compInfo(OPTDESC);

    // set atomic type for single attribute steps to speedup predicate tests
    if(root == null && step.length == 1 && step[0] instanceof AxisPath) {
      final AxisStep curr = (AxisStep) step[0];
      if(curr.axis == ATTR && curr.test.test == Name.STD)
        curr.type = SeqType.NOD_ZO;
    }
    return step;
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
