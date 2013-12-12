package org.basex.query;

import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * An XQuery main module.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class LibraryModule extends StaticScope {
  /** Library name. */
  public final QNm name;

  /**
   * Constructor.
   * @param nm of library
   * @param xqdoc documentation
   * @param sctx static context
   */
  public LibraryModule(final QNm nm, final String xqdoc, final StaticContext sctx) {
    super(null, xqdoc, sctx, null);
    name = nm;
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    return true;
  }

  @Override
  public void compile(final QueryContext ctx) throws QueryException {
  }

  @Override
  public String toString() {
    return "";
  }

  @Override
  public void plan(final FElem e) {
  }
}
