package org.basex.query.func.file;

import java.io.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileChildren extends FileList {
  @Override
  public Value eval(final QueryContext qc) throws QueryException, IOException {
    final TokenList tl = new TokenList();
    list(toPath(arg(0), qc), null, new HofArgs(1), null, -1, null, new HofArgs(1), tl, 0, true, qc);
    return StrSeq.get(tl);
  }
}
