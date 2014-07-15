package org.basex.gui.view.project;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.editor.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Project filter.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class ProjectFilter extends BaseXBack {
  /** Maximum number of filtered hits. */
  private static final int MAXHITS = 256;

  /** Files. */
  private final BaseXTextField files;
  /** Contents. */
  private final BaseXTextField contents;

  /** Project view. */
  private final ProjectView project;
  /** Cached file paths. */
  private final TokenList cache = new TokenList();

  /** Last file search. */
  private String lastFiles = "";
  /** Last content search. */
  private String lastContents = "";
  /** Running flag. */
  private boolean running;
  /** Current filter id. */
  private int threadID;

  /**
   * Constructor.
   * @param view project view
   */
  public ProjectFilter(final ProjectView view) {
    project = view;

    layout(new BorderLayout(0, 2));
    files = new BaseXTextField(view.gui);
    files.addFocusListener(project.lastfocus);

    contents = new BaseXTextField(view.gui);
    contents.hint(Text.FIND_CONTENTS + Text.DOTS);
    contents.addFocusListener(project.lastfocus);

    add(files, BorderLayout.NORTH);
    add(contents, BorderLayout.CENTER);

    final KeyAdapter refreshKeys = new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(BaseXKeys.NEXTLINE.is(e) || BaseXKeys.PREVLINE.is(e) ||
           BaseXKeys.NEXTPAGE.is(e) || BaseXKeys.PREVPAGE.is(e)) {
          project.list.dispatchEvent(e);
        } else {
          for(final GUIPopupCmd cmd : project.list.commands) {
            if(cmd == null) continue;
            for(final BaseXKeys sc : cmd.shortcuts()) {
              if(sc.is(e)) {
                cmd.execute(view.gui);
                e.consume();
                return;
              }
            }
          }
        }
      }
      @Override
      public void keyReleased(final KeyEvent e) {
        refresh(false);
      }
    };
    files.addKeyListener(refreshKeys);
    contents.addKeyListener(refreshKeys);
    refreshLayout();
  }

  /**
   * Resets the filter cache.
   */
  void reset() {
    cache.reset();
    refresh(true);
  }

  /**
   * Initializes the file cache.
   * @param thread current thread id
   */
  private void init(final int thread) {
    if(cache.isEmpty()) {
      final TokenSet set = new TokenSet();
      set.add(Text.PLEASE_WAIT_D);
      project.list.setElements(set, null);
      add(project.root.file, thread);
    }
  }

  /**
   * Initializes the filter cache.
   * @param thread current thread id
   * @param root root directory
   */
  private void add(final IOFile root, final int thread) {
    for(final IOFile file : root.children()) {
      if(file.isDir()) {
        add(file, thread);
      } else {
        cache.add(file.path());
      }
      // newer thread has arrived
      if(threadID != thread) {
        cache.reset();
        return;
      }
    }
  }

  /**
   * Filters the entries.
   * @param file file search string
   * @param content content search string
   * @param thread thread id
   */
  private void filter(final String file, final String content, final int thread) {
    // wait when command is still running
    while(running) {
      Thread.yield();
      // newer thread has arrived
      if(threadID != thread) return;
    }

    // thread is accepted; start filtering
    running = true;
    files.setCursor(CURSORWAIT);
    contents.setCursor(CURSORWAIT);
    init(thread);

    // collect matches
    final TokenSet results = new TokenSet();
    final IntList il = new IntList();
    final TokenParser tp = new TokenParser(Token.token(content));
    while(tp.more()) il.add(Token.lc(tp.next()));
    if(filter(file, il.toArray(), thread, results)) {
      project.list.setElements(results, content.isEmpty() ? null : content);
    }

    files.setCursor(CURSORTEXT);
    contents.setCursor(CURSORTEXT);
    running = false;
  }

  /**
   * Refreshes the filter view.
   * @param force force refresh
   */
  void refresh(final boolean force) {
    final String file = files.getText();
    final String content = contents.getText();
    if(!force && lastFiles.equals(file) && lastContents.equals(content)) return;
    lastFiles = file;
    lastContents = content;
    ++threadID;

    final boolean list = !file.isEmpty() || !content.isEmpty();
    if(list) {
      final Thread t = new Thread() {
        @Override
        public void run() {
          filter(file, content, threadID);
        }
      };
      t.setDaemon(true);
      t.start();
    }
    project.showList(list);
  }

  /**
   * Filters the file search field.
   * @param ea calling editor
   */
  void find(final EditorArea ea) {
    final String string = ea.searchString();
    if(string != null) {
      contents.requestFocusInWindow();
      contents.setText(string);
      if(ea.opened()) {
        final String name = ea.file().name();
        final int i = name.lastIndexOf('.');
        final String file = files.getText();
        final String pattern = file.isEmpty() ? project.gui.gopts.get(GUIOptions.FILES) : file;
        if(i != -1 && !pattern.contains("*") && !pattern.contains("?") ||
            !Pattern.compile(IOFile.regex(pattern)).matcher(name).matches()) {
          files.setText('*' + name.substring(i));
        }
      }
      refresh(false);
    } else {
      files.requestFocusInWindow();
    }
  }

  /**
   * Filters the file search field.
   * @param node node
   */
  void find(final ProjectNode node) {
    if(node != null) files.setText(node.file.path());
    refresh(false);
    files.requestFocusInWindow();
  }

  /**
   * Called when GUI design has changed.
   */
  public void refreshLayout() {
    final String filter = project.gui.gopts.get(GUIOptions.FILES).trim();
    files.hint(filter.isEmpty() ? Text.FIND_FILES + Text.DOTS : filter);
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Chooses tokens from the file cache that match the specified pattern.
   * @param file file pattern
   * @param search search string
   * @param thread current thread id
   * @param results search result
   * @return success flag
   */
  private boolean filter(final String file, final int[] search, final int thread,
      final TokenSet results) {

    // glob pattern
    final String pattern = file.isEmpty() ? project.gui.gopts.get(GUIOptions.FILES) : file;
    if(pattern.contains("*") || pattern.contains("?")) {
      final Pattern pt = Pattern.compile(IOFile.regex(pattern));

      for(final byte[] input : cache) {
        final int offset = offset(input, true);
        if(pt.matcher(Token.string(Token.substring(input, offset))).matches() &&
            filterContent(input, search, results)) return true;
        if(thread != threadID) return false;
      }
    }

    // starts-with, contains, camel case
    final byte[] pttrn = Token.replace(Token.lc(Token.token(pattern)), '\\', '/');
    final TokenSet exclude = new TokenSet();
    final boolean path = Token.indexOf(pttrn, '/') != -1;
    for(int i = 0; i < (path ? 2 : 3); i++) {
      if(!filter(pttrn, search, thread, i, results, exclude, path)) return false;
    }
    return true;
  }

  /**
   * Chooses tokens from the file cache that match the specified pattern.
   * @param pattern file pattern
   * @param search search string
   * @param thread current thread id
   * @param mode search mode (0-2)
   * @param results search result
   * @param exclude exclude file from content search
   * @param path path flag
   * @return success flag
   */
  private boolean filter(final byte[] pattern, final int[] search, final int thread, final int mode,
      final TokenSet results, final TokenSet exclude, final boolean path) {

    if(results.size() < MAXHITS) {
      for(final byte[] input : cache) {
        // check if current file matches the pattern
        final byte[] lc = Token.replace(Token.lc(input), '\\', '/');
        final int offset = offset(lc, path);
        if(mode == 0 ? Token.startsWith(lc, pattern, offset) :
           mode == 1 ? Token.contains(lc, pattern, offset) :
           matches(lc, pattern, offset)) {
          if(!exclude.contains(input)) {
            exclude.add(input);
            if(filterContent(input, search, results)) return true;
          }
        }
        if(thread != threadID) return false;
      }
    }
    return true;
  }

  /**
   * Adds a file to the matches if the specified string is found.
   * Checks the file contents.
   * @param path file path
   * @param search search string
   * @param results search result
   * @return maximum number of results reached
   */
  private static boolean filterContent(final byte[] path, final int[] search,
      final TokenSet results) {

    // accept file; check file contents
    if(filterContent(path, search) && !results.contains(path)) {
      results.add(path);
      if(results.size() >= MAXHITS) return true;
    }
    return false;
  }

  /**
   * Searches a string in a file.
   * @param path file path
   * @param search search string
   * @return success flag
   */
  private static boolean filterContent(final byte[] path, final int[] search) {
    final int cl = search.length;
    if(cl == 0) return true;

    try(final TextInput ti = new TextInput(new IOFile(Token.string(path)))) {
      final IntList il = new IntList(cl - 1);
      int c = 0;
      while(true) {
        if(!il.isEmpty()) {
          if(il.deleteAt(0) == search[c++]) continue;
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
  private static int offset(final byte[] input, final boolean path) {
    if(path) return 0;
    final int a = Token.lastIndexOf(input, '\\');
    final int b = Token.lastIndexOf(input, '/');
    return (a > b ? a : b) + 1;
  }

  /**
   * Checks if the specified string matches a pattern.
   * @param input input string
   * @param pattern pattern
   * @param off offset
   * @return result of check
   */
  private static boolean matches(final byte[] input, final byte[] pattern, final int off) {
    final int il = input.length, pl = pattern.length;
    int p = 0;
    for(int i = off; i < il && p < pl; i++) {
      if(pattern[p] == input[i]) p++;
    }
    return p == pl;
  }
}
