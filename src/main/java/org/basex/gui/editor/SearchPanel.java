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

/**
 * This panel provides search and replace facilities.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class SearchPanel extends BaseXBack {
  /** GUI reference. */
  final GUI gui;
  /** Search button. */
  final BaseXButton button;
  /** Editor view. */
  final EditorNotifier view;
  /** Action: close panel. */
  final BaseXButton close;
  /** Search text. */
  final BaseXTextField search;
  /** Replace text. */
  final BaseXTextField replace;
  /** Mode: regular expression. */
  final BaseXButton regex;
  /** Mode: match case. */
  final BaseXButton mcase;
  /** Mode: multi-line. */
  final BaseXButton multi;
  /** Action: replace text. */
  final BaseXButton rplc;

  /**
   * Constructor.
   * @param main gui reference
   * @param ev editor view
   * @param act button for activating the search
   * @param update add replace components
   */
  public SearchPanel(final GUI main, final EditorNotifier ev,
      final BaseXButton act, final boolean update) {

    layout(new BorderLayout(2, 0));
    mode(Fill.NONE);

    gui = main;
    view = ev;
    button = act;
    search = new BaseXTextField(main);
    search.history(gui, GUIProp.SEARCHED);
    search.setToolTipText(SEARCH);
    replace = new BaseXTextField(main);
    replace.history(gui, GUIProp.REPLACED);
    replace.setToolTipText(REPLACE_WITH);
    regex = onOffButton("s_regex", REGULAR_EXPR, GUIProp.SR_REGEX);
    mcase = onOffButton("s_case", MATCH_CASE, GUIProp.SR_CASE);
    multi = onOffButton("s_multi", MULTI_LINE, GUIProp.SR_MULTI);
    rplc  = new BaseXButton(main, "s_replace", REPLACE_ALL);
    close = new BaseXButton(main, "s_close", CLOSE);
    multi.setEnabled(regex.isEnabled());

    final BaseXBack west = new BaseXBack(Fill.NONE).layout(new TableLayout(1, 3, 1, 0));
    west.add(mcase);
    west.add(regex);
    west.add(multi);

    final BaseXBack center = new BaseXBack(Fill.NONE).layout(new GridLayout(1, 2, 2, 0));
    center.add(search);
    if(update) center.add(replace);

    final BaseXBack east = new BaseXBack(Fill.NONE).layout(new TableLayout(1, 3, 1, 0));
    if(update) east.add(rplc);
    east.add(close);

    add(west, BorderLayout.WEST);
    add(center, BorderLayout.CENTER);
    add(east, BorderLayout.EAST);

    // add interaction to search field
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        activate(true);
      }
    });
    search.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        search.selectAll();
      }
    });
    search.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(ESCAPE.is(e) || ENTER.is(e) && search.getText().trim().isEmpty()) {
          deactivate();
        } else if(FINDPREV.is(e) || FINDPREV2.is(e) || FINDNEXT.is(e) ||
            FINDNEXT2.is(e)) {
          view.getEditor().requestFocusInWindow();
        } else if(ENTER.is(e) && e.isShiftDown()) {
          view.getEditor().jump(SearchDir.BACKWARD);
        } else if(ENTER.is(e)) {
          view.getEditor().jump(SearchDir.FORWARD);
        }
      }
      @Override
      public void keyReleased(final KeyEvent e) {
        main.gprop.set(GUIProp.SR_SEARCH, search.getText());
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

    replace.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(ESCAPE.is(e)) deactivate();
      }
      @Override
      public void keyReleased(final KeyEvent e) {
        main.gprop.set(GUIProp.SR_REPLACE, replace.getText());
      }
    });

    close.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        deactivate();
      }
    });

    rplc.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        search.store();
        replace.store();
        view.getEditor().replace(new ReplaceContext(replace.getText()));
      }
    });
  }

  /**
   * Refreshes the layout.
   */
  public void refreshLayout() {
    final String mf = view.getEditor().getFont().getFamily();
    final Font f = new Font(mf, 0, search.getFont().getSize());
    search.setFont(f);
    replace.setFont(f);
  }

  /**
   * Activates the search panel.
   * @param focus focus search field
   */
  public void activate(final boolean focus) {
    if(!isVisible()) {
      super.setVisible(true);
      button.setSelected(true);
      search();
    }
    if(focus) search.requestFocusInWindow();
  }

  /**
   * Deactivates the search panel.
   */
  public void deactivate() {
    if(!isVisible()) return;
    super.setVisible(false);
    button.setSelected(false);
    view.getEditor().requestFocusInWindow();
    search();
  }

  /**
   * Searches text in the current editor.
   */
  public void search() {
    final String text = isVisible() ? search.getText() : "";
    rplc.setEnabled(!text.isEmpty());
    view.getEditor().search(new SearchContext(this, text));
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
      final Object[] option) {

    final BaseXButton b = new BaseXButton(gui, icon, help);
    b.setSelected(gui.gprop.is(option));
    b.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final boolean sel = !b.isSelected();
        b.setSelected(sel);
        gui.gprop.set(option, sel);
        if(b == regex) multi.setEnabled(sel);
        search();
      }
    });
    return b;
  }
}
