package org.basex.query.scope;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * The scope of an XQuery context value.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ContextScope extends MainModule {
  /**
   * Constructor.
   * @param expr root expression
   * @param declType declared type (can be {@code null})
   * @param vs variable scope
   * @param sc static context
   * @param info input info (can be {@code null})
   * @param doc xqdoc string (can be {@code null})
   */
  public ContextScope(final Expr expr, final SeqType declType, final VarScope vs,
      final StaticContext sc, final InputInfo info, final String doc) {
    super(expr, vs, sc);
    this.declType = declType;
    this.info = info;
    doc(doc);
  }
}
