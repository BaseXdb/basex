package org.basex.gui.text;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXLayout.DropHandler;
import org.basex.util.options.*;

/**
 * This panel provides search and replace facilities.
 *
 * @author BaseX Team 2005-15, BSD License
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
  private final KeyAdapter escape = new KeyAdapter() {
    @Override
    public void keyPressed(final KeyEvent e) {
      if(ESCAPE.is(e)) deactivate(true);
    }
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
  private final BaseXTextField search;
  /** Replace text. */
  private final BaseXTextField replace;

  /** Search button. */
  private AbstractButton button;
  /** Current editor reference. */
  private TextPanel editor;

  /**
   * Constructor.
   * @param main gui reference
   */
  SearchBar(final GUI main) {
    layout(new BorderLayout(2, 0));
    setOpaque(false);
    setVisible(false);

    gui = main;
    search = new BaseXTextField(main);
    search.history(GUIOptions.SEARCHED, gui);
    search.setPreferredSize(null);
    search.hint(Text.FIND + Text.DOTS);

    replace = new BaseXTextField(main);
    replace.history(GUIOptions.REPLACED, gui);
    replace.setPreferredSize(null);
    replace.hint(Text.REPLACE_WITH + Text.DOTS);

    regex = onOffButton("f_regex", Text.REGULAR_EXPR, GUIOptions.SR_REGEX);
    mcase = onOffButton("f_case", Text.MATCH_CASE, GUIOptions.SR_CASE);
    word = onOffButton("f_word", Text.WHOLE_WORD, GUIOptions.SR_WORD);
    multi = onOffButton("f_multi", Text.MULTI_LINE, GUIOptions.SR_MULTI);
    rplc  = BaseXButton.get("f_replace", Text.REPLACE_ALL, false, main);
    cls = BaseXButton.get("f_close", BaseXLayout.addShortcut(Text.CLOSE, ESCAPE.toString()),
        false, main);
    multi.setEnabled(regex.isSelected());
    word.setEnabled(!regex.isSelected());

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
          editor.jump(SearchDir.FORWARD, true);
        } else if(SHIFT_ENTER.is(e)) {
          editor.jump(SearchDir.BACKWARD, true);
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
    button = BaseXButton.get("c_find", BaseXLayout.addShortcut(help, FIND.toString()),
        true, gui);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        if(isVisible()) deactivate(true);
        else activate(null, true);
      }
    });
    return button;
  }

  /**
   * Refreshes the layout.
   */
  public void refreshLayout() {
    if(editor == null) return;
    final Font ef = editor.getFont().deriveFont(7f + (GUIConstants.fontSize >> 1));
    search.setFont(ef);
    replace.setFont(ef);
  }

  /**
   * Resets the search options.
   */
  public void reset() {
    regex.setSelected(false);
    mcase.setSelected(false);
    word.setSelected(false);
    multi.setSelected(false);
  }

  /**
   * Activates the search bar.
   * @param string search string; triggers a new search if it differs from old string.
   * Will be ignored if set to {@code null}
   * @param focus indicates if the search field should be focused
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
      search.store();
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
   * Returns a button that can be switched on and off.
   * @param icon name of icon
   * @param tooltip tooltip text
   * @param option GUI option
   * @return button
   */
  private AbstractButton onOffButton(final String icon, final String tooltip,
      final BooleanOption option) {

    final AbstractButton b = BaseXButton.get(icon, tooltip, true, gui);
    b.setSelected(gui.gopts.get(option));
    b.addKeyListener(escape);
    b.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final boolean sel = b.isSelected();
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
