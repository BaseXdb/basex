package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions on files and directories.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 * @author Christian Gruen
 */
public abstract class FileFn extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    try {
      return item(qc);
    } catch(final NoSuchFileException ex) {
      throw FILE_NOT_FOUND_X.get(info, ex);
    } catch(final AccessDeniedException ex) {
      throw FILE_IE_ERROR_ACCESS_X.get(info, ex);
    } catch(final FileAlreadyExistsException ex) {
      throw FILE_EXISTS_X.get(info, ex);
    } catch(final DirectoryNotEmptyException ex) {
      throw FILE_ID_DIR2_X.get(info, ex);
    } catch(final IOException ex) {
      throw FILE_IO_ERROR_X.get(info, ex);
    }
  }

  /**
   * Evaluates the expression and returns the resulting item.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   * @throws IOException query exception
   */
  @SuppressWarnings("unused")
  public Item item(final QueryContext qc) throws QueryException, IOException {
    return super.item(qc, null);
  }

  /**
   * Checks that the parent of the specified path is a directory, but is no directory itself.
   * @param path file to be written
   * @return specified file
   * @throws QueryException query exception
   */
  final Path checkParentDir(final Path path) throws QueryException {
    if(Files.isDirectory(path)) throw FILE_IS_DIR_X.get(info, path);
    final Path parent = path.getParent();
    if(parent != null && !Files.exists(parent)) throw FILE_NO_DIR_X.get(info, parent);
    return path;
  }

  /**
   * Returns the value of an optional boolean.
   * @param i argument index
   * @param qc query context
   * @return boolean value
   * @throws QueryException query exception
   */
  final boolean optionalBool(final int i, final QueryContext qc) throws QueryException {
    return i < exprs.length && toBoolean(exprs[i], qc);
  }

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
