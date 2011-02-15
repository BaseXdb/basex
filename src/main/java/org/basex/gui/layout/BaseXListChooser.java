package org.basex.gui.layout;

import static org.basex.gui.layout.BaseXKeys.*;
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
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class BaseXListChooser extends BaseXBack {
  /** Scroll pane. */
  private final JScrollPane scroll;
  /** Text field. */
  final BaseXTextField text;
  /** List. */
  final JList list;
  /** List values. */
  String[] values;
  /** Numeric list. */
  boolean num = true;

  /**
   * Default constructor.
   * @param choice the input values for the list
   * @param d dialog reference
   */
  public BaseXListChooser(final String[] choice, final Dialog d) {
    // cache list values
    values = choice.clone();

    // checks if list is purely numeric
    for(final String v : values) num = num && v.matches("[0-9]+");

    layout(new TableLayout(2, 1));
    text = new BaseXTextField(d);
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
        final int op = list.getSelectedIndex();
        int np = op;
        final int page = getHeight() / getFont().getSize();

        if(NEXTLINE.is(e)) {
          np = Math.min(op + 1, values.length - 1);
        } else if(PREVLINE.is(e)) {
          np = Math.max(op - 1, 0);
        } else if(NEXTPAGE.is(e)) {
          np = Math.min(op + page, values.length - 1);
        } else if(PREVPAGE.is(e)) {
          np = Math.max(op - page, 0);
        } else if(TEXTSTART.is(e)) {
          np = 0;
        } else if(TEXTEND.is(e)) {
          np = values.length - 1;
        }
        // choose new list value
        if(op != np && np < values.length) {
          list.setSelectedValue(values[np], true);
          text.setText(values[np]);
          text.selectAll();
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
          for(i = 0; i < values.length; ++i) {
            final String txt2 = values[i].toLowerCase();
            if(txt2.startsWith(txt)) break;
          }
          if(i < values.length) {
            final int c = text.getCaretPosition();
            list.setSelectedValue(values[i], true);
            text.setText(values[i]);
            text.select(c, values[i].length());
          }
        }
        d.action(null);
      }
    });
    add(text);

    final MouseInputAdapter mouse = new MouseInputAdapter() {
      @Override
      public void mouseEntered(final MouseEvent e) {
        BaseXLayout.focus(text, null);
      }
      @Override
      public void mousePressed(final MouseEvent e) {
        final Object i = list.getSelectedValue();
        if(i == null) return;
        text.setText(i.toString());
        text.requestFocusInWindow();
        text.selectAll();
        d.action(null);
      }
      @Override
      public void mouseDragged(final MouseEvent e) {
        mousePressed(e);
      }
      @Override
      public void mouseClicked(final MouseEvent e) {
        if(e.getClickCount() == 2) {
          d.close();
          return;
        }
      }
    };

    list = new JList(choice);
    list.setFocusable(false);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addMouseListener(mouse);
    list.addMouseMotionListener(mouse);
    text.setFont(list.getFont());
    BaseXLayout.addInteraction(list, d);

    scroll = new JScrollPane(list,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    add(scroll);
    setIndex(0);
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
   * @return list index entry
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
   * Resets the data shown in the list.
   * @param data list data
   */
  public void setData(final String[] data) {
    values = data.clone();
    list.setListData(data);
    setIndex(0);
  }

  @Override
  public boolean requestFocusInWindow() {
    return text.requestFocusInWindow();
  }
}
