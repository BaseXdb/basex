package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXRadio;
import org.basex.gui.layout.BaseXText;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * Dialog window for inserting new database nodes.

 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class DialogInsert extends Dialog {
  /** Remembers the last insertion type. */
  private static int lkind = 1;

  /** Resulting update arguments. */
  public StringList result;
  /** Node kind. */
  public int kind;

  /** Background panel. */
  BaseXBack back;
  /** Text area. */
  BaseXTextField input1;
  /** Text area. */
  BaseXText input2;
  /** First label. */
  BaseXLabel label1;
  /** Second label. */
  BaseXLabel label2;
  /** Insert kind selection buttons. */
  BaseXRadio[] radio;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogInsert(final GUI main) {
    super(main, INSERTTITLE);

    label1 = new BaseXLabel(INSERTNAME, true, true);
    label1.setBorder(0, 0, 0, 0);
    label2 = new BaseXLabel(INSERTVALUE, true, true);
    label2.setBorder(0, 0, 0, 0);

    input1 = new BaseXTextField(null, this);
    BaseXLayout.setWidth(input1, 320);

    input2 = new BaseXText(null, true, this);
    input2.setFont(GUIConstants.mfont);
    BaseXLayout.setWidth(input2, 320);

    final BaseXBack knd = new BaseXBack();
    knd.setLayout(new TableLayout(1, 5));
    final ButtonGroup group = new ButtonGroup();

    final ActionListener al = new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        change(e.getSource());
      }
    };

    radio = new BaseXRadio[EDITKIND.length];
    for(int i = 1; i < EDITKIND.length; i++) {
      radio[i] = new BaseXRadio(EDITKIND[i], null, false, this);
      radio[i].addActionListener(al);
      radio[i].setSelected(i == lkind);
      group.add(radio[i]);
      knd.add(radio[i]);
    }
    set(knd, BorderLayout.NORTH);

    back = new BaseXBack();
    back.setBorder(10, 0, 0, 0);

    set(back, BorderLayout.CENTER);
    set(okCancel(this), BorderLayout.SOUTH);

    setResizable(true);
    change(radio[lkind]);
    finish(null);
  }

  /**
   * Activates the specified radio button.
   * @param src button reference
   */
  void change(final Object src) {
    int n = 0;
    for(int r = 0; r < radio.length; r++) if(src == radio[r]) n = r;
    BaseXLayout.setHeight(input2, n == Data.ATTR ? 25 : 200);

    back.removeAll();
    back.setLayout(new TableLayout(
        n == Data.ATTR || n == Data.PI ? 4 : 2, 1, 0, 8));
    if(n != Data.TEXT && n != Data.COMM) {
      back.add(label1);
      back.add(input1);
    }
    if(n != Data.ELEM) {
      back.add(label2);
      back.add(input2);
    }
    pack();
  }

  @Override
  public void close() {
    super.close();

    final String in1 = input1.getText();
    final String in2 = Token.string(input2.getText());

    for(int i = 1; i < EDITKIND.length; i++) if(radio[i].isSelected()) kind = i;

    result = new StringList();
    switch(kind) {
      case Data.ATTR: case Data.PI:
        result.add(in1);
        result.add(in2);
        break;
      case Data.ELEM:
        result.add(in1);
        break;
      case Data.TEXT: case Data.COMM:
        result.add(in2);
        break;
    }
    lkind = kind;
  }
}
