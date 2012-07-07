package org.basex.query.flwor;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * GFLWOR clause.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class GFLWOR extends ParseExpr {
  /** Return expression. */
  Expr ret;
  /** For/Let expression. */
  ForLet[] fl;
  /** Where clause. */
  Expr where;
  /** Order clause. */
  private Order order;
  /** Group by clause. */
  private final Group group;

  /**
   * GFLWOR constructor.
   * @param f variable inputs
   * @param w where clause
   * @param o order expression
   * @param g group by expression
   * @param r return expression
   * @param ii input info
   */
  GFLWOR(final ForLet[] f, final Expr w, final Order o, final Group g, final Expr r,
      final InputInfo ii) {

    super(ii);
    ret = r;
    fl = f;
    where = w;
    group = g;
    order = o;
  }

  /**
   * Returns a GFLWOR instance.
   * @param fl variable inputs
   * @param whr where clause
   * @param ord order expression
   * @param grp group-by expression
   * @param ret return expression
   * @param ii input info
   * @return GFLWOR instance
   */
  public static GFLWOR get(final ForLet[] fl, final Expr whr, final Order ord,
      final Group grp, final Expr ret, final InputInfo ii) {
    return ord == null && grp == null ? new FLWR(fl, whr, ret, ii) :
      new GFLWOR(fl, whr, ord, grp, ret, ii);
  }

  @Override
  public void checkUp() throws QueryException {
    for(final ForLet f : fl) f.checkUp();
    checkNoneUp(where, group, order);
    ret.checkUp();
  }

  @Override
  public Expr compile(final QueryContext ctx) throws QueryException {
    compHoist(ctx);
    compWhere(ctx);

    final boolean grp = ctx.grouping;
    ctx.grouping = group != null;

    // optimize for/let clauses
    final int vs = ctx.vars.size();
    for(int f = 0; f < fl.length; ++f) {
      final ForLet flt = fl[f].compile(ctx);
      // bind variable if it contains a value or will only be evaluated once
      boolean let = true;
      for(int g = f + 1; g < fl.length; g++) let &= flt instanceof Let;
      if(flt.expr.isValue() || let && count(flt.var, f) == 1) flt.bind(ctx);
    }

    // optimize where clause
    boolean empty = false;
    if(where != null) {
      where = where.compile(ctx).compEbv(ctx);
      if(where.isValue()) {
        // test is always false: no results
        empty = !where.ebv(ctx, info).bool(info);
        if(!empty) {
          // always true: test can be skipped
          ctx.compInfo(OPTREMOVE, description(), where);
          where = null;
        }
      }
    }

    if(group != null) group.compile(ctx);
    if(order != null) order.compile(ctx);
    ret = ret.compile(ctx);
    ctx.vars.size(vs);
    ctx.grouping = grp;

    // remove FLWOR expression if WHERE clause always returns false
    if(empty) {
      ctx.compInfo(OPTREMOVE, description(), where);
      return Empty.SEQ;
    }
    // check if return always yields an empty sequence
    if(ret == Empty.SEQ) {
      ctx.compInfo(OPTFLWOR);
      return ret;
    }

    // remove declarations of statically bound or unused variables
    for(int f = 0; f < fl.length; ++f) {
      final ForLet l = fl[f];
      // do not optimize non-deterministic expressions. example:
      // let $a := file:write('file', 'content') return ...
      if(l.var.expr() != null || l.simple(true) && count(l.var, f) == 0 &&
          !l.expr.uses(Use.NDT)) {
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
      return where != null ? new If(info, where, ret, Empty.SEQ) : ret;
    }

    // remove FLWOR expression if a FOR clause yields an empty sequence
    for(final ForLet f : fl) {
      if(f instanceof For && f.size() == 0) {
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
    type = SeqType.get(ret.type().type, size);

    compHoist(ctx);
    return this;
  }

  /**
   * Hoists loop-invariant code. Avoids repeated evaluation of independent
   * variables that return a single value. This method is called twice
   * (before and after all other optimizations).
   * @param ctx query context
   */
  private void compHoist(final QueryContext ctx) {
    // modification counter
    int m = 0;
    for(int i = 1; i < fl.length; i++) {
      final ForLet in = fl[i];
      /* move clauses upwards that contain a single value.
         non-deterministic expressions or fragment constructors creating
         unique nodes are ignored. example:
         for $a in 1 to 2 let $b := math:random() return $b
       */
      if(in.size() != 1 || in.uses(Use.NDT) || in.uses(Use.CNS)) continue;

      // find most outer clause that declares no variables that are used in the
      // inner clause
      int p = -1;
      for(int o = i; o-- != 0 && in.count(fl[o]) == 0; p = o);
      if(p == -1) continue;

      // move clause
      Array.move(fl, p, 1, i - p);
      fl[p] = in;
      if(m++ == 0) ctx.compInfo(OPTFORLET);
    }
  }

  /**
   * Rewrites a where clause to one or more predicates.
   * @param ctx query context
   */
  private void compWhere(final QueryContext ctx) {
    // no where clause specified
    if(where == null) return;

    // check if all clauses are simple, and if variables are removable
    for(final ForLet f : fl) {
      if(f instanceof For && (!f.simple(false) || !where.removable(f.var))) return;
    }

    // create array with tests
    final Expr[] tests = where instanceof And ? ((And) where).expr : new Expr[] { where };

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

    for(int f = 0; f < fl.length; ++f) {
      final ForLet c = fl[f];
      final ExprList el = new ExprList();
      // find all tests that will be bound to the current clause
      for(int t = 0; t < tests.length; ++t) {
        if(tar[t] == f) el.add(tests[t].remove(c.var));
      }
      // none found: continue
      if(el.isEmpty()) continue;

      // attach predicates to axis path or filter, or create a new filter
      final Expr a;
      if(el.size() == 1) {
        // one found: wrap with boolean function if value may be numeric
        final Expr e = el.get(0);
        a = e.type().mayBeNumber() ? Function.BOOLEAN.get(info, e) : e;
      } else {
        // more found: wrap with and expression
        a = new And(info, el.finish());
      }

      // add to clause expression
      if(c.expr instanceof AxisPath) {
        c.expr = ((AxisPath) c.expr).addPreds(a);
      } else if(c.expr instanceof Filter) {
        c.expr = ((Filter) c.expr).addPred(a);
      } else {
        c.expr = new Filter(info, c.expr, a);
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
    if(group != null) group.init(order);
    iter(ctx, iter, 0, keys, vals);
    ctx.vars.size(vs);

    for(final ForLet f : fl) ctx.vars.add(f.var);

    // order != null, otherwise it would have been handled in group
    final Iter ir = group != null ?
        group.gp.ret(ctx, ret, keys, vals) : ctx.iter(order.set(keys, vals));
    ctx.vars.size(vs);
    return ir;
  }

  /**
   * Performs a recursive iteration on the specified variable position.
   * @param ctx query context
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
      } else if(where == null || where.ebv(ctx, info).bool(info)) {
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
    for(final ForLet f : fl) if(f.uses(u)) return true;
    return where != null && where.uses(u) ||
           order != null && order.uses(u) ||
           group != null && group.uses(u) || ret.uses(u);
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
  final int count(final Var v, final int i) {
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
    return (where == null || where.removable(v)) &&
           (order == null || order.removable(v)) &&
           (group == null || group.removable(v)) && ret.removable(v);
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
  public boolean databases(final StringList db) {
    for(final ForLet f : fl) if(!f.databases(db)) return false;
    return (where == null || where.databases(db)) &&
           (order == null || order.databases(db)) &&
           (group == null || group.databases(db)) && ret.databases(db);
  }

  @Override
  public final void plan(final FElem plan) {
    final FElem el = planElem();
    addPlan(plan, el, fl);
    if(where != null) addPlan(el, new FElem(WHR), where);
    if(group != null) group.plan(el);
    if(order != null) order.plan(el);
    addPlan(el, new FElem(RET), ret);
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i != fl.length; ++i) sb.append(i != 0 ? " " : "").append(fl[i]);
    if(where != null) sb.append(' ' + WHERE + ' ' + where);
    if(group != null) sb.append(group);
    if(order != null) sb.append(order);
    return sb.append(' ' + RETURN + ' ' + ret).toString();
  }
}
