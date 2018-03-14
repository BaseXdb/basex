package org.basex.gui.text;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.listener.*;
import org.basex.util.*;

/**
 * This panel provides search and replace facilities.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class SearchBar extends BaseXBack {
  /** Search direction. */
  public enum SearchDir {
    /** Current hit. */
    CURRENT,
    /** Next hit. */
    FORWARD,
    /** Previous hit. */
    BACKWARD,
  }

  /** Escape key listener. */
  private final KeyListener escape = (KeyPressedListener) e -> {
    if(ESCAPE.is(e)) deactivate(true);
  };
  /** Action listener for button clicks. */
  private final ActionListener action = e -> {
    store();
    refreshButtons();
    search();
  };

  /** Mode: regular expression. */
  final AbstractButton regex;
  /** Mode: match case. */
  final AbstractButton mcase;
  /** Mode: whole word. */
  final AbstractButton word;
  /** Mode: multi-line. */
  final AbstractButton multi;
  /** Action: replace text. */
  private final AbstractButton rplc;
  /** Action: close panel. */
  private final AbstractButton cls;

  /** GUI reference. */
  private final GUI gui;
  /** Search text. */
  private final BaseXCombo search;
  /** Replace text. */
  private final BaseXCombo replace;

  /** Search button. */
  private AbstractButton button;
  /** Current editor reference. */
  private TextPanel editor;
  /** Old search text. */
  private String oldSearch = "";

  /**
   * Constructor.
   * @param gui gui reference
   */
  SearchBar(final GUI gui) {
    this.gui = gui;

    layout(new BorderLayout(2, 0));
    setOpaque(false);
    setVisible(false);

    search = new BaseXCombo(gui, true).history(GUIOptions.SEARCHED, gui.gopts);
    search.hint(Text.FIND + Text.DOTS);
    replace = new BaseXCombo(gui, true).history(GUIOptions.REPLACED, gui.gopts);
    replace.hint(Text.REPLACE_WITH + Text.DOTS);

    mcase = button("f_case", Text.MATCH_CASE);
    word = button("f_word", Text.WHOLE_WORD);
    regex = button("f_regex", Text.REGULAR_EXPR);
    multi = button("f_multi", Text.MULTI_LINE);

    rplc  = BaseXButton.get("f_replace", Text.REPLACE_ALL, false, gui);
    cls = BaseXButton.get("f_close", BaseXLayout.addShortcut(Text.CLOSE, ESCAPE.toString()),
        false, gui);

    // add interaction to search field
    search.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(FINDPREV1.is(e) || FINDPREV2.is(e) || FINDNEXT1.is(e) || FINDNEXT2.is(e)) {
          editor.editor.noSelect();
          deactivate(false);
        } else if(ESCAPE.is(e)) {
          deactivate(search.getText().isEmpty());
        } else if(ENTER.is(e)) {
          store();
          editor.jump(SearchDir.FORWARD, true);
        } else if(SHIFT_ENTER.is(e)) {
          editor.jump(SearchDir.BACKWARD, true);
        }
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        final String srch = search.getText();
        if(!oldSearch.equals(srch)) {
          if(regex.isEnabled() && search.getText().matches("^.*(?<!\\\\)\\\\n.*")) {
            multi.setSelected(true);
          }
          oldSearch = srch;
          search();
        }
      }
    });

    BaseXLayout.addDrop(search, object -> {
      setSearch(object.toString());
      store();
      search();
    });

    replace.addKeyListener(escape);

    cls.addKeyListener(escape);
    cls.addActionListener(e -> deactivate(true));

    rplc.addKeyListener(escape);
    rplc.addActionListener(e -> {
      store();
      replace.store();
      final String in = replace.getText();
      editor.replace(new ReplaceContext(regex.isSelected() ? decode(in) : in));
      deactivate(true);
    });

    // set initial values
    final String[] searched = gui.gopts.get(GUIOptions.SEARCHED);
    if(searched.length > 0) setSearch(searched[0]);
    final String[] replaced = gui.gopts.get(GUIOptions.REPLACED);
    if(replaced.length > 0) replace.setText(replaced[0]);
  }

  /**
   * Sets the specified editor and updates the component layout.
   * @param text editor
   * @param srch triggers a search in the specified editor
   */
  public void editor(final TextPanel text, final boolean srch) {
    final boolean ed = text.isEditable();
    if(editor == null || ed != editor.isEditable()) {
      removeAll();
      final BaseXBack west = new BaseXBack(false).layout(new TableLayout(1, 4, 1, 0));
      west.add(mcase);
      west.add(word);
      west.add(regex);
      west.add(multi);

      final BaseXBack center = new BaseXBack(false).layout(new GridLayout(1, 2, 2, 0));
      center.add(search);
      if(ed) center.add(replace);

      final BaseXBack east = new BaseXBack(false).layout(new TableLayout(1, 3, 1, 0));
      if(ed) east.add(rplc);
      east.add(cls);

      add(west, BorderLayout.WEST);
      add(center, BorderLayout.CENTER);
      add(east, BorderLayout.EAST);
    }

    editor = text;
    refreshLayout();
    text.setSearch(this);

    if(srch) search(false);
  }

  /**
   * Returns a search button.
   * @param help help text
   * @return button
   */
  public AbstractButton button(final String help) {
    button = BaseXButton.get("c_find", BaseXLayout.addShortcut(help, FIND.toString()), true, gui);
    button.addActionListener(e -> {
      if(isVisible()) deactivate(true);
      else activate("", true);
    });
    return button;
  }

  /**
   * Refreshes the layout.
   */
  public void refreshLayout() {
    if(editor == null) return;
    final Font ef = editor.getFont().deriveFont((float) (7 + (GUIConstants.fontSize >> 1)));
    search.setFont(ef);
    replace.setFont(ef);
  }

  /**
   * Activates the search bar. A new search is triggered if the new search term differs from
   * the last one.
   * @param string search string (ignored if empty)
   * @param focus indicates if the search field should be focused
   */
  public void activate(final String string, final boolean focus) {
    boolean invisible = !isVisible();
    if(invisible) {
      setVisible(true);
      if(button != null) button.setSelected(true);
    }
    if(focus) search.requestFocusInWindow();

    // set new, different search string
    if(!string.isEmpty() && !new SearchContext(this, search.getText()).matches(string)) {
      regex.setSelected(false);
      setSearch(string);
      store();
      invisible = true;
    }
    // search if string has changed, or if panel was hidden
    if(invisible) search();
  }

  /**
   * Deactivates the search bar.
   * @param close close bar
   * @return {@code true} if panel was closed
   */
  public boolean deactivate(final boolean close) {
    store();
    editor.requestFocusInWindow();
    if(!close || !isVisible()) return false;
    setVisible(false);
    if(button != null) button.setSelected(false);
    search();
    return true;
  }

  /**
   * Refreshes the panel after a successful search operation.
   * @param sc search context
   * @param jump jump to next search result
   */
  void refresh(final SearchContext sc, final boolean jump) {
    final boolean hits = sc.nr != 0, empty = sc.string.isEmpty();
    rplc.setEnabled(hits && !empty);
    search.highlight(hits || empty);
    if(isVisible()) gui.status.setText(Util.info(Text.STRINGS_FOUND_X, sc.nr()));
    if(jump) editor.jump(SearchDir.CURRENT, false);
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Stores the search text.
   */
  private void store() {
    search.store();
  }

  /**
   * Sets a new search text.
   * @param text text
   */
  private void setSearch(final String text) {
    oldSearch = search.getText();
    search.setText(text);
  }

  /**
   * Refreshes the button states.
   */
  private void refreshButtons() {
    final boolean sel = regex.isSelected();
    multi.setEnabled(sel);
    word.setEnabled(!sel);
  }

  /**
   * Searches text in the current editor.
   */
  private void search() {
    search(true);
  }

  /**
   * Searches text in the current editor.
   * @param jump jump to next search result
   */
  private void search(final boolean jump) {
    final String text = isVisible() ? search.getText() : "";
    if(!text.isEmpty()) gui.status.setText(Text.SEARCHING + Text.DOTS);
    editor.search(new SearchContext(this, text), jump);
  }

  /**
   * Returns a button that can be switched on and off.
   * @param icon name of icon
   * @param tooltip tooltip text
   * @return button
   */
  private AbstractButton button(final String icon, final String tooltip) {
    final AbstractButton b = BaseXButton.get(icon, tooltip, true, gui);
    b.addKeyListener(escape);
    b.addActionListener(action);
    return b;
  }

  /**
   * Decodes the specified string and replaces backslashed n's and t's with
   * newlines and tab characters.
   * @param in input
   * @return decoded string
   */
  private static String decode(final String in) {
    final StringBuilder sb = new StringBuilder();
    boolean bs = false;
    final int is = in.length();
    for(int i = 0; i < is; i++) {
      final char ch = in.charAt(i);
      if(bs) {
        if(ch == 'n') {
          sb.append('\n');
        } else if(ch == 't') {
          sb.append('\t');
        } else {
          sb.append('\\');
          if(ch != '\\') sb.append(ch);
        }
        bs = false;
      } else {
        if(ch == '\\') bs = true;
        else sb.append(ch);
      }
    }
    if(bs) sb.append('\\');
    return sb.toString();
  }
}
