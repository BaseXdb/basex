package org.basex.query.func.file;

import java.io.*;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileDescendants extends FileList {
  @Override
  public Value eval(final QueryContext qc) throws QueryException, IOException {
    return paths(true, qc);
  }
}
