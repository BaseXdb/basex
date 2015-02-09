package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

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
public final class FileCreateDir extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws QueryException, IOException {
    final Path path = absolute(toPath(0, qc));

    // find lowest existing path
    for(Path p = path; p != null;) {
      if(Files.exists(p)) {
        if(Files.isRegularFile(p)) throw FILE_EXISTS_X.get(info, p);
        break;
      }
      p = p.getParent();
    }

    Files.createDirectories(path);
    return null;
  }
}
