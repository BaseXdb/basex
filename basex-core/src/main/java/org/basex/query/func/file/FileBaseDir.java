package org.basex.query.func.file;

import java.nio.file.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FileBaseDir extends FileFn {
  @Override
  public Item item(final QueryContext qc) {
    final IO base = sc.baseIO();
    if(!(base instanceof IOFile)) return Empty.VALUE;
    return get(absolute(Paths.get(base.isDir() ? base.path() : base.dir())), true);
  }
}
