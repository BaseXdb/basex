package org.basex.query.func.file;

import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FileCurrentDir extends FileFn {
  @Override
  public Str item(final QueryContext qc) {
    return get(absolute(Paths.get(".")), true);
  }
}
