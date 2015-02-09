package org.basex.query.func.xslt;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions for performing XSLT transformations.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class XsltVersion extends XsltFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Str.get(IMPL[OFFSET + 2]);
  }
}
