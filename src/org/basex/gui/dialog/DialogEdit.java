package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXText;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.util.StringList;
import org.basex.util.XMLToken;

/**
 * Dialog window for editing XML nodes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DialogEdit extends Dialog {
  /** Resulting update arguments. */
  public final StringList result = new StringList();
  /** Node kind. */
  public final int kind;
  /** Pre value. */
  private final int pre;

  /** Button panel. */
  private final BaseXBack buttons;
  /** Info label. */
  private final BaseXLabel info;
  /** Text area. */
  private BaseXTextField input1;
  /** Text area. */
  private BaseXTextField input2;
  /** Text area. */
  private BaseXText input3;
  /** Old content. */
  private String old1;
  /** Old content. */
  private String old2;
  /** Old content. */
  private byte[] old3;

  /**
   * Default constructor.
   * @param main reference to main frame
   * @param p pre value
   */
  public DialogEdit(final GUI main, final int p) {
    super(main, EDITTITLE);
    pre = p;

    // create checkboxes
    BaseXBack pp = new BaseXBack();
    pp.setLayout(new BorderLayout());

    final Context context = gui.context;
    final Data data = context.data();
    kind = data.kind(pre);

    final String title = Main.info(EDITTEXT, EDITKIND[kind]);
    final BaseXLabel label = new BaseXLabel(title, true, true);
    pp.add(label, BorderLayout.NORTH);

    if(kind == Data.ELEM) {
      old1 = string(data.tag(pre));
    } else if(kind == Data.DOC) {
      old1 = string(data.text(pre));
    } else if(kind == Data.TEXT || kind == Data.COMM) {
      old3 = data.text(pre);
    } else if(kind == Data.ATTR) {
      old1 = string(data.attName(pre));
      old2 = string(data.attValue(pre));
    } else {
      final String[] v = string(data.text(pre)).split(" ", 2);
      old1 = v[0];
      old3 = v.length == 1 ? EMPTY : token(v[1]);
    }
    final BaseXBack b = new BaseXBack();
    b.setLayout(new TableLayout(2, 1, 0, 4));
    if(old1 != null) {
      input1 = new BaseXTextField(old1, this);
      input1.addKeyListener(keys);
      BaseXLayout.setWidth(input1, 320);
      b.add(input1);
    }
    if(old2 != null) {
      input2 = new BaseXTextField(old2, this);
      input2.addKeyListener(keys);
      b.add(input2);
    }
    if(old3 != null) {
      input3 = new BaseXText(true, this);
      input3.setText(old3);
      input3.addKeyListener(keys);
      input3.setPreferredSize(new Dimension(320, 200));
      b.add(input3);
      setResizable(true);
    }
    pp.add(b, BorderLayout.CENTER);
    set(pp, BorderLayout.CENTER);
    
    pp = new BaseXBack();
    pp.setLayout(new BorderLayout());
    info = new BaseXLabel(" ");
    info.setBorder(8, 0, 2, 0);
    pp.add(info, BorderLayout.WEST);

    // create buttons
    buttons = okCancel(this);
    pp.add(buttons, BorderLayout.EAST);
    
    set(pp, BorderLayout.SOUTH);
    finish(null);
  }

  @Override
  public void action(final String cmd) {
    String msg = null;
    ok = kind != Data.TEXT || input3.getText().length != 0;
    if(kind != Data.TEXT && kind != Data.COMM) {
      ok = XMLToken.isQName(token(input1.getText()));
      if(!ok && input1.getText().length() != 0) msg = "Invalid name";
    }
    info.setError(msg, true);
    enableOK(buttons, BUTTONOK, ok);
  }

  @Override
  public void close() {
    super.close();
    ok = false;
    if(old1 != null) {
      result.add(input1.getText());
      ok |= !input1.getText().equals(old1);
    }
    if(old2 != null) {
      result.add(input2.getText());
      ok |= !input2.getText().equals(old2);
    }
    if(old3 != null) {
      result.add(string(input3.getText()));
      ok |= !eq(input3.getText(), old3);
    }
  }
}
