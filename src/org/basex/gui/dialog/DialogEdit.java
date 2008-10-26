package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import org.basex.BaseX;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXText;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.util.Token;

/**
 * Dialog window for editing XML nodes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogEdit extends Dialog {
  /** Resulting update arguments. */
  public String[] result;
  /** Node kind. */
  public final int kind;
  
  /** Text area. */
  private BaseXTextField input;
  /** Text area. */
  private BaseXText input3;
  /** Text area. */
  private BaseXText input2;
  /** Old content. */
  private final String old1;
  /** Old content. */
  private String old2;
  /** Button panel. */
  private final BaseXBack buttons;
  /** Pre value. */
  private final int pre;

  /**
   * Default Constructor.
   * @param gui reference to main frame
   * @param p pre value
   */
  public DialogEdit(final GUI gui, final int p) {
    super(gui, EDITTITLE);
    pre = p;

    // create checkboxes
    final BaseXBack pp = new BaseXBack();
    pp.setLayout(new BorderLayout());

    final Context context = GUI.context;
    final Data data = context.data();
    kind = data.kind(pre);

    final String title = BaseX.info(EDITTEXT, EDITKIND[kind]);
    final BaseXLabel label = new BaseXLabel(title, true, true);
    pp.add(label, BorderLayout.NORTH);

    if(kind == Data.ELEM || kind == Data.DOC) {
      final byte[] txt = kind == Data.ELEM ? data.tag(pre) : data.text(pre);
      input = new BaseXTextField(Token.string(txt), null, this);
      old1 = input.getText();
      pp.add(input, BorderLayout.CENTER);
    } else if(kind == Data.TEXT || kind == Data.COMM) {
      setResizable(true);
      input3 = new BaseXText(null, true, this);
      input3.setText(data.text(pre));
      input3.setPreferredSize(new Dimension(400, 200));
      old1 = Token.string(input3.getText());
      pp.add(input3, BorderLayout.CENTER);
    } else {
      byte[][] atts = new byte[2][];
      if(kind == Data.ATTR) {
        atts[0] = data.attName(pre);
        atts[1] = data.attValue(pre);
      } else {
        atts[0] = data.text(pre);
        atts[1] = Token.EMPTY;
        if(Token.contains(atts[0], ' ')) atts = Token.split(atts[0], ' ');
      }
      final BaseXBack b = new BaseXBack();
      b.setLayout(new TableLayout(2, 1, 0, 8));
      input = new BaseXTextField(Token.string(atts[0]), null, this);
      old1 = input.getText();
      b.add(input);
      input2 = new BaseXText(null);
      input2.setText(atts[1]);
      old2 = Token.string(atts[1]);
      b.add(input2);
      pp.add(b, BorderLayout.CENTER);
    }
    if(input != null) input.selectAll();
    else input3.selectAll();
    set(pp, BorderLayout.CENTER);

    // create buttons
    buttons = BaseXLayout.okCancel(this);
    set(buttons, BorderLayout.SOUTH);
    finish(gui);
  }

  @Override
  public void close() {
    super.close();

    final String in = input != null ? input.getText() :
      Token.string(input3.getText());

    if(kind == Data.ELEM || kind == Data.DOC) {
      if(in.length() == 0 || in.equals(old1)) return;
      result = new String[] { in };
    } else if(kind == Data.TEXT || kind == Data.COMM) {
      if(in.equals(old2)) return;
      result = new String[] { in };
    } else {
      final String in2 = Token.string(input2.getText());
      if(in.length() == 0 || in.equals(old1) && in2.equals(old2)) return;
      result = new String[] { in, in2 };
    }
  }
}
