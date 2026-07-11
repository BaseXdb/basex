package org.basex.gui.view.project;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.text.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Project file cache.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ProjectFiles {
  /** Maximum number of filtered hits (speeds up search). */
  static final int MAXHITS = 1000;
  /** Content match result: the file contains the search string. */
  static final int FOUND = 1;
  /** Content match result: the file does not contain the search string. */
  static final int MISSING = 0;
  /** Content match result: the file is binary or unreadable. */
  static final int BINARY = -1;
  /** Maximum file size for the buffered regular-expression search (larger files are skipped). */
  private static final long MAXBYTES = 100L << 20;
  /** Regular-expression metacharacters (a string without any of these is a plain literal). */
  private static final String REGEX_META = "\\.[]{}()*+?^$|";
  /** Parse ID. */
  private static long parseId;
  /** Filter ID. */
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
   * @param content contents filter
   * @param root root directory
   * @return sorted file paths
   * @throws InterruptedException interruption
   */
  String[] filter(final String files, final ContentFilter content, final IOFile root)
      throws InterruptedException {

    final long id = ++filterId;
    final StringList results = new StringList();

    // glob pattern
    final ProjectCache pc = cache(root);
    if(files.contains("*") || files.contains("?")) {
      final Pattern pt = Pattern.compile(IOFile.regex(files));
      for(final String path : pc) {
        if(id != filterId) throw new InterruptedException();
        if(pt.matcher(path).matches() && content.matches(path) && add(path, results)) break;
      }
    } else {
      filter(files, content, id, results, pc);
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

      final TokenObjectMap<byte[]> modules = parse(path, ctx, errs);
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
   * @param errors files with errors
   * @return parsed modules or {@code null}
   */
  static TokenObjectMap<byte[]> parse(final String path, final Context ctx,
      final TreeMap<String, InputInfo> errors) {

    try(TextInput ti = new TextInput(new IOFile(path))) {
      final String input = ti.cache().toString();
      // parse query
      try(QueryContext qc = new QueryContext(ctx)) {
        qc.parse(input, path);
        errors.remove(path);
        return qc.modParsed;
      } catch(final QueryException ex) {
        errors.put(path, ex.info());
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
   * @return ID, or {@code null} if newer file cache exists
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
   * @param content content filter
   * @param id search ID
   * @param results search result
   * @param cache file cache
   * @throws InterruptedException interruption
   */
  private static void filter(final String files, final ContentFilter content, final long id,
      final StringList results, final ProjectCache cache) throws InterruptedException {

    final String query = files.replace('\\', '/');
    final HashSet<String> exclude = new HashSet<>();
    for(final boolean onlyName : new boolean[] { true, false }) {
      for(int mode = 0; mode < 3; mode++) {
        for(final String path : cache) {
          if(id != filterId) throw new InterruptedException();
          // skip files that were already added or whose contents were already scanned
          if(exclude.contains(path)) continue;
          // check if the file name (or path) matches the pattern
          final String file = onlyName ? path.substring(path.lastIndexOf('/') + 1) : path;
          if(nameMatches(mode, file, query)) {
            if(content.matches(path) && add(path, results)) return;
            exclude.add(path);
          }
        }
      }
    }
  }

  /**
   * Checks if a file name or path matches the query with the specified strategy.
   * @param mode match strategy (0: prefix, 1: substring, 2: characters)
   * @param file file name or path
   * @param query search query
   * @return result of check
   */
  private static boolean nameMatches(final int mode, final String file, final String query) {
    return mode == 0 ? SmartStrings.startsWith(file, query) :
           mode == 1 ? SmartStrings.contains(file, query) :
           SmartStrings.containsChars(file, query, false);
  }

  /**
   * Adds a matching path to the results.
   * @param path file path
   * @param results result list
   * @return {@code true} if the hit limit has been reached
   */
  private static boolean add(final String path, final StringList results) {
    results.add(path);
    return results.size() >= MAXHITS;
  }

  /**
   * Content matcher. Also records how many files were examined, skipped because they were too
   * large, or skipped because they were binary, and the error of an invalid regular expression.
   */
  static final class ContentFilter {
    /** Streaming search codepoints ({@code null} for the buffered or trivial matcher). */
    private final int[] cps;
    /** KMP prefix function of {@link #cps} ({@code null} if {@link #cps} is {@code null}). */
    private final int[] lps;
    /** Fold characters to lower case (streaming matcher). */
    private final boolean fold;
    /** Search pattern ({@code null} for the streaming or trivial matcher). */
    private final Pattern pattern;
    /** Error message of an invalid regular expression ({@code null} otherwise). */
    private final String error;
    /** Number of files whose contents were examined. */
    private int searched;
    /** Number of files skipped because they exceed {@link ProjectFiles#MAXBYTES}. */
    private int tooLarge;
    /** Number of files skipped because they are binary. */
    private int binary;

    /**
     * Constructor.
     * @param cps streaming search codepoints (can be {@code null})
     * @param fold fold characters to lower case
     * @param pattern search pattern (can be {@code null})
     * @param error error of an invalid expression (can be {@code null})
     */
    private ContentFilter(final int[] cps, final boolean fold, final Pattern pattern,
        final String error) {
      this.cps = cps;
      this.lps = cps != null ? prefixes(cps) : null;
      this.fold = fold;
      this.pattern = pattern;
      this.error = error;
    }

    /**
     * Checks if the contents of a file match.
     * @param path file path
     * @return result of check
     */
    boolean matches(final String path) {
      if(cps != null) {
        // streaming scan: distinguish a binary file from a plain non-match
        final int result = filterContent(path, cps, lps, fold);
        if(result == BINARY) binary++;
        else searched++;
        return result == FOUND;
      }
      // no pattern: an empty search matches every file, an invalid expression none
      if(pattern == null) return error == null;
      final IOFile file = new IOFile(path);
      // skip files that are too large to scan
      if(file.length() > MAXBYTES) {
        tooLarge++;
        return false;
      }
      final String text = read(file);
      if(text == null) {
        binary++;
        return false;
      }
      searched++;
      return pattern.matcher(text).find();
    }

    /**
     * Returns the number of files whose contents were examined.
     * @return count
     */
    int searched() {
      return searched;
    }

    /**
     * Returns the number of files that were skipped because they were too large.
     * @return count
     */
    int tooLarge() {
      return tooLarge;
    }

    /**
     * Returns the number of files that were skipped because they were binary.
     * @return count
     */
    int binary() {
      return binary;
    }

    /**
     * Returns the error message of an invalid regular expression.
     * @return error message or {@code null}
     */
    String error() {
      return error;
    }
  }

  /**
   * Creates a filter for the file contents. Plain (non-regex, non-word) searches use a
   * memory-light streaming scan; regular-expression and whole-word searches buffer each candidate
   * file, rejecting binary files and skipping files that exceed {@link #MAXBYTES}.
   * @param contents contents search string
   * @param mcase match case
   * @param word whole word
   * @param regex regular expression
   * @param dotall dot matches all
   * @return content filter
   */
  ContentFilter contentFilter(final String contents, final boolean mcase, final boolean word,
      final boolean regex, final boolean dotall) {
    // empty search string matches every file
    if(contents.isEmpty()) return new ContentFilter(null, false, null, null);

    // fast path: literal searches (incl. regex mode without metacharacters) use a streaming scan
    if(!word && (!regex || literal(contents))) {
      final String search = mcase ? contents : contents.toLowerCase(Locale.ENGLISH);
      return new ContentFilter(search.codePoints().toArray(), !mcase, null, null);
    }

    // buffered path: reuse the editor's pattern logic
    try {
      final Pattern pattern = SearchContext.pattern(contents, mcase, word, regex, dotall);
      return new ContentFilter(null, false, pattern, null);
    } catch(final PatternSyntaxException ex) {
      // invalid expression: match no file, remember the error
      return new ContentFilter(null, false, null, ex.getDescription());
    }
  }

  /**
   * Checks if a regular expression is a plain literal, i.e. contains no metacharacters and can
   * hence be matched with the faster streaming scan.
   * @param string regular expression
   * @return result of check
   */
  private static boolean literal(final String string) {
    return string.chars().noneMatch(ch -> REGEX_META.indexOf(ch) != -1);
  }

  /**
   * Searches a string in a file with a memory-light, single-pass KMP scan.
   * @param path file path
   * @param search codepoints of search string
   * @param lps KMP prefix function of the search string
   * @param fold fold characters to lower case (case-insensitive search)
   * @return match result ({@link #FOUND}, {@link #MISSING} or {@link #BINARY})
   */
  static int filterContent(final String path, final int[] search, final int[] lps,
      final boolean fold) {
    final int cl = search.length;
    if(cl == 0) return FOUND;

    // parse input as UTF-8
    try(TextInput ti = new TextInput(new IOFile(path))) {
      int j = 0;
      while(true) {
        final int i = ti.read();
        if(i == -1) return MISSING;
        if(!XMLToken.valid(i)) return BINARY;
        final int cp = fold ? Token.lc(i) : i;
        while(j > 0 && cp != search[j]) j = lps[j - 1];
        if(cp == search[j] && ++j == cl) return FOUND;
      }
    } catch(final IOException ex) {
      // file may not be accessible
      Util.debug(ex);
      return BINARY;
    }
  }

  /**
   * Computes the KMP prefix function of a search string: {@code lps[i]} is the length of the
   * longest proper prefix of {@code search[0..i]} that is also a suffix.
   * @param search codepoints of search string
   * @return prefix function
   */
  static int[] prefixes(final int[] search) {
    final int cl = search.length;
    final int[] lps = new int[cl];
    for(int i = 1, len = 0; i < cl;) {
      if(search[i] == search[len]) {
        lps[i++] = ++len;
      } else if(len > 0) {
        len = lps[len - 1];
      } else {
        lps[i++] = 0;
      }
    }
    return lps;
  }

  /**
   * Reads a file as text for a regular-expression search. Aborts as soon as an invalid (binary)
   * character is read, before the whole file has been loaded.
   * @param file file
   * @return file contents, or {@code null} if the file is binary or not accessible
   */
  private static String read(final IOFile file) {
    try(TextInput ti = new TextInput(file)) {
      final StringBuilder sb = new StringBuilder();
      for(int i; (i = ti.read()) != -1;) {
        if(!XMLToken.valid(i)) return null;
        sb.appendCodePoint(i);
      }
      return sb.toString();
    } catch(final IOException ex) {
      // file may not be accessible
      Util.debug(ex);
      return null;
    }
  }
}
