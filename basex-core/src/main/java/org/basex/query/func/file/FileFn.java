package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions on files and directories.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 * @author Christian Gruen
 */
abstract class FileFn extends StandardFunc {
  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    try {
      return eval(qc);
    } catch(final NoSuchFileException ex) {
      Util.debug(ex);
      throw FILE_NOT_FOUND_X.get(info, ex.getMessage());
    } catch(final NotDirectoryException ex) {
      throw FILE_NO_DIR_X.get(info, ex);
    } catch(final FileAlreadyExistsException ex) {
      throw FILE_EXISTS_X.get(info, ex);
    } catch(final DirectoryNotEmptyException ex) {
      throw FILE_IS_DIR2_X.get(info, ex);
    } catch(final AccessDeniedException ex) {
      throw FILE_ACCESS_X.get(info, ex);
    } catch(final IOException ex) {
      throw FILE_IO_ERROR_X.get(info, ex);
    }
  }

  /**
   * Evaluates the expression and returns the resulting value.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   * @throws IOException query exception
   */
  public abstract Value eval(QueryContext qc) throws QueryException, IOException;

  /**
   * Returns a unified string representation of the path.
   * @param path directory path
   * @param dir directory flag
   * @return path string
   */
  static Str get(final Path path, final boolean dir) {
    final String string = path.toString();
    return Str.get(dir && !string.endsWith(File.separator) ? string + File.separator : string);
  }

  /**
   * Returns the absolute, normalized path.
   * @param path input path
   * @return normalized path
   */
  static Path absolute(final Path path) {
    return path.toAbsolutePath().normalize();
  }
}
