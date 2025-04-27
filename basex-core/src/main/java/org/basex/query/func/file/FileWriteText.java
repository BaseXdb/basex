package org.basex.query.func.file;

import java.io.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FileWriteText extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws IOException, QueryException {
    write(false, false, qc);
    return Empty.VALUE;
  }
}
