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
 * @author BaseX Team 2005-24, BSD License
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
  private String prefix = "";

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
            popupMenu();
          }
        } else if(PREVLINE.is(e)) {
          if(popup.isVisible()) {
            if(--bi < 0) bi = count - 1;
          } else {
            popupMenu();
          }
        }
        if(bi != completions.getSelectedIndex()) completions.setSelectedIndex(bi);
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        if(!NEXTLINE.is(e) && !PREVLINE.is(e)) {
          if(modifier(e) || control(e)) return;
          popupMenu();
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
   * Checks if the input is a command.
   * @return result of check
   */
  private boolean cmdMode() {
    final int mode = gui.gopts.get(GUIOptions.SEARCHMODE);
    return mode == 2 || gui.context.data() == null;
  }

  /**
   * Checks if the input is a query.
   * @return result of check
   */
  private boolean queryMode() {
    final int mode = gui.gopts.get(GUIOptions.SEARCHMODE);
    return mode == 1 || mode == 0 && Strings.startsWith(getText(), '/');
  }

  /**
   * Completes the input with the current combobox choice.
   */
  private void completeInput() {
    final String suffix = completions.getSelectedItem();
    if(suffix.isEmpty()) return;
    final int pl = prefix.length(), ll = pl > 0 ? prefix.charAt(pl - 1) : ' ';
    if(Character.isLetter(ll) && Character.isLetter(suffix.charAt(0))) prefix += " ";
    setText(prefix + suffix);
    popupMenu();
    if(gui.gopts.get(GUIOptions.EXECRT) && !cmdMode()) gui.execute();
  }

  /**
   * Shows or hides a popup menu with input completions.
   */
  private void popupMenu() {
    final String query = getText();

    StringList list = null;
    if(textField().getCaretPosition() == query.length()) {
      final Data data = gui.context.data();
      if(queryMode() && data != null) {
        try(QueryContext qc = new QueryContext(gui.context)) {
          list = qc.suggest(query, data);
        }
      } else if(cmdMode()) {
        list = CommandParser.get(query, gui.context).suggest();
      }
    }

    if(list != null) {
      prefix = list.pop();
      createCombo(list);
    } else {
      popup.setVisible(false);
    }
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

    final int w = getFontMetrics(getFont()).stringWidth(prefix);
    popup.show(this, Math.min(getWidth(), w), getHeight());
  }

  /**
   * Tests if the combo box entries have changed.
   * @param list strings to be compared
   * @return result of check
   */
  private boolean completionsChanged(final StringList list) {
    final int ls = list.size();
    if(ls != completions.getItemCount()) return true;
    for(int l = 0; l < ls; ++l) {
      if(!list.get(l).equals(completions.getItemAt(l))) return true;
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
