package org.basex.query.func.file;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FileList extends FileRead {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    try {
      final Path dir = toPath(0, qc).toRealPath();
      final boolean rec = optionalBool(1, qc);
      final Pattern pat = exprs.length == 3 ? Pattern.compile(IOFile.regex(
          string(toToken(exprs[2], qc))), Prop.CASE ? 0 : Pattern.CASE_INSENSITIVE) : null;

      final TokenList list = new TokenList();
      list(dir.getNameCount(), dir, list, rec, pat);
      return StrSeq.get(list).iter();
    } catch(final NoSuchFileException | NotDirectoryException ex) {
      throw FILE_NO_DIR_X.get(info, ex);
    } catch(final AccessDeniedException ex) {
      throw FILE_IE_ERROR_ACCESS_X.get(info, ex);
    } catch(final IOException ex) {
      throw FILE_IO_ERROR_X.get(info, ex);
    }
  }

  /**
   * Collects the sub-directories and files of the specified directory.
   * @param index index of root path
   * @param dir root path
   * @param list file list
   * @param rec recursive flag
   * @param pat file name pattern; ignored if {@code null}
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private static void list(final int index, final Path dir, final TokenList list, final boolean rec,
      final Pattern pat) throws QueryException, IOException {

    // skip invalid directories
    final ArrayList<Path> children = new ArrayList<>();
    try(DirectoryStream<Path> paths = Files.newDirectoryStream(dir)) {
      for(final Path child : paths) children.add(child);
    } catch(final IOException ex) {
      // only throw exception on root level
      if(index == dir.getNameCount()) throw ex;
    }

    // parse directories, do not follow links
    if(rec) {
      for(final Path child : children) {
        if(Files.isDirectory(child)) list(index, child, list, rec, pat);
      }
    }

    // parse files. ignore directories if a pattern is specified
    for(final Path child : children) {
      if(pat == null || pat.matcher(child.getFileName().toString()).matches()) {
        final Path path = child.subpath(index, child.getNameCount());
        list.add(get(path, Files.isDirectory(child)).string());
      }
    }
  }
}
