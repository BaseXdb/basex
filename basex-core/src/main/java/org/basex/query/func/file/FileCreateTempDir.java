package org.basex.query.func.file;

import java.io.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FileCreateTempDir extends FileCreateTempFile {
  @Override
  public Str item(final QueryContext qc) throws QueryException, IOException {
    return createTemp(true, qc);
  }
}
