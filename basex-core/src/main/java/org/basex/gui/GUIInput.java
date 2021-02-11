package org.basex.gui;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.plaf.basic.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.data.*;
import org.basex.gui.layout.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class offers a text field for keyword and XQuery input.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class GUIInput extends BaseXCombo {
  /** Reference to the main window. */
  private final GUI gui;
  /** Input completions. */
  private final BaseXCombo completions;
  /** BasicComboPopup Menu. */
  private GUIInputPopup popup;

  /** String for temporary input. */
  private String pre = "";

  /**
   * Default constructor.
   * @param main main window reference
   */
  GUIInput(final GUI main) {
    super(main, true);
    gui = main;

    BaseXLayout.resizeFont(this, 1.3f);

    completions = new BaseXCombo(main);
    completions.addActionListener(e -> completeInput());
    popup = new GUIInputPopup(completions);

    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(ESCAPE.is(e)) {
          popup.setVisible(false);
        } else if(ENTER.is(e)) {
          if(popup.isVisible()) {
            completeInput();
            popup.setVisible(false);
          } else {
            updateHistory();
            // evaluate the input
            if(e.getModifiersEx() == 0) gui.execute();
          }
        }

        final int count = completions.getItemCount();
        if(count == 0) return;

        int bi = completions.getSelectedIndex();
        if(NEXTLINE.is(e)) {
          if(popup.isVisible()) {
            if(++bi == count) bi = 0;
          } else {
            showCompletions();
          }
        } else if(PREVLINE.is(e)) {
          if(popup.isVisible()) {
            if(--bi < 0) bi = count - 1;
          } else {
            showCompletions();
          }
        }
        if(bi != completions.getSelectedIndex()) completions.setSelectedIndex(bi);
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        if(!NEXTLINE.is(e) && !PREVLINE.is(e)) {
          if(modifier(e) || control(e)) return;
          showCompletions();
          // skip commands
          if(gui.gopts.get(GUIOptions.EXECRT) && !cmdMode()) main.execute();
        }
      }
    });
  }

  @Override
  public void setText(final String txt) {
    super.setText(txt);
    completions.removeAllItems();
    popup.setVisible(false);
  }

  /**
   * Sets the input mode.
   * @param mode mode
   */
  void mode(final String mode) {
    hint(mode + Text.DOTS).setText("");

    final Data data = gui.context.data();
    final int i = data == null ? 2 : gui.gopts.get(GUIOptions.SEARCHMODE);
    history(i == 0 ? GUIOptions.SEARCH : i == 1 ?
        GUIOptions.XQUERY : GUIOptions.COMMANDS, gui.gopts);
  }

  /**
   * Checks if the query is a command.
   * @return result of check
   */
  private boolean cmdMode() {
    return gui.gopts.get(GUIOptions.SEARCHMODE) == 2 ||
      gui.context.data() == null || Strings.startsWith(getText(), '!');
  }

  /**
   * Completes the input with the current combobox choice.
   */
  private void completeInput() {
    final String suffix = completions.getSelectedItem();
    if(suffix.isEmpty()) return;
    final int pl = pre.length();
    final int ll = pl > 0 ? pre.charAt(pl - 1) : ' ';
    if(Character.isLetter(ll) && Character.isLetter(suffix.charAt(0))) pre += " ";
    setText(pre + suffix);
    showCompletions();
    if(gui.gopts.get(GUIOptions.EXECRT) && !cmdMode()) gui.execute();
  }

  /**
   * Shows the command popup menu.
   */
  private void showCompletions() {
    final String query = getText();
    final int mode = gui.gopts.get(GUIOptions.SEARCHMODE);
    if(cmdMode()) {
      cmdPopup(query);
    } else if(mode == 1 || mode == 0 && Strings.startsWith(query, '/')) {
      queryPopup(query);
    } else {
      popup.setVisible(false);
    }
  }

  /**
   * Shows the command popup menu.
   * @param query query input
   */
  private void cmdPopup(final String query) {
    StringList sl = null;
    final boolean excl = Strings.startsWith(query, '!');
    try {
      pre = excl ? "!" : "";
      final String suf = getText().substring(pre.length());
      CommandParser.get(suf, gui.context).suggest().parse();
    } catch(final QueryException ex) {
      sl = ex.suggest();
      final int marked = ex.markedColumn() + (excl ? 2 : 1);
      if(ex.markedColumn() > -1 && marked <= query.length()) {
        pre = query.substring(0, marked);
      }
    }
    createCombo(sl);
  }

  /**
   * Shows the xpath popup menu.
   * @param query query input
   */
  private void queryPopup(final String query) {
    final Data data = gui.context.data();
    if(data == null) return;

    StringList sl;
    try(QueryContext qc = new QueryContext(gui.context)) {
      final QuerySuggest qs = new QuerySuggest(query, qc, data);
      qs.parseMain();
      sl = qs.complete();
      pre = query.substring(0, qs.mark);
    } catch(final QueryException ex) {
      sl = ex.suggest();
      pre = query.substring(0, ex.column() - 1);
    }
    if(textField().getCaretPosition() < pre.length()) sl = null;
    createCombo(sl);
  }

  /**
   * Creates and shows the combo box.
   * @param sl strings to be added
   */
  private void createCombo(final StringList sl) {
    if(sl == null || sl.isEmpty()) {
      popup.setVisible(false);
      return;
    }
    if(completionsChanged(sl)) {
      completions.setItems(sl.toArray());
      completions.setSelectedIndex(-1);
      popup = new GUIInputPopup(completions);
    }

    final int w = getFontMetrics(getFont()).stringWidth(pre);
    popup.show(this, Math.min(getWidth(), w), getHeight());
  }

  /**
   * Tests if the combo box entries have changed.
   * @param sl strings to be compared
   * @return result of check
   */
  private boolean completionsChanged(final StringList sl) {
    if(sl.size() != completions.getItemCount()) return true;
    final int is = sl.size();
    for(int i = 0; i < is; ++i) {
      if(!sl.get(i).equals(completions.getItemAt(i))) return true;
    }
    return false;
  }

  /** Combo popup menu class, overriding the default constructor. */
  private static final class GUIInputPopup extends BasicComboPopup {
    /**
     * Constructor.
     * @param combo combobox reference
     */
    GUIInputPopup(final JComboBox<Object> combo) {
      super(combo);
      final int h = combo.getMaximumRowCount();
      setPreferredSize(new Dimension(getPreferredSize().width, getPopupHeightForRowCount(h) + 2));
    }
  }
}
