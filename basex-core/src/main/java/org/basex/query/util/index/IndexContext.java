package org.basex.query.util.index;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.util.*;

/**
 * Index predicate: context expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class IndexContext extends IndexPred {
  /**
   * Constructor.
   * @param ii index info
   */
  IndexContext(final IndexInfo ii) {
    super(ii);
  }

  /**
   * Returns the last step pointing to the requested nodes. Examples:
   * <ul>
   *   <li>{@code /xml/a[. = 'A']}         ->  {@code a}</li>
   *   <li>{@code /xml/a/text()[. = 'A']}  ->  {@code text()}</li>
   * </ul>
   * @return parent step
   */
  @Override
  Step step() {
    return ii.step;
  }

  /**
   * Returns the local name and namespace uri of the last name test. Examples:
   * <ul>
   *   <li> //x[. = 'TEXT']  -> x </li>
   *   <li> //@x[. = 'TEXT'] -> x </lI>
   * </ul>
   * @return parent step
   */
  @Override
  Step qname() {
    return ii.step;
  }

  @Override
  Expr invert(final Expr root) throws QueryException {
    final Step st = ii.step;
    if(ii.text || !(st.test instanceof NameTest || st.test instanceof UnionTest)) return root;

    // attribute index request: add attribute step
    final InputInfo info = root instanceof ParseExpr ? ((ParseExpr) root).info : null;
    final Expr step = Step.get(ii.cc, root, st.info, st.test);
    return Path.get(info, root, step);
  }
}
