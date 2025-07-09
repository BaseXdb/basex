package org.basex.query.func.file;

import java.io.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileMove extends FileCopy {
  @Override
  public Value eval(final QueryContext qc) throws IOException, QueryException {
    relocate(false, qc);
    return Empty.VALUE;
  }
}
