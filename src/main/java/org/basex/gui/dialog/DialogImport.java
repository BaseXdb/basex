package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.basex.core.Prop;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.io.IO;
import org.basex.io.IOFile;

/**
 * Dialog window for specifying options for creating a new database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class DialogImport extends Dialog {
  /** Database info. */
  protected BaseXLabel info;
  /** Buttons. */
  protected BaseXBack buttons;
  /** Editable parsing options. */
  protected DialogParsing parsing;

  /** Document to add. */
  protected BaseXTextField path;
  /** Add ZIP archives. */
  private BaseXCheckBox archives;
  /** Skip corrupt files. */
  private BaseXCheckBox skip;
  /** Document filter. */
  private BaseXTextField filter;

  /**
   * Default constructor.
   * @param main reference to the main window
   * @param title window title
   */
  public DialogImport(final GUI main, final String title) {
    super(main, title);
  }

  /**
   * Initializes the dialog components.
   * @param p panel to add components to
   */
  protected void init(final BaseXBack p) {
    p.add(new BaseXLabel(CREATETITLE + COL, true, true).border(0, 0, 4, 0));
    p.add(new BaseXLabel());

    path = new BaseXTextField(gui.gprop.get(GUIProp.CREATEPATH), this);
    path.addKeyListener(keys);
    p.add(path);

    final BaseXButton browse = new BaseXButton(BUTTONBROWSE, this);
    browse.setMnemonic();
    browse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { choose(); }
    });
    p.add(browse);

    final Prop prop = gui.context.prop;
    skip = new BaseXCheckBox(CREATECORRUPT, prop.is(Prop.SKIPCORRUPT), this);
    p.add(skip);
    p.add(new BaseXLabel());

    archives = new BaseXCheckBox(CREATEARCHIVES,
        prop.is(Prop.ADDARCHIVES), this);
    p.add(archives);
    p.add(new BaseXLabel());

    p.add(new BaseXLabel(CREATEPATTERN + COL, true, true).border(8, 0, 4, 0));
    p.add(new BaseXLabel());

    filter = new BaseXTextField(prop.get(Prop.CREATEFILTER), this);
    p.add(filter);
    p.add(new BaseXLabel());

    parsing = new DialogParsing(this);
    // create buttons
    buttons = okCancel(this);
    set(buttons, BorderLayout.SOUTH);
  }

  /**
   * Opens a file dialog to choose an XML document or directory.
   * @return chosen file, or {@code null}
   */
  protected IOFile choose() {
    final IOFile input = inputFile();
    if(input != null) path.setText(input.path());
    return input;
  }

  /**
   * Returns an XML file chosen by the user.
   * @return file chooser
   */
  protected IOFile inputFile() {
    final BaseXFileChooser fc = new BaseXFileChooser(CREATETITLE,
        gui.gprop.get(GUIProp.CREATEPATH), gui);
    fc.addFilter(CREATEXMLDESC, IO.XMLSUFFIX);
    fc.addFilter(CREATEHTMLDESC, IO.HTMLSUFFIXES);
    fc.addFilter(CREATECSVDESC, IO.CSVSUFFIX);
    fc.addFilter(CREATETXTDESC, IO.TXTSUFFIX);
    fc.addFilter(CREATEGZDESC, IO.GZSUFFIX);
    fc.addFilter(CREATEZIPDESC, IO.ZIPSUFFIXES);
    final IOFile file = fc.select(BaseXFileChooser.Mode.FDOPEN);
    if(file != null) gui.gprop.set(GUIProp.CREATEPATH, file.path());
    return file;
  }

  /**
   * Updates the dialog window.
   * @param cmp component
   * @param empty allow empty input
   * @return success flag
   */
  protected boolean action(final Object cmp, final boolean empty) {
    ok = true;
    parsing.action(cmp);
    final String in = path.getText().trim();
    final IO io = IO.get(in);
    gui.gprop.set(GUIProp.CREATEPATH, in);

    final boolean valid = empty ? io.exists() || in.isEmpty() :
      !in.isEmpty() && io.exists();
    final String inf = valid ? "" : PATHWHICH;
    info.setText(null, null);

    if(!inf.isEmpty()) {
      ok = false;
      info.setText(inf, Msg.ERROR);
    }
    filter.setEnabled(valid && io.isDir());
    enableOK(buttons, BUTTONOK, ok);
    return ok;
  }

  @Override
  public void close() {
    if(!ok) return;
    super.close();
    gui.set(Prop.CREATEFILTER, filter.getText());
    gui.set(Prop.ADDARCHIVES, archives.isSelected());
    gui.set(Prop.SKIPCORRUPT, skip.isSelected());
    parsing.setOptions();
  }
}