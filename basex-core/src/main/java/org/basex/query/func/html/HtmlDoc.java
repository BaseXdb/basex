package org.basex.query.func.html;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public class HtmlDoc extends HtmlParse {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] uri = toToken(exprs[0], qc);
    return parse(checkPath(uri), qc);
  }
}
