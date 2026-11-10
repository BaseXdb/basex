package org.basex.query.expr;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.func.map.*;
import org.basex.query.func.map.MapMerge.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Replaces the accumulating expressions of a function body with the accumulated values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class Unfolding {
  /** Accumulating operations. */
  public enum Op {
    /** Sequence construction. */ LIST,
    /** String concatenation. */ CONCAT,
    /** Addition. */ ADD,
    /** Multiplication. */ MULTIPLY,
    /** Map insertion. */ PUT,
    /** Map removal. */ REMOVE;

    /**
     * Indicates if the order of the operands is relevant.
     * @return result of check
     */
    boolean ordered() {
      return this == LIST || this == CONCAT;
    }

    /**
     * Indicates if a map is updated (the map must be the first operand).
     * @return result of check
     */
    boolean map() {
      return this == PUT || this == REMOVE;
    }
  }

  /** Input info (can be {@code null}). */
  protected final InputInfo info;
  /** Accumulator (can be {@code null}). */
  protected final Var acc;
  /** Supported operations. */
  private final EnumSet<Op> supported;

  /** State of the rewrite, shared with nested rewrites. */
  private final State state;

  /**
   * Constructor.
   * @param acc accumulator (can be {@code null})
   * @param supported supported operations
   * @param info input info (can be {@code null})
   */
  public Unfolding(final Var acc, final EnumSet<Op> supported, final InputInfo info) {
    this(acc, supported, info, new State());
  }

  /**
   * Constructor for a nested rewrite that shares the state of another rewrite.
   * @param uf rewrite
   */
  Unfolding(final Unfolding uf) {
    this(uf.acc, uf.supported, uf.info, uf.state);
  }

  /**
   * Constructor.
   * @param acc accumulator (can be {@code null})
   * @param supported supported operations
   * @param info input info (can be {@code null})
   * @param state state of the rewrite
   */
  private Unfolding(final Var acc, final EnumSet<Op> supported, final InputInfo info,
      final State state) {
    this.acc = acc;
    this.supported = supported;
    this.info = info;
    this.state = state;
  }

  /**
   * Checks if an expression may contain accumulating expressions.
   * @param expr expression (can be {@code null})
   * @return result of check
   */
  public static boolean candidate(final Expr expr) {
    return expr instanceof If || expr instanceof ASwitch || expr instanceof GFLWOR ||
        expr instanceof Pipeline || expr instanceof List || expr instanceof Concat ||
        expr instanceof Arith || expr instanceof MapPut || expr instanceof MapRemove;
  }

  /**
   * Rewrites an accumulating expression whose operands contain the hole.
   * @param expr accumulating expression
   * @param o operation
   * @param cc compilation context
   * @return rewritten expression
   * @throws QueryException query exception
   */
  public final Expr step(final Expr expr, final Op o, final CompileContext cc)
      throws QueryException {
    // all accumulating expressions must have the same operation
    if(supported.contains(o) && (state.op == null || state.op == o)) {
      final Expr[] ops = expr.args();
      final int h = Checks.index(ops, this::hole);
      if(h != -1) {
        final Expr step = replace(ops, h, o, cc);
        if(step != null) {
          state.op = o;
          return step;
        }
      } else {
        final Expr dist = distribute(expr, cc);
        if(dist != null) return dist.unfold(this, cc);
      }
    }
    return base(expr, cc);
  }

  /**
   * Distributes an accumulating expression over the branches of an operand with a hole.
   * @param expr accumulating expression
   * @param cc compilation context
   * @return distributed expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr distribute(final Expr expr, final CompileContext cc) throws QueryException {
    if(!(expr instanceof final Arr arr)) return null;
    // a single operand with the hole: no exponential growth
    final Expr[] ops = arr.exprs;
    final int i = Checks.index(ops, this::contains);
    if(i == -1) return null;
    for(int o = i + 1; o < ops.length; o++) {
      if(contains(ops[o])) return null;
    }
    final Expr branching = ops[i];
    final Expr[] conds = branching.conditions();
    if(conds == null || Checks.any(conds, cond -> cond.has(Flag.NDT) || contains(cond))) {
      return null;
    }

    // the operand is replaced by each branch: no need to copy it
    ops[i] = Empty.VALUE;
    // (A, if(C) then X else Y) → if(C) then (A, X) else (A, Y)
    return branching.mapBranches(branch -> {
      final Arr copy = (Arr) arr.copy(cc, new IntObjectMap<>());
      copy.exprs[i] = branch;
      try {
        return copy.optimize(cc);
      } catch(final QueryException ex) {
        // the branch may never be evaluated: raise the error at runtime
        return FnError.get(ex);
      }
    }, cc);
  }

  /**
   * Returns the operation of the accumulating expressions.
   * @return operation (can be {@code null})
   */
  public final Op op() {
    return state.op;
  }

  /**
   * Indicates if all branches returned the accumulator or accumulated a value.
   * @return result of check
   */
  public final boolean valid() {
    return state.valid;
  }

  /**
   * Indicates if the accumulator is returned unchanged by some branches.
   * @return result of check
   */
  public final boolean unchanged() {
    return state.unchanged;
  }

  /**
   * Indicates if the accumulator is the first operand of the accumulating expressions.
   * @return result of check
   */
  public final boolean first() {
    return state.first;
  }

  /**
   * Checks if an operand is the hole.
   * @param expr operand
   * @return result of check
   */
  protected boolean hole(final Expr expr) {
    return expr instanceof final VarRef vr && vr.var == acc;
  }

  /**
   * Checks if an expression contains the hole.
   * @param expr expression
   * @return result of check
   */
  protected boolean contains(final Expr expr) {
    return expr.uses(acc);
  }

  /**
   * Creates the replacement for an accumulating expression.
   * @param ops operands
   * @param hole position of the hole
   * @param o operation
   * @param cc compilation context
   * @return replacement (can be {@code null})
   * @throws QueryException query exception
   */
  protected Expr replace(final Expr[] ops, final int hole, final Op o, final CompileContext cc)
      throws QueryException {
    // $acc OP X, X OP $acc: same order in all branches
    final boolean frst = hole == 0;
    if(!frst && (hole != ops.length - 1 || o.map()) || state.op != null && state.first != frst) {
      return null;
    }
    state.first = frst;
    final Expr[] rest = Array.remove(ops, hole);
    // integers and decimals only: type promotion of other numbers depends on the order
    if((o == Op.ADD || o == Op.MULTIPLY) && !rest[0].seqType().instanceOf(Types.DECIMAL_O)) {
      return null;
    }
    return o == Op.PUT ? cc.function(Function._MAP_ENTRY, info, rest) :
      rest.length == 1 ? rest[0] : o == Op.LIST ? List.get(cc, info, rest) :
      new Concat(info, rest).optimize(cc);
  }

  /**
   * Rewrites an expression that is no accumulating expression with a hole.
   * @param expr expression
   * @param cc compilation context
   * @return rewritten expression
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Expr base(final Expr expr, final CompileContext cc) throws QueryException {
    if(hole(expr)) {
      state.unchanged = true;
      return Empty.VALUE;
    }
    state.valid = false;
    return expr;
  }

  /**
   * Combines the initial value and the accumulated values.
   * @param init initial value (can be {@code null})
   * @param values accumulated values
   * @param cc compilation context
   * @return reduced expression
   * @throws QueryException query exception
   */
  public final Expr reduce(final Expr init, final Expr values, final CompileContext cc)
      throws QueryException {
    final Op op = state.op;
    final boolean first = state.first, zero = init == null ||
        op == Op.ADD && init instanceof final Itr itr && itr.itr() == 0;
    final Expr[] ops = zero ? new Expr[] { values } :
      first ? new Expr[] { init, values } : new Expr[] { values, init };
    return switch(op == null ? Op.LIST : op) {
      case LIST -> List.get(cc, info, ops);
      case CONCAT -> new Concat(info, ops).optimize(cc);
      case ADD -> cc.function(Function.SUM, info, List.get(cc, info, ops));
      // products: keep the order of the multiplications (intermediate overflows)
      case MULTIPLY -> {
        final Expr mult = cc.function(Function.OP, info, Str.get(Calc.MULTIPLY.name));
        yield init == null ? cc.function(Function.FOLD_RIGHT, info, values, Itr.ONE, mult) :
          cc.function(Function.FOLD_LEFT, info, values, init, mult);
      }
      case PUT -> {
        final XQMap options = XQMap.get(Str.get(MergeOptions.DUPLICATES.name()),
            Str.get(Duplicates.USE_LAST.toString()));
        yield cc.function(Function._MAP_MERGE, info, List.get(cc, info, ops), options);
      }
      case REMOVE -> cc.function(Function._MAP_REMOVE, info, init, values);
    };
  }

  /**
   * State of a rewrite.
   */
  private static final class State {
    /** Operation of the accumulating expressions (can be {@code null}). */
    private Op op;
    /** Indicates if the accumulator is the first operand of the accumulating expressions. */
    private boolean first;
    /** Indicates if the accumulator is returned unchanged by some branches. */
    private boolean unchanged;
    /** Indicates if all branches return the accumulator or accumulate a value. */
    private boolean valid = true;
  }
}
