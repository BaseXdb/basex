package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXRadio;
import org.basex.gui.layout.BaseXTextArea;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;

/**
 * Dialog window for inserting new database nodes.

 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Lukas Kircher
 */
public final class DialogInsert extends Dialog {
  /** Resulting query. */
  public String query;
  /** Text area. */
  protected JTextComponent input;
  /** Text area. */
  protected JTextComponent input2;
  /** Insert kind selection buttons. */
  protected BaseXRadio[] radio;
  /** Button panel. */
  private final BaseXBack buttons;

  /**
   * Default Constructor.
   * @param parent parent frame
   */
  public DialogInsert(final JFrame parent) {
    super(parent, INSERTTITLE);

    final BaseXBack pp = new BaseXBack();
    pp.setLayout(new BorderLayout());

    BaseXBack b = new BaseXBack();
    b.setLayout(new TableLayout(5, 1, 0, 8));

    final BaseXLabel label = new BaseXLabel(INSERTNAME, true);
    label.setBorder(0, 0, 0, 0);
    final BaseXLabel label2 = new BaseXLabel(" ", true);
    label2.setBorder(0, 0, 0, 0);
    label2.setEnabled(false);

    input = new BaseXTextField(null, this);
    BaseXLayout.setWidth(input, 320);

    input2 = new BaseXTextArea(null);
    input2.setFont(GUIConstants.mfont);
    input2.setEnabled(false);
    input2.setSelectionStart(0);
    final JScrollPane sp = new JScrollPane(input2);
    sp.setPreferredSize(new Dimension(320, 120));
    setResizable(true);

    final BaseXBack kind = new BaseXBack();
    kind.setLayout(new TableLayout(1, 5));
    final ButtonGroup group = new ButtonGroup();
    
    final ActionListener al = new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final Object src = e.getSource();
        input.setEnabled(src != radio[2] && src != radio[4]);
        label.setEnabled(src != radio[2] && src != radio[4]);
        label.setText(src == radio[2] || src == radio[4] ? "" : INSERTNAME);
        input2.setEnabled(src != radio[1]);
        label2.setEnabled(src != radio[1]);
        label2.setText(src == radio[1] ? "" : INSERTVALUE);
      }
    };

    radio = new BaseXRadio[EDITKIND.length];
    for(int i = 1; i < EDITKIND.length; i++) {
      radio[i] = new BaseXRadio(EDITKIND[i], null, false, this);
      radio[i].addActionListener(al);
      group.add(radio[i]);
      kind.add(radio[i]);
    }
    radio[1].setSelected(true);

    b.add(kind);
    b.add(label);
    b.add(input);
    b.add(label2);
    b.add(sp, BorderLayout.NORTH);
    
    pp.add(b, BorderLayout.CENTER);
    set(pp, BorderLayout.CENTER);

    buttons = BaseXLayout.okCancel(this);
    set(buttons, BorderLayout.SOUTH);
    finish(parent);
  }

  @Override
  public void close() {
    super.close();

    final String in1 = input.getText().replaceAll("\"", "\\\"");
    final String in2 = input2.getText().replaceAll("\"", "\\\"");
    query = null;

    for(int i = 1; i < KINDS.length; i++) {
      if(radio[i].isSelected()) {
        query = KINDS[i];
        if(i != 2 && i != 4) query += " \"" + in1 + "\"";
        if(i != 1) query += " \"" + in2 + "\"";
      }
    }
  }
}
