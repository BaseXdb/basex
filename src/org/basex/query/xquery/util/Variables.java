package org.basex.query.xquery.util;

import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;

/**
 * Variables.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Variables {
  /** Global variables. */
  private final Vars global = new Vars();
  /** Local variables. */
  private final Vars local = new Vars();

  /**
   * Indexes a global variable.
   * @param v variable
   */
  public void addGlobal(final Var v) {
    global.add(v);
  }
  
  /**
   * Returns the global variables.
   * @return global variables
   */
  public Vars getGlobal() {
    return global;
  }

  /**
   * Indexes a local variable.
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
   * Returns the current number of local variables.
   * @return number of variables
   */
  public int size() {
    return local.size;
  }

  /**
   * Sets the number of local variables.
   * @param s number of variables to be set
   */
  public void reset(final int s) {
    local.size = s;
  }
  
  /**
   * Compiles the functions.
   * @param ctx xquery context
   * @throws XQException xquery exception
   */
  public void comp(final XQContext ctx) throws XQException {
    global.comp(ctx);
  }

  @Override
  public String toString() {
    return local.toString();
  }

  /**
   * Serializes the variables.
   * @param ser serializer
   * @throws Exception exception
   */
  public void plan(final Serializer ser) throws Exception {
    global.plan(ser);
  }
}
