package org.basex.gui.layout;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.MouseInputAdapter;
import org.basex.gui.dialog.Dialog;
import org.basex.util.Token;

/**
 * Combination of TextField and a List, communicating with each other.
 * List entries are automatically completed if they match the first characters
 * of the typed in text. Moreover, the cursor keys can be used to scroll
 * through the list, and list entries can be chosen with mouse clicks.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXListChooser extends BaseXBack {
  /** List. */
  JList list;
  /** List Values. */
  String[] values;
  /** Text field. */
  BaseXTextField text;
  /** Numeric list. */
  boolean num = true;
  /** Scroll pane. */
  private JScrollPane scroll;
  
  /**
   * Default Constructor.
   * @param parent the notifier receives notifications when changes
   * have been taken place, when a final value has been chosen or when
   * the user has canceled (e.g. by pressing Escape).
   * @param choice the input values for the list.
   * @param help help text
   */
  public BaseXListChooser(final Dialog parent, final String[] choice,
      final byte[] help) {
    // cache list values
    values = choice.clone();
    
    // checks if list is purely numeric
    for(final String v : values) {
      for(int c = 0; c < v.length(); c++) {
        num = num && v.charAt(c) >= '0' && v.charAt(c) <= '9';
      }
      if(!num) break;
    }

    // initialize panel
    setLayout(new TableLayout(2, 1));
    // create a text field
    text = new BaseXTextField(help, parent);
    text.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        text.selectAll();
      }
    });
    text.addKeyListener(new KeyAdapter() {
      boolean typed;
      
      @Override
      public void keyPressed(final KeyEvent e) {
        final int oldpos = list.getSelectedIndex();
        int newpos = oldpos;
        final int page = getHeight() / getFont().getSize();
        
        // process key events
        if(e.getKeyCode() == KeyEvent.VK_DOWN) {
          newpos = Math.min(oldpos + 1, values.length - 1);
        } else if(e.getKeyCode() == KeyEvent.VK_UP) {
          newpos = Math.max(oldpos - 1, 0);
        } else if(e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
          newpos = Math.min(oldpos + page, values.length - 1);
        } else if(e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
          newpos = Math.max(oldpos - page, 0);
        } else if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_HOME) {
          newpos = 0;
        } else if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_END) {
          newpos = values.length - 1;
        }
        // choose new list value
        if(oldpos != newpos && newpos < values.length) {
          list.setSelectedValue(values[newpos], true);
          text.setText(values[newpos]);
          text.selectAll();
          parent.action(null);
          typed = false;
        }
      }

      @Override
      public void keyTyped(final KeyEvent e) {
        final char ch = e.getKeyChar();
        if(num) {
          typed = ch >= '0' && ch <= '9';
          if(!typed) e.consume();
        } else {
          typed = ch >= ' ' && ch != 127;
        }
      }
      
      @Override
      public void keyReleased(final KeyEvent e) {
        if(typed) {
          typed = false;
          
          final String txt = text.getText().trim().toLowerCase();
          int i = 0;
          for(i = 0; i < values.length; i++) {
            final String txt2 = values[i].toLowerCase();
            if(txt2.startsWith(txt)) break;
          }
          if(i < values.length) {
            final int c = text.getCaretPosition();
            list.setSelectedValue(values[i], true);
            text.setText(values[i]);
            text.select(c, values[i].length());
          } else if(num) {
            //parent.action(null);
          }
        }
        parent.action(null);
      }
    });
    add(text);

    final MouseInputAdapter mouse = new MouseInputAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        text.setText(list.getSelectedValue().toString());
        text.requestFocusInWindow();
        text.selectAll();
        parent.action(null);
      }
      @Override
      public void mouseDragged(final MouseEvent e) {
        mousePressed(e);
      }
      @Override
      public void mouseClicked(final MouseEvent e) {
        if(e.getClickCount() == 2) {
          parent.close();
          return;
        }
      }
    };
    
    // initialize list
    list = new JList(choice);
    list.setFocusable(false);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addMouseListener(mouse);
    list.addMouseMotionListener(mouse);
    text.setFont(list.getFont());
    BaseXLayout.addHelp(list, help);
    
    scroll = new JScrollPane(list,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    add(scroll);
    setIndex(0);
    
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        text.requestFocusInWindow();
      }
    });
   }
  
  /**
   * Chooses the specified value in the text field and list. 
   * @param value the value to be set
   */
  public void setValue(final String value) {
    list.setSelectedValue(value, true);
    text.setText(value);
  }

  /**
   * Returns the value of the text field.
   * @return text field value
   */
  public String getValue() {
    return text.getText();
  }

  /**
   * Returns the numeric representation of the user input. If the
   * text field is invalid, the list entry is returned. 
   * @return numeric value
   */
  public int getNum() {
    final int i = Token.toInt(text.getText());
    if(i != Integer.MIN_VALUE) return i;

    final Object value = list.getSelectedValue();
    return value != null ? Integer.parseInt(value.toString()) : 0;
  }

  /**
   * Returns the current list index.
   * @return list index entry.
   */
  public int getIndex() {
    return list.getSelectedIndex();
  }

  /**
   * Sets the specified list index.
   * @param i list entry
   */
  public void setIndex(final int i) {
    if(i < values.length) setValue(values[i]);
    else text.setText("");
  }

  @Override
  public void setSize(final int w, final int h) {
    BaseXLayout.setWidth(text, w);
    BaseXLayout.setSize(scroll, w, h);
  }

  /**
   * Sets a tooltip.
   * @param tooltip tooltip text
   */
  public void setToolTip(final String tooltip) {
    text.setToolTipText(tooltip);
    list.setToolTipText(tooltip);
  }

  /**
   * Resets the data shown in the list.
   * @param data list data
   */
  public void setData(final String[] data) {
    values = data.clone();
    list.setListData(data);
    setIndex(0);
  }

  /**
   * Focuses the text field.
   */
  public void focus() {
    text.requestFocusInWindow();
  }
}
