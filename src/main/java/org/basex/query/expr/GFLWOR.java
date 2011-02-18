package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.FunDef;
import org.basex.query.item.Empty;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.path.AxisPath;
import org.basex.query.util.ValueList;
import org.basex.query.util.Var;
import org.basex.util.Array;
import org.basex.util.InputInfo;

/**
 * GFLWOR clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class GFLWOR extends ParseExpr {
  /** Return expression. */
  protected Expr ret;
  /** For/Let expression. */
  protected ForLet[] fl;
  /** Where clause. */
  protected Expr where;
  /** Order clause. */
  protected Order order;
  /** Group by clause. */
  protected Group group;

  /**
   * GFLWOR constructor.
   * @param f variable inputs
   * @param w where clause
   * @param o order expression
   * @param g group by expression
   * @param r return expression
   * @param ii input info
   */
  GFLWOR(final ForLet[] f, final Expr w, final Order o, final Group g,
      final Expr r, final InputInfo ii) {

    super(ii);
    ret = r;
    fl = f;
    where = w;
    group = g;
    order = o;
  }

  /**
   * Returns a GFLWOR instance.
   * @param f variable inputs
   * @param w where clause
   * @param o order expression
   * @param g group by expression
   * @param r return expression
   * @param ii input info
   * @return GFLWOR instance
   */
  public static GFLWOR get(final ForLet[] f, final Expr w, final OrderBy[] o,
      final Var[] g, final Expr r, final InputInfo ii) {

    if(o == null && g == null) return new FLWR(f, w, r, ii);
    final Order ord = o == null ? null : new Order(ii, o);
    final Group grp = g == null ? null : new Group(ii, g);
    return new GFLWOR(f, w, ord, grp, r, ii);
  }
  
  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    compForLet(ctx);
    compWhere(ctx);

    final boolean grp = ctx.grouping;
    ctx.grouping = group != null;

    // optimize for/let clauses
    final int vs = ctx.vars.size();
    for(final ForLet f : fl) f.comp(ctx);

    // optimize where clause
    boolean empty = false;
    if(where != null) {
      where = checkUp(where, ctx).comp(ctx).compEbv(ctx);
      if(where.value()) {
        // test is always false: no results
        empty = !where.ebv(ctx, input).bool(input);
        if(!empty) {
          // always true: test can be skipped
          ctx.compInfo(OPTREMOVE, desc(), where);
          where = null;
        }
      }
    }

    if(group != null) group.comp(ctx);
    if(order != null) order.comp(ctx);
    ret = ret.comp(ctx);
    ctx.vars.reset(vs);
    ctx.grouping = grp;

    // check if return always yields an empty sequence
    if(ret == Empty.SEQ) {
      ctx.compInfo(OPTFLWOR);
      return ret;
    }

    // remove FLWOR expression if WHERE clause always returns false
    if(empty) {
      ctx.compInfo(OPTREMOVE, desc(), where);
      return Empty.SEQ;
    }

    // remove declarations of statically bound variables
    for(int f = 0; f != fl.length; ++f) {
      if(fl[f].var.expr() != null) {
        ctx.compInfo(OPTVAR, fl[f].var);
        fl = Array.delete(fl, f--);
      }
    }

    // no clauses left: simplify expression
    // an optional order clause can be safely ignored
    if(fl.length == 0) {
      // if where clause exists: where A return B -> if A then B else ()
      // otherwise: return B -> B
      ctx.compInfo(OPTFLWOR);
      return where != null ? new If(input, where, ret, Empty.SEQ) : ret;
    }

    // remove FLWOR expression if a FOR clause yields an empty sequence
    for(final ForLet f : fl) {
      if(f instanceof For && (f.empty() || f.size() == 0)) {
        ctx.compInfo(OPTFLWOR);
        return Empty.SEQ;
      }
    }

    // compute number of results to speed up count() operations
    if(where == null && group == null) {
      size = ret.size();
      if(size != -1) {
        // multiply loop runs
        for(final ForLet f : fl) {
          final long s = f.size();
          if(s == -1) {
            size = s;
            break;
          }
          size *= s;
        }
      }
    }
    type = SeqType.get(ret.type().type, SeqType.Occ.ZM);
    return this;
  }

  /**
   * Optimizes for/let clauses. Avoids repeated calls to static let clauses.
   * @param ctx query context
   */
  private void compForLet(final QueryContext ctx) {
    // check if all clauses are simple, and if variables are removable
    boolean m = false;
    // loop through all clauses
    for(int f = fl.length - 1; f >= 0; --f) {
      ForLet t = fl[f];
      // ignore for clauses
      if(t instanceof For) continue;
      // loop through all outer clauses
      for(int g = f - 1; g >= 0; --g) {
        // stop if variable is shadowed or used by the current clause
        if(fl[g].shadows(t.var) || t.uses(fl[g].var)) break;
        // ignore let clauses and fragment constructors
        if(fl[g] instanceof Let || t.uses(Use.FRG)) continue;
        // stop if variable is used by as position or score
        final For fr = (For) fl[g];
        if(fr.pos != null && t.uses(fr.pos) ||
           fr.score != null && t.uses(fr.score)) break;

        // move let clause to outer position
        System.arraycopy(fl, g, fl, g + 1, f - g);
        fl[g] = t;
        t = fl[f];
        if(!m) ctx.compInfo(OPTFORLET);
        m = true;
      }
    }
  }

  /**
   * Optimizes a where clause.
   * @param ctx query context
   */
  private void compWhere(final QueryContext ctx) {
    // no where clause specified
    if(where == null) return;

    // check if all clauses are simple, and if variables are removable
    for(final ForLet f : fl)
      if(f instanceof For && (!f.simple() || !where.removable(f.var))) return;

    // create array with tests
    final Expr[] tests = where instanceof And ? ((And) where).expr :
      new Expr[] { where };

    // find which tests access which variables. if a test will not use any of
    // the variables defined in the local context, they will be added to the
    // first binding
    final int[] tar = new int[tests.length];
    for(int t = 0; t < tests.length; ++t) {
      int fr = -1;
      for(int f = fl.length - 1; f >= 0; --f) {
        // remember index of most inner FOR clause
        if(fl[f] instanceof For) fr = f;
        // predicate is found that uses the current variable
        if(tests[t].uses(fl[f].var)) {
          // stop rewriting if no most inner FOR clause is defined
          if(fr == -1) return;
          // attach predicate to the corresponding FOR clause, and stop
          tar[t] = fr;
          break;
        }
      }
    }
    // convert where clause to predicate(s)
    ctx.compInfo(OPTWHERE);

    // bind tests to the corresponding variables
    for(int t = 0; t < tests.length; ++t) {
      final ForLet f = fl[tar[t]];
      Expr e = tests[t].remove(f.var);
      // wrap test with boolean() if the result is numeric
      if(e.type().mayBeNum()) e = FunDef.BOOLEAN.get(input, e);
      // attach predicates to axis path or filter, or create a new filter
      if(f.expr instanceof AxisPath) {
        f.expr = ((AxisPath) f.expr).addPreds(e);
      } else if(f.expr instanceof Filter) {
        f.expr = ((Filter) f.expr).addPred(e);
      } else {
        f.expr = new Filter(input, f.expr, e);
      }
    }
    // eliminate where clause
    where = null;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Iter[] iter = new Iter[fl.length];
    final int vs = ctx.vars.size();
    for(int f = 0; f < fl.length; ++f) iter[f] = ctx.iter(fl[f]);

    // evaluate pre grouping tuples
    final int s = (int) size();
    final ValueList vl = s >= 0 ? new ValueList(s) : new ValueList();
    if(order != null) order.init(vl, s);
    if(group != null) group.init(fl, order);
    iter(ctx, vl, iter, 0);
    ctx.vars.reset(vs);

    for(final ForLet aFl : fl) ctx.vars.add(aFl.var);

    // order != null, otherwise it would have been handled in group
    final Iter ir = group != null ? group.gp.ret(ctx, ret) : ctx.iter(order);
    ctx.vars.reset(vs);
    return ir;
  }

  /**
   * Performs a recursive iteration on the specified variable position.
   * @param ctx root reference
   * @param vl value lists
   * @param it iterator
   * @param p variable position
   * @throws QueryException query exception
   */
  private void iter(final QueryContext ctx, final ValueList vl,
      final Iter[] it, final int p) throws QueryException {

    final boolean more = p + 1 != fl.length;
    while(it[p].next() != null) {
      if(more) {
        iter(ctx, vl, it, p + 1);
      } else if(where == null || where.ebv(ctx, input).bool(input)) {
        if(group != null) {
          group.gp.add(ctx);
        } else if(order != null) {
          // order by will be handled in group by otherwise
          order.add(ctx);
          vl.add(ret.value(ctx));
        }
      }
    }
  }

  @Override
  public final boolean uses(final Use u) {
    return u == Use.VAR || ret.uses(u);
  }

  @Override
  public final boolean uses(final Var v) {
    for(final ForLet f : fl) {
      if(f.uses(v)) return true;
      if(f.shadows(v)) return false;
    }
    return where != null && where.uses(v)
        || order != null && order.uses(v)
        || group != null && group.uses(v) || ret.uses(v);
  }

  @Override
  public final boolean removable(final Var v) {
    for(final ForLet f : fl) {
      if(!f.removable(v)) return false;
      if(f.shadows(v)) return true;
    }
    return (where == null || where.removable(v))
        && (order == null || order.removable(v))
        && (group == null || group.removable(v)) && ret.removable(v);
  }

  @Override
  public final Expr remove(final Var v) {
    for(final ForLet f : fl) {
      f.remove(v);
      if(f.shadows(v)) return this;
    }
    if(where != null) where = where.remove(v);
    if(order != null) order = order.remove(v);
    ret = ret.remove(v);
    return this;
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final ForLet f : fl) f.plan(ser);
    if(where != null) {
      ser.openElement(WHR);
      where.plan(ser);
      ser.closeElement();
    }
    if(group != null) group.plan(ser);
    if(order != null) order.plan(ser);
    ser.openElement(RET);
    ret.plan(ser);
    ser.closeElement();
    ser.closeElement();
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i != fl.length; ++i) sb.append((i != 0 ? " " : "") + fl[i]);
    if(where != null) sb.append(" " + WHERE + " " + where);
    if(group != null) sb.append(group);
    if(order != null) sb.append(order);
    return sb.append(" " + RETURN + " " + ret).toString();
  }
}
