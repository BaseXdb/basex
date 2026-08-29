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
public final class FileDirSeparator extends StandardFunc {
  @Override
  public Str value(final QueryContext qc) {
    return Str.get(File.separator);
  }
}
