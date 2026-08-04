package org.basex.query.func.file;

import java.io.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FilePathSeparator extends StandardFunc {
  @Override
  protected Item item(final QueryContext qc) {
    return Str.get(File.pathSeparator);
  }
}
