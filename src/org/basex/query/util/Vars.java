package org.basex.query.util;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.ExprInfo;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.util.Array;

/**
 * Variables.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Vars extends ExprInfo {
  /** Variable expressions. */
  public Var[] vars = new Var[8];
  /** Number of stored variables. */
  public int size;

  /**
   * Indexes the specified variable name and returns its id.
   * @param v variable
   */
  void add(final Var v) {
    if(size == vars.length) vars = Array.extend(vars);
    vars[size++] = v;
  }

  /**
   * Finds and returns the specified variable.
   * @param v variable
   * @return variable
   */
  public Var get(final Var v) {
    for(int s = size - 1; s >= 0; s--) if(v.eq(vars[s])) return vars[s];
    return null;
  }

  /**
   * Compiles the variables.
   * @param ctx query context
   * @throws QueryException xquery exception
   */
  void comp(final QueryContext ctx) throws QueryException {
    for(int i = 0; i < size; i++) vars[i].comp(ctx);
  }

  @Override
  public String color() {
    return "66FF66";
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i < size; i++)
      sb.append((i == 0 ? "" : "\n") + i + ": " + vars[i]);
    return sb.toString();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    if(size == 0) return;
    ser.openElement(this);
    for(int i = 0; i < size; i++) vars[i].plan(ser);
    ser.closeElement();
  }
}
