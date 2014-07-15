package org.basex.query;

import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * An XQuery library module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class LibraryModule extends Module {
  /** Library name. */
  public final QNm name;

  /**
   * Constructor.
   * @param name of library
   * @param doc documentation
   * @param funcs user-defined functions
   * @param vars static variables
   * @param modules imported modules
   * @param sc static context
   */
  public LibraryModule(final QNm name, final String doc, final TokenObjMap<StaticFunc> funcs,
      final TokenObjMap<StaticVar> vars, final TokenSet modules, final StaticContext sc) {
    super(null, doc, funcs, vars, modules, sc, null);
    this.name = name;
  }

  /**
   * Returns the module namespace URI.
   * @return URI
   */
  public byte[] uri() {
    return name.uri();
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    return true;
  }

  @Override
  public void compile(final QueryContext qc) {
  }

  @Override
  public String toString() {
    return "";
  }

  @Override
  public void plan(final FElem e) {
  }
}
