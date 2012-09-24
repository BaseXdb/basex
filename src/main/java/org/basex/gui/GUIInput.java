package org.basex.gui;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.plaf.basic.*;

import org.basex.core.parse.*;
import org.basex.data.*;
import org.basex.gui.layout.*;
import org.basex.query.*;
import org.basex.util.list.*;

/**
 * This class offers a text field for keyword and XQuery input.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Andreas Weiler
 */
public final class GUIInput extends BaseXTextField {
  /** Reference to main window. */
  final GUI gui;
  /** JComboBox. */
  final BaseXCombo box;
  /** BasicComboPopup Menu. */
  ComboPopup pop;

  /** String for temporary input. */
  private String pre = "";

  /**
   * Default constructor.
   * @param main main window reference
   */
  GUIInput(final GUI main) {
    super(main);
    gui = main;

    final Font f = getFont();
    setFont(f.deriveFont((float) f.getSize() + 2));

    box = new BaseXCombo(main);
    box.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        if(e.getModifiers() == InputEvent.BUTTON1_MASK) completeInput();
      }
    });
    pop = new ComboPopup(box);

    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        final int count = box.getItemCount();
        if(ENTER.is(e)) {
          if(pop.isVisible()) {
            completeInput();
          } else {
            // store current input in history
            final String txt = getText();
            final StringList sl = new StringList().add(txt);

            final GUIProp gprop = gui.gprop;
            final int i = main.context.data() == null ? 2 :
              gprop.num(GUIProp.SEARCHMODE);
            final String[] hs = i == 0 ? gprop.strings(GUIProp.SEARCH) :
              i == 1 ? gprop.strings(GUIProp.XQUERY) :
              gprop.strings(GUIProp.COMMANDS);
            for(int p = 0; p < hs.length && sl.size() < 11; ++p) {
              if(!hs[p].equals(txt)) sl.add(hs[p]);
            }
            gprop.set(i == 0 ? GUIProp.SEARCH : i == 1 ? GUIProp.XQUERY :
              GUIProp.COMMANDS, sl.toArray());

            // evaluate the input
            if(e.getModifiers() == 0) main.execute();
          }
          return;
        }
        if(count == 0) return;

        int bi = box.getSelectedIndex();
        if(NEXTLINE.is(e)) {
          if(!pop.isVisible()) {
            showPopup();
          } else {
            if(++bi == count) bi = 0;
          }
        } else if(PREVLINE.is(e)) {
          if(!pop.isVisible()) {
            showPopup();
          } else {
            if(--bi < 0) bi = count - 1;
          }
        }
        if(bi != box.getSelectedIndex()) box.setSelectedIndex(bi);
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        if(ESCAPE.is(e)) {
          pop.setVisible(false);
        } else if(ENTER.is(e)) {
          pop.hide();
        } else if(!NEXTLINE.is(e) && !PREVLINE.is(e)) {
          if(modifier(e) || control(e)) return;
          showPopup();
          // skip commands
          if(gui.gprop.is(GUIProp.EXECRT) && !cmdMode()) main.execute();
        }
      }
    });
  }

  @Override
  public void setText(final String txt) {
    super.setText(txt);
    box.removeAllItems();
    pop.setVisible(false);
  }

  /**
   * Checks if the query is a command.
   * @return result of check
   */
  boolean cmdMode() {
    return gui.gprop.num(GUIProp.SEARCHMODE) == 2 ||
      gui.context.data() == null || getText().startsWith("!");
  }

  /**
   * Completes the input with the current combobox choice.
   */
  void completeInput() {
    final Object sel = box.getSelectedItem();
    if(sel == null) return;
    final String suf = sel.toString();
    final int pl = pre.length();
    final int ll = pl > 0 ? pre.charAt(pl - 1) : ' ';
    if(Character.isLetter(ll) && Character.isLetter(suf.charAt(0))) pre += " ";
    setText(pre + sel);
    showPopup();
    if(gui.gprop.is(GUIProp.EXECRT) && !cmdMode()) gui.execute();
  }

  /**
   * Shows the command popup menu.
   */
  void showPopup() {
    final String query = getText();
    final int mode = gui.gprop.num(GUIProp.SEARCHMODE);
    if(cmdMode()) {
      cmdPopup(query);
    } else if(mode == 1 || mode == 0 && query.startsWith("/")) {
      queryPopup(query);
    } else {
      pop.setVisible(false);
    }
  }

  /**
   * Shows the command popup menu.
   * @param query query input
   */
  private void cmdPopup(final String query) {
    StringList sl = null;
    final boolean excl = query.startsWith("!");
    try {
      pre = excl ? "!" : "";
      final String suf = getText().substring(pre.length());
      new CommandParser(suf, gui.context).suggest();
    } catch(final QueryException ex) {
      sl = ex.suggest();
      final int marked = ex.markedCol() + (excl ? 2 : 1);
      if(ex.markedCol() > -1 && marked <= query.length()) {
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
    try {
      final QueryContext qc = new QueryContext(gui.context);
      final QuerySuggest qs = new QuerySuggest(query, qc, data);
      qs.parse();
      sl = qs.complete();
      pre = query.substring(0, qs.im);
    } catch(final QueryException ex) {
      sl = ex.suggest();
      pre = query.substring(0, ex.col() - (ex.col() == 1 ? 1 : 0));
    }
    if(getCaretPosition() < pre.length()) sl = null;
    createCombo(sl);
  }

  /**
   * Creates and shows the combo box.
   * @param sl strings to be added
   */
  private void createCombo(final StringList sl) {
    if(sl == null || sl.isEmpty()) {
      pop.setVisible(false);
      return;
    }
    if(comboChanged(sl)) {
      box.setModel(new DefaultComboBoxModel(sl.toArray()));
      box.setSelectedIndex(-1);
      pop = new ComboPopup(box);
    }

    final int w = getFontMetrics(getFont()).stringWidth(pre);
    pop.show(this, Math.min(getWidth(), w), getHeight());
  }

  /**
   * Tests if the combo box entries have changed.
   * @param sl strings to be compared
   * @return result of check
   */
  private boolean comboChanged(final StringList sl) {
    if(sl.size() != box.getItemCount()) return true;
    final int is = sl.size();
    for(int i = 0; i < is; ++i) {
      if(!sl.get(i).equals(box.getItemAt(i))) return true;
    }
    return false;
  }

  /** Combo popup menu class, overriding the default constructor. */
  private static final class ComboPopup extends BasicComboPopup {
    /**
     * Constructor.
     * @param combo combobox reference
     */
    ComboPopup(final JComboBox combo) {
      super(combo);
      final int h = combo.getMaximumRowCount();
      setPreferredSize(new Dimension(getPreferredSize().width,
          getPopupHeightForRowCount(h) + 2));
    }
  }
}
