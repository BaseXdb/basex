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
 * @author BaseX Team 2005-21, BSD License
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

  /** Mode: regular expression. */
  final AbstractButton regex;
  /** Mode: match case. */
  final AbstractButton mcase;
  /** Mode: whole word. */
  final AbstractButton word;
  /** Mode: multi-line. */
  final AbstractButton multi;
  /** Action: replace. */
  private final AbstractButton rplc;
  /** Action: close. */
  private final AbstractButton cls;

  /** GUI reference. */
  private final GUI gui;
  /** Search text. */
  private final BaseXCombo find;
  /** Replace text. */
  private final BaseXCombo replace;

  /** Escape key listener. */
  private final KeyListener keys;

  /** Search button. */
  private AbstractButton search;
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

    find = new BaseXCombo(gui, true).history(GUIOptions.SEARCHED, gui.gopts);
    find.hint(Text.FIND + Text.DOTS);
    replace = new BaseXCombo(gui, true).history(GUIOptions.REPLACED, gui.gopts);
    replace.hint(Text.REPLACE_WITH + Text.DOTS);

    final ActionListener al = e -> search();
    mcase = button("f_case", BaseXLayout.addShortcut(Text.MATCH_CASE, MATCHCASE.toString()), al);
    word = button("f_word", BaseXLayout.addShortcut(Text.WHOLE_WORD, WHOLEWORD.toString()), al);
    regex = button("f_regex", BaseXLayout.addShortcut(Text.REGULAR_EXPR, REGEX.toString()), al);
    multi = button("f_multi", BaseXLayout.addShortcut(Text.MULTI_LINE, MULTILINE.toString()), al);

    rplc  = BaseXButton.get("f_replace", Text.REPLACE_ALL, false, gui);
    cls = BaseXButton.get("f_close", BaseXLayout.addShortcut(Text.CLOSE, ESCAPE.toString()),
        false, gui);

    // add interaction to search field
    find.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(FINDPREV1.is(e) || FINDPREV2.is(e) || FINDNEXT1.is(e) || FINDNEXT2.is(e)) {
          editor.editor.noSelect();
          deactivate(false);
        } else if(ENTER.is(e)) {
          find.updateHistory();
          editor.jump(SearchDir.FORWARD, true);
        } else if(SHIFT_ENTER.is(e)) {
          editor.jump(SearchDir.BACKWARD, true);
        }
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        final String srch = find.getText();
        if(!oldSearch.equals(srch)) {
          if(regex.isEnabled() && find.getText().matches("^.*(?<!\\\\)\\\\n.*")) {
            multi.setSelected(true);
          }
          oldSearch = srch;
          search();
        }
      }
    });

    BaseXLayout.addDrop(find, object -> {
      setSearch(object.toString());
      find.updateHistory();
      search();
    });

    // add default shortcuts to all interaction components
    keys = (KeyPressedListener) e -> {
      if(ESCAPE.is(e)) deactivate(false);
      else if(MATCHCASE.is(e)) toggle(mcase);
      else if(WHOLEWORD.is(e)) toggle(word);
      else if(REGEX.is(e)) toggle(regex);
      else if(MULTILINE.is(e)) toggle(multi);
    };
    mcase.addKeyListener(keys);
    word.addKeyListener(keys);
    regex.addKeyListener(keys);
    multi.addKeyListener(keys);
    find.addKeyListener(keys);
    replace.addKeyListener(keys);
    rplc.addKeyListener(keys);
    cls.addKeyListener(keys);

    rplc.addActionListener(e -> {
      deactivate(true);
      final String in = replace.getText();
      editor.replace(new ReplaceContext(regex.isSelected() ? decode(in) : in));
    });
    cls.addActionListener(e -> deactivate(true));

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
    final boolean editable = text.isEditable();
    if(editor == null || editable != editor.isEditable()) {
      removeAll();
      final BaseXBack west = new BaseXBack(false).layout(new ColumnLayout(1));
      west.add(mcase);
      west.add(word);
      west.add(regex);
      west.add(multi);

      final BaseXBack center = new BaseXBack(false).layout(new GridLayout(1, 2, 2, 0));
      center.add(find);
      if(editable) center.add(replace);

      final BaseXBack east = new BaseXBack(false).layout(new ColumnLayout(1));
      if(editable) east.add(rplc);
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
    search = BaseXButton.get("c_find", BaseXLayout.addShortcut(help, FIND.toString()), true, gui);
    search.addActionListener(e -> {
      if(isVisible()) deactivate(true);
      else activate("", true, false);
    });
    return search;
  }

  /**
   * Refreshes the layout.
   */
  public void refreshLayout() {
    if(editor == null) return;
    final Font ef = editor.getFont().deriveFont((float) GUIConstants.dmfont.getSize() + 2);
    find.setFont(ef);
    replace.setFont(ef);
  }

  /**
   * Activates the search bar. A new search is triggered if the new search term differs from
   * the last one.
   * @param string search string (ignored if empty)
   * @param enforce enforce search
   * @param focus indicates if the search field should be focused
   */
  public void activate(final String string, final boolean focus, final boolean enforce) {
    boolean invisible = !isVisible();
    if(invisible) {
      setVisible(true);
      if(search != null) search.setSelected(true);
    }
    if(focus) find.requestFocusInWindow();

    // set new, different search string
    if(!string.isEmpty() && !new SearchContext(this, find.getText()).matches(string)) {
      setSearch(string);
      find.updateHistory();
      invisible = true;
    }
    // search if string has changed, or if panel was hidden
    if(invisible || enforce) search();
  }

  /**
   * Deactivates the search bar.
   * @param close close bar
   * @return {@code true} if panel was closed
   */
  public boolean deactivate(final boolean close) {
    final boolean closing = close && isVisible();
    if(closing) {
      setVisible(false);
      if(search != null) search.setSelected(false);
      search();
    }
    editor.requestFocusInWindow();
    return closing;
  }

  /**
   * Toggles a button and starts a new search.
   * @param button button to toggle
   */
  public void toggle(final AbstractButton button) {
    button.setSelected(!button.isSelected());
    activate(find.getText(), false, true);
  }

  /**
   * Searches text in the current editor.
   */
  private void search() {
    final boolean sel = regex.isSelected();
    multi.setEnabled(sel);
    word.setEnabled(!sel);
    search(true);
  }

  /**
   * Refreshes the panel after a successful search operation.
   * @param sc search context
   * @param jump jump to next search result
   */
  void refresh(final SearchContext sc, final boolean jump) {
    final boolean hits = sc.nr != 0, empty = sc.string.isEmpty();
    rplc.setEnabled(hits && !empty);
    find.highlight(hits || empty);
    if(isVisible()) gui.status.setText(Util.info(Text.STRINGS_FOUND_X, sc.nr()));
    if(jump) editor.jump(SearchDir.CURRENT, false);
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Sets a new search text.
   * @param text text
   */
  private void setSearch(final String text) {
    oldSearch = find.getText();
    find.setText(text);
  }

  /**
   * Searches text in the current editor.
   * @param jump jump to next search result
   */
  private void search(final boolean jump) {
    final String text = isVisible() ? find.getText() : "";
    if(!text.isEmpty()) gui.status.setText(Text.SEARCHING + Text.DOTS);
    editor.search(new SearchContext(this, text), jump);
  }

  /**
   * Returns a button that can be switched on and off.
   * @param icon name of icon
   * @param tooltip tooltip text
   * @param action action listener
   * @return button
   */
  private AbstractButton button(final String icon, final String tooltip,
      final ActionListener action) {
    final AbstractButton b = BaseXButton.get(icon, tooltip, true, gui);
    b.addKeyListener(keys);
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
