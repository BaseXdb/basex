package org.basex.query.scope;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * An XQuery library module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class LibraryModule extends AModule {
  /**
   * Constructor.
   * @param doc documentation
   * @param funcs user-defined functions
   * @param vars static variables
   * @param modules imported modules
   * @param sc static context
   */
  public LibraryModule(final String doc, final TokenObjMap<StaticFunc> funcs,
      final TokenObjMap<StaticVar> vars, final TokenSet modules, final StaticContext sc) {
    super(sc, null, doc, null, funcs, vars, modules);
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    return true;
  }

  @Override
  public void comp(final CompileContext cc) {
  }

  @Override
  public void plan(final QueryPlan plan) {
  }

  @Override
  public void plan(final QueryString qs) {
  }
}
