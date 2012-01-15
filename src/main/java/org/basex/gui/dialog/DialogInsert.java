package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXRadio;
import org.basex.gui.layout.BaseXEditor;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.util.Util;
import org.basex.util.XMLToken;
import org.basex.util.list.StringList;

/**
 * Dialog window for inserting new database nodes.

 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class DialogInsert extends Dialog {
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
  private final BaseXEditor input2;
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
    super(main, INSERTTITLE);

    label1 = new BaseXLabel(EDITNAME + COLS, true, true).border(0, 0, 0, 0);
    label2 = new BaseXLabel(EDITVALUE + COLS, true, true).border(0, 0, 0, 0);

    input1 = new BaseXTextField(this);
    input1.addKeyListener(keys);
    BaseXLayout.setWidth(input1, 320);

    input2 = new BaseXEditor(true, this);
    input2.setFont(GUIConstants.mfont);
    input2.addKeyListener(keys);
    BaseXLayout.setWidth(input2, 320);

    final BaseXBack knd = new BaseXBack(new TableLayout(1, 5));
    final ButtonGroup group = new ButtonGroup();

    final ActionListener al = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        change(e.getSource());
      }
    };

    final int lkind = gui.gprop.num(GUIProp.LASTINSERT);
    radio = new BaseXRadio[EDITKIND.length];
    for(int i = 1; i < EDITKIND.length; ++i) {
      radio[i] = new BaseXRadio(EDITKIND[i], false, this);
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
    info = new BaseXLabel(" ").border(8, 0, 2, 0);
    pp.add(info, BorderLayout.WEST);

    buttons = okCancel(this);
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
    BaseXLayout.setHeight(input2, n == Data.ATTR ? 25 : 200);

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
      b.add(input2, BorderLayout.CENTER);
      back.add(b, BorderLayout.CENTER);
    }
    pack();
  }

  @Override
  public void action(final Object cmp) {
    for(int i = 1; i < EDITKIND.length; ++i) if(radio[i].isSelected()) kind = i;
    gui.gprop.set(GUIProp.LASTINSERT, kind);

    String msg = null;
    ok = kind != Data.TEXT || input2.getText().length != 0;
    if(kind != Data.TEXT && kind != Data.COMM) {
      ok = XMLToken.isQName(token(input1.getText()));
      if(!ok && !input1.getText().isEmpty()) msg = Util.info(INVALID, EDITNAME);
    }
    info.setText(msg, Msg.ERROR);
    enableOK(buttons, BUTTONOK, ok);
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
