package org.basex.query.func.file;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.regex.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
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
    final String glob = toStringOrNull(arg(2), qc);

    final Pattern pattern = glob == null ? null :
      Pattern.compile(IOFile.regex(glob, false), Prop.CASE ? 0 : Pattern.CASE_INSENSITIVE);
    final TokenList tl = new TokenList();
    list(dir, null, new HofArgs(1), pattern, dir.getNameCount(),
        null, new HofArgs(1), tl, recursive ? Integer.MAX_VALUE : 0, true, qc);
    return StrSeq.get(tl);
  }

  /**
   * Collects the subdirectories and files of the specified directory.
   * @param root root path
   * @param recurse subtree predicate (can be {@code null})
   * @param recurseArgs arguments for the subtree predicate
   * @param pattern file name pattern; ignored if {@code null}
   * @param index index of root path for relative paths; {@code -1} for absolute paths
   * @param filter inclusion predicate (can be {@code null})
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
    final HashSet<Path> links = new HashSet<>();
    try {
      // depth 1: the attributes are taken from the directory listing
      Files.walkFileTree(root, Set.of(), 1, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs)
            throws IOException {
          if(path.equals(root)) throw new NotDirectoryException(path.toString());
          qc.checkStop();
          final boolean dir = attrs.isDirectory() ||
              attrs.isSymbolicLink() && Files.isDirectory(path);
          if(dir && IOFile.isLink(attrs)) links.add(path);
          (dir ? dirs : files).add(path);
          return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult visitFileFailed(final Path path, final IOException ex)
            throws IOException {
          if(path.equals(root)) throw ex;
          files.add(path);
          return FileVisitResult.CONTINUE;
        }
      });
    } catch(final IOException ex) {
      // skip entries that cannot be accessed; throw exception only on root level
      if(top) {
        throw ex;
      }
      return;
    }

    // add directories
    for(final Path child : dirs) {
      final Str path = add(child, true, pattern, filter, filterArgs, index, list, qc);
      // recursive traversal: descend if the depth allows it, do not follow links
      if(depth > 0 && !links.contains(child)) {
        final Str p = path != null ? path : get(subPath(child, index), true);
        if(recurse == null || test(recurse, recurseArgs.set(0, p), qc)) {
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
   * @param filter inclusion predicate (can be {@code null})
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
    if(filter == null || test(filter, filterArgs.set(0, path), qc)) list.add(path.string());
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
}
