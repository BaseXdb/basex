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
  private final TokenObjMap<StaticFunc> funcs;
  /** Static variables. */
  private final TokenObjMap<StaticVar> vars;
  /** Namespace URIs of imported modules. */
  private final TokenSet imports;

  /**
   * Constructor.
   * @param scp variable scope
   * @param xqdoc documentation
   * @param funcs user-defined functions
   * @param vars static variables
   * @param imports namespace URIs of imported modules
   * @param sctx static context
   * @param info input info
   */
  public Module(final VarScope scp, final String xqdoc, final TokenObjMap<StaticFunc> funcs,
      final TokenObjMap<StaticVar> vars, final TokenSet imports, final StaticContext sctx,
      final InputInfo info) {
    super(scp, xqdoc, sctx, info);
    this.funcs = funcs;
    this.vars = vars;
    this.imports = imports;
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
