package org.basex.gui.view.project;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Project filter.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class ProjectFilter extends BaseXBack implements KeyListener {
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
  String lastFiles = "";
  /** Last content search. */
  String lastContents = "";
  /** Running flag. */
  boolean running;
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
    files.hint(Text.FIND_FILES);

    contents = new BaseXTextField(view.gui);
    contents.hint(Text.FIND_CONTENTS);

    add(files, BorderLayout.NORTH);
    add(contents, BorderLayout.CENTER);

    files.addKeyListener(this);
    contents.addKeyListener(this);
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
  void init(final int thread) {
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
  void add(final IOFile root, final int thread) {
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
   * @param pattern files pattern
   * @param content contents pattern
   * @param thread thread id
   */
  void filter(final String pattern, final String content, final int thread) {
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
    final TokenSet matches = new TokenSet();
    final IntList il = new IntList();
    final TokenParser tp = new TokenParser(Token.token(content));
    while(tp.more()) il.add(Token.lc(tp.next()));
    if(filter(pattern, il.toArray(), thread, matches)) {
      project.list.setElements(matches, content.isEmpty() ? null : content);
    } else {
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
    final String file = files.getText().trim();
    final String content = contents.getText().trim();
    if(!force && lastFiles.equals(file) && lastContents.equals(content)) return;
    lastFiles = file;
    lastContents = content;
    ++threadID;

    final Component oldView = project.scroll.getViewport().getView(), newView;
    if(file.isEmpty() && content.isEmpty()) {
      newView = project.tree;
    } else {
      newView = project.list;
      final Thread t = new Thread() {
        @Override
        public void run() {
          filter(file, content, threadID);
        }
      };
      t.setDaemon(true);
      t.start();
    }
    if(oldView != newView) project.scroll.setViewportView(newView);
  }

  /**
   * Filters the file search field.
   */
  void focus() {
    files.requestFocusInWindow();
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    refresh(false);
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    if(BaseXKeys.NEXTLINE.is(e) || BaseXKeys.PREVLINE.is(e) ||
       BaseXKeys.NEXTPAGE.is(e) || BaseXKeys.PREVPAGE.is(e)) {
      project.list.dispatchEvent(e);
    } else if(BaseXKeys.REFRESH.is(e)) {
      reset();
    }
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    if(BaseXKeys.ENTER.is(e)) {
      project.list.open();
    } else if(BaseXKeys.ESCAPE.is(e)) {
      files.setText("");
      contents.setText("");
    }
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Chooses tokens from the file cache that match the specified pattern.
   * @param pattern file pattern
   * @param content content pattern
   * @param thread current thread id
   * @param matches set with matches
   * @return success flag
   */
  private boolean filter(final String pattern, final int[] content, final int thread,
      final TokenSet matches) {

    // glob pattern
    if(pattern.contains("*") || pattern.contains("?")) {
      final Pattern pt = Pattern.compile(IOFile.regex(pattern));
      for(final byte[] input : cache) {
        final int offset = offset(input, true);
        if(pt.matcher(Token.string(Token.substring(input, offset))).matches() &&
            filterContent(input, content, matches)) return true;
        if(thread != threadID) return false;
      }
    }
    // starts-with, contains, camel case
    final byte[] patt = Token.token(pattern);
    for(int i = 0; i < 3; i++) {
      if(!filter(patt, content, thread, i, matches)) return false;
    }
    return true;
  }

  /**
   * Chooses tokens from the file cache that match the specified pattern.
   * @param pattern file pattern
   * @param cont content pattern
   * @param thread current thread id
   * @param mode search mode (0-2)
   * @param matches set with matches
   * @return success flag
   */
  private boolean filter(final byte[] pattern, final int[] cont, final int thread, final int mode,
      final TokenSet matches) {

    if(matches.size() < MAXHITS) {
      final boolean path = Token.indexOf(pattern, '\\') != -1 || Token.indexOf(pattern, '/') != -1;
      for(final byte[] input : cache) {
        // check if current file matches the pattern
        final int offset = offset(input, path);
        if(mode == 0 && Token.startsWith(input, pattern, offset) ||
           mode == 1 && Token.contains(input, pattern, offset) ||
           matches(input, pattern, offset)) {
          if(filterContent(input, cont, matches)) return true;
        }
        if(thread != threadID) return false;
      }
    }
    return true;
  }

  /**
   * Checks the file contents.
   * @param input input path
   * @param content content pattern
   * @param matches set with matches
   * @return maximum number of results reached
   */
  private boolean filterContent(final byte[] input, final int[] content, final TokenSet matches) {
    // accept file; check file contents
    if(filterContent(input, content) && !matches.contains(input)) {
      matches.add(input);
      if(matches.size() >= MAXHITS) return true;
    }
    return false;
  }

  /**
   * Searches a string in a file.
   * @param path file path
   * @param cont file contents
   * @return result of check
   */
  private boolean filterContent(final byte[] path, final int[] cont) {
    final int cl = cont.length;
    if(cl == 0) return true;

    TextInput ti = null;
    try {
      ti = new TextInput(new IOFile(Token.string(path)));
      for(int c = 0, cp = 0; (cp = ti.read()) != -1;) {
        if(!XMLToken.valid(cp)) break;
        if(Token.lc(cp) == cont[c]) {
          if(++c == cl) return true;
        } else {
          c = 0;
        }
      }
    } catch(final IOException ex) {
      // file may not be accessible
      Util.debug(ex);
    } finally {
      if(ti != null) try { ti.close(); } catch(final IOException ignored) { }
    }
    return false;
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
      final byte ic = input[i];
      final byte pc = pattern[p];
      if(pc == ic || pc > 0x61 && pc < 0x7a && pc == (ic | 0x20) ||
        (pc == '/' || pc == '\\') && (ic == '/' || ic == '\\')) p++;
    }
    return p == pl;
  }
}
