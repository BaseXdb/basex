package org.basex.gui.editor;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;

import org.basex.gui.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.editor.Editor.SearchDir;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXLayout.DropHandler;
import org.basex.util.options.*;

/**
 * This panel provides search and replace facilities.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class SearchBar extends BaseXBack {
  /** Escape key listener. */
  private final KeyAdapter escape = new KeyAdapter() {
    @Override
    public void keyPressed(final KeyEvent e) {
      if(ESCAPE.is(e)) deactivate(true);
    }
  };

  /** GUI reference. */
  private final GUI gui;
  /** Action: close panel. */
  private final BaseXButton cls;
  /** Search text. */
  private final BaseXTextField search;
  /** Replace text. */
  private final BaseXTextField replace;
  /** Mode: regular expression. */
  final BaseXButton regex;
  /** Mode: match case. */
  final BaseXButton mcase;
  /** Mode: whole word. */
  final BaseXButton word;
  /** Mode: multi-line. */
  final BaseXButton multi;
  /** Action: replace text. */
  private final BaseXButton rplc;

  /** Search button. */
  private BaseXButton button;
  /** Current editor reference. */
  private Editor editor;

  /**
   * Constructor.
   * @param main gui reference
   */
  SearchBar(final GUI main) {
    layout(new BorderLayout(2, 0));
    mode(Fill.NONE);
    setVisible(false);

    gui = main;
    search = new BaseXTextField(main);
    search.history(gui, GUIOptions.SEARCHED);
    search.setToolTipText(SEARCH);
    search.setPreferredSize(null);
    replace = new BaseXTextField(main);
    replace.history(gui, GUIOptions.REPLACED);
    replace.setToolTipText(REPLACE_WITH);
    replace.setPreferredSize(null);

    regex = onOffButton("s_regex", REGULAR_EXPR, GUIOptions.SR_REGEX);
    mcase = onOffButton("s_case", MATCH_CASE, GUIOptions.SR_CASE);
    word = onOffButton("s_word", WHOLE_WORD, GUIOptions.SR_WORD);
    multi = onOffButton("s_multi", MULTI_LINE, GUIOptions.SR_MULTI);
    rplc  = new BaseXButton(main, "s_replace", REPLACE_ALL);
    cls = new BaseXButton(main, "s_close", CLOSE);
    multi.setEnabled(regex.isSelected());
    word.setEnabled(!regex.isSelected());

    // add interaction to search field
    search.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(FINDPREV.is(e) || FINDPREV2.is(e) || FINDNEXT.is(e) || FINDNEXT2.is(e)) {
          editor.text.noSelect();
          deactivate(false);
        } else if(ESCAPE.is(e)) {
          deactivate(search.getText().isEmpty());
        } else if(ENTER.is(e)) {
          editor.jump(e.isShiftDown() ? SearchDir.BACKWARD : SearchDir.FORWARD, true);
        }
      }
      @Override
      public void keyReleased(final KeyEvent e) {
        main.gopts.set(GUIOptions.SR_SEARCH, search.getText());
        search();
      }
    });

    BaseXLayout.addDrop(search, new DropHandler() {
      @Override
      public void drop(final Object object) {
        search.setText(object.toString());
        search();
      }
    });

    replace.addKeyListener(escape);
    replace.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        main.gopts.set(GUIOptions.SR_REPLACE, replace.getText());
      }
    });

    cls.addKeyListener(escape);
    cls.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        deactivate(true);
      }
    });

    rplc.addKeyListener(escape);
    rplc.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        search.store();
        replace.store();
        final String in = replace.getText();
        editor.replace(new ReplaceContext(regex.isSelected() ? decode(in) : in));
        deactivate(true);
      }
    });
  }

  /**
   * Sets the specified editor and updates the component layout.
   * @param e editor
   * @param srch triggers a search in the specified editor
   */
  public void editor(final Editor e, final boolean srch) {
    final boolean ed = e.isEditable();
    if(editor == null || ed != editor.isEditable()) {
      removeAll();
      final BaseXBack wst = new BaseXBack(Fill.NONE).layout(new TableLayout(1, 4, 1, 0));
      wst.add(mcase);
      wst.add(word);
      wst.add(regex);
      wst.add(multi);

      final BaseXBack ctr = new BaseXBack(Fill.NONE).layout(new GridLayout(1, 2, 2, 0));
      ctr.add(search);
      if(ed) ctr.add(replace);

      final BaseXBack est = new BaseXBack(Fill.NONE).layout(new TableLayout(1, 3, 1, 0));
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
   * Sets the search button.
   * @param b button
   */
  public void setButton(final BaseXButton b) {
    button = b;
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        if(isVisible()) deactivate(true);
        else activate(null, true);
      }
    });
  }

  /**
   * Refreshes the layout.
   */
  public void refreshLayout() {
    if(editor == null) return;
    final Font ef = GUIConstants.mfont.deriveFont(7f + (GUIConstants.fontSize >> 1));
    search.setFont(ef);
    replace.setFont(ef);
  }

  /**
   * Activates the search bar.
   * @param string search string; triggers a new search if it differs from old string.
   * Will be ignored if set to {@code null}
   * @param focus indicates if text field should be focused
   */
  public void activate(final String string, final boolean focus) {
    boolean action = !isVisible();
    if(action) {
      setVisible(true);
      if(button != null) button.setSelected(true);
    }
    if(focus) search.requestFocusInWindow();

    // set new, different search string
    if(string != null && !new SearchContext(this, search.getText()).matches(string)) {
      search.setText(string);
      regex.setSelected(false);
      action = true;
    }
    // search if string has changed, or if panel was hidden
    if(action) search();
  }

  /**
   * Deactivates the search bar.
   * @param close close bar
   * @return {@code true} if panel was closed
   */
  public boolean deactivate(final boolean close) {
    search.store();
    editor.requestFocusInWindow();
    if(!close || !isVisible()) return false;
    setVisible(false);
    if(button != null) button.setSelected(false);
    search();
    return true;
  }

  /**
   * Searches text in the current editor.
   */
  void search() {
    search(true);
  }

  /**
   * Searches text in the current editor.
   * @param jump jump to next hit
   */
  void search(final boolean jump) {
    final String text = isVisible() ? search.getText() : "";
    editor.search(new SearchContext(this, text), jump);
  }

  /**
   * Refreshes the panel for the specified context.
   * @param sc search context
   */
  void refresh(final SearchContext sc) {
    final boolean nohits = sc.nr == 0;
    final boolean empty = sc.search.isEmpty();
    rplc.setEnabled(!nohits && !empty);
    search.setBackground(nohits && !empty ? GUIConstants.LRED : GUIConstants.WHITE);
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Returns a button that can be switched on and off.
   * @param icon name of icon
   * @param help help text
   * @param option GUI option
   * @return button
   */
  private BaseXButton onOffButton(final String icon, final String help,
      final BooleanOption option) {

    final BaseXButton b = new BaseXButton(gui, icon, help);
    b.setSelected(gui.gopts.get(option));
    b.addKeyListener(escape);
    b.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final boolean sel = !b.isSelected();
        b.setSelected(sel);
        gui.gopts.set(option, sel);
        if(b == regex) {
          multi.setEnabled(sel);
          word.setEnabled(!sel);
        }
        search();
      }
    });
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
    for(int i = 0; i < in.length(); i++) {
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
