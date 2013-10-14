package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.gui.layout.BaseXLayout.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Panel for importing new database resources.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public final class DialogImport extends BaseXBack {
  /** Available parsers. */
  private static final String[] PARSING = {
    DataText.M_XML, DataText.M_HTML, DataText.M_JSON,
    DataText.M_CSV, DataText.M_TEXT, DataText.M_RAW
  };

  /** User feedback. */
  final BaseXLabel info;
  /** Resource to add. */
  final BaseXTextField input;
  /** Browse button. */
  final BaseXButton browse;
  /** Parser. */
  final BaseXCombo parser;
  /** DB name. */
  String dbname;

  /** Dialog reference. */
  private final GUI gui;
  /** Parsing options. */
  private final DialogParsing parsing;
  /** Add contents of archives. */
  private final BaseXCheckBox archives;
  /** Skip corrupt files. */
  private final BaseXCheckBox skipCorrupt;
  /** Add remaining files as raw files. */
  private final BaseXCheckBox addRaw;
  /** Document filter. */
  private final BaseXTextField filter;

  /**
   * Constructor.
   * @param dial dialog reference
   * @param panel feature panel
   * @param parse parsing dialog
   */
  public DialogImport(final BaseXDialog dial, final BaseXBack panel, final DialogParsing parse) {
    gui = dial.gui;
    parsing = parse;

    layout(new TableLayout(10, 1));
    border(8);

    // add options
    add(new BaseXLabel(FILE_OR_DIR + COL, true, true).border(0, 0, 6, 0));

    final String path = gui.gopts.get(GUIOptions.INPUTPATH);
    input = new BaseXTextField(path, dial);
    input.history(gui, GUIOptions.INPUTS);

    final IO io = IO.get(path);
    if(io instanceof IOFile && !path.isEmpty()) dbname = io.dbname();

    browse = new BaseXButton(BROWSE_D, dial);
    browse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { choose(); }
    });
    final BaseXBack b = new BaseXBack(new TableLayout(1, 2, 8, 0));
    b.add(input);
    b.add(browse);
    add(b);

    // add additional options
    add(panel);
    add(Box.createVerticalStrut(12));

    final MainOptions opts = gui.context.options;
    final StringList parsers = new StringList(PARSING.length);
    for(final String p : PARSING) parsers.add(p.toUpperCase(Locale.ENGLISH));

    parser = new BaseXCombo(dial, parsers.toArray());
    parser.setSelectedItem(opts.get(MainOptions.PARSER).toUpperCase(Locale.ENGLISH));
    filter = new BaseXTextField(opts.get(MainOptions.CREATEFILTER), dial);
    BaseXLayout.setWidth(filter, 200);

    addRaw = new BaseXCheckBox(ADD_RAW_FILES, MainOptions.ADDRAW, opts, dial);
    skipCorrupt = new BaseXCheckBox(SKIP_CORRUPT_FILES, MainOptions.SKIPCORRUPT, opts, dial);
    archives = new BaseXCheckBox(PARSE_ARCHIVES, MainOptions.ADDARCHIVES, opts, dial);

    final BaseXBack p = new BaseXBack(new TableLayout(2, 2, 20, 0));
    p.add(new BaseXLabel(INPUT_FORMAT, false, true).border(0, 0, 6, 0));
    p.add(new BaseXLabel(FILE_PATTERNS + COL, false, true).border(0, 0, 6, 0));
    p.add(parser);
    p.add(filter);
    add(p);
    add(Box.createVerticalStrut(8));
    add(addRaw);
    add(skipCorrupt);
    add(archives);

    // add info label
    info = new BaseXLabel(" ").border(32, 0, 6, 0);
    add(info);

    final DropHandler dh = new DropHandler() {
      @Override
      public void drop(final Object object) {
        input.setText(object.toString());
        action(input, dial instanceof DialogNew);
      }
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
   * Returns an XML file chosen by the user.
   * @return file chooser
   */
  IOFile inputFile() {
    final String path = gui.gopts.get(GUIOptions.INPUTPATH);
    final BaseXFileChooser fc = new BaseXFileChooser(FILE_OR_DIR, path, gui);
    fc.textFilters();
    fc.filter(ZIP_ARCHIVES, IO.ZIPSUFFIXES);
    final IOFile file = fc.select(Mode.FDOPEN);
    if(file != null) gui.gopts.set(GUIOptions.INPUTPATH, file.path());
    return file;
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

    boolean multi = io.isDir() || io.isArchive();
    archives.setEnabled(multi);
    multi &= archives.isSelected();
    filter.setEnabled(multi);

    final String type = parser();
    final boolean raw = type.equals(DataText.M_RAW);
    addRaw.setEnabled(multi && !raw && !gui.context.options.get(MainOptions.MAINMEM));
    skipCorrupt.setEnabled(!raw);

    if(comp == parser) {
      parsing.setType(type);
      if(multi) filter.setText(raw ? "*" : "*." + type);
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
    final String type = parser();
    gui.set(MainOptions.PARSER, type);
    gui.set(MainOptions.CREATEFILTER, filter.getText());
    gui.set(MainOptions.ADDARCHIVES, archives.isSelected());
    gui.set(MainOptions.SKIPCORRUPT, skipCorrupt.isSelected());
    gui.set(MainOptions.ADDRAW, addRaw.isSelected());
    input.store();
    parsing.setOptions();
  }

  /**
   * Opens a file dialog to choose an input file or directory,
   * and updates the panel.
   */
  void choose() {
    // get user input (may be canceled)
    final IOFile in = inputFile();
    if(in == null) return;
    input.setText(in.path());
    setType(in.path());
  }

  /**
   * Sets the correct input type.
   * @param in input path
   */
  void setType(final String in) {
    // get file path, update input path and database name
    final IO io = IO.get(in);
    if(!in.isEmpty() && io instanceof IOFile) dbname = io.dbname();

    final boolean dir = io.isDir();
    final boolean archive = io.isArchive();
    if(dir || archive) {
      return;
      //filter.setText('*' + IO.XMLSUFFIX);
    }

    // evaluate input type
    String type = null;
    // input type of single file
    final String path = io.path();
    final int i = path.lastIndexOf('.');
    if(i != -1) {
      // analyze file suffix
      final String suf = path.substring(i).toLowerCase(Locale.ENGLISH);
      if(Token.eq(suf, IO.XMLSUFFIXES)) type = DataText.M_XML;
      else if(Token.eq(suf, IO.XSLSUFFIXES)) type = DataText.M_XML;
      else if(Token.eq(suf, IO.HTMLSUFFIXES)) type = DataText.M_HTML;
      else if(Token.eq(suf, IO.CSVSUFFIX)) type = DataText.M_CSV;
      else if(Token.eq(suf, IO.TXTSUFFIXES)) type = DataText.M_TEXT;
      else if(Token.eq(suf, IO.JSONSUFFIX)) type = DataText.M_JSON;
    }
    // unknown suffix: analyze first bytes
    if(type == null) type = guess(io);
    // default parser: XML
    if(type == null) type = DataText.M_XML;

    // choose correct parser (default: XML)
    parser.setSelectedItem(type.toUpperCase(Locale.ENGLISH));
  }

  /**
   * Returns the parsing type.
   * @return type
   */
  String parser() {
    return parser.getSelectedItem().toLowerCase(Locale.ENGLISH);
  }

  /**
   * Guesses the content type of the specified input.
   * @param in input stream
   * @return type
   */
  static String guess(final IO in) {
    if(!in.exists() || in instanceof IOUrl) return null;

    BufferInput ti = null;
    try {
      ti = new BufferInput(in);
      int b = ti.read();
      // input starts with opening bracket: may be xml
      if(b == '<') return DataText.M_XML;

      for(int c = 0; b >= 0 && ++c < IO.BLOCKSIZE;) {
        // treat as raw data if characters are no ascii
        if(b < ' ' && !Token.ws(b) || b >= 128) return DataText.M_RAW;
        b = ti.read();
      }
      // all characters were of type ascii
      return DataText.M_TEXT;
    } catch(final IOException ignored) {
    } finally {
      if(ti != null) try { ti.close(); } catch(final IOException ignored) { }
    }
    // could not evaluate type
    return null;
  }
}
