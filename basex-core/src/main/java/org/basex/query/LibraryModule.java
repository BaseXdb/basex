package org.basex.query;

import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * An XQuery main module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class LibraryModule extends StaticScope {
  /** Library name. */
  public final QNm name;

  /**
   * Constructor.
   * @param name of library
   * @param doc documentation
   * @param sc static context
   */
  LibraryModule(final QNm name, final String doc, final StaticContext sc) {
    super(null, doc, sc, null);
    this.name = name;
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
