package org.basex.query.xquery.util;

import org.basex.data.Serializer;
import org.basex.query.ExprInfo;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;

/**
 * Variables.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Vars extends ExprInfo {
  /** Variable expressions. */
  public Var[] vars = new Var[2];
  /** Number of stored variables. */
  public int size;

  /**
   * Indexes the specified variable name and returns its id.
   * @param v variable
   */
  void add(final Var v) {
    if(size == vars.length) resize();
    vars[size++] = v;
  }

  /**
   * Resizes the sequence array.
   */
  private void resize() {
    final Var[] tmp = new Var[size << 1];
    System.arraycopy(vars, 0, tmp, 0, size);
    vars = tmp;
  }

  /**
   * Finds and returns the specified variable.
   * @param v variable
   * @return variable
   */
  public Var get(final Var v) {
    for(int s = size - 1; s >= 0; s--) {
      if(v == vars[s] || v.name.eq(vars[s].name)) return vars[s];
    }
    return null;
  }

  /**
   * Returns the specified variable.
   * @param o offset
   * @return variable
   */
  public Var get(final int o) {
    return vars[o];
  }

  /**
   * Compiles the variables.
   * @param ctx xquery context
   * @throws XQException xquery exception
   */
  void comp(final XQContext ctx) throws XQException {
    for(int i = 0; i < size; i++) vars[i].comp(ctx);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i < size; i++) sb.append((i == 0 ? "" : "\n") + i + ": " +
        vars[i]);
    return sb.toString();
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    if(size == 0) return;
    ser.openElement(this);
    for(int i = 0; i < size; i++) vars[i].plan(ser);
    ser.closeElement(this);
  }
}
