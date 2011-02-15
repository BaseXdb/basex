package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.basex.core.Prop;
import org.basex.core.cmd.Add;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;

/**
 * Add document dialog.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Andreas Weiler
 */
public final class DialogAdd extends Dialog {
  /** Document to add. */
  private final BaseXTextField path;
  /** Directory path. */
  private final BaseXTextField target;
  /** Database info. */
  private final BaseXLabel info;
  /** Buttons. */
  private final BaseXBack buttons;
  /** Editable parsing options. */
  final DialogParsing parsing;
  /** Document filter. */
  private final BaseXTextField filter;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogAdd(final GUI main) {
    super(main, GUIADD);

    final BaseXBack p = new BaseXBack(new TableLayout(10, 2, 6, 0)).border(8);
    p.add(new BaseXLabel(CREATETITLE + COL, true, true).border(0, 0, 4, 0));
    p.add(new BaseXLabel());

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

    p.add(new BaseXLabel(CREATEPATTERN + COL, true, true).border(8, 0, 4, 0));
    p.add(new BaseXLabel());

    filter = new BaseXTextField(main.context.prop.get(Prop.CREATEFILTER), this);
    p.add(filter);
    p.add(new BaseXLabel());

    p.add(new BaseXLabel(CREATETARGET, true, true).border(8, 0, 4, 0));
    p.add(new BaseXLabel());

    target = new BaseXTextField("/", this);
    target.addKeyListener(keys);
    p.add(target);
    p.add(new BaseXLabel());

    info = new BaseXLabel(" ").border(18, 0, 0, 0);
    p.add(info);

    parsing = new DialogParsing(this);

    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.addTab(GENERALINFO, p);
    tabs.addTab(PARSEINFO, parsing);
    set(tabs, BorderLayout.CENTER);

    // create buttons
    buttons = okCancel(this);
    set(buttons, BorderLayout.SOUTH);
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
    parsing.action(cmp);
    final String in = path.getText().trim();
    final IO io = IO.get(in);

    final boolean exists = !in.isEmpty() && io.exists();
    final String inf = exists ? "" : PATHWHICH;
    info.setText(null, null);

    if(!inf.isEmpty()) {
      ok = false;
      info.setText(inf, Msg.ERROR);
    }

    filter.setEnabled(exists && io.isDir());
    enableOK(buttons, BUTTONOK, ok);
  }

  /**
   * Returns the add command to be executed.
   * @return add command
   */
  public Add cmd() {
    final String in = path.getText().trim();
    final String to = target.getText().trim();
    parsing.close();
    gui.set(Prop.CREATEFILTER, filter.getText());
    return new Add(in, null, to.isEmpty() ? null : to);
  }
}
