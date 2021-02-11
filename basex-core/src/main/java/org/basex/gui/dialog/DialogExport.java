package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.*;
import org.basex.io.*;

/**
 * Dialog window for changing some project's preferences.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DialogExport extends BaseXDialog {
  /** Directory path. */
  private final BaseXCombo path;
  /** Database info. */
  private final BaseXLabel info;
  /** Serialization parameters. */
  private final BaseXSerial serial;
  /** Buttons. */
  private final BaseXBack buttons;

  /**
   * Default constructor.
   * @param gui reference to the main window
   */
  public DialogExport(final GUI gui) {
    super(gui, EXPORT);

    // create checkboxes
    final BaseXBack p = new BaseXBack(new RowLayout());
    p.add(new BaseXLabel(OUTPUT_DIR + COL, true, true).border(0, 0, 6, 0));

    // output label
    BaseXBack pp = new BaseXBack(new ColumnLayout(8));

    path = new BaseXCombo(this, true).history(GUIOptions.INPUTS, gui.gopts);
    BaseXLayout.setWidth(path, BaseXTextField.DWIDTH);
    path.setText(gui.gopts.get(GUIOptions.INPUTPATH));
    pp.add(path);

    final BaseXButton browse = new BaseXButton(this, BROWSE_D);
    browse.addActionListener(e -> choose());
    pp.add(browse);
    p.add(pp);

    serial = new BaseXSerial(this, gui.context.options.get(MainOptions.EXPORTER));
    serial.border(8, 0, 6, 0);
    p.add(serial);
    info = new BaseXLabel(" ").border(8, 0, 0, 0);
    p.add(info);

    // indentation
    set(p, BorderLayout.CENTER);

    // buttons
    pp = new BaseXBack(new BorderLayout());
    buttons = okCancel();
    pp.add(buttons, BorderLayout.EAST);
    set(pp, BorderLayout.SOUTH);

    action(serial);
    finish();
  }

  /**
   * Opens a file dialog to choose an XML document or directory.
   */
  private void choose() {
    final IOFile io = new BaseXFileChooser(this, CHOOSE_DIR, path.getText()).select(Mode.DOPEN);
    if(io != null) path.setText(io.path());
  }

  /**
   * Returns the chosen XML file or directory path.
   * @return file or directory
   */
  public String path() {
    return path.getText().trim();
  }

  @Override
  public void action(final Object comp) {
    final String pth = path();
    final IOFile file = new IOFile(pth);
    ok = !pth.isEmpty();
    if(ok) gui.gopts.setFile(GUIOptions.INPUTPATH, file);

    final String text = file.isDir() && file.children().length > 0 ? DIR_NOT_EMPTY : null;
    info.setText(text, ok ? Msg.WARN : Msg.ERROR);
    enableOK(buttons, B_OK, ok);
  }

  @Override
  public void close() {
    if(!ok) return;

    gui.set(MainOptions.EXPORTER, serial.options());
    path.updateHistory();
    super.close();
  }
}
