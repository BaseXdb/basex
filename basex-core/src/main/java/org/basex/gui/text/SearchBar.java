package org.basex.gui.text;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.listener.*;

/**
 * This panel provides search and replace facilities.
 *
 * @author BaseX Team, BSD License
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
  /** Mode: dot matches newline. */
  final AbstractButton dotall;
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
  /** Hit count label. */
  private final BaseXLabel count;
  /** Total number of hits ({@code -1} if no search is active). */
  private int total = -1;
  /** Whether the next refresh-driven jump should select its hit (set by {@link #replaceNext}). */
  private boolean selectNext;

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
    find.hint(Text.FIND + "\u2026");
    replace = new BaseXCombo(gui, true).history(GUIOptions.REPLACED, gui.gopts);
    replace.hint(Text.REPLACE_WITH + "\u2026");
    count = new BaseXLabel(" ");

    final ActionListener al = e -> search();
    mcase = button("f_case", BaseXLayout.addShortcut(Text.MATCH_CASE, MATCHCASE.toString()), al);
    word = button("f_word", BaseXLayout.addShortcut(Text.WHOLE_WORD, WHOLEWORD.toString()), al);
    regex = button("f_regex", BaseXLayout.addShortcut(Text.REGULAR_EXPR, REGEX.toString()), al);
    dotall = button("f_dotall", BaseXLayout.addShortcut(Text.DOT_ALL, DOTALL.toString()), al);

    rplc  = BaseXButton.get("f_replace", BaseXLayout.addShortcut(Text.REPLACE_ALL,
        REPLACEALL.toString()), false, gui);
    rplc.setFocusable(true);
    cls = BaseXButton.get("f_close", BaseXLayout.addShortcut(Text.CLOSE, ESCAPE.toString()),
        false, gui);

    // add interaction to search field
    find.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(FINDPREV.is(e) || FINDNEXT.is(e)) {
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

    // replace the current hit when Enter is pressed in the replace field
    replace.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(ENTER.is(e)) {
          replace.updateHistory();
          replaceNext();
        }
      }
    });

    // add default shortcuts to all interaction components
    keys = (KeyPressedListener) e -> {
      if(ESCAPE.is(e)) deactivate(false);
      else if(MATCHCASE.is(e)) toggle(mcase);
      else if(WHOLEWORD.is(e)) toggle(word);
      else if(REGEX.is(e)) toggle(regex);
      else if(DOTALL.is(e)) toggle(dotall);
      else if(REPLACEALL.is(e)) replaceAll();
    };
    mcase.addKeyListener(keys);
    word.addKeyListener(keys);
    regex.addKeyListener(keys);
    dotall.addKeyListener(keys);
    find.addKeyListener(keys);
    replace.addKeyListener(keys);
    rplc.addKeyListener(keys);
    cls.addKeyListener(keys);

    rplc.addActionListener(e -> replaceAll());
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
      final BaseXToolBar west = new BaseXToolBar();
      west.add(mcase);
      west.add(word);
      west.add(regex);
      west.add(dotall);

      final BaseXBack found = new BaseXBack(false).layout(new BorderLayout(8, 0));
      found.add(find, BorderLayout.CENTER);
      found.add(count, BorderLayout.EAST);

      final BaseXBack center = new BaseXBack(false).layout(new GridLayout(1, 2, 2, 0));
      center.add(found);
      if(editable) center.add(replace);

      final BaseXToolBar east = new BaseXToolBar();
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
   * Indicates whether the current hits can be replaced.
   * @return result of check
   */
  public boolean replaceEnabled() {
    return editor != null && editor.isEditable() && isVisible() && rplc.isEnabled();
  }

  /**
   * Replaces all hits of the current search.
   */
  public void replaceAll() {
    if(!replaceEnabled()) return;
    deactivate(true);
    final String in = replace.getText();
    editor.replace(new ReplaceContext(regex.isSelected() ? normalize(in) : in));
  }

  /**
   * Replaces the current hit and advances to the next one.
   */
  public void replaceNext() {
    if(!replaceEnabled() || editor.editor.searchIndex() < 0) return;
    final String in = replace.getText();
    final boolean rgx = regex.isSelected();
    if(editor.replaceNext(new ReplaceContext(rgx ? normalize(in) : in, true))) {
      selectNext = true;
      search();
    }
  }

  /**
   * Searches text in the current editor.
   */
  private void search() {
    final boolean sel = regex.isSelected();
    dotall.setEnabled(sel);
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
    total = empty ? -1 : sc.nr;
    count.setText(countText());
    // a Replace Next selects the following hit; a plain search only repositions the view
    final boolean select = selectNext;
    selectNext = false;
    if(jump) editor.jump(SearchDir.CURRENT, select);
  }

  /**
   * Updates the hit count to the current navigation position.
   */
  void refreshCount() {
    count.setText(countText());
  }

  /**
   * Returns the hit count text ("N/M" with a trailing space, empty if no search is active).
   * @return count text
   */
  private String countText() {
    if(total < 0) return "";
    int n = editor == null ? -1 : editor.editor.searchIndex();
    if(n < 0 || n >= total) n = 0;
    // count string: thin spaces (U+2009) around the slash, trailing space for padding
    return (total == 0 ? 0 : n + 1) + "\u2009/\u2009" + total + " ";
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
    final SearchContext sc = new SearchContext(this, text);
    if(sc.error != null) {
      // invalid regular expression: highlight the field red, show the cause as its tooltip
      find.highlight(false);
      find.setToolTipText(sc.error);
      total = -1;
      count.setText("");
    } else {
      find.setToolTipText(Text.FIND + "\u2026");
      editor.search(sc, jump);
    }
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
   * Normalizes a regular-expression replacement string to a valid Java replacement.
   * Supported: {@code \n}, {@code \t}, {@code \\}, {@code \$}, group references
   * {@code $1}/{@code \1}/{@code ${name}}; a bare {@code $} is treated as literal.
   * @param in input
   * @return normalized string
   */
  static String normalize(final String in) {
    final StringBuilder sb = new StringBuilder();
    final int is = in.length();
    for(int i = 0; i < is; i++) {
      final char ch = in.charAt(i);
      if(ch == '\\') {
        final char n = i + 1 < is ? in.charAt(++i) : 0;
        if(n == 'n') sb.append('\n');
        else if(n == 't') sb.append('\t');
        else if(n == '\\') sb.append("\\\\");
        else if(n == '$') sb.append("\\$");
        else if(n >= '0' && n <= '9') sb.append('$').append(n);
        else {
          sb.append("\\\\");
          if(n != 0) sb.append(n);
        }
      } else if(ch == '$') {
        final char n = i + 1 < is ? in.charAt(i + 1) : 0;
        if(n >= '0' && n <= '9') {
          sb.append('$');
        } else if(n == '{') {
          // ${digits} -> $digits (numbered); ${name} passes through as a named reference
          int k = i + 2;
          while(k < is && in.charAt(k) != '}') k++;
          final String name = in.substring(i + 2, Math.min(k, is));
          if(k < is && !name.isEmpty() && name.chars().allMatch(Character::isDigit)) {
            sb.append('$').append(name);
            i = k;
          } else {
            sb.append('$');
          }
        } else {
          sb.append("\\$");
        }
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }
}
