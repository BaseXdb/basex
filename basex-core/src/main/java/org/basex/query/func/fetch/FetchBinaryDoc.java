package org.basex.query.func.fetch;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FetchBinaryDoc extends FetchDoc {
  @Override
  protected DBNode item(final QueryContext qc) throws QueryException {
    final Bin source = toBin(arg(0), qc);
    // input is streamed: large binaries are not cached in main memory
    return fetch(new IOStream(source.input(info)), qc);
  }
}
