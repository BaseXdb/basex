package org.basex.gui.view.project;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

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
  /** Strings. */
  final StringList cache = new StringList();
  /** Last string. */
  String last = "";

  /**
   * Constructor.
   * @param view project view
   */
  public ProjectFilter(final ProjectView view) {
    super(view.gui);
    history(view.gui, GUIOptions.FILTERS);

    addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        final BaseXTree tree = view.tree;
        final ProjectList list = view.list;
        final String pattern = getText().trim();
        if(last.equals(pattern)) return;

        final Component oldView = view.scroll.getViewport().getView(), newView;
        if(pattern.isEmpty()) {
          newView = tree;
        } else {
          setCursor(CURSORWAIT);
          init(view);
          list.model.removeAllElements();
          for(final String file : filter(pattern)) list.model.addElement(file);
          setCursor(CURSORARROW);
          newView = list;
        }
        if(oldView != newView) view.scroll.setViewportView(newView);
        last = pattern;
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
   * @param view project view
   */
  void init(final ProjectView view) {
    if(cache.isEmpty()) {
      final Enumeration<?> en = view.root.children();
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
   * Creates a list with all filtered paths.
   * @param pattern pattern
   * @return result of check
   */
  private StringList filter(final String pattern) {
    final StringList match = new StringList();
    for(final String input : cache) {
      final int a = input.lastIndexOf('\\'), b = input.lastIndexOf('/'), c = a > b ? a : b;
      final String in = c == -1 ? input : input.substring(c + 1);
      if(in.startsWith(pattern)) {
        match.add(input);
        if(match.size() > 100) break;
      }
    }
    for(final String input : cache) {
      final int a = input.lastIndexOf('\\'), b = input.lastIndexOf('/'), c = a > b ? a : b;
      final String in = c == -1 ? input : input.substring(c + 1);
      if(matches(in, pattern) && !match.contains(input)) {
        match.add(input);
        if(match.size() > 100) break;
      }
    }
    return match;
  }

  /**
   * Checks if the specified string matches a pattern.
   * @param input input string
   * @param pattern pattern
   * @return result of check
   */
  private static boolean matches(final String input, final String pattern) {
    final int il = input.length(), pl = pattern.length();
    int p = 0;
    for(int i = 0; i < il && p < pl; i++) {
      final char ic = input.charAt(i);
      final char pc = pattern.charAt(p);
      if(Character.isLowerCase(pc) && pc == Character.toLowerCase(ic) || pc == ic) p++;
    }
    return p == pl;
  }
}
