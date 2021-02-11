package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Dialog window for inserting new database nodes.

 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public final class DialogInsert extends BaseXDialog {
  /** Resulting update arguments. */
  public final StringList result = new StringList();
  /** Node kind. */
  public int kind;

  /** Button panel. */
  private final BaseXBack buttons;
  /** Background panel. */
  private final BaseXBack back;
  /** Info label. */
  private final BaseXLabel info;
  /** Text area. */
  private final BaseXTextField input1;
  /** Text area. */
  private final TextPanel input2;
  /** First label. */
  private final BaseXLabel label1;
  /** Second label. */
  private final BaseXLabel label2;
  /** Insert kind selection buttons. */
  private final BaseXRadio[] radio;

  /**
   * Default constructor.
   * @param gui reference to the main window
   */
  public DialogInsert(final GUI gui) {
    super(gui, INSERT_NEW_DATA);

    label1 = new BaseXLabel(NAME + COLS, true, true).border(0, 0, 0, 0);
    label2 = new BaseXLabel(VALUE + COLS, true, true).border(0, 0, 0, 0);

    input1 = new BaseXTextField(this);
    BaseXLayout.setWidth(input1, 500);

    input2 = new TextPanel(this, true);
    input2.addKeyListener(keys);
    BaseXLayout.setWidth(input2, 500);

    final BaseXBack knd = new BaseXBack(new ColumnLayout());
    final ButtonGroup group = new ButtonGroup();

    final ActionListener al = e -> change(e.getSource());

    final int lkind = gui.gopts.get(GUIOptions.LASTINSERT);
    final int nl = NODE_KINDS.length;
    radio = new BaseXRadio[nl];
    for(int i = 1; i < nl; ++i) {
      radio[i] = new BaseXRadio(this, NODE_KINDS[i], false);
      radio[i].addActionListener(al);
      radio[i].setSelected(i == lkind);
      radio[i].addKeyListener(keys);
      group.add(radio[i]);
      knd.add(radio[i]);
    }
    set(knd, BorderLayout.NORTH);

    back = new BaseXBack().border(10, 0, 0, 0);
    set(back, BorderLayout.CENTER);

    final BaseXBack pp = new BaseXBack(new BorderLayout());
    info = new BaseXLabel(" ").border(12, 0, 6, 0);
    pp.add(info, BorderLayout.WEST);

    buttons = okCancel();
    pp.add(buttons, BorderLayout.EAST);
    set(pp, BorderLayout.SOUTH);

    setResizable(true);
    change(radio[lkind]);

    action(null);
    finish();
  }

  /**
   * Activates the specified radio button.
   * @param src button reference
   */
  private void change(final Object src) {
    int n = 0;
    final int rl = radio.length;
    for(int r = 0; r < rl; ++r) {
      if(src == radio[r]) n = r;
    }
    final int h = n == Data.ATTR ? input1.getHeight() : 350;
    input2.setPreferredSize(new Dimension(input2.getPreferredSize().width, h));

    back.removeAll();
    back.layout(new BorderLayout(0, 4));
    if(n != Data.TEXT && n != Data.COMM) {
      final BaseXBack b = new BaseXBack(new BorderLayout(0, 4));
      b.add(label1, BorderLayout.NORTH);
      b.add(input1, BorderLayout.CENTER);
      back.add(b, BorderLayout.NORTH);
    }
    if(n != Data.ELEM) {
      final BaseXBack b = new BaseXBack(new BorderLayout(0, 4));
      b.add(label2, BorderLayout.NORTH);
      b.add(new SearchEditor(gui, input2), BorderLayout.CENTER);
      back.add(b, BorderLayout.CENTER);
    }
    pack();
  }

  @Override
  public void action(final Object cmp) {
    final int nl = NODE_KINDS.length;
    for(int n = 1; n < nl; ++n) {
      if(radio[n].isSelected()) kind = n;
    }
    gui.gopts.set(GUIOptions.LASTINSERT, kind);

    ok = kind != Data.TEXT || input2.getText().length != 0;
    String msg = null;
    if(kind != Data.TEXT && kind != Data.COMM) {
      ok = XMLToken.isQName(token(input1.getText()));
      if(!ok && !input1.getText().isEmpty()) msg = Util.info(INVALID_X, NAME);
    }
    info.setText(msg, Msg.ERROR);
    enableOK(buttons, B_OK, ok);
  }

  @Override
  public void close() {
    final String in1 = input1.getText(), in2 = string(input2.getText());
    switch(kind) {
      case Data.ATTR: case Data.PI:
        result.add(in1).add(in2);
        break;
      case Data.ELEM:
        result.add(in1);
        break;
      case Data.TEXT: case Data.COMM:
        result.add(in2);
        break;
    }
    super.close();
  }
}
