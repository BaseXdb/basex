package org.basex.query.util.index;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;

/**
 * Index predicate: context expression.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
class IndexContext extends IndexPred {
  /**
   * Constructor.
   * @param info index info
   */
  IndexContext(final IndexInfo info) {
    super(info);
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
    return info.step;
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
    return info.step;
  }

  @Override
  Expr invert(final Expr root) throws QueryException {
    final Step st = info.step;
    if(info.text || !(st.test instanceof NameTest || st.test instanceof UnionTest)) return root;

    // attribute index request: add attribute step
    final Expr step = Step.get(info.cc, root, st.info(), st.test);
    return Path.get(root.info(), root, step);
  }
}
