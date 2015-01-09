package org.basex.query.func.file;

import java.io.*;
import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FilePathToNative extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws QueryException, IOException {
    final Path nat = toPath(0, qc).toRealPath();
    return get(nat, Files.isDirectory(nat));
  }
}
