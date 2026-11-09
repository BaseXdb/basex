package org.basex.query.expr;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.map.*;
import org.basex.util.*;

/**
 * Rewrites the accumulating expressions of a function body.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Unfolding {
  /** Accumulating operations. */
  public enum Op {
    /** Sequence construction. */ LIST,
    /** String concatenation. */ CONCAT,
    /** Addition. */ ADD,
    /** Multiplication. */ MULTIPLY,
    /** Map insertion. */ PUT;

    /**
     * Indicates if the order of the operands is relevant.
     * @return result of check
     */
    boolean ordered() {
      return this == LIST || this == CONCAT;
    }
  }

  /** Supported operations. */
  private final EnumSet<Op> supported;
  /** Operation of the accumulating expressions (can be {@code null}). */
  private Op op;

  /**
   * Constructor.
   * @param supported supported operations
   */
  protected Unfolding(final EnumSet<Op> supported) {
    this.supported = supported;
  }

  /**
   * Checks if an expression may contain accumulating expressions.
   * @param expr expression (can be {@code null})
   * @return result of check
   */
  public static boolean candidate(final Expr expr) {
    return expr instanceof If || expr instanceof ASwitch || expr instanceof GFLWOR ||
        expr instanceof Pipeline || expr instanceof List || expr instanceof Concat ||
        expr instanceof Arith || expr instanceof MapPut;
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
    if(supported.contains(o) && (op == null || op == o)) {
      final Expr[] ops = expr.args();
      final int h = Checks.index(ops, this::hole);
      final Expr step = h != -1 ? replace(ops, h, o, cc) : null;
      if(step != null) {
        op = o;
        return step;
      }
    }
    return base(expr);
  }

  /**
   * Returns the operation of the accumulating expressions.
   * @return operation (can be {@code null})
   */
  public final Op op() {
    return op;
  }

  /**
   * Checks if an operand is the hole.
   * @param expr operand
   * @return result of check
   */
  protected abstract boolean hole(Expr expr);

  /**
   * Creates the replacement for an accumulating expression.
   * @param ops operands
   * @param hole position of the hole
   * @param o operation
   * @param cc compilation context
   * @return replacement (can be {@code null})
   * @throws QueryException query exception
   */
  protected abstract Expr replace(Expr[] ops, int hole, Op o, CompileContext cc)
      throws QueryException;

  /**
   * Rewrites an expression that is no accumulating expression with a hole.
   * @param expr expression
   * @return rewritten expression
   */
  public Expr base(final Expr expr) {
    return expr;
  }
}
