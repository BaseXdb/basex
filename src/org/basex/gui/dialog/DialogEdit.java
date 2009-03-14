package org.basex.gui.dialog;

import static org.basex.Text.*;
import static org.basex.util.Token.*;
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

/**
 * Dialog window for editing XML nodes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
  private BaseXTextField input2;
  /** Text area. */
  private BaseXText input3;
  /** Old content. */
  private String old1;
  /** Old content. */
  private String old2;
  /** Button panel. */
  private final BaseXBack buttons;
  /** Pre value. */
  private final int pre;

  /**
   * Default Constructor.
   * @param main reference to main frame
   * @param p pre value
   */
  public DialogEdit(final GUI main, final int p) {
    super(main, EDITTITLE);
    pre = p;

    // create checkboxes
    final BaseXBack pp = new BaseXBack();
    pp.setLayout(new BorderLayout());

    final Context context = gui.context;
    final Data data = context.data();
    kind = data.kind(pre);

    final String title = BaseX.info(EDITTEXT, EDITKIND[kind]);
    final BaseXLabel label = new BaseXLabel(title, true, true);
    pp.add(label, BorderLayout.NORTH);

    if(kind == Data.ELEM || kind == Data.DOC) {
      final byte[] txt = kind == Data.ELEM ? data.tag(pre) : data.text(pre);
      input = new BaseXTextField(string(txt), null, this);
      old1 = input.getText();
      pp.add(input, BorderLayout.CENTER);
    } else if(kind == Data.TEXT || kind == Data.COMM) {
      setResizable(true);
      input3 = new BaseXText(gui, null, true, this);
      input3.setText(data.text(pre));
      input3.setPreferredSize(new Dimension(400, 200));
      old1 = string(input3.getText());
      pp.add(input3, BorderLayout.CENTER);
    } else {
      if(kind == Data.ATTR) {
        old1 = string(data.attName(pre));
        old2 = string(data.attValue(pre));
      } else {
        old1 = string(data.text(pre));
        old2 = "";
        final int i = old1.indexOf(' ');
        if(i != -1) {
          old2 = old1.substring(i + 1);
          old1 = old1.substring(0, i);
        }
      }
      final BaseXBack b = new BaseXBack();
      b.setLayout(new TableLayout(2, 1, 0, 8));
      input = new BaseXTextField(old1, null, this);
      b.add(input);
      if(kind == Data.ATTR) {
        input2 = new BaseXTextField(old2, null, this);
        b.add(input2);
      } else {
        input3 = new BaseXText(gui, null, true, this);
        input3.setText(token(old2));
        input3.setPreferredSize(new Dimension(400, 200));
        b.add(input3);
      }
      pp.add(b, BorderLayout.CENTER);
    }
    if(input != null) input.selectAll();
    else input3.selectAll();
    set(pp, BorderLayout.CENTER);

    // create buttons
    buttons = BaseXLayout.okCancel(this);
    set(buttons, BorderLayout.SOUTH);
    finish();
  }

  @Override
  public void close() {
    super.close();

    final String in = input != null ? input.getText() :
      string(input3.getText());

    if(kind == Data.ELEM || kind == Data.DOC) {
      if(in.length() == 0 || in.equals(old1)) return;
      result = new String[] { in };
    } else if(kind == Data.TEXT || kind == Data.COMM) {
      if(in.equals(old2)) return;
      result = new String[] { in };
    } else if(kind == Data.ATTR) {
      final String in2 = input2.getText();
      if(in.length() == 0 || in.equals(old1) && in2.equals(old2)) return;
      result = new String[] { in, in2 };
    } else {
      final String in2 = string(input3.getText());
      if(in.length() == 0 || in.equals(old1) && in2.equals(old2)) return;
      result = new String[] { in, in2 };
    }
  }
}
