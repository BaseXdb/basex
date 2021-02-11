package org.basex.gui.view.project;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Project file cache.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class ProjectFiles {
  /** Maximum number of filtered hits (speeds up search). */
  private static final int MAXHITS = 256;
  /** Parse id. */
  private static long parseId;
  /** Filter id. */
  private static long filterId;

  /** Project view. */
  private final ProjectView view;

  /** Files with errors. */
  private TreeMap<String, InputInfo> errors = new TreeMap<>();
  /** Current file cache (can be {@code null}). */
  private ProjectCache cache;

  /**
   * Project files.
   * @param view project view
   */
  ProjectFiles(final ProjectView view) {
    this.view = view;
  }

  /**
   * Invalidates the file cache.
   */
  void reset() {
    cache = null;
    // stop currently running operations
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
   * @param files files filter
   * @param contents contents filter
   * @param root root directory
   * @return sorted file paths
   * @throws InterruptedException interruption
   */
  String[] filter(final String files, final String contents, final IOFile root)
      throws InterruptedException {

    final long id = ++filterId;
    final StringList results = new StringList();
    final int[] search = new TokenParser(Token.lc(Token.token(contents))).toArray();

    // glob pattern
    final ProjectCache pc = cache(root);
    if(files.contains("*") || files.contains("?")) {
      final Pattern pt = Pattern.compile(IOFile.regex(files));
      for(final String path : pc) {
        if(pt.matcher(path).matches() && filterContent(path, search)) {
          results.add(path);
          if(results.size() >= MAXHITS) break;
        }
        if(id != filterId) throw new InterruptedException();
      }
    } else {
      filter(files, search, id, results, pc);
    }
    return results.finish();
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

    // collect files to be parsed (parse main modules first)
    final StringList paths = new StringList(), libs = new StringList();
    for(final String path : cache(root)) {
      final IOFile file = new IOFile(path);
      if(file.hasSuffix(IO.XQSUFFIXES)) {
        (file.hasSuffix(IO.XQMSUFFIX) ? libs : paths).add(path);
      }
    }
    paths.add(libs);

    // parse modules
    for(final String path : paths) {
      if(id != parseId) throw new InterruptedException();
      if(parsed.contains(path)) continue;

      final TokenMap modules = parse(path, ctx, errs);
      if(modules != null) {
        for(final byte[] mod : modules) parsed.add(Token.string(mod));
      }
    }
    errors = errs;
  }

  /**
   * Parses a single file.
   * @param path file path
   * @param ctx database context
   * @param errs files with errors
   * @return parsed modules or {@code null}
   */
  static TokenMap parse(final String path, final Context ctx,
      final TreeMap<String, InputInfo> errs) {

    try(TextInput ti = new TextInput(new IOFile(path))) {
      // parse query
      try(QueryContext qc = new QueryContext(ctx)) {
        final String input = ti.cache().toString();
        final boolean library = QueryProcessor.isLibrary(input);
        qc.parse(input, library, path);
        errs.remove(path);
        return qc.modParsed;
      } catch(final QueryException ex) {
        errs.put(path, ex.info());
      }
    } catch(final IOException ex) {
      // file may not be accessible
      Util.debug(ex);
    }
    return null;
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
      cache = new ProjectCache(view.gui.gopts.get(GUIOptions.SHOWHIDDEN));
      cache.scan(Paths.get(root.path()), p -> p != cache);
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
   * Chooses tokens from the file cache that match the specified pattern.
   * @param files files filter
   * @param search codepoints of search string
   * @param id search id
   * @param results search result
   * @param cache file cache
   * @throws InterruptedException interruption
   */
  private static void filter(final String files, final int[] search, final long id,
      final StringList results, final ProjectCache cache) throws InterruptedException {

    final String query = files.replace('\\', '/');
    final HashSet<String> exclude = new HashSet<>();
    for(final boolean onlyName : new boolean[] { true, false }) {
      for(int mode = 0; mode < 5; mode++) {
        for(final String path : cache) {
          // check if file has already been added, or it its contents have been scanned
          if(exclude.contains(path)) continue;
          // check if current file matches the pattern
          final String file = onlyName ? path.substring(path.lastIndexOf('/') + 1) : path;
          if(mode == 0 ? SmartStrings.startsWith(file, query) :
             mode == 1 ? SmartStrings.contains(file, query) :
             SmartStrings.matches(file, query)) {

            // check file contents
            if(filterContent(path, search)) {
              // add path, skip remaining files if limit has been reached
              results.add(path);
              if(results.size() >= MAXHITS) return;
            }
            exclude.add(path);
          }
          if(id != filterId) throw new InterruptedException();
        }
      }
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

    // parse input as UTF-8
    try(TextInput ti = new TextInput(new IOFile(path))) {
      final IntList il = new IntList(cl - 1);
      int c = 0;
      while(true) {
        // process cached characters
        while(!il.isEmpty()) {
          if(il.remove(0) == search[c]) {
            c++;
          } else {
            c = 0;
          }
        }
        // read and cache new characters
        while(true) {
          final int i = ti.read();
          if(i == -1 || !XMLToken.valid(i)) return false;
          final int cp = Token.lc(i);
          if(c > 0) il.add(cp);
          if(cp == search[c]) {
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
}
