package org.basex.query.func.file;

import java.io.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FileMove extends FileCopy {
  @Override
  public Item item(final QueryContext qc) throws IOException, QueryException {
    return relocate(false, qc);
  }
}
