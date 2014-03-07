package org.basex.query;

import org.basex.query.func.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;


/**
 * An XQuery module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public abstract class Module extends StaticScope {
  /** User-defined functions. */
  final TokenObjMap<StaticFunc> funcs;
  /** Static variables. */
  final TokenObjMap<StaticVar> vars;
  /** Imported modules. */
  final TokenObjMap<LibraryModule> modules;

  /**
   * Constructor.
   * @param scp variable scope
   * @param xqdoc documentation
   * @param funcs user-defined functions
   * @param vars static variables
   * @param modules imported modules
   * @param sctx static context
   * @param ii input info
   */
  public Module(final VarScope scp, final String xqdoc, final TokenObjMap<StaticFunc> funcs,
      final TokenObjMap<StaticVar> vars, final TokenObjMap<LibraryModule> modules,
      final StaticContext sctx, final InputInfo ii) {

    super(scp, xqdoc, sctx, ii);
    this.funcs = funcs;
    this.vars = vars;
    this.modules = modules;
  }

  /**
   * Return static variables.
   * @return static variables
   */
  public TokenObjMap<StaticVar> vars() {
    return vars;
  }

  /**
   * Return static functions.
   * @return static functions
   */
  public TokenObjMap<StaticFunc> funcs() {
    return funcs;
  }
}
