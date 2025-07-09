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
public class FileWriteText extends FileFn {
  @Override
  public Value eval(final QueryContext qc) throws IOException, QueryException {
    return write(false, false, qc);
  }
}
