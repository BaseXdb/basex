package org.basex.query.expr.gflwor;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * The GFLWOR {@code window} clause.
 *
 * @author BaseX Team 2005-21, BSD License
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
  /** The {@code only} flag. */
  private final boolean only;
  /** The end condition, possibly {@code null}. */
  private Condition end;

  /**
   * Constructor.
   * @param sliding {@code sliding window} flag
   * @param var window variable
   * @param expr input sequence
   * @param start start condition
   * @param only {@code only} flag
   * @param end end condition (can be {@code null})
   * @throws QueryException query exception
   */
  public Window(final boolean sliding, final Var var, final Expr expr, final Condition start,
      final boolean only, final Condition end) throws QueryException {
    super(var.info, SeqType.ITEM_ZM, vars(var, start, end));
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
   * @return non-{@code null} variables
   * @throws QueryException query exception if the variable names aren't unique
   */
  private static Var[] vars(final Var vr, final Condition st, final Condition nd)
      throws QueryException {

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
      for(int w = v; --w >= 0;) {
        if(var.name.eq(vs[w].name)) throw DUPLWIND_X.get(vr.info, vs[w]);
      }
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
            final ValueBuilder vb = new ValueBuilder(qc).add(fst);
            final Item[] st = vals == null ? new Item[] { curr, prev, next } : vals;
            final long ps = vals == null ? pos : spos;
            vals = null;

            while(readNext()) {
              if(start.matches(qc, curr, pos, prev, next)) {
                vals = new Item[] { curr, prev, next };
                spos = pos;
                break;
              }
              vb.add(curr);
            }

            start.bind(qc, st[0], ps, st[1], st[2]);
            qc.set(var, vb.value());
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
            final ValueBuilder vb = new ValueBuilder(qc);
            boolean found = false;
            do {
              vb.add(curr);
              if(end.matches(qc, curr, pos, prev, next)) {
                found = true;
                break;
              }
            } while(readNext());

            // don't return dangling items if the {@code only} flag was specified
            if(found || !only) {
              qc.set(var, vb.value());
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
            if(start.matches(qc, curr, pos, prev, next)) break;
            prev = curr;
          }

          if(curr != null) {
            final ValueBuilder vb = new ValueBuilder(qc);
            final Iterator<Item> iter = queue.iterator();
            // the first element is already the {@code next} one
            if(iter.hasNext()) iter.next();
            Item pr = prev, it = curr, nx = next;
            long ps = pos;
            do {
              vb.add(it);
              if(end.matches(qc, it, ps++, pr, nx)) break;
              pr = it;
              it = nx;
              if(iter.hasNext()) {
                nx = iter.next();
              } else {
                nx = next();
                if(nx != null) queue.addLast(nx);
              }
            } while(it != null);

            // return window if end was found or {@code only} isn't set
            if(!(it == null && only)) {
              start.bind(qc, curr, pos, prev, next);
              prev = curr;
              qc.set(var, vb.value());
              return true;
            }
          }

          // abort if no more tuples from above
          if(!prepareNext(qc, sub)) return false;
          queue.clear();
        }
      }

      /**
       * Tries to advance the start of the queue by one element and returns the removed
       * element in case of success, {@code null} otherwise.
       * @return removed element or {@code null}
       * @throws QueryException evaluation exception
       */
      private Item advance() throws QueryException {
        Item item = queue.pollFirst();
        if(item == null) item = next();
        if(item != null) pos++;
        return item;
      }
    };
  }

  @Override
  public Clause compile(final CompileContext cc) throws QueryException {
    expr = expr.compile(cc);
    start.compile(expr, cc);
    if(end != null) end.compile(expr, cc);
    return optimize(cc);
  }

  @Override
  public Clause optimize(final CompileContext cc) throws QueryException {
    exprType.assign(expr.seqType().union(Occ.ZERO));
    var.refineType(seqType(), cc);
    return this;
  }

  @Override
  public boolean has(final Flag... flags) {
    return expr.has(flags) || start.has(flags) || end != null && end.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext v) {
    return expr.inlineable(v) && start.inlineable(v) && (end == null || end.inlineable(v));
  }

  @Override
  public VarUsage count(final Var v) {
    final VarUsage us = end == null ? start.count(v) : start.count(v).plus(end.count(v));
    return us == VarUsage.NEVER ? expr.count(v) : VarUsage.MORE_THAN_ONCE;
  }

  @Override
  public Clause inline(final InlineContext ic) throws QueryException {
    final Expr inlined = expr.inline(ic);
    final Condition st = start.inline(ic);
    final Condition en = end == null ? null : end.inline(ic);
    if(inlined != null) expr = inlined;
    if(st != null) start = st;
    if(en != null) end = en;
    return inlined != null || st != null || en != null ? optimize(ic.cc) : null;
  }

  @Override
  public Window copy(final CompileContext cc, final IntObjMap<Var> vm) {
    try {
      return copyType(new Window(sliding, cc.copy(var, vm), expr.copy(cc, vm), start.copy(cc, vm),
          only, end != null ? end.copy(cc, vm) : null));
    } catch(final QueryException ex) {
      // checks have already been done
      throw Util.notExpected(ex);
    }
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor) && start.accept(visitor) &&
        (end == null || end.accept(visitor)) && visitor.declared(var);
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(expr);
    checkNoUp(start);
    checkNoUp(end);
  }

  @Override
  public void calcSize(final long[] minMax) {
    // number of results cannot be anticipated
    minMax[0] = 0;
    minMax[1] = expr.seqType().zero() ? 0 : -1;
  }

  @Override
  public int exprSize() {
    return expr.exprSize() + start.exprSize() + (end == null ? 0 : end.exprSize());
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Window)) return false;
    final Window w = (Window) obj;
    return sliding == w.sliding && var.equals(w.var) && expr.equals(w.expr) &&
        start.equals(w.start) && only == w.only && Objects.equals(end, w.end);
  }

  @Override
  public void plan(final QueryPlan plan) {
    final FElem elem = plan.attachVariable(plan.create(this, SLIDING, sliding), var, false);
    plan.add(elem, start, end, expr);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(FOR).token(sliding ? SLIDING : TUMBLING).token(WINDOW).token(var).token(IN).
      token(expr).token(start);
    if(end != null) {
      if(only) qs.token(ONLY);
      qs.token(end);
    }
  }

  /**
   * Evaluator for the Window clause.
   *
   * @author BaseX Team 2005-21, BSD License
   * @author Leo Woerteler
   */
  private abstract class WindowEval extends Eval {
    /** Expression iterator. */
    private Iter iter;
    /** Previous item. */
    Item prev;
    /** Current position. */
    long pos;

    /**
     * Reads the next item from {@code iter} if it is not {@code null} and sets it to
     * {@code null} if it is drained.
     * @return next item or {@code null}
     * @throws QueryException evaluation exception
     */
    final Item next() throws QueryException {
      if(iter == null) return null;
      final Item item = iter.next();
      if(item == null) iter = null;
      return item;
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
      pos = 0;
      return true;
    }
  }

  /**
   * Evaluator for the Tumbling Window clause.
   *
   * @author BaseX Team 2005-21, BSD License
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
     * Reads a new current item and populates the {@code next} variable if it's used.
     * @return next item
     * @throws QueryException evaluation exception
     */
    final boolean readNext() throws QueryException {
      prev = curr;
      pos++;
      final Item item = next();
      // serve the stored item if available
      if(next != null) {
        curr = next;
        next = item;
      } else if(item != null && popNext) {
        // only assign if necessary
        next = next();
        curr = item;
      } else {
        curr = item;
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
      while(readNext()) {
        if(start.matches(qc, curr, pos, prev, next)) return true;
      }
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
