package org.basex.gui.view.project;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Project file cache.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
final class ProjectFiles {
  /** Maximum number of filtered hits (speeds up search). */
  private static final int MAXHITS = 256;
  /** Parse id. */
  private static long parseId;
  /** Filter id. */
  private static long filterId;

  /** Files with errors. */
  private TreeMap<String, InputInfo> errors = new TreeMap<>();
  /** Current file cache (can be {@code null}). */
  private ProjectCache cache;

  /**
   * Invalidates the file cache.
   */
  void reset() {
    cache = null;
    // stop existing operations
    ++filterId;
    ++parseId;
  }

  /**
   * Returns paths to files with errors.
   * @return file list
   */
  TreeMap<String, InputInfo> errors() {
    return errors;
  }

  /**
   * Chooses files that match the specified pattern.
   * @param file file filter
   * @param content content filter
   * @param root root directory
   * @return sorted file paths
   * @throws InterruptedException interruption
   */
  String[] filter(final String file, final String content, final IOFile root)
      throws InterruptedException {

    final long id = ++filterId;
    final TreeSet<String> results = new TreeSet<>();
    final int[] search = new TokenParser(Token.lc(Token.token(content))).toArray();

    // glob pattern
    final ProjectCache pc = cache(root);
    if(file.contains("*") || file.contains("?")) {
      final Pattern pt = Pattern.compile(IOFile.regex(file));
      for(final String path : pc) {
        final int offset = offset(path, true);
        if(pt.matcher(path.substring(offset)).matches() && filterContent(path, search)) {
          results.add(path);
          if(results.size() >= MAXHITS) break;
        }
        if(id != filterId) throw new InterruptedException();
      }
    } else {
      // starts-with, contains, camel case
      final String pttrn = file.toLowerCase(Locale.ENGLISH).replace('\\', '/');
      final HashSet<String> exclude = new HashSet<>();
      final boolean pathSearch = pttrn.indexOf('/') != -1;
      for(int i = 0; i < (pathSearch ? 2 : 3); i++) {
        filter(pttrn, search, i, results, exclude, pathSearch, pc, id);
      }
    }
    return results.toArray(new String[results.size()]);
  }

