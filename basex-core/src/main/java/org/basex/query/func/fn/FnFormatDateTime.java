package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnFormatDateTime extends Format {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return formatDate(AtomType.DATE_TIME, qc);
  }
}
