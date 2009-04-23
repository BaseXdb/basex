package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
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
  public final StringList result = new StringList();
  /** Node kind. */
  public int kind;
  
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
  /** Button panel. */
  private final BaseXBack buttons;

  /**
   * Default Constructor.
   * @param main reference to the main window
   */
  public DialogInsert(final GUI main) {
    super(main, INSERTTITLE);

    final BaseXBack pp = new BaseXBack();
    pp.setLayout(new BorderLayout());

    final BaseXBack b = new BaseXBack();
    b.setLayout(new TableLayout(5, 1, 0, 8));

    label1 = new BaseXLabel(INSERTNAME, true, true);
    label1.setBorder(0, 0, 0, 0);
    label2 = new BaseXLabel(" ", true, true);
    label2.setBorder(0, 0, 0, 0);
    label2.setEnabled(false);

    input1 = new BaseXTextField(null);
    BaseXLayout.setWidth(input1, 320);

    input2 = new BaseXText(gui, null);
    input2.setFont(GUIConstants.mfont);
    input2.setPreferredSize(new Dimension(400, 200));
    input2.setEnabled(false);
    setResizable(true);

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

    b.add(knd);
    b.add(label1);
    b.add(input1);
    b.add(label2);
    pp.add(b, BorderLayout.CENTER);

    set(pp, BorderLayout.NORTH);
    set(input2, BorderLayout.CENTER);

    buttons = BaseXLayout.okCancel(this);
    set(buttons, BorderLayout.SOUTH);
    change(radio[lkind]);
    finish(null);
  }
  
  /**
   * Activates the specified radio button.
   * @param src button reference
   */
  void change(final Object src) {
    input1.setEnabled(src != radio[2] && src != radio[4]);
    label1.setEnabled(src != radio[2] && src != radio[4]);
    label1.setText(src == radio[2] || src == radio[4] ? "" : INSERTNAME);
    input2.setEnabled(src != radio[1]);
    label2.setEnabled(src != radio[1]);
    label2.setText(src == radio[1] ? "" : INSERTVALUE);
  }

  @Override
  public void close() {
    super.close();

    final String in1 = input1.getText();
    final String in2 = Token.string(input2.getText());

    for(int i = 1; i < KINDS.length; i++) if(radio[i].isSelected()) kind = i;

    switch(kind) {
      case 0: case 3: case 5:
        result.add(in1);
        result.add(in2);
        break;
      case 1:
        result.add(in1);
        break;
      case 2: case 4:
        result.add(in2);
        break;
    }
    result.add(null);
    lkind = kind;
  }
}
