package org.basex.query.func.file;

import java.io.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FileAppend extends FileWrite {
  @Override
  public Item item(final QueryContext qc) throws IOException, QueryException {
    return write(true, qc);
  }
}
