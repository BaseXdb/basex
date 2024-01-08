package org.basex.query.scope;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * An XQuery module.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public abstract class AModule extends StaticScope {
  /** User-defined functions. */
  public ArrayList<StaticFunc> funcs;
  /** Static variables. */
  public ArrayList<StaticVar> vars;
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
   * @param fn user-defined functions
   * @param vr static variables
   * @param md URIs of modules
   * @param ns namespaces
   * @param d documentation string
   */
  public void set(final ArrayList<StaticFunc> fn, final ArrayList<StaticVar> vr, final TokenSet md,
      final TokenMap ns, final String d) {
    funcs = fn;
    vars = vr;
    modules = md;
    namespaces = ns;
    doc(d);
  }
}
