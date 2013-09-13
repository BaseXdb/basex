package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.editor.*;
import org.basex.gui.layout.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Dialog window for inserting new database nodes.

 * @author BaseX Team 2005-12, BSD License
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
  private final Editor input2;
  /** First label. */
  private final BaseXLabel label1;
  /** Second label. */
  private final BaseXLabel label2;
  /** Insert kind selection buttons. */
  private final BaseXRadio[] radio;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogInsert(final GUI main) {
    super(main, INSERT_NEW_DATA);

    label1 = new BaseXLabel(NAME + COLS, true, true).border(0, 0, 0, 0);
    label2 = new BaseXLabel(VALUE + COLS, true, true).border(0, 0, 0, 0);

    input1 = new BaseXTextField(this);
    BaseXLayout.setWidth(input1, 500);

    input2 = new Editor(true, this);
    input2.addKeyListener(keys);
    BaseXLayout.setWidth(input2, 500);

    final BaseXBack knd = new BaseXBack(new TableLayout(1, 5));
    final ButtonGroup group = new ButtonGroup();

    final ActionListener al = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        change(e.getSource());
      }
    };

    final int lkind = gui.gprop.num(GUIProp.LASTINSERT);
    radio = new BaseXRadio[NODE_KINDS.length];
    for(int i = 1; i < NODE_KINDS.length; ++i) {
      radio[i] = new BaseXRadio(NODE_KINDS[i], false, this);
      radio[i].addActionListener(al);
      radio[i].setSelected(i == lkind);
      radio[i].addKeyListener(keys);
      group.add(radio[i]);
      knd.add(radio[i]);
    }
    set(knd, BorderLayout.NORTH);

    back = new BaseXBack(10, 0, 0, 0);
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
    finish(null);
  }

  /**
   * Activates the specified radio button.
   * @param src button reference
   */
  void change(final Object src) {
    int n = 0;
    for(int r = 0; r < radio.length; ++r) if(src == radio[r]) n = r;
    BaseXLayout.setHeight(input2, n == Data.ATTR ? 25 : 350);

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
    for(int i = 1; i < NODE_KINDS.length; ++i) {
      if(radio[i].isSelected()) kind = i;
    }
    gui.gprop.set(GUIProp.LASTINSERT, kind);

    String msg = null;
    ok = kind != Data.TEXT || input2.getText().length != 0;
    if(kind != Data.TEXT && kind != Data.COMM) {
      ok = XMLToken.isQName(token(input1.getText()));
      if(!ok && !input1.getText().isEmpty()) msg = Util.info(INVALID_X, NAME);
    }
    info.setText(msg, Msg.ERROR);
    enableOK(buttons, B_OK, ok);
  }

  @Override
  public void close() {
    super.close();

    final String in1 = input1.getText();
    final String in2 = string(input2.getText());
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
  }
}
