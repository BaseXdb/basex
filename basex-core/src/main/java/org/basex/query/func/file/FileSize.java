package org.basex.query.func.file;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileSize extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws IOException, QueryException {
    final Path path = toPath(arg(0), qc);
    final boolean recursive = toBooleanOrFalse(arg(1), qc);

    final long size;
    if(recursive) {
      size = size(path, qc);
    } else {
      final BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
      size = attrs.isDirectory() ? 0 : attrs.size();
    }
    return Itr.get(size);
  }

  /**
   * Recursively computes the file size.
   * @param path current path
   * @param qc query context
   * @return file size
   * @throws IOException I/O exception
   */
  private static long size(final Path path, final QueryContext qc) throws IOException {
    final BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
    long s = 0;
    if(attrs.isDirectory()) {
      try(DirectoryStream<Path> children = Files.newDirectoryStream(path)) {
        for(final Path child : children) {
          qc.checkStop();
          s += size(child, qc);
        }
      } catch(final IOException ex) {
        Util.debug(ex);
      }
    } else if(attrs.isRegularFile()) {
      s = attrs.size();
    }
    return s;
  }
}
