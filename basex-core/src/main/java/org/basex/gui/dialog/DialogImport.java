package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.util.Strings.*;

import java.io.*;
import java.util.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.*;
import org.basex.gui.layout.BaseXLayout.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Panel for importing new database resources. Embedded by both the database creation and
 * properties dialog.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class DialogImport extends BaseXBack {
  /** User feedback. */
  final BaseXLabel info;
  /** Resource to add. */
  final BaseXCombo input;
  /** Browse button. */
  final BaseXButton browse;
  /** Chosen parser. */
  final BaseXCombo parsers;
  /** DB name. */
  String dbName;

  /** GUI reference. */
  private final GUI gui;
  /** Dialog reference. */
  private final BaseXDialog dialog;
  /** Parsing options. */
  private final DialogParsing parsing;
  /** Add contents of archives. */
  private final BaseXCheckBox addArchives;
  /** Prefix database path with name of archive. */
  private final BaseXCheckBox archiveName;
  /** Skip corrupt files. */
  private final BaseXCheckBox skipCorrupt;
  /** Add remaining files as raw files. */
  private final BaseXCheckBox addRaw;
  /** Document filter. */
  private final BaseXTextField createFilter;

  /**
   * Constructor.
   * @param dialog dialog reference
   * @param panel feature panel
   * @param parsing parsing dialog
   */
  DialogImport(final BaseXDialog dialog, final BaseXBack panel, final DialogParsing parsing) {
    this.dialog = dialog;
    this.parsing = parsing;
    gui = dialog.gui;

    layout(new RowLayout());
    border(8);

    // add options
    add(new BaseXLabel(FILE_OR_DIR + COL, true, true).border(0, 0, 6, 0));

    final String path = gui.gopts.get(GUIOptions.INPUTPATH);
    input = new BaseXCombo(dialog, true).history(GUIOptions.INPUTS, gui.gopts);
    BaseXLayout.setWidth(input, BaseXTextField.DWIDTH);
    input.setText(path);

    final IO io = IO.get(path);
    if(io instanceof IOFile && !path.isEmpty()) dbName = io.dbName();

    browse = new BaseXButton(dialog, BROWSE_D);
    browse.addActionListener(e -> choose());
    final BaseXBack b = new BaseXBack(new ColumnLayout(8));
    b.add(input);
    b.add(browse);
    add(b);

    // add additional options
    add(panel);
    add(Box.createVerticalStrut(12));

    final MainOptions opts = gui.context.options;
    final StringList ps = new StringList();
    for(final MainParser mp : MainParser.values()) ps.add(mp.name());
    parsers = new BaseXCombo(dialog, ps.finish());
    parsers.setSelectedItem(opts.get(MainOptions.PARSER).name());

    createFilter = new BaseXTextField(dialog, opts.get(MainOptions.CREATEFILTER));
    createFilter.setColumns(30);

    addRaw = new BaseXCheckBox(dialog, ADD_RAW_FILES, MainOptions.ADDRAW, opts);
    skipCorrupt = new BaseXCheckBox(dialog, SKIP_CORRUPT_FILES, MainOptions.SKIPCORRUPT, opts);
    addArchives = new BaseXCheckBox(dialog, PARSE_ARCHIVES, MainOptions.ADDARCHIVES, opts);
    archiveName = new BaseXCheckBox(dialog, ADD_ARCHIVE_NAME, MainOptions.ARCHIVENAME, opts);

    final BaseXBack p = new BaseXBack(new TableLayout(2, 2, 20, 0));
    p.add(new BaseXLabel(INPUT_FORMAT, false, true).border(0, 0, 6, 0));
    p.add(new BaseXLabel(FILE_PATTERNS + COL, false, true).border(0, 0, 6, 0));
    p.add(parsers);
    p.add(createFilter);
    add(p);
    add(Box.createVerticalStrut(8));
    add(addRaw);
    add(skipCorrupt);
    add(addArchives);
    add(archiveName);

    // add info label
    info = new BaseXLabel(" ").border(20, 0, 6, 0);
    add(info);

    final DropHandler dh = object -> {
      input.setText(object.toString());
      action(input, dialog instanceof DialogNew);
    };

    BaseXLayout.addDrop(this, dh);
    BaseXLayout.addDrop(input, dh);
  }

  /**
   * Returns the input field path string.
   * @return path
   */
  String input() {
    return input.getText().trim();
  }

  /**
   * Updates the dialog window.
   * @param comp component
   * @param empty allow empty input
   * @return success flag, or {@code false} if specified input is not found
   */
  boolean action(final Object comp, final boolean empty) {
    boolean ok = parsing.action();

    final String in = input.getText().trim();
    final IO io = IO.get(in);
    gui.gopts.set(GUIOptions.INPUTPATH, in);

    final boolean multi = io.isDir() || io.isArchive();
    addArchives.setEnabled(multi);
    createFilter.setEnabled(multi);
    archiveName.setEnabled(addArchives.isSelected());

    final MainParser parser = MainParser.valueOf(parsers.getSelectedItem());
    final boolean raw = parser == MainParser.RAW;
    addRaw.setEnabled(multi && !raw && !gui.context.options.get(MainOptions.MAINMEM));
    skipCorrupt.setEnabled(!raw);

    if(comp == parsers) {
      parsing.setType(parser);
      if(multi) createFilter.setText(raw ? "*" : "*." + parser);
    }

    ok &= empty ? in.isEmpty() || io.exists() : !in.isEmpty() && io.exists();
    if(ok && comp == input) setType(in);

    info.setText(null, null);
    return ok;
  }

  /**
   * Sets the parsing options.
   */
  void setOptions() {
    gui.set(MainOptions.PARSER, MainParser.valueOf(parsers.getSelectedItem()));
    gui.set(MainOptions.CREATEFILTER, createFilter.getText());
    gui.set(MainOptions.ADDARCHIVES, addArchives.isSelected());
    gui.set(MainOptions.ARCHIVENAME, archiveName.isSelected());
    gui.set(MainOptions.SKIPCORRUPT, skipCorrupt.isSelected());
    gui.set(MainOptions.ADDRAW, addRaw.isSelected());
    input.updateHistory();
    parsing.setOptions();
  }

  /**
   * Opens a file dialog to choose an input file or directory,
   * and updates the panel.
   */
  private void choose() {
    String path = input.getText();
    final BaseXFileChooser fc = new BaseXFileChooser(dialog, FILE_OR_DIR, path);
    fc.textFilters().filter(ZIP_ARCHIVES, false, IO.ZIPSUFFIXES);
    final IOFile file = fc.select(Mode.FDOPEN);
    if(file == null) return;

    gui.gopts.setFile(GUIOptions.INPUTPATH, file);
    path = file.path();
    input.setText(path);
    setType(path);
  }

  /**
   * Sets the correct input type.
   * @param in input path
   */
  void setType(final String in) {
    // get file path, update input path and database name
    final IO io = IO.get(in);
    if(!in.isEmpty() && io instanceof IOFile) dbName = io.dbName();

    final boolean dir = io.isDir();
    final boolean archive = io.isArchive();
    if(dir || archive) return;

    // evaluate input type
    MainParser type = null;
    // input type of single file
    final String path = io.path();
    final int i = path.lastIndexOf('.');
    if(i != -1) {
      // analyze file suffix
      final String suf = path.substring(i).toLowerCase(Locale.ENGLISH);
      if(eq(suf, gui.gopts.xmlSuffixes()) || eq(suf, IO.XSLSUFFIXES)) type = MainParser.XML;
      else if(eq(suf, IO.HTMLSUFFIXES)) type = MainParser.HTML;
      else if(eq(suf, IO.CSVSUFFIX)) type = MainParser.CSV;
      else if(eq(suf, IO.TXTSUFFIXES)) type = MainParser.TEXT;
      else if(eq(suf, IO.JSONSUFFIX)) type = MainParser.JSON;
    }
    // unknown suffix: analyze first bytes
    if(type == null) type = guess(io);
    // default parser: XML
    if(type == null) type = MainParser.XML;

    // choose correct parser (default: XML)
    parsers.setSelectedItem(type.name());
  }

  /**
   * Guesses the content type of the specified input.
   * @param in input stream
   * @return type or {@code null}
   */
  private static MainParser guess(final IO in) {
    if(!in.exists() || in instanceof IOUrl) return null;

    try(BufferInput bi = BufferInput.get(in)) {
      int b = bi.read();
      // input starts with opening bracket: may be xml
      if(b == '<') return MainParser.XML;

      for(int c = 0; b >= 0 && ++c < IO.BLOCKSIZE;) {
        // treat as raw data if characters are no ascii
        if(b < ' ' && !Token.ws(b) || b >= 128) return MainParser.RAW;
        b = bi.read();
      }
      // all characters were of type ascii
      return MainParser.TEXT;
    } catch(final IOException ignored) { }
    // could not evaluate type
    return null;
  }
}
