package org.basex.gui.layout;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComboBox;

import org.basex.gui.GUI;
import org.basex.gui.dialog.Dialog;

/**
 * Project specific ComboBox implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXCombo extends JComboBox {
  /** Maximum number of strings to be stored. */
  private static final int MAX = 30;
  /** Last Input. */
  public String last = "";
  /** Button help. */
  public byte[] help;

  /**
   * Constructor.
   * @param choice combobox choice.
   * @param e editable combobox
   * @param hlp help text
   */
  public BaseXCombo(final String[] choice, final byte[] hlp, final boolean e) {
    this(choice, hlp, e, null);
  }

  /**
   * Constructor.
   * @param choice combobox choice.
   * @param hlp help text
   * @param edit editable combobox
   * @param list action listener
   */
  public BaseXCombo(final String[] choice, final byte[] hlp, final boolean edit,
      final Dialog list) {
    
    super(choice);
    setMaximumRowCount(edit ? 5 : 12);
    BaseXLayout.addDefaultKeys(this, list);
    help = hlp;

    final Component comp = edit ? getEditor().getEditorComponent() : this; 
    comp.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(final MouseEvent e) {
        GUI.get().focus(e.getComponent(), help);
      }
    });
    if(!edit) return;
    
    setEditable(true);
    setText("");
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(e.getKeyChar() != 0xFFFF && isPopupVisible()) setPopupVisible(false);

        final int key = e.getKeyCode();
        final boolean ctrl = e.isControlDown();
        if(ctrl && (key == KeyEvent.VK_Z || key == KeyEvent.VK_Y)) {
          final String t = getText();
          setText(last);
          last = t;
        }

        if(key != KeyEvent.VK_ENTER) return;

        // add current input to top of combo box
        final String txt = getText();
        removeItem(txt);
        if(txt.length() == 0) return;
        final int s = getItemCount();
        if(s >= MAX) removeItemAt(s - 1);
        insertItemAt(txt, 0);
        setSelectedIndex(0);
      }
    });
    
    //ComboBoxRenderer renderer = new ComboBoxRenderer(this);
    //setRenderer(renderer);
  }

  /**
   * Returns the current text.
   * @return text
   */
  public String getText() {
    return getEditor().getItem().toString().trim();
  }

  /**
   * Sets a text.
   * @param txt text to be set
   */
  public void setText(final String txt) {
    last = txt;
    getEditor().setItem(txt);
  }

  /**
   * Returns all texts.
   * @return text
   */
  public String[] history() {
    final String[] items = new String[getItemCount()];
    for(int i = 0; i < items.length; i++) items[i] = getItemAt(i).toString();
    return items;
  }

  /**
   * Sets texts.
   * @param txt texts to be set
   */
  public void history(final String[] txt) {
    removeAllItems();
    for(int i = 0; i < txt.length; i++) addItem(txt[i]);
  }
  
  @Override
  public void addKeyListener(final KeyListener l) {
    if(isEditable()) getEditor().getEditorComponent().addKeyListener(l);
    else super.addKeyListener(l);
  } 
  /*
  class ComboBoxRenderer extends JLabel implements ListCellRenderer {
    BaseXCombo b;
    
    public ComboBoxRenderer(BaseXCombo box) {
      b = box;   
    }

  public Component getListCellRendererComponent(
                    JList list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

    if (index == 0) {
      JLabel b = new JLabel();
      b.setText("bla");
      return b;
    }
    return this;
    
    }
  
  protected void paintComponent(Graphics g) 
  { 
    String[] t = b.getText().split(" ");
    for (int i=0; i<t.length; i++) {
      if ((t[i].startsWith("\"") || t[i].startsWith("\'"))) {
        g.setColor(Color.RED); 
        g.drawString(t[i], 100 + 10*i, 100);
         System.out.println(t[i]);

      } else {
        System.out.println(t[i]);
        g.setColor(Color.BLACK);
        g.drawString(t[i], 100 + 10*i, 100);
        
      }
    }
  }   */
}
