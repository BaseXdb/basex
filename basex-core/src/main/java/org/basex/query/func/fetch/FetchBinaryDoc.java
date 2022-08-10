package org.basex.query.func.fetch;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FetchBinaryDoc extends FetchDoc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Bin value = toBin(exprs[0], qc);
    return fetch(new IOContent(value.binary(info)), qc);
  }
}
