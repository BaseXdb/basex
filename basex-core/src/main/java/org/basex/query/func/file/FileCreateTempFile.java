package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FileCreateTempFile extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws QueryException, IOException {
    return createTemp(false, qc);
  }

  /**
   * Creates a temporary file or directory.
   * @param qc query context
   * @param dir create a directory instead of a file
   * @return path of created file or directory
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final synchronized Item createTemp(final boolean dir, final QueryContext qc)
      throws QueryException, IOException {

    final String prefix = toString(exprs[0], qc);
    final String suffix = defined(1) ? toString(exprs[1], qc) : "";
    final Path root;
    if(defined(2)) {
      root = toPath(2, qc);
      if(Files.isRegularFile(root)) throw FILE_NO_DIR_X.get(info, root);
    } else {
      root = Paths.get(Prop.TEMPDIR);
    }

    // choose non-existing file path
    final Random rnd = new Random();
    Path file;
    do {
      file = root.resolve(prefix + rnd.nextLong() + suffix);
    } while(Files.exists(file));

    // create directory or file
    if(dir) {
      Files.createDirectory(file);
    } else {
      Files.createFile(file);
    }
    return get(file, dir);
  }
}
