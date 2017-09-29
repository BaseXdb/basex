package org.basex.gui.text;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.listener.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This panel provides search and replace facilities.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class SearchBar extends BaseXBack {
  /** History values for buttons. */
  private final HashMap<String, boolean[]> modeHistory = new HashMap<>();

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

  /** Mode buttons. */
  final AbstractButton[] modeButtons;
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
  private final BaseXTextField search;
  /** Replace text. */
  private final BaseXTextField replace;

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

    search = new BaseXTextField(gui);
    search.history(GUIOptions.SEARCHED).hint(Text.FIND + Text.DOTS);
    search.setPreferredSize(null);

    replace = new BaseXTextField(gui);
    replace.history(GUIOptions.REPLACED).hint(Text.REPLACE_WITH + Text.DOTS);
    replace.setPreferredSize(null);

    mcase = button("f_case", Text.MATCH_CASE);
    word = button("f_word", Text.WHOLE_WORD);
    regex = button("f_regex", Text.REGULAR_EXPR);
    multi = button("f_multi", Text.MULTI_LINE);
    modeButtons = new AbstractButton[] { mcase, word, regex, multi };

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
        } else if(NEXTLINE.is(e) || PREVLINE.is(e)) {
          setModes(modeHistory.get(search.getText()));
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
    initModes();
    setModes(modeHistory.get(search.getText()));
  }

  /**
   * Sets the specified editor and updates the component layout.
   * @param e editor
   * @param srch triggers a search in the specified editor
   */
  public void editor(final TextPanel e, final boolean srch) {
    final boolean ed = e.isEditable();
    if(editor == null || ed != editor.isEditable()) {
      removeAll();
      final BaseXBack wst = new BaseXBack(false).layout(new TableLayout(1, 4, 1, 0));
      wst.add(mcase);
      wst.add(word);
      wst.add(regex);
      wst.add(multi);

      final BaseXBack ctr = new BaseXBack(false).layout(new GridLayout(1, 2, 2, 0));
      ctr.add(search);
      if(ed) ctr.add(replace);

      final BaseXBack est = new BaseXBack(false).layout(new TableLayout(1, 3, 1, 0));
      if(ed) est.add(rplc);
      est.add(cls);

      add(wst, BorderLayout.WEST);
      add(ctr, BorderLayout.CENTER);
      add(est, BorderLayout.EAST);
    }

    editor = e;
    refreshLayout();
    e.setSearch(this);

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
   * Resets the search options.
   * @param values array with four booleans (ignored if {@code null})
   */
  public void setModes(final boolean[] values) {
    if(values != null) {
      final int ml = modeButtons.length;
      for(int m = 0; m < ml; m++) modeButtons[m].setSelected(values[m]);
    }
    refreshButtons();
  }

  /**
   * Activates the search bar. A new search is triggered if the new seaerch term differs from
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
   * Refreshes the panel for the specified context.
   * @param sc search context
   */
  void refresh(final SearchContext sc) {
    final boolean nohits = sc.nr == 0;
    final boolean empty = sc.search.isEmpty();
    rplc.setEnabled(!nohits && !empty);
    search.setBackground(nohits && !empty ? GUIConstants.LRED : GUIConstants.BACK);
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Initializes the mode history map.
   */
  private void initModes() {
    final String[] searchModes = Strings.split(gui.gopts.get(GUIOptions.SEARCHMODES), ',');
    final String[] searches = gui.gopts.get(GUIOptions.SEARCHED);
    final int ml = Math.min(searchModes.length, searches.length);
    for(int m = 0; m < ml; m++) {
      final int bl = modeButtons.length;
      final BoolList list = new BoolList(bl);
      for(final char ch : searchModes[m].toCharArray()) list.add(ch == '!');
      modeHistory.put(searches[m], list.size() == bl ? list.finish() : new boolean[bl]);
    }
  }

  /**
   * Stores the search text.
   */
  private void store() {
    search.store();

    final String text = search.getText();
    final HashMap<String, boolean[]> map = new HashMap<>();
    final StringBuilder sb = new StringBuilder();
    for(final String t : gui.gopts.get(GUIOptions.SEARCHED)) {
      boolean[] modes = modeHistory.get(t);
      if(modes == null || t.equals(text)) {
        final BoolList list = new BoolList(modeButtons.length);
        for(final AbstractButton mode : modeButtons) list.add(mode.isSelected());
        modes = list.finish();
      }
      if(sb.length() > 0) sb.append(',');
      for(final boolean mode : modes) sb.append(mode ? '!' : '.');
      map.put(t, modes);
    }
    gui.gopts.set(GUIOptions.SEARCHMODES, sb.toString());
    modeHistory.clear();
    modeHistory.putAll(map);
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
   * @param jump jump to next hit
   */
  private void search(final boolean jump) {
    final String text = isVisible() ? search.getText() : "";
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
