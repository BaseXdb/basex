package org.basex.gui.view.project;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import org.basex.core.*;
import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Project filter.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class ProjectFilter extends BaseXTextField {
  /** Project view. */
  private ProjectView project;
  /** Cached file paths. */
  private final TokenList cache = new TokenList();

  /** Last entered string. */
  String last = "";
  /** Running flag. */
  boolean running;
  /** Current filter id. */
  private int threadID;

  /**
   * Constructor.
   * @param view project view
   */
  public ProjectFilter(final ProjectView view) {
    super(view.gui);
    project = view;

    addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        final String pattern = getText().trim();
        if(last.equals(pattern)) return;
        last = pattern;

        final Component oldView = view.scroll.getViewport().getView(), newView;
        if(pattern.isEmpty()) {
          newView = view.tree;
          ++threadID;
        } else {
          newView = view.list;
          final Thread t = new Thread() {
            @Override
            public void run() {
              filter(Token.token(pattern));
            }
          };
          t.setDaemon(true);
          t.start();
        }
        if(oldView != newView) view.scroll.setViewportView(newView);
      }

      @Override
      public void keyPressed(final KeyEvent e) {
        if(BaseXKeys.NEXTLINE.is(e) || BaseXKeys.PREVLINE.is(e) ||
           BaseXKeys.NEXTPAGE.is(e) || BaseXKeys.PREVPAGE.is(e) ||
           BaseXKeys.LINESTART.is(e) || BaseXKeys.LINEEND.is(e)) {
          project.list.dispatchEvent(e);
        }
      }

      @Override
      public void keyTyped(final KeyEvent e) {
        if(BaseXKeys.ENTER.is(e)) view.list.open();
        else if(BaseXKeys.ESCAPE.is(e)) setText("");
      }
    });
  }

  /**
   * Resets the filter cache.
   */
  void reset() {
    cache.reset();
  }

  /**
   * Initializes the file cache.
   * @param thread current thread id
   */
  void init(final int thread) {
    if(cache.isEmpty()) {
      final TreeSet<String> wait = new TreeSet<String>();
      wait.add(Text.PLEASE_WAIT_D);
      project.list.addElements(wait);
      reset();
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
   * @param pattern pattern
   */
  void filter(final byte[] pattern) {
    // wait when command is still running
    final int thread = ++threadID;
    while(running) {
      Thread.yield();
      // newer thread has arrived
      if(threadID != thread) return;
    }

    // thread is accepted; start filtering
    running = true;
    setCursor(CURSORWAIT);
    init(thread);
    final TreeSet<String> files = filter(pattern, thread);
    if(files != null) project.list.addElements(files);
    setCursor(CURSORARROW);
    running = false;
  }

  /**
   * Creates a list with all filtered paths.
   * @param pattern pattern
   * @param thread current thread id
   * @return result of check
   */
  private TreeSet<String> filter(final byte[] pattern, final int thread) {
    final TreeSet<String> match = new TreeSet<String>();
    for(int i = 0; i < 3; i++) if(!filter(pattern, thread, i, match)) return null;
    return match;
  }

  /**
   * Chooses tokens from the file cache that match the specified pattern.
   * @param pattern pattern
   * @param thread current thread id
   * @param mode search mode (0-2)
   * @param match set with matches
   * @return success flag
   */
  private boolean filter(final byte[] pattern, final int thread, final int mode,
      final TreeSet<String> match) {

    final boolean path = Token.indexOf(pattern, '\\') != -1 || Token.indexOf(pattern, '/') != -1;
    for(final byte[] input : cache) {
      final int offset = offset(input, path);
      if(mode == 0 && Token.startsWith(input, pattern, offset) ||
         mode == 1 && Token.startsWith(input, pattern, offset) ||
         mode == 2 && Token.contains(input, pattern, offset) ||
         matches(input, pattern, offset)) {
        match.add(Token.string(input));
        if(match.size() >= 100) break;
      }
      if(thread != threadID) return false;
    }
    return true;
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
