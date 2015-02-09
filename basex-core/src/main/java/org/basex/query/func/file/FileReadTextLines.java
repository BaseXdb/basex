package org.basex.query.func.file;

import org.basex.query.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FileReadTextLines extends FileRead {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    return Parse.textIter(text(qc).string(info));
  }
}
