package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FileChildren extends FileRead {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    try {
      final TokenList tl = new TokenList();
      try(DirectoryStream<Path> paths = Files.newDirectoryStream(toPath(0, qc))) {
        for(final Path path : paths) {
          qc.checkStop();
          tl.add(get(path, Files.isDirectory(path)).string());
        }
      }
      return StrSeq.get(tl);
    } catch(final NoSuchFileException | NotDirectoryException ex) {
      throw FILE_NO_DIR_X.get(info, ex);
    } catch(final AccessDeniedException ex) {
      throw FILE_IE_ERROR_ACCESS_X.get(info, ex);
    } catch(final IOException ex) {
      throw FILE_IO_ERROR_X.get(info, ex);
    }
  }
}
