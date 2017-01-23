package org.basex.query.func.file;

import java.nio.file.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FileBaseDir extends FileFn {
  @Override
  public Item item(final QueryContext qc) {
    final IO base = sc.baseIO();
    if(!(base instanceof IOFile)) return null;
    return get(absolute(Paths.get(base.isDir() ? base.path() : base.dir())), true);
  }
}
