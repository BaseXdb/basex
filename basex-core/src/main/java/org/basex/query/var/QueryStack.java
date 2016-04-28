package org.basex.query.var;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * The query stack, containing local variable bindings of all active scopes.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Leo Woerteler
 */
public final class QueryStack {
  /** Initial stack size. */
  private static final int INIT = 1 << 5;
  /** The currently assigned values. */
  private Value[] stack = new Value[INIT];
  /** The currently assigned variables. */
  private Var[] vars = new Var[INIT];
  /** The frame pointer, marking the start of the current stack frame. */
  private int start;
  /** The stack limit, marking the end of the current stack frame. */
  private int end;

  /**
   * Enters a new stack frame.
   * @param size size of this frame
   * @return stack pointer of the old frame
   */
  public int enterFrame(final int size) {
    final int e = end, ne = e + size;
    ensureCapacity(ne);
    final int s = start;
    start = e;
    end = ne;
    return s;
  }

  /**
   * Prepares the current stack frame to be reused.
   * @param size new frame size
   */
  public void reuseFrame(final int size) {
    final int s = start;
    ensureCapacity(s + size);
    final Value[] stck = stack;
    for(int e = end; --e >= s;) stck[e] = null;
    end = s + size;
  }

  /**
   * Exits a stack frame and makes all bound variables eligible for garbage collection.
   * @param frame frame pointer of the underlying stack frame
   */
  public void exitFrame(final int frame) {
    final int s = start;
    final Value[] stck = stack;
    for(int en = end; --en >= s;) stck[en] = null;
    end = s;
    start = frame;

    final int sl = stck.length;
    int len = sl;
    while(len > INIT && sl <= len >> 2) len >>= 1;
    if(len != sl) resize(len);
  }

  /**
   * Ensures that the query stack has at least the given size.
   * @param newSize required size of the stack
   */
  private void ensureCapacity(final int newSize) {
    final int sl = stack.length;
    int len = sl;
    while(newSize > len) len <<= 1;
    if(len != sl) resize(len);
  }

  /**
   * Resizes the stacks.
   * @param len new size
   */
  private void resize(final int len) {
    final int os = end;
    final Value[] nst = new Value[len];
    System.arraycopy(stack, 0, nst, 0, os);
    stack = nst;
    final Var[] nvr = new Var[len];
    System.arraycopy(vars, 0, nvr, 0, os);
    vars = nvr;
  }

  /**
   * Calculates the position of the given variable on the stack.
   * @param var variable
   * @return position
   */
  private int pos(final Var var) {
    final int pos = start + var.slot;
    if(pos < start || end <= pos) {
      throw Util.notExpected(var + " index: " + pos + ", slot: " + var.slot);
    }
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
   * @param qc query context
   * @throws QueryException if the value does not have the right type
   */
  public void set(final Var var, final Value val, final QueryContext qc) throws QueryException {
    final int pos = pos(var);
    stack[pos] = var.checkType(val, qc, false);
    vars[pos] = var;
  }

  /**
   * Creates a dump of the current variable stack.
   * @return string dump
   */
  public String dump() {
    final StringBuilder sb = new StringBuilder(QueryText.DEBUGLOCAL + ':');
    for(int i = end; --i >= 0;) {
      if(vars[i] != null) {
        sb.append(Prop.NL).append("  $").append(vars[i].name).append(" := ").append(stack[i]);
        if(i == start && i > 0) sb.append(Prop.NL).append(QueryText.DEBUGGLOBAL + ':');
      }
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Util.className(this)).append('[');
    for(int i = 0; i < end; i++) sb.append(i == 0 ? "" : ", ").append(stack[i]);
    return sb.append(']').toString();
  }
}
