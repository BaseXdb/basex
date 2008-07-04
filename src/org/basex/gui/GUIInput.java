package org.basex.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboPopup;

import org.basex.core.Commands;
import org.basex.data.Data;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXTextField;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * This class offers a text field for XPath input.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
public final class GUIInput extends BaseXTextField {
  /** XPath input completions. */
  private static final String[] XPATH = { "*", "comment()", "node()",
    "processing-instruction()", "text()", "ancestor-or-self::", "ancestor::",
      "attribute::", "child::", "descendant-or-self::", "descendant::",
      "following-sibling::", "following::", "parent::", "preceding-sibling::",
      "preceding::", "self::" };

  /** BasicComboPopup Menu. */
  ComboPopup pop;
  /** JComboBox. */
  BaseXCombo box;
  /** Main window reference. */
  GUI main;
  /** String for temporary input. */
  String pre = "";

  /**
   * Default Constructor.
   * @param m main window reference
   */
  public GUIInput(final GUI m) {
    super(null);
    final Font f = getFont();
    setFont(f.deriveFont((float) f.getSize() + 2));

    main = m;
    box = new BaseXCombo();
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

            final int i = !GUI.context.db() ? 2 : GUIProp.searchmode;
            final String[] hs = i == 0 ? GUIProp.search : i == 1 ?
                GUIProp.xpath : GUIProp.commands;
            for(int p = 0; p < hs.length && sl.size < 10; p++) {
              if(!hs[p].equals(txt)) sl.add(hs[p]);
            }
            if(i == 0) GUIProp.search = sl.finish();
            else if(i == 1) GUIProp.xpath = sl.finish();
            else GUIProp.commands = sl.finish();

            // evaluate the input
            main.execute();
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

        final boolean enter = c == KeyEvent.VK_ENTER;
        if(!enter) showPopup();

        // skip commands
        if(GUIProp.execrt && !cmdMode()) main.execute();
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
    return GUIProp.searchmode == 2 || !GUI.context.db() ||
      getText().startsWith("!");
  }

  /**
   * Completes the input with the current combobox choice.
   */
  protected void completeInput() {
    Object sel = box.getSelectedItem();
    if(sel == null) sel = box.getItemAt(0);
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
    } else if(GUIProp.searchmode == 1) {
      xpathPopup(query);
    }
  }

  /**
   * Shows the command popup menu.
   * @param query query input
   */
  private void cmdPopup(final String query) {
    final StringList sl = new StringList();
    pre = query.startsWith("!") ? "!" : "";
    final String suf = getText().substring(pre.length());

    if(suf.length() != 0) {
      for(final String cmd : Commands.list()) {
        if(cmd.startsWith(suf) && !cmd.equals(suf)) sl.add(cmd);
      }
    }
    sl.sort();

    createCombo(sl);
  }

  /**
   * Shows the xpath popup menu.
   * @param query query input
   */
  private void xpathPopup(final String query) {
    final StringList sl = new StringList();

    final int slash = Math.max(query.lastIndexOf('/'),
        Math.max(query.lastIndexOf('['), query.lastIndexOf('(')));
    final int axis = query.lastIndexOf("::");
    final boolean test = axis > slash;

    pre = "";
    if(test) pre = query.substring(0, axis + 2);
    else if(slash != -1) pre = query.substring(0, slash + 1);

    if(query.length() != 0) {
      final String suf = getText().substring(pre.length());
      final String[] all = xpathList();
      if(test) {
        final boolean attest = pre.endsWith("attribute::");
        for(final String a : all) {
          if(a.endsWith("::")) continue;
          if(attest ^ a.startsWith("@")) continue;
          final String at = a.substring(attest ? 1 : 0);
          if(at.startsWith(suf) && !at.equals(suf)) sl.add(at);
        }
      } else {
        for(final String a : all) {
          if(a.startsWith(suf) && !a.equals(suf)) sl.add(a);
        }
      }
    }
    createCombo(sl);
  }

  /**
   * Creates a combo box list.
   * @return list
   */
  private String[] xpathList() {
    final StringList sl = new StringList();
    final Data data = GUI.context.data();

    for(int i = 1; i <= data.tags.size(); i++) {
      if(data.tags.counter(i) == 0) continue;
      sl.add(Token.string(data.tags.key(i)));
    }
    for(int i = 1; i <= data.atts.size(); i++) {
      if(data.atts.counter(i) == 0) continue;
      sl.add("@" + Token.string(data.atts.key(i)));
    }
    sl.sort();

    for(final String c : XPATH) sl.add(c);
    return sl.finish();
  }

  /**
   * Creates and shows the combo box.
   * @param sl strings to be added
   */
  private void createCombo(final StringList sl) {
    if(sl.size == 0) {
      box.setSelectedItem(null);
      pop.setVisible(false);
      return;
    }

    if(comboChanged(sl)) {
      box.setSelectedItem(null);
      box.removeAllItems();
      for(int i = 0; i < sl.size; i++) box.addItem(sl.list[i]);
      pop = new ComboPopup(box);
      box.setSelectedIndex(0);
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
