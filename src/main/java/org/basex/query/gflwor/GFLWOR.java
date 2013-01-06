package org.basex.query.gflwor;

import java.util.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.iter.Iter;
import org.basex.query.util.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * General FLWOR expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public class GFLWOR extends ParseExpr {
  /** Return expression. */
  Expr ret;
  /** FLWOR clauses. */
  private final ArrayList<Clause> clauses;
  /** XQuery 3.0 flag. */
  private boolean xq30;

  /**
   * Constructor.
   * @param ii input info
   * @param cls FLWOR clauses
   * @param rt return expression
   */
  public GFLWOR(final InputInfo ii, final ArrayList<Clause> cls, final Expr rt) {
    super(ii);
    clauses = cls;
    ret = rt;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    // Start evaluator, doing nothing, once.
    Eval e = new Eval() {
      /** First-evaluation flag. */
      private boolean first = true;
      @Override
      public boolean next(final QueryContext c) {
        if(!first) return false;
        first = false;
        return true;
      }
    };

    for(final Clause cls : clauses) e = cls.eval(e);
    final Eval ev = e;

    return new Iter() {
      /** Return iterator. */
      private Iter sub = Empty.ITER;
      /** If the iterator has been emptied. */
      private boolean drained;
      @Override
      public Item next() throws QueryException {
        if(drained) return null;
        while(true) {
          final Item it = sub.next();
          if(it != null) return it;
          if(!ev.next(ctx)) {
            drained = true;
            return null;
          }
          sub = ret.iter(ctx);
        }
      }
    };
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    final Iterator<Clause> iter = clauses.iterator();
    while(iter.hasNext()) {
      // the first round of constant propagation is free
      final Clause c = iter.next();
      c.compile(ctx, scp);
      if(c instanceof Let) ((Let) c).bindConst(ctx);
    }
    ret = ret.compile(ctx, scp);

    // the other optimizations are applied until nothing changes any more
    boolean changed;
    do {
      // remove FLWOR expressions when all clauses were removed
      if(clauses.isEmpty()) {
        ctx.compInfo(QueryText.OPTFLWOR, this);
        return ret;
      }

      // inline let expressions if they are used only once (and not in a loop)
      changed = inlineLets(ctx);

      // clean unused variables from group-by and order-by expression
      changed |= cleanDeadVars(ctx);

      // slide let clauses out to avoid repeated evaluation
      changed |= slideLetsOut(ctx);
    } while(changed);
    return this;
  }

  /**
   * Inline let expressions if they are used only once (and not in a loop).
   * @param ctx query context
   * @return change flag
   */
  private boolean inlineLets(final QueryContext ctx) {
    boolean change = false;
    for(int i = 0; i < clauses.size(); i++) {
      final Clause c = clauses.get(i);
      if(c instanceof Let) {
        final Let lt = (Let) c;
        if(lt.expr.uses(Use.NDT)) continue;
        final int uses = count(lt.var, i);
        if(uses == 0) {
          ctx.compInfo(QueryText.OPTVAR, lt.var);
          clauses.remove(i--);
          change = true;
        } else if(uses > 0 && !lt.expr.uses(Use.CTX) && !lt.score) {
          for(int j = i + 1; j < uses; j++) {
            // [LW] implement inlining
          }
        }
      }
    }
    return change;
  }

  /**
   * Cleans dead entries from the tuples that {@link GroupBy} and {@link OrderBy} handle.
   * @param ctx query context
   * @return change flag
   */
  private boolean cleanDeadVars(final QueryContext ctx) {
    final BitArray used = new BitArray();
    final VarVisitor marker = new VarVisitor() {
      @Override
      public boolean used(final LocalVarRef ref) {
        used.set(ref.var.id);
        return true;
      }
    };

    ret.visitVars(marker);
    boolean change = false;
    for(int i = clauses.size(); --i >= 0;) {
      final Clause curr = clauses.get(i);
      change |= curr.clean(ctx, used);
      curr.visitVars(marker);
    }
    return change;
  }

  /**
   * Optimization pass which tries to slide let expressions out of loops. Care is taken
   * that no unnecessary relocations are done.
   * @param ctx query context
   * @return {@code true} if there were relocations, {@code false} otherwise
   */
  private boolean slideLetsOut(final QueryContext ctx) {
    boolean change = false;
    for(int i = 1; i < clauses.size(); i++) {
      final Clause l = clauses.get(i);
      if(!(l instanceof Let) || l.uses(Use.NDT) || l.uses(Use.CNS)) continue;
      final Let let = (Let) l;

      // find insertion position
      int insert = -1;
      for(int j = i; --j >= 0;) {
        final Clause curr = clauses.get(j);
        if(!curr.skippable(let)) break;
        // insert directly above the highest skippable for or window clause
        // this guarantees that no unnecessary swaps occur
        if(curr instanceof For || curr instanceof Window) insert = j;
      }

      if(insert >= 0) {
        clauses.add(insert, clauses.remove(i));
        if(!change) ctx.compInfo(QueryText.OPTFORLET);
        change = true;
        // it's safe to go on because clauses below the current one are never touched
      }
    }
    return change;
  }

  /**
   * Checks how often a variable is used within this FLWOR expression.
   * @param v variable
   * @param p position of the declaring clause
   * @return {@code 0} for no uses, {@code -1} for more than one and {@code n} > 0
   *   if the only usage is in the {@code n}-th clause ({@code clauses.size()}
   *   means return expression)
   */
  private int count(final Var v, final int p) {
    final int[] count = new int[1];
    final VarVisitor uses = new VarVisitor() {
      @Override
      public boolean used(final LocalVarRef ref) {
        return !ref.var.is(v) || ++count[0] == 1;
      }
    };

    final int sz = clauses.size();
    int n = sz;
    for(int i = p + 1; i < sz; i++) {
      if(!clauses.get(i).visitVars(uses)) return -1;
      if(count[0] == 1 && n == sz) n = i;
    }
    if(!ret.visitVars(uses)) return -1;
    return n * count[0];
  }

  @Override
  public boolean uses(final Use u) {
    if(u == Use.VAR || u == Use.X30 && xq30) return true;
    for(final Clause cls : clauses) if(cls.uses(u)) return true;
    return ret.uses(u);
  }

  @Override
  public boolean removable(final Var v) {
    for(final Clause cl : clauses) if(!cl.removable(v)) return false;
    return ret.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    for(final Clause cl : clauses) cl.remove(v);
    return ret.remove(v);
  }

  @Override
  public boolean visitVars(final VarVisitor visitor) {
    for(final Clause cl : clauses) if(!cl.visitVars(visitor)) return false;
    if(!ret.visitVars(visitor)) return false;
    for(int i = clauses.size(); --i >= 0;)
      if(!clauses.get(i).undeclare(visitor)) return false;
    return true;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    for(final Clause cl : clauses) cl.plan(e);
    ret.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Clause cl : clauses) sb.append(cl).append(' ');
    return sb.append(QueryText.RETURN).append(' ').append(ret).toString();
  }

  @Override
  public void checkUp() throws QueryException {
    for(final Clause clause : clauses) clause.checkUp();
    ret.checkUp();
  }

  @Override
  public boolean databases(final StringList db) {
    for(final Clause clause : clauses) if(!clause.databases(db)) return false;
    return ret.databases(db);
  }

  /**
   * Evaluator for FLWOR clauses.
   *
   * @author BaseX Team 2005-12, BSD License
   * @author Leo Woerteler
   */
  interface Eval {
    /**
     * Makes the next evaluation step if available. This method is guaranteed
     * to not be called again if it has once returned {@code false}.
     * @param ctx query context
     * @return {@code true} if step was made, {@code false} if no more
     * results exist
     * @throws QueryException evaluation exception
     */
    boolean next(final QueryContext ctx) throws QueryException;
  }

  /**
   * A FLWOR clause.
   *
   * @author BaseX Team 2005-12, BSD License
   * @author Leo Woerteler
   */
  public abstract static class Clause extends ParseExpr {
    /** All variables declared in this clause. */
    Var[] vars;
    /**
     * Constructor.
     * @param ii input info
     * @param vs declared variables
     */
    protected Clause(final InputInfo ii, final Var... vs) {
      super(ii);
      vars = vs;
    }

    /**
     * Cleans unused variables from this clause.
     * @param ctx query context
     * @param used list of the IDs of all variables used in the following clauses
     * @return {@code true} if something changed, {@code false} otherwise
     */
    @SuppressWarnings("unused")
    boolean clean(final QueryContext ctx, final BitArray used) {
      return false;
    }

    /**
     * Evaluates the clause.
     * @param sub wrapped evaluator
     * @return evaluator
     */
    abstract Eval eval(final Eval sub);

    @Override
    public abstract Clause compile(QueryContext ctx, final VarScope scp)
        throws QueryException;

    @Deprecated
    @Override
    public Iter iter(final QueryContext ctx) throws QueryException {
      throw Util.notexpected();
    }

    @Deprecated
    @Override
    public Value value(final QueryContext ctx) throws QueryException {
      throw Util.notexpected();
    }

    @Deprecated
    @Override
    public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
      throw Util.notexpected();
    }

    /**
     * Checks if the given {@link Let} clause can be slided over this clause.
     * @param let let clause
     * @return result of check
     */
    boolean skippable(final Let let) {
      return let.visitVars(new VarVisitor() {
        @Override
        public boolean used(final LocalVarRef ref) {
          for(final Var v : vars) if(v.is(ref.var)) return false;
          return true;
        }
      });
    }

    /**
     * Undeclares all declared variables.
     * @param visitor variable visitor
     * @return continue
     */
    final boolean undeclare(final VarVisitor visitor) {
      for(int i = vars.length; --i >= 0;) if(!visitor.undeclared(vars[i])) return false;
      return true;
    }

    /**
     * All declared variables of this clause.
     * @return declared variables
     */
    public final Var[] vars() {
      return vars;
    }

    /**
     * Checks if the given variable is declared by this clause.
     * @param v variable
     * @return {code true} if the variable was declared here, {@code false} otherwise
     */
    public final boolean declares(final Var v) {
      for(final Var decl : vars) if(v.is(decl)) return true;
      return false;
    }
  }
}
