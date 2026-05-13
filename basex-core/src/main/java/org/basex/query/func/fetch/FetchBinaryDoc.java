package org.basex.query.func.fetch;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FetchBinaryDoc extends FetchDoc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Bin source = toBin(arg(0), qc);
    return fetch(new IOContent(source.binary(info)), qc);
  }
}
