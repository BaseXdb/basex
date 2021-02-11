package org.basex.gui.layout;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Combination of text field and a list, communicating with each other.
 * List entries are automatically completed if they match the first characters
 * of the typed in text. Moreover, the cursor keys can be used to scroll
 * through the list, and list entries can be chosen with mouse clicks.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BaseXList extends BaseXBack {
  /** Text field. */
  private final BaseXTextField text;
  /** List. */
  private final JList<String> list;
  /** Scroll pane. */
  private final JScrollPane scroll;

  /** List values. */
  private String[] values;
  /** Numeric list. */
  private boolean num = true;

  /**
   * Default constructor.
   * @param dialog dialog reference
   * @param choice the input values for the list
   */
  public BaseXList(final BaseXDialog dialog, final String... choice) {
    this(dialog, true, choice);
  }

  /**
   * Default constructor.
   * @param dialog dialog reference
   * @param single only allow single choices
   * @param choice the input values for the list
   */
  public BaseXList(final BaseXDialog dialog, final boolean single, final String... choice) {
    // cache list values
    values = choice.clone();

    // checks if list is purely numeric
    for(final String v : values) num = num && v.matches("[0-9]+");

    layout(new RowLayout());
    text = new BaseXTextField(dialog);
    text.addKeyListener(new KeyAdapter() {
      boolean multi, typed;
      String old = "";

      @Override
      public void keyPressed(final KeyEvent e) {
        final int page = getHeight() / getFont().getSize();
        final int[] inds = list.getSelectedIndices();
        final int op1 = inds.length == 0 ? -1 : inds[0];
        final int op2 = inds.length == 0 ? -1 : inds[inds.length - 1];
        int np1 = op1, np2 = op2;

        if(NEXTLINE.is(e)) {
          np2 = Math.min(op2 + 1, values.length - 1);
        } else if(PREVLINE.is(e)) {
          np1 = Math.max(op1 - 1, 0);
        } else if(NEXTPAGE.is(e)) {
          np2 = Math.min(op2 + page, values.length - 1);
        } else if(PREVPAGE.is(e)) {
          np1 = Math.max(op1 - page, 0);
        } else if(TEXTSTART.is(e)) {
          np1 = 0;
        } else if(TEXTEND.is(e)) {
          np2 = values.length - 1;
        } else {
          return;
        }

        final IntList il = new IntList();
        for(int n = np1; n <= np2; n++) il.add(n);
        // choose new list value
        final int nv = op2 == np2 ? np1 : np2;
        final String val = values[nv];
        list.setSelectedValue(val, true);
        multi = il.size() > 1;
        if(e.isShiftDown() && !single) {
          list.setSelectedIndices(il.finish());
          text.setText("");
        } else {
          list.setSelectedIndex(nv);
          text.setText(val);
          text.selectAll();
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
        multi = false;
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        String txt = text.getText().trim().toLowerCase(Locale.ENGLISH);
        if(!txt.equals(old) && !multi) {
          final boolean glob = txt.matches("^.*[*?,].*$");
          final String regex = glob ? IOFile.regex(txt, false) : null;

          final IntList il = new IntList();
          final int vl = values.length;
          for(int v = 0; v < vl; ++v) {
            final String value = values[v].trim().toLowerCase(Locale.ENGLISH);
            if(glob) {
              if(value.matches(regex)) il.add(v);
            } else if(value.startsWith(txt)) {
              if(typed) {
                final int c = text.getCaretPosition();
                text.setText(values[v]);
                text.select(c, values[v].length());
                txt = value;
              }
              il.add(v);
              break;
            }
          }
          if(!il.isEmpty()) {
            list.setSelectedValue(values[il.get(il.size() - 1)], true);
          }
          list.setSelectedIndices(il.finish());
        }
        dialog.action(BaseXList.this);
        typed = false;
        old = txt;
      }
    });
    add(text);

    final MouseInputAdapter mouse = new MouseInputAdapter() {
      @Override
      public void mouseEntered(final MouseEvent e) {
        BaseXLayout.focus(text);
      }
      @Override
      public void mousePressed(final MouseEvent e) {
        final List<String> vals = list.getSelectedValuesList();
        text.setText(vals.size() == 1 ? vals.get(0) : "");
        text.requestFocusInWindow();
        text.selectAll();
        dialog.action(BaseXList.this);
      }
      @Override
      public void mouseDragged(final MouseEvent e) {
        mousePressed(e);
      }
      @Override
      public void mouseClicked(final MouseEvent e) {
        if(e.getClickCount() == 2) dialog.close();
      }
    };

    list = new JList<>(choice);
    list.setFocusable(false);
    if(single) list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addMouseListener(mouse);
    list.addMouseMotionListener(mouse);
    text.setFont(list.getFont());
    text.setPreferredSize(new Dimension(list.getWidth(), text.getPreferredSize().height));
    BaseXLayout.addInteraction(list, dialog);

    scroll = new JScrollPane(list);
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

  @Override
  public void setEnabled(final boolean en) {
    list.setEnabled(en);
    text.setEnabled(en);
  }

  @Override
  public boolean isEnabled() {
    return list.isEnabled();
  }

  /**
   * Sets the specified font.
   * @param font font name
   * @param style style
   */
  public void setFont(final String font, final int style) {
    final Font f = text.getFont();
    text.setFont(new Font(font, style, f.getSize()));
  }

  /**
   * Returns all list choices.
   * @return list index entry
   */
  public String[] getList() {
    return values;
  }

  /**
   * Returns the selected value of the text field.
   * An empty string is returned if no or multiple values are selected.
   * @return text field value
   */
  public String getValue() {
    final List<String> vals = list.getSelectedValuesList();
    return vals.size() == 1 ? vals.get(0) : "";
  }

  /**
   * Returns all selected values.
   * @return text field value
   */
  public StringList getValues() {
    final StringList sl = new StringList();
    for(final String val : list.getSelectedValuesList()) sl.add(val);
    return sl;
  }

  /**
   * Returns the numeric representation of the user input. If the
   * text field is invalid, the list entry is returned.
   * @return numeric value
   */
  public int getNum() {
    final int i = Strings.toInt(text.getText());
    if(i != Integer.MIN_VALUE) return i;
    final Object value = list.getSelectedValue();
    return value != null ? Strings.toInt(value.toString()) : 0;
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
  private void setIndex(final int i) {
    if(i < values.length) setValue(values[i]);
    else text.setText("");
  }

  @Override
  public void setSize(final int w, final int h) {
    setWidth(w);
    BaseXLayout.setHeight(scroll, h);
  }

  /**
   * Sets the width of the component.
   * @param w width
   */
  public void setWidth(final int w) {
    BaseXLayout.setWidth(text, w);
    BaseXLayout.setWidth(scroll, w);
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
