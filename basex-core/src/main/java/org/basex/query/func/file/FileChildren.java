package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FileChildren extends FileRead {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    try {
      final TokenList children = new TokenList();
      try(DirectoryStream<Path> paths = Files.newDirectoryStream(toPath(0, qc))) {
        for(final Path child : paths) children.add(get(child, Files.isDirectory(child)).string());
      }
      return StrSeq.get(children).iter();
    } catch(final NoSuchFileException | NotDirectoryException ex) {
      throw FILE_NO_DIR_X.get(info, ex);
    } catch(final AccessDeniedException ex) {
      throw FILE_IE_ERROR_ACCESS_X.get(info, ex);
    } catch(final IOException ex) {
      throw FILE_IO_ERROR_X.get(info, ex);
    }
  }
}
