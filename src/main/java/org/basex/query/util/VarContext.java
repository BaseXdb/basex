package org.basex.query.util;

import java.io.IOException;
import org.basex.data.ExprInfo;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryException;
import org.basex.query.QueryParser;
import org.basex.query.item.QNm;

/**
 * This class references all in-scope variables.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class VarContext extends ExprInfo {
  /** Global variables. */
  private final VarStack global = new VarStack();
  /** Local variables. */
  private VarStack local = new VarStack();

  /**
   * Returns the global variables.
   * @return global variables
   */
  public VarStack globals() {
    return global;
  }

  /**
   * Returns the local variables.
   * @return local variables
   */
  public VarStack locals() {
    return local;
  }

  /**
   * Creates a new variable stack and returns the old one.
   * @param c stack capacity
   * @return local variables
   */
  public VarStack cache(final int c) {
    final VarStack vl = local;
    local = new VarStack(c);
    return vl;
  }

  /**
   * Resets the local variables to the specified instance.
   * @param l local variables
   */
  public void reset(final VarStack l) {
    local = l;
  }

  /**
   * Adds or replaces a global variable.
   * @param v variable to be added
   */
  public void updateGlobal(final Var v) {
    global.update(v);
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
   * Returns a variable with the specified name.
   * @param var variable
   * @return variable
   */
  public Var get(final QNm var) {
    final Var v = local.get(var);
    return v != null ? v : global.get(var);
  }

  /**
   * Returns a variable instance with the same id.
   * @param name variable name
   * @return variable
   */
  public Var get(final Var name) {
    final Var v = local.get(name);
    return v != null ? v : global.get(name);
  }

  /**
   * Checks if none of the global variables contains an updating expression.
   * Called by the {@link QueryParser}.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    global.checkUp();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    global.plan(ser);
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
}
