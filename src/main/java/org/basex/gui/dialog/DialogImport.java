package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.basex.core.Prop;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;
import org.basex.io.IOFile;

/**
 * Panel for importing new database resources.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class DialogImport extends BaseXBack {
  /** User feedback. */
  final BaseXLabel info;
  /** Resource to add. */
  final BaseXTextField input;
  /** Add contents of archives. */
  final BaseXCheckBox archives;
  /** Skip corrupt files. */
  final BaseXCheckBox skip;
  /** Add remaining files as raw files. */
  final BaseXCheckBox raw;
  /** Document filter. */
  final BaseXTextField filter;
  /** Dialog reference. */
  final GUI gui;

  /**
   * Constructor.
   * @param dialog dialog reference
   * @param panel feature panel
   */
  public DialogImport(final Dialog dialog, final BaseXBack panel) {
    gui = dialog.gui;

    layout(new TableLayout(9, 1));
    border(8);

    // add options
    add(new BaseXLabel(CREATETITLE + COL, true, true).border(0, 0, 4, 0));

    final BaseXBack b = new BaseXBack(new TableLayout(1, 2, 8, 0));
    input = new BaseXTextField(gui.gprop.get(GUIProp.CREATEPATH), dialog);
    input.addKeyListener(dialog.keys);
    b.add(input);

    final BaseXButton browse = new BaseXButton(BUTTONBROWSE, dialog);
    browse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final IOFile in = inputFile();
        if(in != null) input.setText(in.path());
      }
    });
    b.add(browse);
    add(b);

    final Prop prop = gui.context.prop;
    skip = new BaseXCheckBox(CREATECORRUPT, prop.is(Prop.SKIPCORRUPT), dialog);
    add(skip);

    archives = new BaseXCheckBox(CREATEARCHIVES, prop.is(Prop.ADDARCHIVES),
        dialog);
    add(archives);

    add(new BaseXLabel(CREATEPATTERN + COL, true, true).border(8, 0, 4, 0));
    filter = new BaseXTextField(prop.get(Prop.CREATEFILTER), dialog);
    add(filter);
    raw = new BaseXCheckBox(CREATERAW, prop.is(Prop.ADDRAW), dialog);
    add(raw);

    // add additional options
    add(panel);

    // add info label
    info = new BaseXLabel(" ").border(24, 0, 4, 0);
    add(info);
  }

  /**
   * Returns the input field path string.
   * @return path
   */
  String input() {
    return input.getText().trim();
  }

  /**
   * Opens a file dialog to choose an XML document or directory.
   * @return chosen file, or {@code null}
   */
  protected IOFile choose() {
    final IOFile in = inputFile();
    if(in != null) input.setText(in.path());
    return in;
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
   * Sets parser/import options.
   */
  public void setOptions() {
    gui.set(Prop.CREATEFILTER, filter.getText());
    gui.set(Prop.ADDARCHIVES, archives.isSelected());
    gui.set(Prop.SKIPCORRUPT, skip.isSelected());
    gui.set(Prop.ADDRAW, raw.isSelected());
  }

  /**
   * Updates the dialog window.
   * @param empty allow empty input
   * @return success flag, or {@code false} if specified input is not found
   */
  protected boolean action(final boolean empty) {
    boolean ok = true;
    info.setText(null, null);

    final String in = input.getText().trim();
    final IO io = IO.get(in);
    gui.gprop.set(GUIProp.CREATEPATH, in);

    ok = empty ? in.isEmpty() || io.exists() : !in.isEmpty() && io.exists();
    final boolean dir = ok && io.isDir();
    filter.setEnabled(dir);
    raw.setEnabled(dir);
    return ok;
  }
}
