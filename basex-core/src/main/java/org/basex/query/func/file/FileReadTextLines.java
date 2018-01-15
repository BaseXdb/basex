package org.basex.query.func.file;

import org.basex.query.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FileReadTextLines extends FileRead {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    return Parse.lineIter(text(qc).string(info));
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    return Parse.lines(text(qc).string(info));
  }
}
