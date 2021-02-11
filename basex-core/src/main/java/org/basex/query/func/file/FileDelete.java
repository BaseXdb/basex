package org.basex.query.func.file;

import java.io.*;
import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FileDelete extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws QueryException, IOException {
    final Path path = toPath(0, qc);
    if(optionalBool(1, qc)) {
      delete(path, qc);
    } else {
      Files.delete(path);
    }
    return Empty.VALUE;
  }

  /**
   * Recursively deletes a file path.
   * @param path path to be deleted
   * @param qc query context
   * @throws IOException I/O exception
   */
  private synchronized void delete(final Path path, final QueryContext qc) throws IOException {
    if(Files.isDirectory(path)) {
      try(DirectoryStream<Path> children = Files.newDirectoryStream(path)) {
        qc.checkStop();
        for(final Path child : children) {
          delete(child, qc);
        }
      }
    }
    Files.delete(path);
  }
}
