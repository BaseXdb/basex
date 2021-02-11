package org.basex.query.func.file;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FileReadText extends FileRead {
  @Override
  public Item item(final QueryContext qc) throws QueryException {
    return text(qc);
  }
}
