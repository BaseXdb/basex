package org.basex.gui.layout;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.basex.core.Prop;
import org.basex.gui.GUIProp;
import org.basex.gui.dialog.Dialog;
import org.basex.io.IO;
import org.basex.io.IOFile;

/**
 * Options panel for adding XML.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class BaseXAddOptions extends BaseXBack {

  /** Document to add. */
  private BaseXTextField inputFld;
  /** Add ZIP archives. */
  private BaseXCheckBox archives;
  /** Skip corrupt files. */
  private BaseXCheckBox skip;
  /** Document filter. */
  private BaseXTextField filter;

  /** Dialog reference. */
  final Dialog dialog;

  /**
   * Constructor.
   * @param d dialog ref
   */
  public BaseXAddOptions(final Dialog d) {
    dialog = d;

    final BaseXBack p = new BaseXBack(new TableLayout(11, 2, 8, 0)).border(8);
    p.add(new BaseXLabel(CREATETITLE + COL, true, true).border(0, 0, 4, 0));
    p.add(new BaseXLabel());

    inputFld = new BaseXTextField(dialog.gui.gprop.get(GUIProp.CREATEPATH),
        dialog);
    inputFld.addKeyListener(dialog.keys);
    p.add(inputFld);

    final BaseXButton browse = new BaseXButton(BUTTONBROWSE, dialog);
    browse.setMnemonic();
    browse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        choose();
      }
    });
    p.add(browse);

    final Prop prop = dialog.gui.context.prop;
    skip = new BaseXCheckBox(CREATECORRUPT, prop.is(Prop.SKIPCORRUPT), dialog);
    p.add(skip);
    p.add(new BaseXLabel());

    archives = new BaseXCheckBox(CREATEARCHIVES, prop.is(Prop.ADDARCHIVES),
        dialog);
    p.add(archives);
    p.add(new BaseXLabel());

    p.add(new BaseXLabel(CREATEPATTERN + COL, true, true).border(8, 0, 4, 0));
    p.add(new BaseXLabel());

    filter = new BaseXTextField(prop.get(Prop.CREATEFILTER), dialog);
    p.add(filter);
    p.add(new BaseXLabel());

    final BaseXBack all = new BaseXBack(new BorderLayout(10, 10));
    all.add(p, BorderLayout.NORTH);

    add(all);
  }

  /**
   * Returns the input field path string.
   * @return path
   */
  public String getInput() {
    return inputFld.getText().trim();
  }

  /**
   * Opens a file dialog to choose an XML document or directory.
   * @return chosen file, or {@code null}
   */
  protected IOFile choose() {
    final IOFile input = inputFile();
    if(input != null) inputFld.setText(input.path());
    return input;
  }

  /**
   * Returns an XML file chosen by the user.
   * @return file chooser
   */
  protected IOFile inputFile() {
    final BaseXFileChooser fc = new BaseXFileChooser(CREATETITLE,
        dialog.gui.gprop.get(GUIProp.CREATEPATH), dialog.gui);
    fc.addFilter(CREATEXMLDESC, IO.XMLSUFFIX);
    fc.addFilter(CREATEHTMLDESC, IO.HTMLSUFFIXES);
    fc.addFilter(CREATECSVDESC, IO.CSVSUFFIX);
    fc.addFilter(CREATETXTDESC, IO.TXTSUFFIX);
    fc.addFilter(CREATEGZDESC, IO.GZSUFFIX);
    fc.addFilter(CREATEZIPDESC, IO.ZIPSUFFIXES);
    final IOFile file = fc.select(BaseXFileChooser.Mode.FDOPEN);
    if(file != null) dialog.gui.gprop.set(GUIProp.CREATEPATH, file.path());
    return file;
  }

  /**
   * Sets parser/import options.
   */
  public void setOptions() {
    dialog.gui.set(Prop.CREATEFILTER, filter.getText());
    dialog.gui.set(Prop.ADDARCHIVES, archives.isSelected());
    dialog.gui.set(Prop.SKIPCORRUPT, skip.isSelected());
  }
}
