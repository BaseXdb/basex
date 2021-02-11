package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.awt.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Dialog window for editing XML nodes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DialogEdit extends BaseXDialog {
  /** Resulting update arguments. */
  public final StringList result = new StringList();
  /** Node kind. */
  public final int kind;
  /** Button panel. */
  private final BaseXBack buttons;
  /** Info label. */
  private final BaseXLabel info;

  /** Text area. */
  private BaseXTextField input1;
  /** Text area. */
  private BaseXTextField input2;
  /** Text area. */
  private TextPanel input3;
  /** Old content. */
  private String old1;
  /** Old content. */
  private String old2;
  /** Old content. */
  private String old3;

  /**
   * Default constructor.
   * @param gui reference to the main frame
   * @param pre pre value
   */
  public DialogEdit(final GUI gui, final int pre) {
    super(gui, EDIT_DATA);

    // create checkboxes
    BaseXBack pp = new BaseXBack(new BorderLayout());

    final Context context = gui.context;
    final Data data = context.data();
    kind = data.kind(pre);

    final String title = Util.info(EDIT_X, NODE_KINDS[kind]);
    final BaseXLabel label = new BaseXLabel(title, true, true);
    pp.add(label, BorderLayout.NORTH);

    if(kind == Data.ELEM) {
      old1 = string(data.name(pre, kind));
    } else if(kind == Data.DOC) {
      old1 = string(data.text(pre, true));
    } else if(kind == Data.TEXT || kind == Data.COMM) {
      old3 = string(data.atom(pre));
    } else if(kind == Data.ATTR) {
      old1 = string(data.name(pre, kind));
      old2 = string(data.atom(pre));
    } else {
      old1 = string(data.name(pre, kind));
      old3 = string(data.atom(pre));
    }
    final BaseXBack b = new BaseXBack(new BorderLayout(0, 4));
    if(old1 != null) {
      input1 = new BaseXTextField(this, old1);
      BaseXLayout.setWidth(input1, 500);
      b.add(input1, BorderLayout.NORTH);
    }
    if(old2 != null) {
      input2 = new BaseXTextField(this, old2);
      b.add(input2, BorderLayout.CENTER);
    }
    if(old3 != null) {
      input3 = new TextPanel(this, old3, true);
      input3.addKeyListener(keys);
      BaseXLayout.setWidth(input3, BaseXTextField.DWIDTH);
      BaseXLayout.setHeight(input3, BaseXTextField.DWIDTH);
      b.add(new SearchEditor(gui, input3), BorderLayout.CENTER);
      setResizable(true);
    }
    pp.add(b, BorderLayout.CENTER);
    set(pp, BorderLayout.CENTER);

    pp = new BaseXBack(new BorderLayout());
    info = new BaseXLabel(" ").border(12, 0, 6, 0);
    pp.add(info, BorderLayout.WEST);

    // create buttons
    buttons = okCancel();
    pp.add(buttons, BorderLayout.EAST);

    set(pp, BorderLayout.SOUTH);
    finish();
  }

  @Override
  public void action(final Object cmp) {
    ok = kind != Data.TEXT || input3.getText().length != 0;
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
      final String text = string(input3.getText());
      result.add(text);
      ok |= !text.equals(old3);
    }
    super.close();
  }
}
