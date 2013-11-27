package org.basex.gui.view.project;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.io.*;
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
  /** Current filter id. */
  private int threadID;
  /** Cached file paths. */
  final StringList cache = new StringList();
  /** Last entered string. */
  String last = "";
  /** Running flag. */
  boolean running;

  /**
   * Constructor.
   * @param view project view
   */
  public ProjectFilter(final ProjectView view) {
    super(view.gui);
    history(view.gui, GUIOptions.FILTERS);
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
        } else {
          newView = view.list;
          final Thread t = new Thread() {
            @Override
            public void run() {
              filter(pattern);
            }
          };
          t.setDaemon(true);
          t.start();
        }
        if(oldView != newView) view.scroll.setViewportView(newView);
      }
      @Override
      public void keyTyped(final KeyEvent e) {
        if(BaseXKeys.ENTER.is(e)) view.list.open();
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
   * Initializes the filter cache.
   */
  void init() {
    if(cache.isEmpty()) {
      project.list.addElements(new StringList(Text.PLEASE_WAIT_D));

      final Enumeration<?> en = project.root.children();
      while(en.hasMoreElements()) {
        final Object obj = en.nextElement();
        if(obj instanceof ProjectDir) {
          final IOFile root = ((ProjectDir) obj).file;
          for(final String path : root.descendants()) {
            cache.add(root.path() + File.separator + path);
          }
        }
      }
    }
  }

  /**
   * Filters the entries.
   * @param pattern pattern
   */
  void filter(final String pattern) {
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
    init();

    final StringList files = filter(pattern, thread);
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
  private StringList filter(final String pattern, final int thread) {
    final StringList match = new StringList();
    for(final String input : cache) {
      final int a = input.lastIndexOf('\\'), b = input.lastIndexOf('/'), c = a > b ? a : b;
      if(input.startsWith(pattern, c + 1)) {
        match.add(input);
        if(match.size() >= 100) return match;
      }
      if(threadID != thread) return null;
    }
    for(final String input : cache) {
      final int a = input.lastIndexOf('\\'), b = input.lastIndexOf('/'), c = a > b ? a : b;
      if(input.substring(c + 1).contains(pattern) && !match.contains(input)) {
        match.add(input);
        if(match.size() >= 100) return match;
      }
      if(threadID != thread) return null;
    }
    for(final String input : cache) {
      final int a = input.lastIndexOf('\\'), b = input.lastIndexOf('/'), c = a > b ? a : b;
      if(matches(input, pattern, c + 1) && !match.contains(input)) {
        match.add(input);
        if(match.size() >= 100) return match;
      }
      if(threadID != thread) return null;
    }
    return match;
  }

  /**
   * Checks if the specified string matches a pattern.
   * @param input input string
   * @param pattern pattern
   * @param off offset
   * @return result of check
   */
  private static boolean matches(final String input, final String pattern, final int off) {
    final int il = input.length(), pl = pattern.length();
    int p = 0;
    for(int i = off; i < il && p < pl; i++) {
      final char ic = input.charAt(i);
      final char pc = pattern.charAt(p);
      if(Character.isLowerCase(pc) && pc == Character.toLowerCase(ic) || pc == ic) p++;
    }
    return p == pl;
  }
}
