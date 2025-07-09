package org.basex.query.func.file;

import java.nio.file.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileBaseDir extends FileFn {
  @Override
  public Value eval(final QueryContext qc) {
    final IO base = sc().baseIO();
    if(!(base instanceof IOFile)) return Empty.VALUE;
    return get(absolute(Paths.get(base.isDir() ? base.path() : base.dir())), true);
  }
}
