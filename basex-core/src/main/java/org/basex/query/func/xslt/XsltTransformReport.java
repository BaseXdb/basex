package org.basex.query.func.xslt;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class XsltTransformReport extends XsltTransform {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return transform(qc, false);
  }
}
