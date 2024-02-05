package org.basex.query.func.file;

import java.io.*;
import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FilePathToNative extends FileFn {
  @Override
  public Str item(final QueryContext qc) throws QueryException, IOException {
    final Path nat = toPath(arg(0), qc).toRealPath();
    return get(nat, Files.isDirectory(nat));
  }
}
