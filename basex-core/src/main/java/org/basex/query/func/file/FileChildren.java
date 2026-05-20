package org.basex.query.func.file;

import java.io.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
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
    final FItem recurse = constantFn(false), filter = constantFn(true);
    list(toPath(arg(0), qc), recurse, new HofArgs(1), null, -1,
        filter, new HofArgs(1), tl, Integer.MAX_VALUE, true, qc);
    return StrSeq.get(tl);
  }
}
