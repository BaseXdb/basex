package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FileCopy extends FileFn {
  @Override
  public Value eval(final QueryContext qc) throws IOException, QueryException {
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
  final void relocate(final boolean copy, final QueryContext qc)
      throws QueryException, IOException {

    final Path src = absolute(toPath(arg(0), qc));
    if(!Files.exists(src)) throw FILE_NOT_FOUND_X.get(info, src);
    Path trg = absolute(toPath(arg(1), qc));

    // source and target refer to the same file (identical path, symbolic link,
    // or case difference on case-insensitive file systems)
    if(Files.exists(trg) && Files.isSameFile(src, trg)) {
      // adjust capitalization of the file name if necessary
      if(!copy && !src.toString().equals(trg.toString())) move(src, trg);
      return;
    }

    if(Files.isDirectory(trg)) {
      // target is a directory: attach file name
      trg = trg.resolve(src.getFileName());
      if(!Files.isDirectory(src) && Files.isDirectory(trg))
        throw FILE_IS_DIR_X.get(info, trg);
    } else if(Files.exists(trg) && Files.isDirectory(src)) {
      // if target is file, source cannot be a directory
      throw FILE_IS_DIR_X.get(info, src);
    }

    // reject targets located inside the source directory
    if(Files.isDirectory(src) && trg.startsWith(src))
      throw FILE_CYCLIC_X_X.get(info, trg, src);

    relocate(src, trg, copy, qc);
  }

  /**
   * Recursively relocates files.
   * @param src source path
   * @param trg target path
   * @param copy copy or move files
   * @param job job
   * @throws IOException I/O exception
   */
  public static void relocate(final Path src, final Path trg, final boolean copy, final Job job)
      throws IOException {

    job.checkStop();
    if(Files.isDirectory(src)) {
      if(!Files.exists(trg)) Files.createDirectory(trg);
      try(DirectoryStream<Path> children = Files.newDirectoryStream(src)) {
        for(final Path child : children) {
          relocate(child, trg.resolve(child.getFileName()), copy, job);
        }
      }
      if(!copy) Files.delete(src);
    } else {
      if(!Files.exists(trg.getParent())) Files.createDirectories(trg.getParent());
      if(copy) {
        Files.copy(src, trg, StandardCopyOption.REPLACE_EXISTING);
      } else {
        move(src, trg);
      }
    }
  }

  /**
   * Moves a file or directory, handling case-only differences on case-insensitive
   * file systems via an intermediate path.
   * @param src source path
   * @param trg target path
   * @throws IOException I/O exception
   */
  private static void move(final Path src, final Path trg) throws IOException {
    Path path = src;
    final String s = src.getFileName().toString(), t = trg.getFileName().toString();
    if(!s.equals(t) && s.equalsIgnoreCase(t)) {
      path = src.resolveSibling(UUID.randomUUID().toString());
      Files.move(src, path);
    }
    Files.move(path, trg, StandardCopyOption.REPLACE_EXISTING);
  }
}
