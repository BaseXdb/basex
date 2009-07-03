package org.basex.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboPopup;
import org.basex.core.CommandParser;
import org.basex.data.Data;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXTextField;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QuerySuggest;
import org.basex.util.StringList;

/**
 * This class offers a text field for keyword and XQuery input.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
public final class GUIInput extends BaseXTextField {
  /** Reference to main window. */
  private final GUI gui;
  /** BasicComboPopup Menu. */
  ComboPopup pop;
  /** JComboBox. */
  BaseXCombo box;
  /** String for temporary input. */
  String pre = "";

  /**
   * Default Constructor.
   * @param main main window reference
   */
  public GUIInput(final GUI main) {
    super(null);
    gui = main;

    final Font f = getFont();
    setFont(f.deriveFont((float) f.getSize() + 2));

    box = new BaseXCombo(new String[] {}, null);
    box.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if(e.getModifiers() == 16) completeInput();
      }
    });
    pop = new ComboPopup(box);

    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        main.checkKeys(e);

        final int count = box.getItemCount();
        final int c = e.getKeyCode();

        if(c == KeyEvent.VK_ENTER) {
          if(pop.isVisible()) {
            completeInput();
          } else {
            // store current input in history
            final String txt = getText();
            final StringList sl = new StringList();
            sl.add(txt);

            final int i = !main.context.db() ? 2 : GUIProp.searchmode;
            final String[] hs = i == 0 ? GUIProp.search : i == 1 ?
                GUIProp.xquery : GUIProp.commands;
            for(int p = 0; p < hs.length && sl.size < 10; p++) {
              if(!hs[p].equals(txt)) sl.add(hs[p]);
            }
            if(i == 0) GUIProp.search = sl.finish();
            else if(i == 1) GUIProp.xquery = sl.finish();
            else GUIProp.commands = sl.finish();

            // evaluate the input
            if(e.getModifiers() == 0) main.execute();
          }
          return;
        }
        if(count == 0) return;

        int bi = box.getSelectedIndex();
        if(c == KeyEvent.VK_DOWN) {
          if(!pop.isVisible()) {
            showPopup();
          } else {
            if(++bi == count) bi = 0;
          }
        } else if(c == KeyEvent.VK_UP) {
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
        final int c = e.getKeyCode();
        if(e.getKeyChar() == 0xFFFF || e.isControlDown() ||
            c == KeyEvent.VK_DOWN || c == KeyEvent.VK_UP) return;

        if(c == KeyEvent.VK_ESCAPE) {
          pop.setVisible(false);
          return;
        }

        if(c == KeyEvent.VK_ENTER) {
          pop.hide();
        } else {
          showPopup();
          // skip commands
          if(GUIProp.execrt && !cmdMode()) main.execute();
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
  protected boolean cmdMode() {
    return GUIProp.searchmode == 2 || !gui.context.db() ||
      getText().startsWith("!");
  }

  /**
   * Completes the input with the current combobox choice.
   */
  protected void completeInput() {
    Object sel = box.getSelectedItem();
    if(sel == null) return;
    final String suf = sel.toString();
    final int pl = pre.length();
    final int ll = pl > 0 ? pre.charAt(pl - 1) : ' ';
    if(Character.isLetter(ll) && Character.isLetter(suf.charAt(0))) pre += " ";
    setText(pre + sel);
    showPopup();
  }

  /**
   * Shows the command popup menu.
   */
  protected void showPopup() {
    final String query = getText();
    if(cmdMode()) {
      cmdPopup(query);
    } else if(GUIProp.searchmode == 1 ||
        GUIProp.searchmode == 0 && query.startsWith("/")) {
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
      new CommandParser(suf, gui.context).parse();
    } catch(final QueryException ex) {
      sl = ex.complete();
      pre = query.substring(0, ex.col() - (excl ? 0 : 1));
    }
    createCombo(sl);
  }

  /**
   * Shows the xpath popup menu.
   * @param query query input
   */
  private void queryPopup(final String query) {
    final Data data = gui.context.data();
    if(data == null || !data.meta.uptodate) return;

    StringList sl = null;
    try {
      final QuerySuggest qs = new QuerySuggest(new QueryContext(), gui.context);
      qs.parse(query, null, null);
      sl = qs.complete();
      pre = query.substring(0, qs.qm);
    } catch(final QueryException ex) {
      sl = ex.complete();
      pre = query.substring(0, ex.col() - 1);
    }
    if(getCaretPosition() < pre.length()) sl = null;
    createCombo(sl);
  }

  /**
   * Creates and shows the combo box.
   * @param sl strings to be added
   */
  private void createCombo(final StringList sl) {
    if(sl == null || sl.size == 0) {
      //box.setSelectedItem(null);
      pop.setVisible(false);
      return;
    }

    if(comboChanged(sl)) {
      //box.setSelectedItem(null);
      box.setModel(new DefaultComboBoxModel(sl.finish()));
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
    if(sl.size != box.getItemCount()) return true;
    for(int i = 0; i < sl.size; i++) {
      if(!sl.list[i].equals(box.getItemAt(i))) return true;
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