  /**
   * Refreshes the view after a file has been saved.
   * @param root root directory
   * @param ctx database context
   * @throws InterruptedException interruption
   */
  void parse(final IOFile root, final Context ctx) throws InterruptedException {
    final long id = ++parseId;
    final HashSet<String> parsed = new HashSet<>();
    final TreeMap<String, InputInfo> errs = new TreeMap<>();

    // collect files to be parsed
    final ProjectCache pc = cache(root);
    final StringList mods = new StringList(), lmods = new StringList();
    for(final String path : pc) {
      final IOFile file = new IOFile(path);
      if(file.hasSuffix(IO.XQSUFFIXES)) (file.hasSuffix(IO.XQMSUFFIX) ? lmods : mods).add(path);
    }
    mods.add(lmods);

    // parse modules
    for(final String path : mods) {
      if(id != parseId) throw new InterruptedException();
      if(parsed.contains(path)) continue;

      final IOFile file = new IOFile(path);
      try(TextInput ti = new TextInput(file)) {
        // parse query
        try(QueryContext qc = new QueryContext(ctx)) {
          final String input = ti.cache().toString();
          final boolean lib = QueryProcessor.isLibrary(input);
          qc.parse(input, lib, path, null);
          // parsing was successful: remember path
          parsed.add(path);
          for(final byte[] mod : qc.modParsed) parsed.add(Token.string(mod));
        } catch(final QueryException ex) {
          // parsing failed: remember path
          errs.put(path, ex.info());
          parsed.add(path);
        }
      } catch(final IOException ex) {
        // file may not be accessible
        Util.debug(ex);
      }
    }
    errors = errs;
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Returns the current file cache.
   * @param root root directory
   * @return id, or {@code null} if newer file cache exists
   * @throws InterruptedException interruption
   */
  private ProjectCache cache(final IOFile root) throws InterruptedException {
    final ProjectCache pc = cache;
    if(pc == null) {
      // no file cache available: create and return new one
      cache = new ProjectCache();
      add(root, cache);
      cache.finish();
    } else {
      // wait until file cache is initialized
      while(!pc.valid()) {
        Performance.sleep(1);
        // file cache was replaced with newer version
        if(pc != cache) throw new InterruptedException();
      }
    }
    // return existing file cache
    return cache;
  }

  /**
   * Recursively populates the cache.
   * @param root root directory
   * @param pc file cache
   * @throws InterruptedException interruption
   */
  private void add(final IOFile root, final ProjectCache pc) throws InterruptedException {
    // check if file cache was replaced or invalidated
    if(pc != cache) throw new InterruptedException();

    final IOFile[] files = root.children();
    for(final IOFile file : files) {
      if(file.name().equals(IO.IGNORESUFFIX)) return;
    }
    for(final IOFile file : files) {
      if(file.isDir()) {
        add(file, pc);
      } else {
        pc.add(file.path());
      }
    }
  }

  /**
   * Chooses tokens from the file cache that match the specified pattern.
   * @param pattern file pattern
   * @param search search string
   * @param mode search mode (0-2)
   * @param results search result
   * @param exclude exclude file from content search
   * @param pathSearch path flag
   * @param pc file cache
   * @param id search id
   * @throws InterruptedException interruption
   */
  private static void filter(final String pattern, final int[] search, final int mode,
      final TreeSet<String> results, final HashSet<String> exclude, final boolean pathSearch,
      final ProjectCache pc, final long id) throws InterruptedException {

    if(results.size() >= MAXHITS) return;

    for(final String path : pc) {
      // check if current file matches the pattern
      final String lc = path.toLowerCase(Locale.ENGLISH).replace('\\', '/');
      final int offset = offset(lc, pathSearch);
      if(mode == 0 ? lc.startsWith(pattern, offset) :
         mode == 1 ? (lc.indexOf(pattern, offset) != -1) :
         matches(lc, pattern, offset)) {
        if(!exclude.contains(path)) {
          exclude.add(path);
          if(filterContent(path, search)) {
            results.add(path);
            if(results.size() >= MAXHITS) return;
          }
        }
      }
      if(id != filterId) throw new InterruptedException();
    }
  }

  /**
   * Searches a string in a file.
   * @param path file path
   * @param search codepoints of search string
   * @return success flag
   */
  private static boolean filterContent(final String path, final int[] search) {
    final int cl = search.length;
    if(cl == 0) return true;

    try(TextInput ti = new TextInput(new IOFile(path))) {
      final IntList il = new IntList(cl - 1);
      int c = 0;
      while(true) {
        if(!il.isEmpty()) {
          if(il.remove(0) == search[c++]) continue;
          c = 0;
        }
        while(true) {
          final int cp = ti.read();
          if(cp == -1 || !XMLToken.valid(cp)) return false;
          final int lc = Token.lc(cp);
          if(c > 0) il.add(lc);
          if(lc == search[c]) {
            if(++c == cl) return true;
          } else {
            c = 0;
            break;
          }
        }
      }
    } catch(final IOException ex) {
      // file may not be accessible
      Util.debug(ex);
      return false;
    }
  }

  /**
   * Returns the offset after the last slash, or {@code 0} if full paths are to be processed.
   * @param input input string
   * @param path full path processing
   * @return resulting offset
   */
  private static int offset(final String input, final boolean path) {
    if(path) return 0;
    final int a = input.lastIndexOf('\\'), b = input.lastIndexOf('/');
    return (a > b ? a : b) + 1;
  }

  /**
   * Checks if the all characters of the pattern occur in the input in the given order.
   * @param input input string
   * @param pattern pattern
   * @param off offset
   * @return result of check
   */
  private static boolean matches(final String input, final String pattern, final int off) {
    final int il = input.length(), pl = pattern.length();
    int p = 0;
    for(int i = off; i < il && p < pl; i++) {
      if(pattern.charAt(p) == input.charAt(i)) p++;
    }
    return p == pl;
  }
}
