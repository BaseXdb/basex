package org.basex.query.func.file;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FileLastModified extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws IOException, QueryException {
    final Path path = toPath(0, qc);
    final BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
    return new Dtm(attrs.lastModifiedTime().toMillis(), info);
  }
}
