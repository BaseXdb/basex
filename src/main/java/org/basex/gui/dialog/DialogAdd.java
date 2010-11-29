package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.basex.core.Command;
import org.basex.core.cmd.Add;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;
import org.basex.util.Util;

/**
 * Add document dialog.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public class DialogAdd extends Dialog {
  /** Document to add. */
  private final BaseXTextField file;
  /** Directory path. */
  private final BaseXTextField path;
  /** Name of document. */
  private final BaseXTextField name;
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

    final BaseXBack pp = new BaseXBack(new TableLayout(7, 2, 0, 4));
    pp.add(new BaseXLabel(CREATETITLE));
    pp.add(new BaseXLabel(" "));
    file = new BaseXTextField(this);
    pp.add(file);
    final BaseXButton browse = new BaseXButton(BUTTONBROWSE, this);
    browse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { choose(); }
    });
    pp.add(browse);

    pp.add(new BaseXLabel("Name: "));
    pp.add(new BaseXLabel(" "));
    name = new BaseXTextField(this);
    name.addKeyListener(keys);
    pp.add(name);
    pp.add(new BaseXLabel(" "));
    pp.add(new BaseXLabel("Target Path: "));
    pp.add(new BaseXLabel(" "));
    path = new BaseXTextField(this);
    path.addKeyListener(keys);
    pp.add(path);

    set(pp, BorderLayout.CENTER);

    // create buttons
    final BaseXBack p = new BaseXBack(new BorderLayout());
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
    if(io != null) file.setText(io.path());
  }

  @Override
  public void action(final Object cmp) {
    ok = true;
    final IO io = IO.get(file.getText());
    final boolean exists = !file.getText().isEmpty() && io.exists();
    String inf = !exists ? PATHWHICH : "";
    info.setText(null, null);
    if(!name.getText().isEmpty() &&
        !Command.validName(name.getText())) inf = Util.info(INVALID, EDITNAME);
    if(!inf.equals("")) {
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
    final String in = file.getText().trim();
    final String as = name.getText().trim();
    final String to = path.getText().trim();
    return new Add(in, as.isEmpty() ? null : as, to.isEmpty() ? null : to);
  }
}
