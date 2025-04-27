package org.basex.query.func.file;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;

import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Functions on files and directories.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 * @author Christian Gruen
 */
abstract class FileFn extends StandardFunc {
  /** Line separator. */
  private static final byte[] NL = token(Prop.NL);

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
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
  final Path toParent(final Path path) throws QueryException {
    if(Files.isDirectory(path)) throw FILE_IS_DIR_X.get(info, path.toAbsolutePath());
    final Path parent = path.getParent();
    if(parent != null && !Files.exists(parent))
      throw FILE_NO_DIR_X.get(info, parent.toAbsolutePath());
    return path;
  }

  /**
   * Writes contents to a file.
   * @param append append flag
   * @param lines write lines
   * @param qc query context
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final void write(final boolean append, final boolean lines, final QueryContext qc)
      throws QueryException, IOException {

    final Path path = toParent(toPath(arg(0), qc));
    final String encoding = toEncodingOrNull(arg(2), FILE_UNKNOWN_ENCODING_X, qc);
    final Charset cs = encoding == null || encoding == Strings.UTF8 ? null :
      Charset.forName(encoding);

    try(PrintOutput out = PrintOutput.get(new FileOutputStream(path.toFile(), append))) {
      if(lines) {
        final Iter values = arg(1).iter(qc);
        for(Item item; (item = qc.next(values)) != null;) {
          if(!item.type.isStringOrUntyped()) throw typeError(item, AtomType.STRING, info);

          final byte[] token = item.string(info);
          out.write(cs == null ? token : string(token).getBytes(cs));
          out.write(cs == null ? NL : Prop.NL.getBytes(cs));
        }
      } else {
        // workaround to preserve streamable string items
        Item value = toAtomItem(arg(1), qc);
        if(!(value instanceof AStr)) value = Str.get(toToken(value));
        if(cs == null) {
          try(TextInput in = value.stringInput(info)) {
            for(int cp; (cp = in.read()) != -1;) out.print(cp);
          }
        } else {
          out.write(string(value.string(info)).getBytes(cs));
        }
      }
    }
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
