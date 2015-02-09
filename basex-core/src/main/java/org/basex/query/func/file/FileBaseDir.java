package org.basex.query.func.file;

import java.nio.file.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FileBaseDir extends FileFn {
  @Override
  public Item item(final QueryContext qc) {
    final IO base = sc.baseIO();
    return base instanceof IOFile ? get(absolute(Paths.get(base.dir())), true) : null;
  }
}
