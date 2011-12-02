package org.basex.query.util;

import java.io.IOException;
import org.basex.data.ExprInfo;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryException;
import org.basex.query.QueryParser;
import org.basex.query.item.QNm;

/**
 * Container for all global and local variables that are specified in the
 * current context.
 *
 * @author BaseX Team 2005-11, BSD License
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
   * Returns the local variables.
   * @return local variables
   */
  public VarList local() {
    return local;
  }

  /**
   * Sets a global variable.
   * @param v variable to be added
   */
  public void setGlobal(final Var v) {
    global.set(v);
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
  public Var get(final QNm var) {
    final Var v = local.get(var);
    return v != null ? v : global.get(var);
  }

  /**
   * Finds the variable with the specified name.
   * @param name variable name
   * @return variable
   */
  public Var get(final Var name) {
    final Var v = local.get(name);
    return v != null ? v : global.get(name);
  }

  /**
   * Checks if all global variables have been correctly declared.
   * Called by the {@link QueryParser}.
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
   * Sets the number of local variables to the specified value.
   * @param s number of variables to be set
   */
  public void size(final int s) {
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
