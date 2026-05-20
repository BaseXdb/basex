package org.basex.query.func.file;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FileList extends FileFn {
  @Override
  public Value eval(final QueryContext qc) throws QueryException, IOException {
    final Path dir = toPath(arg(0), qc).toRealPath();
    final boolean recursive = toBooleanOrFalse(arg(1), qc);
    final String pattern = toStringOrNull(arg(2), qc);

    final Pattern pttrn = pattern == null ? null :
      Pattern.compile(IOFile.regex(pattern, false), Prop.CASE ? 0 : Pattern.CASE_INSENSITIVE);
    final TokenList tl = new TokenList();
    final FItem recurse = constantFn(recursive), filter = constantFn(true);
    list(dir, recurse, new HofArgs(1), pttrn, dir.getNameCount(),
        filter, new HofArgs(1), tl, Integer.MAX_VALUE, true, qc);
    return StrSeq.get(tl);
  }

  /**
   * Collects the subdirectories and files of the specified directory.
   * @param root root path
   * @param recurse subtree predicate
   * @param recurseArgs arguments for the subtree predicate
   * @param pattern file name pattern; ignored if {@code null}
   * @param index index of root path for relative paths; {@code -1} for absolute paths
   * @param filter inclusion predicate
   * @param filterArgs arguments for the inclusion predicate
   * @param list file list
   * @param depth maximum number of subdirectory levels to descend ({@code 0}: none)
   * @param top {@code true} on the initial call (errors will be propagated)
   * @param qc query context
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final void list(final Path root, final FItem recurse, final HofArgs recurseArgs,
      final Pattern pattern, final int index, final FItem filter, final HofArgs filterArgs,
      final TokenList list, final int depth, final boolean top, final QueryContext qc)
      throws QueryException, IOException {

    // collect directories and files first (reduces number of open directory streams)
    final ArrayList<Path> dirs = new ArrayList<>(), files = new ArrayList<>();
    try(DirectoryStream<Path> paths = Files.newDirectoryStream(root)) {
      for(final Path path : paths) {
        qc.checkStop();
        (Files.isDirectory(path) ? dirs : files).add(path);
      }
    } catch(final IOException ex) {
      // skip entries that cannot be accessed; throw exception only on root level
      if(top) {
        Util.debug(ex);
        throw ex;
      }
      return;
    }

    // add directories
    for(final Path child : dirs) {
      final Str path = add(child, true, pattern, filter, filterArgs, index, list, qc);
      // recursive traversal: descend if the depth allows it, do not follow links
      if(depth > 0 && !Files.isSymbolicLink(child)) {
        final Str p = path != null ? path : get(subPath(child, index), true);
        if(test(recurse, recurseArgs.set(0, p), qc)) {
          list(child, recurse, recurseArgs, pattern, index, filter, filterArgs,
              list, depth - 1, false, qc);
        }
      }
    }

    // add files
    for(final Path child : files) {
      add(child, false, pattern, filter, filterArgs, index, list, qc);
    }
  }

  /**
   * Adds an entry to the result list, applying pattern and filter checks.
   * @param child raw path
   * @param isDir directory flag
   * @param pattern file name pattern (can be {@code null})
   * @param filter inclusion predicate
   * @param filterArgs arguments for the inclusion predicate
   * @param index index of root path (or {@code -1})
   * @param list file list
   * @param qc query context
   * @return display path, or {@code null} if the pattern excluded the entry
   * @throws QueryException query exception
   */
  private Str add(final Path child, final boolean isDir, final Pattern pattern,
      final FItem filter, final HofArgs filterArgs, final int index, final TokenList list,
      final QueryContext qc) throws QueryException {
    // pattern check (operates on the file name)
    if(pattern != null && !pattern.matcher(child.getFileName().toString()).matches()) return null;

    final Str path = get(subPath(child, index), isDir);
    if(test(filter, filterArgs.set(0, path), qc)) list.add(path.string());
    return path;
  }

  /**
   * Returns the effective result path.
   * @param child raw path
   * @param index index of root path for relative paths; {@code -1} for absolute paths
   * @return display path
   */
  private static Path subPath(final Path child, final int index) {
    return index < 0 ? child : child.subpath(index, child.getNameCount());
  }

  /**
   * Creates a boolean predicate function.
   * @param value return value
   * @return function item
   */
  final FItem constantFn(final boolean value) {
    return new FuncItem(info, Bln.get(value), new Var[0], AnnList.EMPTY,
      FuncType.get(Types.BOOLEAN_O), 0, null);
  }
}
