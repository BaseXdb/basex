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
 * @author BaseX Team 2005-14, BSD License
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
  private byte[] old3;

  /**
   * Default constructor.
   * @param main reference to main frame
   * @param p pre value
   */
  public DialogEdit(final GUI main, final int p) {
    super(main, EDIT_DATA);

    // create checkboxes
    BaseXBack pp = new BaseXBack(new BorderLayout());

    final Context context = gui.context;
    final Data data = context.data();
    kind = data.kind(p);

    final String title = Util.info(EDIT_X, NODE_KINDS[kind]);
    final BaseXLabel label = new BaseXLabel(title, true, true);
    pp.add(label, BorderLayout.NORTH);

    if(kind == Data.ELEM) {
      old1 = string(data.name(p, kind));
    } else if(kind == Data.DOC) {
      old1 = string(data.text(p, true));
    } else if(kind == Data.TEXT || kind == Data.COMM) {
      old3 = data.atom(p);
    } else if(kind == Data.ATTR) {
      old1 = string(data.name(p, kind));
      old2 = string(data.atom(p));
    } else {
      old1 = string(data.name(p, kind));
      old3 = data.atom(p);
    }
    final BaseXBack b = new BaseXBack(new BorderLayout(0, 4));
    if(old1 != null) {
      input1 = new BaseXTextField(old1, this);
      BaseXLayout.setWidth(input1, 500);
      b.add(input1, BorderLayout.NORTH);
    }
    if(old2 != null) {
      input2 = new BaseXTextField(old2, this);
      b.add(input2, BorderLayout.CENTER);
    }
    if(old3 != null) {
      input3 = new TextPanel(old3, true, this);
      input3.addKeyListener(keys);
      input3.setPreferredSize(new Dimension(500, 350));
      b.add(new SearchEditor(main, input3), BorderLayout.CENTER);
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
    finish(null);
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
