package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.*;

/**
 * Dialog window for changing the used fonts.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogReplace extends BaseXDialog {
  /** Search text. */
  public final BaseXTextField search;
  /** Replace text. */
  public final BaseXTextField replace;
  /** Regular expressions. */
  public final BaseXCheckBox regex;
  /** Case sensitivity. */
  public final BaseXCheckBox casee;

  /** User feedback. */
  private final BaseXLabel info;
  /** Buttons. */
  private final BaseXBack buttons;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogReplace(final GUI main) {
    super(main, REPLACE_TEXT, true);

    buttons = okCancel();

    final GUIProp gprop = main.gprop;
    search = new BaseXTextField(gprop.get(GUIProp.SR_SEARCH), this);
    replace = new BaseXTextField(gprop.get(GUIProp.SR_REPLACE), this);
    regex = new BaseXCheckBox(REGULAR_EXPR, gprop.is(GUIProp.SR_REGEX), this);
    casee = new BaseXCheckBox(MATCH_CASE, gprop.is(GUIProp.SR_CASE), this);
    info = new BaseXLabel(" ").border(8, 0, 16, 0);

    BaseXBack p = new BaseXBack(new TableLayout(5, 1, 8, 2));
    p.add(new BaseXLabel(SEARCH + COLS, false, true));
    p.add(search);
    p.add(new BaseXLabel(REPLACE_WITH + COLS, false, true).border(8, 0, 0, 0));
    p.add(replace);
    p.add(info);
    set(p, BorderLayout.CENTER);

    final BaseXBack options = new BaseXBack(new TableLayout(2, 1));
    options.add(regex);
    options.add(casee);

    p = new BaseXBack(new BorderLayout());
    p.add(options, BorderLayout.WEST);
    p.add(buttons, BorderLayout.EAST);
    set(p, BorderLayout.SOUTH);

    finish(gprop.nums(GUIProp.REPLACELOC));
  }

  @Override
  public void action(final Object comp) {
    final String st = search.getText();
    final String rt = replace.getText();
    final boolean re = regex.isSelected();
    final GUIProp gprop = gui.gprop;
    gprop.set(GUIProp.SR_SEARCH, st);
    gprop.set(GUIProp.SR_REPLACE, rt);
    gprop.set(GUIProp.SR_REGEX, re);
    gprop.set(GUIProp.SR_CASE, casee.isSelected());
    ok = !st.isEmpty();

    String inf = null;
    Msg icon = Msg.ERROR;
    if(re) {
      // check regex
      try {
        Pattern.compile(st);
      } catch(final Exception ex) {
        inf = ex.getMessage().replaceAll(Prop.NL + ".*", "");
        ok = false;
      }
    }
    info.setText(inf, icon);

    enableOK(buttons, B_OK, ok);
  }
}
