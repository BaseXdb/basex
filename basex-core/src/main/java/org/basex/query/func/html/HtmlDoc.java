package org.basex.query.func.html;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class HtmlDoc extends HtmlParse {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String href = toStringOrNull(exprs[0], qc);
    return href != null ? parse(toIO(href), qc) : Empty.VALUE;
  }
}
