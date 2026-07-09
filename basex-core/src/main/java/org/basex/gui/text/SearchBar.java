package org.basex.gui.text;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.listener.*;
import org.basex.util.*;

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
  /** Mode: dot matches all. */
  final AbstractButton dotall;
  /** Action: find previous hit. */
  private final AbstractButton prev;
  /** Action: find next hit. */
  private final AbstractButton next;
  /** Action: replace all hits. */
  private final AbstractButton rplc;
  /** Action: replace next hit. */
  private final AbstractButton rplcNext;
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
  /** Direction of the jump that follows the next search. */
  private SearchDir jumpDir = SearchDir.CURRENT;
  /** Whether that jump selects its hit. */
  private boolean jumpSelect;

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

    final ActionListener al = e -> {
      modes();
      search();
    };
    mcase = button("f_case", BaseXLayout.addShortcut(Text.MATCH_CASE, MATCHCASE.toString()), al);
    word = button("f_word", BaseXLayout.addShortcut(Text.WHOLE_WORD, WHOLEWORD.toString()), al);
    regex = button("f_regex", BaseXLayout.addShortcut(Text.REGULAR_EXPR, REGEX.toString()), al);
    dotall = button("f_dotall", BaseXLayout.addShortcut(Text.DOT_ALL, DOTALL.toString()), al);

    // restore the search modes of the last session
    mcase.setSelected(gui.gopts.get(GUIOptions.MATCHCASE));
    word.setSelected(gui.gopts.get(GUIOptions.WHOLEWORD));
    regex.setSelected(gui.gopts.get(GUIOptions.REGEX));
    dotall.setSelected(gui.gopts.get(GUIOptions.DOTALL));
    modes();

    prev = BaseXButton.get("f_prev",
        BaseXLayout.addShortcut(Text.FIND_PREVIOUS, FINDPREV.toString()), false, gui);
    next = BaseXButton.get("f_next",
        BaseXLayout.addShortcut(Text.FIND_NEXT, FINDNEXT.toString()), false, gui);
    rplcNext = BaseXButton.get("f_replace",
        BaseXLayout.addShortcut(Text.REPLACE_NEXT, ENTER.toString()), false, gui);
    rplc = BaseXButton.get("f_replaceall",
        BaseXLayout.addShortcut(Text.REPLACE_ALL, META_ENTER.toString()), false, gui);
    cls = BaseXButton.get("f_close", BaseXLayout.addShortcut(Text.CLOSE, ESCAPE.toString()),
        false, gui);

    // add interaction to search field
    find.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(ENTER.is(e) || FINDNEXT.is(e)) {
          editor.jump(SearchDir.FORWARD, true);
        } else if(SHIFT_ENTER.is(e) || FINDPREV.is(e)) {
          editor.jump(SearchDir.BACKWARD, true);
        } else if(META_ENTER.is(e)) {
          replaceAll();
        } else {
          return;
        }
        e.consume();
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

    replace.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(ENTER.is(e)) {
          replaceNext();
        } else if(META_ENTER.is(e)) {
          replaceAll();
        } else {
          return;
        }
        e.consume();
      }
    });

    // add default shortcuts to all interaction components
    keys = (KeyPressedListener) e -> {
      if(ESCAPE.is(e)) deactivate(false);
      else if(MATCHCASE.is(e)) toggle(mcase);
      else if(WHOLEWORD.is(e)) toggle(word);
      else if(REGEX.is(e)) toggle(regex);
      else if(DOTALL.is(e)) toggle(dotall);
    };
    mcase.addKeyListener(keys);
    word.addKeyListener(keys);
    regex.addKeyListener(keys);
    dotall.addKeyListener(keys);
    find.addKeyListener(keys);
    replace.addKeyListener(keys);
    prev.addKeyListener(keys);
    next.addKeyListener(keys);
    rplc.addKeyListener(keys);
    rplcNext.addKeyListener(keys);
    cls.addKeyListener(keys);

    prev.addActionListener(e -> editor.jump(SearchDir.BACKWARD, true));
    next.addActionListener(e -> editor.jump(SearchDir.FORWARD, true));
    rplc.addActionListener(e -> replaceAll());
    rplcNext.addActionListener(e -> replaceNext());
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

      final BaseXToolBar meta = new BaseXToolBar();
      meta.add(count);
      meta.add(Box.createHorizontalStrut(6));
      meta.add(prev);
      meta.add(next);
      meta.add(Box.createHorizontalStrut(6));

      final BaseXBack found = new BaseXBack(false).layout(new BorderLayout(8, 0));
      found.add(find, BorderLayout.CENTER);
      found.add(meta, BorderLayout.EAST);

      final BaseXBack center = new BaseXBack(false).layout(new GridLayout(1, 2, 2, 0));
      center.add(found);
      if(editable) center.add(replace);

      final BaseXToolBar east = new BaseXToolBar();
      if(editable) {
        east.add(rplcNext);
        east.add(rplc);
      }
      east.add(cls);

      add(west, BorderLayout.WEST);
      add(center, BorderLayout.CENTER);
      add(east, BorderLayout.EAST);
    }

    // a requested jump belongs to the editor it was requested for
    if(editor != text) resetJump();
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
   * @param focus indicates if the search field should be focused
   * @param enforce enforce search
   * @return {@code true} if a search was triggered
   */
  public boolean activate(final String string, final boolean focus, final boolean enforce) {
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
    final boolean srch = invisible || enforce;
    if(srch) search();
    return srch;
  }

  /**
   * Activates the search bar and jumps to a hit.
   * @param string search string (ignored if empty)
   * @param dir search direction
   */
  public void find(final String string, final SearchDir dir) {
    jumpDir = dir;
    jumpSelect = true;
    if(!activate(string, false, false)) {
      // no new search: the current results are up to date
      resetJump();
      editor.jump(dir, true);
    }
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
    modes();
    activate(find.getText(), false, true);
  }

  /**
   * Indicates whether the current hits can be replaced.
   * @return result of check
   */
  private boolean replaceEnabled() {
    return editor != null && editor.isEditable() && isVisible() && rplc.isEnabled();
  }

  /**
   * Replaces all hits of the current search.
   */
  private void replaceAll() {
    if(!replaceEnabled()) return;
    try {
      // a selection restricts the replacement, unless it is one of the search hits
      final ReplaceContext rc = new ReplaceContext(replacement());
      editor.replace(rc);
      gui.status.setText(Util.info(Text.STRINGS_REPLACED_X, BaseXLayout.format(rc.count)), true);
      deactivate(true);
    } catch(final Exception ex) {
      replaceFailed(ex);
    }
  }

  /**
   * Replaces the current hit and advances to the next one.
   */
  private void replaceNext() {
    if(!replaceEnabled()) return;
    try {
      if(editor.replaceNext(new ReplaceContext(replacement(), true))) {
        // select the hit that follows the replacement
        jumpSelect = true;
        search();
      }
    } catch(final Exception ex) {
      replaceFailed(ex);
    }
  }

  /**
   * Returns the contents of the replace field as a Java replacement string.
   * @return replacement
   */
  private String replacement() {
    final String in = replace.getText();
    return regex.isSelected() ? normalize(in) : Matcher.quoteReplacement(in);
  }

  /**
   * Flags a failed replacement by reddening the replace field.
   * @param ex exception
   */
  private void replaceFailed(final Exception ex) {
    Util.debug(ex);
    replace.highlight(GUIConstants.lightRed);
    replace.setToolTipText(ex.getLocalizedMessage());
  }

  /**
   * Adopts the current search modes and remembers them for the next session.
   */
  private void modes() {
    final boolean rgx = regex.isSelected();
    dotall.setEnabled(rgx);
    gui.gopts.set(GUIOptions.MATCHCASE, mcase.isSelected());
    gui.gopts.set(GUIOptions.WHOLEWORD, word.isSelected());
    gui.gopts.set(GUIOptions.REGEX, rgx);
    gui.gopts.set(GUIOptions.DOTALL, dotall.isSelected());
  }

  /**
   * Searches text in the current editor.
   */
  private void search() {
    search(true);
  }

  /**
   * Refreshes the panel after a search operation.
   * @param te editor the search was started for
   * @param sc search context
   * @param jump jump to next search result
   */
  void refresh(final TextEditor te, final SearchContext sc, final boolean jump) {
    // discard the results of a search for an editor that was replaced by a tab switch
    if(editor.editor != te) return;

    // an empty search string yields no hits, but is no failed search either
    final boolean hits = te.searchSize() != 0, error = sc.error != null;
    prev.setEnabled(hits);
    next.setEnabled(hits);
    rplc.setEnabled(hits);
    rplcNext.setEnabled(hits);
    find.highlight(error ? GUIConstants.lightRed :
      hits || sc.string.isEmpty() ? GUIConstants.backColor : GUIConstants.paleGray);
    find.setToolTipText(error ? sc.error : Text.FIND + "\u2026");
    replace.highlight(GUIConstants.backColor);
    replace.setToolTipText(Text.REPLACE_WITH + "\u2026");
    // a requested jump is deferred until its results arrive; it refreshes the count itself
    final SearchDir dir = jumpDir;
    final boolean select = jumpSelect;
    resetJump();
    if(jump) editor.jump(dir, select);
    else refreshCount();
  }

  /**
   * Discards a requested jump.
   */
  private void resetJump() {
    jumpDir = SearchDir.CURRENT;
    jumpSelect = false;
  }

  /**
   * Updates the hit count to the current navigation position.
   */
  void refreshCount() {
    count.setText(countText());
  }

  /**
   * Returns the hit count text ("N/M"). Before a hit is navigated to, N is 0.
   * @return count text
   */
  private String countText() {
    final int n = editor.editor.searchIndex(), total = editor.editor.searchSize();
    return BaseXLayout.format(n < 0 ? 0 : n + 1) + "\u2009/\u2009" + BaseXLayout.format(total);
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Sets a new search text, and remembers it as the one that is searched for next.
   * @param text text
   */
  private void setSearch(final String text) {
    oldSearch = text;
    find.setText(text);
  }

  /**
   * Searches text in the current editor.
   * @param jump jump to next search result
   */
  private void search(final boolean jump) {
    // an invalid regular expression yields no hits; the panel is refreshed as after any search
    editor.search(new SearchContext(this, isVisible() ? find.getText() : ""), jump);
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
