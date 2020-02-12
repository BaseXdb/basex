package org.basex.query.util.index;

import org.basex.query.expr.*;
import org.basex.query.expr.path.*;

/**
 * Index predicate: context expression.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public class IndexContext extends IndexPred {
  /**
   * Returns the last step pointing to the requested nodes. Examples:
   * <ul>
   *   <li>{@code /xml/a[. = 'A']}        -> {@code a}</li>
   *   <li>{@code /xml/a/text()[. = 'A']} -> {@code text()}</li>
   * </ul>
   * @param ii index info
   * @return parent step
   */
  @Override
  Step step(final IndexInfo ii) {
    return ii.step;
  }

  /**
   * Returns the local name and namespace uri of the last name test. Examples:
   * <ul>
   *   <li> //x[. = 'TEXT']  -> x </li>
   *   <li> //@x[. = 'TEXT'] -> x </lI>
   * </ul>
   * @param ii index info
   * @return parent step
   */
  @Override
  Step qname(final IndexInfo ii) {
    return ii.step;
  }

  @Override
  ParseExpr invert(final ParseExpr root, final IndexInfo ii) {
    final Step step = ii.step;
    if(ii.text || !(step.test instanceof NameTest || step.test instanceof UnionTest)) return root;
    // attribute index request: add attribute step
    return Path.get(root.info, root, Step.get(step.info, Axis.SELF, step.test));
  }
}
