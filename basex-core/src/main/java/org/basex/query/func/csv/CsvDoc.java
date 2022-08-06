package org.basex.query.func.csv;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class CsvDoc extends CsvParse {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String uri = toStringOrNull(exprs[0], qc);
    return uri != null ? parse(toIO(uri), qc) : Empty.VALUE;
  }
}
