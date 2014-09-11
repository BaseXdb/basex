package org.basex.query.func.file;

import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FileCurrentDir extends FileFn {
  @Override
  public Item item(final QueryContext qc) {
    return get(absolute(Paths.get(".")), true);
  }
}
