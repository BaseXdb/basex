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
public class FileCopy extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws IOException, QueryException {
    return relocate(true, qc);
  }

  /**
   * Transfers a file path, given a source and a target.
   * @param copy copy flag (no move)
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final synchronized Item relocate(final boolean copy, final QueryContext qc)
      throws QueryException, IOException {

    final Path source = toPath(0, qc);
    if(!Files.exists(source)) throw FILE_NOT_FOUND_X.get(info, source);
    final Path src = absolute(source);
    Path trg = absolute(toPath(1, qc));

    if(Files.isDirectory(trg)) {
      // target is a directory: attach file name
      trg = trg.resolve(src.getFileName());
      if(Files.isDirectory(trg)) throw FILE_IS_DIR_X.get(info, trg);
    } else if(!Files.exists(trg)) {
      // target does not exist: ensure that parent exists
      if(!Files.isDirectory(trg.getParent())) throw FILE_NO_DIR_X.get(info, trg);
    } else if(Files.isDirectory(src)) {
      // if target is file, source cannot be a directory
      throw FILE_IS_DIR_X.get(info, src);
    }

    // ignore operations on identical, canonical source and target path
    if(copy) {
      copy(src, trg);
    } else {
      Files.move(src, trg, StandardCopyOption.REPLACE_EXISTING);
    }
    return null;
  }

  /**
   * Recursively copies files.
   * @param src source path
   * @param trg target path
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized void copy(final Path src, final Path trg)
      throws QueryException, IOException {

    if(Files.isDirectory(src)) {
      Files.createDirectory(trg);
      try(DirectoryStream<Path> paths = Files.newDirectoryStream(src)) {
        for(final Path p : paths) copy(p, trg.resolve(p.getFileName()));
      }
    } else {
      Files.copy(src, trg, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
