package org.basex.query.func.file;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileSize extends FileFn {
  @Override
  public Value eval(final QueryContext qc) throws IOException, QueryException {
    final Path path = toPath(arg(0), qc);
    final boolean recursive = toBooleanOrFalse(arg(1), qc);

    // directories: sum up the sizes of the descendants, without descending into links
    final BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
    return Itr.get(!attrs.isDirectory() ? attrs.size() :
      recursive ? new IOFile(path).size(false, qc) : 0);
  }
}
