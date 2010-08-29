package org.basex.query.util;

import java.io.IOException;
import org.basex.data.ExprInfo;
import org.basex.data.Serializer;
import org.basex.query.QueryException;

/**
 * Container for all global and local variables that are specified in the
 * current context.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Variables extends ExprInfo {
  /** Global variables. */
  private final VarList global = new VarList();
  /** Local variables. */
  private final VarList local = new VarList();

  /**
   * Returns the global variables.
   * @return global variables
   */
  public VarList global() {
    return global;
  }

  /**
   * Adds a global variable.
   * @param v variable to be added
   */
  public void addGlobal(final Var v) {
    global.add(v);
    v.global = true;
  }

  /**
   * Adds a local variable.
   * @param v variable
   */
  public void add(final Var v) {
    local.add(v);
  }

  /**
   * Finds the specified variable.
   * @param var variable
   * @return variable
   */
  public Var get(final Var var) {
    final Var v = local.get(var);
    return v != null ? v : global.get(var);
  }

  /**
   * Checks if all functions have been correctly declared, and initializes
   * all function calls.
   * @throws QueryException query exception
   */
  public void check() throws QueryException {
    global.check();
  }

  /**
   * Returns the current number of local variables.
   * @return number of variables
   */
  public int size() {
    return local.size;
  }

  /**
   * Resets the number of local variables.
   * @param s number of variables to be set
   */
  public void reset(final int s) {
    local.size = s;
  }

  @Override
  public String toString() {
    return local.toString();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    global.plan(ser);
  }
}
