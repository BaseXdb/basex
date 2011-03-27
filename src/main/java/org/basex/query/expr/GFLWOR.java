package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import java.util.ArrayList;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.FunDef;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
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
 * @author BaseX Team 2005-11, BSD License
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

    for(int f = 0; f < fl.length; ++f) {
      final ForLet flt = fl[f];
      flt.comp(ctx);
      // pre-evaluate and bind variable if it is used exactly once,
      // or if it contains a value
      if(count(flt.var, f) == 1 || flt.expr.value()) flt.bind(ctx);
    }

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

    // remove declarations of statically bound or unused variables
    for(int f = 0; f < fl.length; ++f) {
      final ForLet l = fl[f];
      if(l.var.expr() != null || l.simple(true) && count(l.var, f) == 0) {
        ctx.compInfo(OPTVAR, l.var);
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
      // ignore for clauses and constructors
      // [LW] why is the CTX check needed (it actually is), can it be replaced?
      if(t instanceof For || t.uses(Use.CTX) || t.uses(Use.CNS)) continue;
      // loop through all outer clauses
      for(int g = f - 1; g >= 0; --g) {
        // stop if variable used by the current clause
        if(t.count(fl[g].var) != 0) break;
        // ignore let clauses
        if(fl[g] instanceof Let) continue;
        // stop if variable is used as position or score
        final For fr = (For) fl[g];
        if(fr.pos != null && t.count(fr.pos) != 0 ||
           fr.score != null && t.count(fr.score) != 0) break;

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
    for(final ForLet f : fl) {
      if(f instanceof For && (!f.simple(false) || !where.removable(f.var)))
        return;
    }

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
        if(tests[t].count(fl[f].var) != 0) {
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
    ArrayList<Item[]> keys = null;
    ValueList vals = null;
    if(order != null) {
      keys = new ArrayList<Item[]>();
      vals = new ValueList();
    }
    if(group != null) group.init(fl, order);
    iter(ctx, iter, 0, keys, vals);
    ctx.vars.reset(vs);

    for(final ForLet f : fl) ctx.vars.add(f.var);

    // order != null, otherwise it would have been handled in group
    final Iter ir = group != null ?
        group.gp.ret(ctx, ret, keys, vals) : ctx.iter(order.set(keys, vals));
    ctx.vars.reset(vs);
    return ir;
  }

  /**
   * Performs a recursive iteration on the specified variable position.
   * @param ctx root reference
   * @param it iterator
   * @param p variable position
   * @param ks sort keys
   * @param vs values to sort
   * @throws QueryException query exception
   */
  private void iter(final QueryContext ctx, final Iter[] it, final int p,
      final ArrayList<Item[]> ks, final ValueList vs) throws QueryException {
    final boolean more = p + 1 != fl.length;
    while(it[p].next() != null) {
      if(more) {
        iter(ctx, it, p + 1, ks, vs);
      } else if(where == null || where.ebv(ctx, input).bool(input)) {
        if(group != null) {
          group.gp.add(ctx);
        } else if(order != null) {
          // order by will be handled in group by otherwise
          order.add(ctx, ret, ks, vs);
        }
      }
    }
  }

  @Override
  public final boolean uses(final Use u) {
    return u == Use.VAR || ret.uses(u);
  }

  @Override
  public final int count(final Var v) {
    return count(v, 0);
  }

  /**
   * Counts how often the specified variable is used, starting from the
   * specified for/let index.
   * @param v variable to be checked
   * @param i index
   * @return number of occurrences
   */
  public final int count(final Var v, final int i) {
    int c = 0;
    for(int f = i; f < fl.length; f++) c += fl[f].count(v);
    if(where != null) c += where.count(v);
    if(order != null) c += order.count(v);
    if(group != null) c += group.count(v);
    return c + ret.count(v);
  }

  @Override
  public final boolean removable(final Var v) {
    for(final ForLet f : fl) if(!f.removable(v)) return false;
    return (where == null || where.removable(v))
        && (order == null || order.removable(v))
        && (group == null || group.removable(v)) && ret.removable(v);
  }

  @Override
  public final Expr remove(final Var v) {
    for(final ForLet f : fl) f.remove(v);
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
