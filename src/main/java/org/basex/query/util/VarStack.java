package org.basex.query.util;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.util.*;

/**
 * Variable stack.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class VarStack extends ExprInfo {
  /** Variable expressions. */
  public Var[] vars;
  /** Number of stored variables. */
  public int size;

  /**
   * Default constructor.
   */
  public VarStack() {
    this(4);
  }

  /**
   * Default constructor.
   * @param c initial capacity
   */
  public VarStack(final int c) {
    vars = new Var[c];
  }

  /**
   * Adds or replaces the specified variable.
   * @param v variable
   */
  public void update(final Var v) {
    final int i = indexOf(v);
    if(i == -1) add(v);
    else vars[i] = v;
  }

  /**
   * Adds the specified variable.
   * @param v variable
   */
  public void add(final Var v) {
    if(size == vars.length) vars = Arrays.copyOf(vars, Array.newSize(size));
    vars[size++] = v;
  }

  /**
   * Returns a variable with the specified name; should only be
   * used while parsing because it ignores ids of variables.
   * @param name variable name
   * @return variable
   */
  public Var get(final QNm name) {
    for(int i = size; i-- > 0;) if(name.eq(vars[i].name)) return vars[i];
    return null;
  }

  /**
   * Returns a variable with the same id.
   * @param v variable
   * @return variable
   */
  public Var get(final Var v) {
    final int i = indexOf(v);
    return i == -1 ? null : vars[i];
  }

  /**
   * Returns the index of a variable with the same id, or {@code -1}.
   * @param v variable
   * @return index
   */
  private int indexOf(final Var v) {
    for(int s = size - 1; s >= 0; s--) if(v.is(vars[s])) return s;
    return -1;
  }

  /**
   * Checks if the given variable is in this list.
   * @param v variable
   * @return {@code true} if the variable was found, {@code false} otherwise
   */
  public boolean contains(final Var v) {
    return indexOf(v) != -1;
  }

  /**
   * Checks if none of the variables contains an updating expression.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    for(int i = 0; i < size; ++i) vars[i].checkUp();
  }

  @Override
  public void plan(final FElem plan) {
    if(size == 0) return;
    final FElem e = planElem();
    for(int i = 0; i < size; ++i) vars[i].plan(e);
    addPlan(plan, e);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i < size; ++i)
      sb.append((i == 0 ? "" : Text.NL) + i + Text.COLS + vars[i]);
    return sb.toString();
  }
}
