package org.basex.gui.view.project;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import org.basex.core.*;
import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Project filter.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class ProjectFilter extends BaseXBack implements KeyListener {
  /** Files. */
  private final BaseXTextField files;
  /** Contents. */
  private final BaseXTextField contents;

  /** Project view. */
  private ProjectView project;
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
  }

  /**
   * Initializes the file cache.
   * @param thread current thread id
   */
  void init(final int thread) {
    if(cache.isEmpty()) {
      project.list.setElements(new StringList(Text.PLEASE_WAIT_D), null);
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
   * @param file files pattern
   * @param cont contents pattern
   */
  void filter(final byte[] file, final byte[] cont) {
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

    final StringList list = filter(file, cont, thread);
    if(list != null) {
      project.list.setElements(list, cont.length == 0 ? null : Token.string(cont));
    }
    setCursor(CURSORARROW);
    running = false;
  }

  /**
   * Filters the file search field.
   */
  void focus() {
    files.requestFocusInWindow();
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    final String file = files.getText().trim();
    final String cont = contents.getText().trim();
    if(lastFiles.equals(file) && lastContents.equals(cont)) return;
    lastFiles = file;
    lastContents = cont;

    final Component oldView = project.scroll.getViewport().getView(), newView;
    if(file.isEmpty() && cont.isEmpty()) {
      newView = project.tree;
      ++threadID;
    } else {
      newView = project.list;
      final Thread t = new Thread() {
        @Override
        public void run() {
          filter(Token.token(file), Token.token(cont));
        }
      };
      t.setDaemon(true);
      t.start();
    }
    if(oldView != newView) project.scroll.setViewportView(newView);
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
    if(BaseXKeys.ENTER.is(e)) {
      project.list.open();
    } else if(BaseXKeys.ESCAPE.is(e)) {
      files.setText("");
      contents.setText("");
    }
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Creates a list with all filtered paths.
   * @param file file pattern
   * @param cont content pattern
   * @param thread current thread id
   * @return result of check
   */
  private StringList filter(final byte[] file, final byte[] cont, final int thread) {
    final StringList match = new StringList();
    for(int i = 0; i < 3; i++) if(!filter(file, cont, thread, i, match)) return null;
    return match;
  }

  /**
   * Chooses tokens from the file cache that match the specified pattern.
   * @param file file pattern
   * @param cont content pattern
   * @param thread current thread id
   * @param mode search mode (0-2)
   * @param match set with matches
   * @return success flag
   */
  private boolean filter(final byte[] file, final byte[] cont, final int thread, final int mode,
      final StringList match) {

    final boolean path = Token.indexOf(file, '\\') != -1 || Token.indexOf(file, '/') != -1;
    for(final byte[] input : cache) {
      // check if current file matches the pattern
      final int offset = offset(input, path);
      if(mode == 0 && Token.startsWith(input, file, offset) ||
         mode == 1 && Token.contains(input, file, offset) ||
         matches(input, file, offset)) {

        // accept file; check file contents
        final String in = Token.string(input);
        if(search(in, cont) && !match.contains(in)) {
          match.add(in);
          if(match.size() >= 100) break;
        }
      }
      if(thread != threadID) return false;
    }
    return true;
  }

  /**
   * Searches a string in a file.
   * @param path file path
   * @param cont file contents
   * @return result of check
   */
  private boolean search(final String path, final byte[] cont) {
    final int cl = cont.length;
    if(cl == 0) return true;

    BufferInput bi = null;
    try {
      bi = new BufferInput(new IOFile(path));
      for(int c = 0, b = 0; (b = bi.read()) != -1;) {
        if(b == cont[c]) {
          c++;
          if(c == cl) return true;
        } else {
          c = 0;
        }
      }
    } catch(final IOException ex) {
      Util.errln(ex);
    } finally {
      if(bi != null) try { bi.close(); } catch(final IOException ignored) { }
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
