package org.basex.query.scope;

import org.basex.query.expr.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * The scope of an XQuery context value.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class ContextScope extends MainModule {
  /**
   * Constructor.
   * @param expr root expression
   * @param declType declare type (can be {@code null})
   * @param vs variable scope
   * @param info input info (can be {@code null})
   * @param doc xqdoc string (can be {@code null})
   */
  public ContextScope(final Expr expr, final SeqType declType, final VarScope vs,
      final InputInfo info, final String doc) {
    super(expr, vs);
    this.declType = declType;
    this.info = info;
    doc(doc);
  }
}
