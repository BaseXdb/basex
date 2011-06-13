package org.basex.query.util;

import java.io.IOException;
import java.util.Arrays;
import org.basex.core.Text;
import org.basex.data.ExprInfo;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.item.QNm;

/**
 * Variable stack.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class VarList extends ExprInfo {
  /** Variable expressions. */
  public Var[] vars = new Var[8];
  /** Number of stored variables. */
  public int size;

  /**
   * Stores the specified variable.
   * @param v variable
   */
  public void set(final Var v) {
    final int i = indexOf(v);
    if(i == -1) add(v);
    else vars[i] = v;
  }

  /**
   * Adds the specified variable.
   * @param v variable
   */
  public void add(final Var v) {
    if(size == vars.length) vars = Arrays.copyOf(vars, size << 1);
    vars[size++] = v;
  }

  /**
   * Finds and returns the variable with the specified name, this should only be
   * used while parsing because it ignores variable IDs.
   * @param name variable name
   * @return variable
   */
  public Var get(final QNm name) {
    for(int i = size; i-- > 0;) if(name.eq(vars[i].name)) return vars[i];
    return null;
  }

  /**
   * Finds and returns the specified variable.
   * @param v variable
   * @return variable
   */
  public Var get(final Var v) {
    final int i = indexOf(v);
    return i == -1 ? null : vars[i];
  }

  /**
   * Returns the index of the specified variable, or {@code -1}.
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
   * Checks if all variables have been correctly declared.
   * @throws QueryException query exception
   */
  public void check() throws QueryException {
    for(int i = 0; i < size; ++i) vars[i].check();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    if(size == 0) return;
    ser.openElement(this);
    for(int i = 0; i < size; ++i) vars[i].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i < size; ++i)
      sb.append((i == 0 ? "" : Text.NL) + i + ": " + vars[i]);
    return sb.toString();
  }
}
