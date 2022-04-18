package org.basex.query.scope;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * An XQuery module.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Leo Woerteler
 */
public abstract class AModule extends StaticScope {
  /** User-defined functions. */
  public TokenObjMap<StaticFunc> funcs;
  /** Static variables. */
  public TokenObjMap<StaticVar> vars;
  /** URIs of modules. */
  public TokenSet modules;
  /** Namespaces. */
  public TokenMap namespaces;

  /**
   * Constructor.
   * @param sc static context
   */
  AModule(final StaticContext sc) {
    super(sc);
  }

  /**
   * Assigns module properties.
   * @param f user-defined functions
   * @param v static variables
   * @param m URIs of modules
   * @param n namespaces
   * @param d documentation string
   */
  public void set(final TokenObjMap<StaticFunc> f, final TokenObjMap<StaticVar> v,
      final TokenSet m, final TokenMap n, final String d) {
    funcs = f;
    vars = v;
    modules = m;
    namespaces = n;
    doc(d);
  }
}
