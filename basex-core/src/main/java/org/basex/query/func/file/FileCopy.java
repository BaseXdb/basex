package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

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
public class FileCopy extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws IOException, QueryException {
    relocate(true, qc);
    return Empty.VALUE;
  }

  /**
   * Transfers a file path, given a source and a target.
   * @param copy copy flag (no move)
   * @param qc query context
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final synchronized void relocate(final boolean copy, final QueryContext qc)
      throws QueryException, IOException {

    final Path source = toPath(0, qc);
    if(!Files.exists(source)) throw FILE_NOT_FOUND_X.get(info, source.toAbsolutePath());
    final Path src = absolute(source);
    Path trg = absolute(toPath(1, qc));

    if(Files.isDirectory(trg)) {
      // target is a directory: attach file name
      trg = trg.resolve(src.getFileName());
      if(Files.isDirectory(trg)) throw FILE_IS_DIR_X.get(info, trg.toAbsolutePath());
    } else if(!Files.exists(trg)) {
      // target does not exist: ensure that parent exists
      if(!Files.isDirectory(trg.getParent())) throw FILE_NO_DIR_X.get(info, trg.toAbsolutePath());
    } else if(Files.isDirectory(src)) {
      // if target is file, source cannot be a directory
      throw FILE_IS_DIR_X.get(info, src.toAbsolutePath());
    }

    // ignore operations on identical, canonical source and target path
    relocate(src, trg, copy, qc);
  }

  /**
   * Recursively copies files.
   * @param src source path
   * @param trg target path
   * @param copy copy flag
   * @param qc query context
   * @throws IOException I/O exception
   */
  private synchronized void relocate(final Path src, final Path trg, final boolean copy,
      final QueryContext qc) throws IOException {

    if(Files.isDirectory(src)) {
      Files.createDirectory(trg);
      try(DirectoryStream<Path> children = Files.newDirectoryStream(src)) {
        qc.checkStop();
        for(final Path child : children) {
          relocate(child, trg.resolve(child.getFileName()), copy, qc);
        }
      }
      if(!copy) Files.delete(src);
    } else if(copy) {
      Files.copy(src, trg, StandardCopyOption.REPLACE_EXISTING);
    } else {
      Files.move(src, trg, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
