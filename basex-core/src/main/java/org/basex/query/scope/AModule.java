package org.basex.query.scope;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * An XQuery module.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public abstract class AModule extends StaticScope {
  /** User-defined functions. */
  public ArrayList<StaticFunc> funcs;
  /** Static variables. */
  public ArrayList<StaticVar> vars;
  /** Public types. */
  public QNmMap<SeqType> types;
  /** URIs of modules. */
  public TokenSet modules;
  /** Namespaces. */
  public TokenObjectMap<byte[]> namespaces;
  /** Options. */
  public QNmMap<String> options;

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
   * @param tp public types
   * @param md URIs of modules
   * @param ns namespaces
   * @param opts options
   * @param dc documentation string
   */
  public void set(final ArrayList<StaticFunc> fn, final ArrayList<StaticVar> vr,
      final QNmMap<SeqType> tp, final TokenSet md, final TokenObjectMap<byte[]> ns,
      final QNmMap<String> opts, final String dc) {
    funcs = fn;
    vars = vr;
    types = tp;
    modules = md;
    namespaces = ns;
    options = opts;
    doc(dc);
  }
}
