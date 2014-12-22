package org.basex.query.expr.gflwor;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.GFLWOR.Clause;
import org.basex.query.expr.gflwor.GFLWOR.Eval;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * the GFLWOR {@code window} clause.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class Window extends Clause {
  /** {@code sliding window} flag. */
  private final boolean sliding;
  /** The window variable. */
  private final Var var;
  /** The sequence. */
  private Expr expr;
  /** The start condition. */
  private Condition start;
  /** the {@code only} flag. */
  private final boolean only;
  /** The end condition, possibly {@code null}. */
  private Condition end;

  /**
   * Constructor.
   * @param info input info
   * @param sliding {@code sliding window} flag
   * @param var window variable
   * @param expr sequence
   * @param start start condition
   * @param only {@code only} flag
   * @param end end condition
   * @throws QueryException query exception
   */
  public Window(final InputInfo info, final boolean sliding, final Var var, final Expr expr,
      final Condition start, final boolean only, final Condition end) throws QueryException {
    super(info, vars(var, start, end, info));
    this.sliding = sliding;
    this.var = var;
    this.expr = expr;
    this.start = start;
    this.only = only;
    this.end = end;
  }

  /**
   * Gathers all non-{@code null} variables.
   * @param vr window variable
   * @param st start condition
   * @param nd end condition, might be {@code null}
   * @param info input info for the error message
   * @return non-{@code null} variables
   * @throws QueryException query exception if the variable names aren't unique
   */
  private static Var[] vars(final Var vr, final Condition st, final Condition nd,
      final InputInfo info) throws QueryException {
    // determine the size of the array beforehand
    final int stn = st.nVars();
    final Var[] vs = new Var[1 + stn + (nd == null ? 0 : nd.nVars())];

    // write variables to the array
    st.writeVars(vs, 0);
    if(nd != null) nd.writeVars(vs, stn);
    final int vl = vs.length;
    vs[vl - 1] = vr;

    // check for duplicates
    for(int v = 0; v < vl; v++) {
      final Var var = vs[v];
      for(int w = v; --w >= 0;)
        if(var.name.eq(vs[w].name)) throw WINDOWUNIQ_X.get(info, vs[w]);
    }
    return vs;
  }

  @Override
  Eval eval(final Eval sub) {
    return sliding ? slidingEval(sub) : end == null ? tumblingEval(sub) : tumblingEndEval(sub);
  }

  /**
   * Evaluator for tumbling windows.
   * @param sub wrapped evaluator
   * @return evaluator for tumbling windows
   */
  private Eval tumblingEval(final Eval sub) {
    return new TumblingEval() {
      /** Values for the current start item. */
      private Item[] vals;
      /** Position of the start item. */
      private long spos;
      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        while(true) {
          // find first item
          final Item fst = vals != null ? vals[0] : findStart(qc) ? curr : null;

          // find end item
          if(fst != null) {
            final ValueBuilder window = new ValueBuilder(
                new Item[] {fst, null, null, null}, 1);
            final Item[] st = vals == null ? new Item[] { curr, prev, next } : vals;
            final long ps = vals == null ? p : spos;
            vals = null;

            while(readNext()) {
              if(start.matches(qc, curr, p, prev, next)) {
                vals = new Item[]{ curr, prev, next };
                spos = p;
                break;
              }
              window.add(curr);
            }

            start.bind(qc, st[0], ps, st[1], st[2]);
            qc.set(var, window.value(), info);
            return true;
          }

          // no more iterations from above, we're done here
          if(!prepareNext(qc, sub)) return false;
          vals = null;
        }
      }
    };
  }

  /**
   * Evaluator for tumbling windows with an {@code end} condition.
   * @param sub wrapped evaluator
   * @return evaluator for tumbling windows
   */
  private Eval tumblingEndEval(final Eval sub) {
    return new TumblingEval() {
      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        while(true) {
          if(findStart(qc)) {
            // find end item
            final ValueBuilder window = new ValueBuilder();
            boolean found = false;
            do {
              window.add(curr);
              if(end.matches(qc, curr, p, prev, next)) {
                found = true;
                break;
              }
            } while(readNext());

            // don't return dangling items if the {@code only} flag was specified
            if(found || !only) {
              qc.set(var, window.value(), info);
              return true;
            }
          }

          // no more iterations from above, we're done here
          if(!prepareNext(qc, sub)) return false;
        }
      }
    };
  }

  /**
   * Evaluator for sliding windows.
   * @param sub wrapped evaluator
   * @return evaluator for tumbling windows
   */
  private Eval slidingEval(final Eval sub) {
    return new WindowEval() {
      /** Queue holding the items of the current window. */
      private final ArrayDeque<Item> queue = new ArrayDeque<>();
      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        while(true) {
          Item curr, next = null;
          while((curr = advance()) != null) {
            next = queue.peekFirst();
            if(next == null && (next = next()) != null) queue.addLast(next);
            if(start.matches(qc, curr, p, prev, next)) break;
            prev = curr;
          }

          if(curr != null) {
            final ValueBuilder cache = new ValueBuilder();
            final Iterator<Item> qiter = queue.iterator();
            // the first element is already the {@code next} one
            if(qiter.hasNext()) qiter.next();
            Item pr = prev, it = curr, nx = next;
            long ps = p;
            do {
              cache.add(it);
              if(end.matches(qc, it, ps++, pr, nx)) break;
              pr = it;
              it = nx;
              if(qiter.hasNext()) {
                nx = qiter.next();
              } else {
                nx = next();
                if(nx != null) queue.addLast(nx);
              }
            } while(it != null);

            // return window if end was found or {@code only} isn't set
            if(!(it == null && only)) {
              start.bind(qc, curr, p, prev, next);
              prev = curr;
              qc.set(var, cache.value(), info);
              return true;
            }
          }

          // abort if no more tuples from above
          if(!prepareNext(qc, sub)) return false;
          queue.clear();
        }
      }

      /**
       * tries to advance the start of the queue by one element and returns the removed
       * element in case of success, {@code null} otherwise.
       * @return removed element or {@code null}
       * @throws QueryException evaluation exception
       */
      private Item advance() throws QueryException {
        Item it = queue.pollFirst();
        if(it == null) it = next();
        if(it != null) p++;
        return it;
      }
    };
  }

  @Override
  public Clause compile(final QueryContext qc, final VarScope scp) throws QueryException {
    expr = expr.compile(qc, scp);
    start.compile(qc, scp);
    if(end != null) end.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Clause optimize(final QueryContext qc, final VarScope sc) throws QueryException {
    final SeqType st = expr.seqType();
    var.refineType(st.withOcc(Occ.ZERO_MORE), qc, info);
    return this;
  }

  @Override
  public boolean has(final Flag flag) {
    return expr.has(flag) || start.has(flag) || end != null && end.has(flag);
  }

  @Override
  public boolean removable(final Var v) {
    return expr.removable(v) && start.removable(v) && (end == null || end.removable(v));
  }

  @Override
  public VarUsage count(final Var v) {
    final VarUsage us = end == null ? start.count(v) : start.count(v).plus(end.count(v));
    return us == VarUsage.NEVER ? expr.count(v) : VarUsage.MORE_THAN_ONCE;
  }

  @Override
  public Clause inline(final QueryContext qc, final VarScope scp, final Var v, final Expr ex)
      throws QueryException {

    final Expr e = expr.inline(qc, scp, v, ex);
    final Condition st = start.inline(qc, scp, v, ex);
    final Condition en = end == null ? null : end.inline(qc, scp, v, ex);
    if(e != null) expr = e;
    if(st != null) start = st;
    if(en != null) end = en;
    return e != null || st != null || en != null ? optimize(qc, scp) : null;
  }

  @Override
  public Window copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Var v = scp.newCopyOf(qc, var);
    vs.put(var.id, v);
    try {
      return new Window(info, sliding, v, expr.copy(qc, scp, vs),
          start.copy(qc, scp, vs), only, end != null ? end.copy(qc, scp, vs) : null);
    } catch(final QueryException e) {
      // checks have already been done
      throw Util.notExpected(e);
    }
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor) && start.accept(visitor) &&
        (end == null || end.accept(visitor)) && visitor.declared(var);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem(token(SLIDING), token(sliding));
    var.plan(e);
    expr.plan(e);
    start.plan(e);
    if(end != null) end.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(FOR).append(' ').append(
        sliding ? SLIDING : TUMBLING).append(' ').append(WINDOW).append(' ').append(var
            ).append(' ').append(IN).append(' ').append(expr).append(' ').append(start);
    if(end != null) {
      if(only) sb.append(' ').append(ONLY);
      sb.append(' ').append(end);
    }
    return sb.toString();
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(expr, start, end);
  }

  @Override
  void calcSize(final long[] minMax) {
    minMax[0] = 0;
    if(expr.isEmpty()) minMax[1] = 0;
  }

  @Override
  public int exprSize() {
    return expr.exprSize() + start.exprSize() + (end == null ? 0 : end.exprSize());
  }

  /**
   * A window {@code start} of {@code end} condition.
   *
   * @author BaseX Team 2005-14, BSD License
   * @author Leo Woerteler
   */
  public static final class Condition extends Single {
    /** Start condition flag. */
    private final boolean start;
    /** Item variable. */
    private final Var item;
    /** Position variable. */
    private final Var pos;
    /** Previous item. */
    private final Var prev;
    /** Next item. */
    private final Var next;

    /**
     * Constructor.
     * @param start start condition flag
     * @param item item variable
     * @param pos position variable
     * @param prev previous variable
     * @param next next variable
     * @param cond condition expression
     * @param info input info
     */
    public Condition(final boolean start, final Var item, final Var pos, final Var prev,
        final Var next, final Expr cond, final InputInfo info) {
      super(info, cond);
      this.start = start;
      this.item = item;
      this.pos = pos;
      this.prev = prev;
      this.next = next;
    }

    @Override
    public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
      expr = expr.compile(qc, scp).optimizeEbv(qc, scp);
      return this;
    }

    @Override
    public Condition optimize(final QueryContext qc, final VarScope scp) {
      return this;
    }

    @Override
    public Condition inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
        throws QueryException {
      return (Condition) super.inline(qc, scp, var, ex);
    }

    @Override
    public Condition copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
      final Var it = item == null ? null : scp.newCopyOf(qc, item),
                ps = pos  == null ? null : scp.newCopyOf(qc, pos),
                pr = prev == null ? null : scp.newCopyOf(qc, prev),
                nx = next == null ? null : scp.newCopyOf(qc, next);
      if(it != null) vs.put(item.id, it);
      if(ps != null) vs.put(pos.id,  ps);
      if(pr != null) vs.put(prev.id, pr);
      if(nx != null) vs.put(next.id, nx);
      return new Condition(start, it, ps, pr, nx, expr.copy(qc, scp, vs), info);
    }

    /**
     * Number of non-{@code null} variables in this condition.
     * @return number of variables
     */
    private int nVars() {
      int i = 0;
      if(item != null) i++;
      if(pos  != null) i++;
      if(prev != null) i++;
      if(next != null) i++;
      return i;
    }

    /**
     * Checks if this condition binds the item following the current one in the input.
     * @return result of check
     */
    private boolean usesNext() {
      return next != null;
    }

    /**
     * Write all non-{@code null} variables in this condition to the given array.
     *
     * @param arr array to write to
     * @param p start position
     */
    private void writeVars(final Var[] arr, final int p) {
      int i = p;
      if(item != null) arr[i++] = item;
      if(pos  != null) arr[i++] = pos;
      if(prev != null) arr[i++] = prev;
      if(next != null) arr[i]   = next;
    }

    /**
     * Binds the variables and checks if the item satisfies this condition.
     * @param qc query context for variable binding
     * @param it current item
     * @param p position in the input sequence
     * @param pr previous item
     * @param nx next item
     * @return {@code true} if {@code it} matches the condition, {@code false} otherwise
     * @throws QueryException query exception
     */
    private boolean matches(final QueryContext qc, final Item it, final long p, final Item pr,
        final Item nx) throws QueryException {
      // bind variables
      bind(qc, it, p, pr, nx);

      // evaluate as effective boolean value
      return expr.ebv(qc, info).bool(info);
    }

    /**
     * Binds this condition's variables to the given values.
     * @param qc query context
     * @param it current item
     * @param p position
     * @param pr previous item
     * @param nx next item
     * @throws QueryException query exception
     */
    private void bind(final QueryContext qc, final Item it, final long p, final Item pr,
        final Item nx) throws QueryException {
      if(item != null) qc.set(item, it == null ? Empty.SEQ : it, info);
      if(pos  != null) qc.set(pos,  Int.get(p),                  info);
      if(prev != null) qc.set(prev, pr == null ? Empty.SEQ : pr, info);
      if(next != null) qc.set(next, nx == null ? Empty.SEQ : nx, info);
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder(start ? START : END);
      if(item != null) sb.append(' ').append(item);
      if(pos  != null) sb.append(' ').append(AT).append(' ').append(pos);
      if(prev != null) sb.append(' ').append(PREVIOUS).append(' ').append(prev);
      if(next != null) sb.append(' ').append(NEXT).append(' ').append(next);
      return sb.append(' ').append(WHEN).append(' ').append(expr).toString();
    }

    @Override
    public void plan(final FElem plan) {
      final FElem e = new FElem(start ? START : END);

      // mapping variable names to roles
      if(item != null) e.add(planAttr(VAR, token(item.toString())));
      if(pos  != null) e.add(planAttr(token(AT), token(pos.toString())));
      if(prev != null) e.add(planAttr(token(PREVIOUS), token(prev.toString())));
      if(next != null) e.add(planAttr(token(NEXT), token(next.toString())));

      // IDs and stack slots
      if(item != null) item.plan(e);
      if(pos  != null) pos.plan(e);
      if(prev != null) prev.plan(e);
      if(next != null) next.plan(e);

      expr.plan(e);
      plan.add(e);
    }

    @Override
    public boolean accept(final ASTVisitor visitor) {
      return (item == null || visitor.declared(item))
          && (pos  == null || visitor.declared(pos))
          && (prev == null || visitor.declared(prev))
          && (next == null || visitor.declared(next))
          && expr.accept(visitor);
    }

    @Override
    public int exprSize() {
      return expr.exprSize();
    }
  }

  /**
   * Evaluator for the Window clause.
   *
   * @author BaseX Team 2005-14, BSD License
   * @author Leo Woerteler
   */
  private abstract class WindowEval implements Eval {
    /** Expression iterator. */
    private Iter iter;
    /** Previous item. */
    Item prev;
    /** Current position. */
    long p;

    /**
     * Reads the next item from {@code iter} if it isn't {@code null} and sets it to
     * {@code null} if it's drained.
     * @return success flag
     * @throws QueryException evaluation exception
     */
    final Item next() throws QueryException {
      if(iter == null) return null;
      final Item it = iter.next();
      if(it == null) iter = null;
      return it;
    }

    /**
     * Tries to prepare the next round.
     * @param qc query context
     * @param sub sub-evaluator
     * @return {@code true} if the next round could be prepared, {@code false} otherwise
     * @throws QueryException evaluation exception
     */
    boolean prepareNext(final QueryContext qc, final Eval sub) throws QueryException {
      if(!sub.next(qc)) return false;
      iter = expr.iter(qc);
      prev = null;
      p = 0;
      return true;
    }
  }

  /**
   * Evaluator for the Tumbling Window clause.
   *
   * @author BaseX Team 2005-14, BSD License
   * @author Leo Woerteler
   */
  private abstract class TumblingEval extends WindowEval {
    /** If the next item is used. */
    private final boolean popNext = start.usesNext() || end != null && end.usesNext();
    /** Current item. */
    Item curr;
    /** Next item. */
    Item next;

    /**
     * Reads a new current item and populates the {@code nxt} variable if it's used.
     * @return next item
     * @throws QueryException evaluation exception
     */
    final boolean readNext() throws QueryException {
      prev = curr;
      p++;
      final Item n = next();
      // serve the stored item if available
      if(next != null) {
        curr = next;
        next = n;
      } else if(n != null && popNext) {
        // only assign if necessary
        next = next();
        curr = n;
      } else {
        curr = n;
      }
      return curr != null;
    }

    /**
     * Finds the next item in the sequence satisfying the start condition.
     * @param qc query context
     * @return {@code true} if the current binding satisfies the start condition,
     *   {@code false} otherwise
     * @throws QueryException evaluation exception
     */
    final boolean findStart(final QueryContext qc) throws QueryException {
      while(readNext())
        if(start.matches(qc, curr, p, prev, next)) return true;
      return false;
    }

    @Override
    boolean prepareNext(final QueryContext qc, final Eval sub) throws QueryException {
      if(!super.prepareNext(qc, sub)) return false;
      curr = null;
      next = null;
      return true;
    }
  }
}
