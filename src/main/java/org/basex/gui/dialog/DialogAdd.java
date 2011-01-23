package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.basex.core.cmd.Add;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;

/**
 * Add document dialog.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Andreas Weiler
 */
public class DialogAdd extends Dialog {
  /** Document to add. */
  private final BaseXTextField path;
  /** Directory path. */
  private final BaseXTextField target;
  /** Database info. */
  private final BaseXLabel info;
  /** Buttons. */
  private final BaseXBack buttons;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogAdd(final GUI main) {
    super(main, GUIADD);

    BaseXBack p = new BaseXBack(new TableLayout(4, 2, 6, 0));
    p.add(new BaseXLabel(CREATETITLE + COL, true, true).border(0, 0, 4, 0));
    p.add(new BaseXLabel());
    p.border(0, 0, 8, 0);

    final IO in = IO.get(gui.gprop.get(GUIProp.CREATEPATH));
    path = new BaseXTextField(in.dir(), this);
    path.addKeyListener(keys);
    p.add(path);

    final BaseXButton browse = new BaseXButton(BUTTONBROWSE, this);
    browse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { choose(); }
    });
    p.add(browse);

    p.add(new BaseXLabel(CREATETARGET, true, true).border(8, 0, 4, 0));
    p.add(new BaseXLabel());

    target = new BaseXTextField("/", this);
    target.addKeyListener(keys);
    p.add(target);
    p.add(new BaseXLabel());

    set(p, BorderLayout.CENTER);

    // create buttons
    p = new BaseXBack(new BorderLayout());
    info = new BaseXLabel(" ").border(18, 0, 0, 0);
    p.add(info, BorderLayout.WEST);
    buttons = okCancel(this);
    p.add(buttons, BorderLayout.EAST);
    set(p, BorderLayout.SOUTH);

    action(null);
    finish(null);
  }

  /**
   * Opens a file dialog to choose an XML document or directory.
   */
  void choose() {
    final IO io = new BaseXFileChooser(DIALOGFC, path.getText(), gui).
      select(BaseXFileChooser.Mode.FDOPEN);
    if(io != null) path.setText(io.path());
    action(null);
  }

  @Override
  public void action(final Object cmp) {
    ok = true;

    final String in = path.getText().trim();
    final IO io = IO.get(in);

    final boolean exists = !in.isEmpty() && io.exists();
    final String inf = exists ? "" : PATHWHICH;
    info.setText(null, null);

    if(!inf.isEmpty()) {
      ok = false;
      info.setText(inf, Msg.ERROR);
    }
    enableOK(buttons, BUTTONOK, ok);
  }

  /**
   * Returns the add command to be executed.
   * @return add command
   */
  public Add cmd() {
    final String in = path.getText().trim();
    final String to = target.getText().trim();
    return new Add(in, null, to.isEmpty() ? null : to);
  }
}
