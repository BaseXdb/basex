package org.basex.query.var;

import org.basex.query.*;
import org.basex.query.func.prof.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * The query stack, containing local variable bindings of all active scopes.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class QueryStack {
  /** Initial stack size. */
  private static final int INIT = 1 << 5;
  /** Assigned values. */
  private Value[] values = new Value[INIT];
  /** Declared variables. */
  private Var[] vars = new Var[INIT];
  /** Frame pointer, marking the start of the current stack frame. */
  private int start;
  /** Stack limit, marking the end of the current stack frame. */
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
    final Value[] vls = values;
    for(int e = end; --e >= s;) vls[e] = null;
    end = s + size;
  }

  /**
   * Exits a stack frame and makes all bound variables eligible for garbage collection.
   * @param fp frame pointer of the underlying stack frame
   */
  public void exitFrame(final int fp) {
    final int s = start;
    final Value[] vls = values;
    for(int en = end; --en >= s;) vls[en] = null;
    end = s;
    start = fp;

    final int vl = vls.length;
    int ns = vl;
    while(ns > INIT && vl <= ns >> 2) ns >>= 1;
    if(ns != vl) resize(ns);
  }

  /**
   * Checks if tail calls should be eliminated.
   * @param size new frame size
   * @return result of check
   */
  public boolean tco(final int size) {
    return end + (size << 1) > INIT;
  }

  /**
   * Ensures that the query stack has at least the given size.
   * @param size required stack size
   */
  private void ensureCapacity(final int size) {
    final int vl = values.length;
    int ns = vl;
    while(size > ns) ns <<= 1;
    if(ns != vl) resize(ns);
  }

  /**
   * Resizes the stacks.
   * @param size new size
   */
  private void resize(final int size) {
    final int os = end;
    final Value[] vls = new Value[size];
    Array.copy(values, os, vls);
    values = vls;
    final Var[] vrs = new Var[size];
    Array.copy(vars, os, vrs);
    vars = vrs;
  }

  /**
   * Gets the value bound to the given variable in the current stack frame.
   * @param var variable
   * @return bound value, or {@link Empty#UNDEFINED} reference (required for {@link ProfVariables})
   */
  public Value get(final Var var) {
    final int vs = var.slot;
    final Value value = vs == -1 ? null : values[start + vs];
    return value != null ? value : Empty.UNDEFINED;
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
    vars[pos] = var;
    values[pos] = var.checkType(value, qc, null);
  }

  @Override
  public String toString() {
    return new TokenBuilder().add(getClass()).add('[').addAll(values, ", ").add(']').toString();
  }
}
