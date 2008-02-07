package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;
import org.basex.BaseX;
import org.basex.core.Context;
import org.basex.core.proc.Insert;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTextArea;
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
  /** Resulting query. */
  public String query;
  /** Text area. */
  private JTextComponent input;
  /** Text area. */
  private JTextComponent input2;
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
    final int kind = data.kind(pre);

    final String title = BaseX.info(EDITTEXT, EDITKIND[kind]);
    final BaseXLabel label = new BaseXLabel(title, true);
    pp.add(label, BorderLayout.NORTH);

    if(kind == Data.ELEM || kind == Data.DOC) {
      input = new BaseXTextField(Token.string(data.tag(pre)), null, this);
      pp.add(input, BorderLayout.CENTER);
    } else if(kind == Data.TEXT || kind == Data.COMM) {
      setResizable(true);
      input = new BaseXTextArea(Token.string(data.text(pre)), null, this);
      input.setFont(GUIConstants.mfont);
      final JScrollPane sp = new JScrollPane(input);
      sp.setPreferredSize(new Dimension(400, 200));
      pp.add(sp, BorderLayout.CENTER);
    } else {
      String[] atts = new String[2];
      if(kind == Data.ATTR) {
        atts[0] = Token.string(data.attName(pre));
        atts[1] = Token.string(data.attValue(pre));
      } else {
        atts[0] = Token.string(data.text(pre));
        atts[1] = "";
        if(atts[0].contains(" ")) atts = atts[0].split(" ", 2);
      }
      final BaseXBack b = new BaseXBack();
      b.setLayout(new TableLayout(2, 1, 0, 8));
      input = new BaseXTextField(atts[0], null, this);
      b.add(input);
      input2 = new BaseXTextField(atts[1], null, this);
      old2 = atts[1];
      b.add(input2);
      pp.add(b, BorderLayout.CENTER);
    }
    input.selectAll();
    old1 = input.getText();
    set(pp, BorderLayout.CENTER);

    // create buttons
    buttons = BaseXLayout.okCancel(this);
    set(buttons, BorderLayout.SOUTH);
    finish(gui);
  }

  @Override
  public void close() {
    super.close();
    query = null;

    final Context context = GUI.context;
    final Data data = context.data();
    final int kind = data.kind(pre);
    final String in = input.getText().replaceAll("\"", "\\\"");

    if(kind == Data.ELEM || kind == Data.DOC) {
      if(in.length() == 0 || in.equals(old1)) return;
      query = Insert.ELEM + " \"" + in + "\"";
    } else if(kind == Data.TEXT || kind == Data.COMM) {
      if(in.equals(old2)) return;
      query = Insert.KINDS[kind] + " \"" + in + "\"";
    } else {
      final String in2 = input2.getText();
      if(in.length() == 0 || in.equals(old1) && in2.equals(old2)) return;
      query = Insert.KINDS[kind] + " \"" + in + "\" \"" +
        in2.replaceAll("\"", "\\\"") + "\"";
    }
  }
}
