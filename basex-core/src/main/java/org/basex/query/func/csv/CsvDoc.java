package org.basex.query.func.csv;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class CsvDoc extends CsvParse {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String source = toStringOrNull(arg(0), qc);
    return source != null ? parse(toIO(source), qc) : Empty.VALUE;
  }
}
