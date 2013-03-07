package org.basex.query;
import org.basex.query.value.Value;
import org.basex.query.var.Var;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * The query stack, containing local variable bindings of all active scopes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public class QueryStack {
  /** Initial stack size. */
  private static final int INIT = 1 << 5;
  /** The stack. */
  private Value[] stack = new Value[INIT];
  /** The frame pointer, marking the start of the current stack frame. */
  private int fp;
  /** The stack limit, marking the end of the current stack frame. */
  private int sl;

  /**
   * Enters a new stack frame.
   * @param size size of this frame
   * @return stack pointer of the old frame
   */
  public int enterFrame(final int size) {
    final int nsl = sl + size;
    if(nsl > stack.length) {
      int len = stack.length;
      do len *= 2; while(nsl > len);
      final Value[] nst = new Value[len];
      System.arraycopy(stack, 0, nst, 0, sl);
      stack = nst;
    }
    final int ret = fp;
    fp = sl;
    sl = nsl;
    return ret;
  }

  /**
   * Exits a stack frame and makes all bound variables eligible for garbage collection.
   * @param fpt frame pointer of the underlying stack frame
   */
  public void exitFrame(final int fpt) {
    while(--sl >= fp) stack[sl] = null;
    sl = fp;
    fp = fpt;
    if(stack.length > INIT && sl <= stack.length / 4) {
      int len = stack.length;
      do len /= 2; while(len > INIT && sl <= len / 4);
      final Value[] nst = new Value[len];
      System.arraycopy(stack, 0, nst, 0, sl);
      stack = nst;
    }
  }

  /**
   * Calculates the position of the given variable on the stack.
   * @param v variable
   * @return position
   */
  private int pos(final Var v) {
    final int pos = fp + v.slot;
    if(pos < fp || sl <= pos) throw Util.notexpected(v);
    return pos;
  }

  /**
   * Gets the value bound to the given variable in the current stack frame.
   * @param var variable
   * @return bound value
   */
  public Value get(final Var var) {
    return stack[pos(var)];
  }

  /**
   * Sets the value of the given variable in the current stack frame.
   * @param var variable to bind the value to
   * @param val value to bind
   * @param ctx query context
   * @param ii input info
   * @throws QueryException if the value does not have the right type
   */
  public void set(final Var var, final Value val, final QueryContext ctx,
      final InputInfo ii) throws QueryException {
    stack[pos(var)] = var.checkType(val, ctx, ii);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Util.name(this)).append('[');
    for(int i = 0; i < sl; i++) sb.append(i == 0 ? "" : ", ").append(stack[i]);
    return sb.append(']').toString();
  }
}
