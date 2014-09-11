package org.basex.query.func.file;

import java.io.*;
import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class FileDelete extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws QueryException, IOException {
    final Path path = toPath(0, qc);
    if(optionalBool(1, qc)) {
      delete(path);
    } else {
      Files.delete(path);
    }
    return null;
  }

  /**
   * Recursively deletes a file path.
   * @param path path to be deleted
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized void delete(final Path path) throws QueryException, IOException {
    if(Files.isDirectory(path)) {
      try(DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
        for(final Path p : paths) delete(p);
      }
    }
    Files.delete(path);
  }
}
