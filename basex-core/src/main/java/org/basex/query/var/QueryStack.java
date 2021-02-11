package org.basex.query.var;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * The query stack, containing local variable bindings of all active scopes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class QueryStack {
  /** Initial stack size. */
  private static final int INIT = 1 << 3;
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
   * @param size required stack size
   */
  private void ensureCapacity(final int size) {
    final int sl = stack.length;
    int len = sl;
    while(size > len) len <<= 1;
    if(len != sl) resize(len);
  }

  /**
   * Resizes the stacks.
   * @param size new size
   */
  private void resize(final int size) {
    final int os = end;
    final Value[] nst = new Value[size];
    Array.copy(stack, os, nst);
    stack = nst;
    final Var[] nvr = new Var[size];
    Array.copy(vars, os, nvr);
    vars = nvr;
  }

  /**
   * Gets the value bound to the given variable in the current stack frame.
   * @param var variable
   * @return bound value
   */
  public Value get(final Var var) {
    return stack[start + var.slot];
  }

  /**
   * Sets the value of the given variable in the current stack frame.
   * @param var variable to bind the value to
   * @param value value to bind
   * @param qc query context
   * @throws QueryException if the value does not have the right type
   */
  public void set(final Var var, final Value value, final QueryContext qc) throws QueryException {
    final int pos = start + var.slot;
    stack[pos] = var.checkType(value, qc, false);
    vars[pos] = var;
  }

  /**
   * Creates a dump of the current variable stack.
   * @return string dump
   */
  public String dump() {
    final TokenBuilder tb = new TokenBuilder().add(QueryText.DEBUGLOCAL).add(':');
    for(int i = end; --i >= 0;) {
      if(vars[i] != null) {
        tb.add(Prop.NL).add("  $").add(vars[i].name).add(" := ").add(stack[i]);
        if(i == start && i > 0) tb.add(Prop.NL).add(QueryText.DEBUGGLOBAL).add(':');
      }
    }
    return tb.toString();
  }

  @Override
  public String toString() {
    return new TokenBuilder().add(getClass()).add('[').addAll(stack, ", ").add(']').toString();
  }
}
