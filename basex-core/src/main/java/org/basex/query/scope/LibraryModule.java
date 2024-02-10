package org.basex.query.scope;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;

/**
 * An XQuery library module.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class LibraryModule extends AModule {
  /**
   * Constructor.
   * @param sc static context
   */
  public LibraryModule(final StaticContext sc) {
    super(sc);
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    return true;
  }

  @Override
  public Expr compile(final CompileContext cc) {
    return null;
  }

  @Override
  public void toXml(final QueryPlan plan) {
  }

  @Override
  public void toString(final QueryString qs) {
  }
}
