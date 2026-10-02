package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FileCreateTempFile extends FileFn {
  @Override
  public Value eval(final QueryContext qc) throws QueryException, IOException {
    return createTemp(false, qc);
  }

  /**
   * Creates a temporary file or directory.
   * @param directory create a directory instead of a file
   * @param qc query context
   * @return path of created file or directory
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final Str createTemp(final boolean directory, final QueryContext qc)
      throws QueryException, IOException {

    final String prefix = toStringOrNull(arg(0), qc);
    final String suffix = toStringOrNull(arg(1), qc);
    final String dir = toStringOrNull(arg(2), qc);
    final Path cd = qc.resources.currentDir;
    final Path root = dir != null ? toPath(dir, qc) : cd != null ? cd : Paths.get(Prop.TEMPDIR);

    if(Files.isRegularFile(root)) throw FILE_NO_DIR_X.get(info, root);

    // create directory or file; choose another name if the path exists
    final Random rnd = new Random();
    while(true) {
      final StringBuilder path = new StringBuilder();
      if(prefix != null) path.append(prefix);
      path.append(rnd.nextLong());
      if(suffix != null) path.append(suffix);
      final Path file = root.resolve(path.toString());
      try {
        if(directory) {
          Files.createDirectory(file);
        } else {
          Files.createFile(file);
        }
        return get(file, directory);
      } catch(final FileAlreadyExistsException ignore) {
        // choose another name
      }
    }
  }
}
