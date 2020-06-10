package org.basex.query.scope;

import org.basex.query.*;
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
public abstract class AModule extends StaticScope {
  /** User-defined functions. */
  private final TokenObjMap<StaticFunc> funcs;
  /** Static variables. */
  private final TokenObjMap<StaticVar> vars;
  /** Namespace URIs of imported modules (currently not used). */
  protected final TokenSet imports;

  /**
   * Constructor.
   * @param sc static context
   * @param vs variable scope (can be {@code null})
   * @param doc documentation (can be {@code null})
   * @param info input info (can be {@code null})
   * @param funcs user-defined functions (can be {@code null})
   * @param vars static variables (can be {@code null})
   * @param imports namespace URIs of imported modules (can be {@code null})
   */
  AModule(final StaticContext sc, final VarScope vs, final String doc, final InputInfo info,
      final TokenObjMap<StaticFunc> funcs, final TokenObjMap<StaticVar> vars,
      final TokenSet imports) {
    super(sc, vs, doc, info);
    this.funcs = funcs;
    this.vars = vars;
    this.imports = imports;
  }

  /**
   * Return static variables.
   * @return static variables
   */
  public final TokenObjMap<StaticVar> vars() {
    return vars;
  }

  /**
   * Return static functions.
   * @return static functions
   */
  public final TokenObjMap<StaticFunc> funcs() {
    return funcs;
  }
}
