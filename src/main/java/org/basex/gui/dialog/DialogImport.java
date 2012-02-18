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
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Panel for importing new database resources.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class DialogImport extends BaseXBack {
  /** Available parsers. */
  private static final String[] PARSING = {
    DataText.M_XML, DataText.M_JSON, DataText.M_HTML,
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
  public DialogImport(final Dialog dial, final BaseXBack panel,
      final DialogParsing parse) {

    gui = dial.gui;
    parsing = parse;

    layout(new TableLayout(10, 1));
    border(8);

    // add options
    add(new BaseXLabel(FILE_OR_DIR + COL, true, true).border(0, 0, 6, 0));

    final String in = gui.gprop.get(GUIProp.CREATEPATH);
    input = new BaseXTextField(gui.gprop.get(GUIProp.CREATEPATH), dial);
    input.addKeyListener(dial.keys);

    final IO io = IO.get(in);
    if(io instanceof IOFile && !in.isEmpty()) dbname = io.dbname();

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

    final Prop prop = gui.context.prop;
    final StringList parsers = new StringList(PARSING.length);
    for(final String p : PARSING) parsers.add(p.toUpperCase(Locale.ENGLISH));

    parser = new BaseXCombo(dial, parsers.toArray());
    parser.setSelectedItem(prop.get(Prop.PARSER).toUpperCase(Locale.ENGLISH));
    filter = new BaseXTextField(prop.get(Prop.CREATEFILTER), dial);
    BaseXLayout.setWidth(filter, 200);

    addRaw = new BaseXCheckBox(ADD_RAW_FILES, prop.is(Prop.ADDRAW), dial);
    skipCorrupt = new BaseXCheckBox(SKIP_CORRUPT_FILES, prop.is(Prop.SKIPCORRUPT), dial);
    archives = new BaseXCheckBox(PARSE_ARCHIVES, prop.is(Prop.ADDARCHIVES), dial);

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
    info = new BaseXLabel(" ").border(24, 0, 6, 0);
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
   * Returns an XML file chosen by the user.
   * @return file chooser
   */
  IOFile inputFile() {
    final String path = gui.gprop.get(GUIProp.CREATEPATH);
    final BaseXFileChooser fc = new BaseXFileChooser(FILE_OR_DIR, path, gui);
    fc.addFilter(XML_DOCUMENTS, IO.XMLSUFFIXES);
    fc.addFilter(HTML_DOCUMENTS, IO.HTMLSUFFIXES);
    fc.addFilter(JSON_DOCUMENTS, IO.JSONSUFFIX);
    fc.addFilter(CSV_DOCUMENTS, IO.CSVSUFFIX);
    fc.addFilter(PLAIN_TEXT, IO.TXTSUFFIXES);
    fc.addFilter(ZIP_ARCHIVES, IO.ZIPSUFFIXES);
    final IOFile file = fc.select(Mode.FDOPEN);
    if(file != null) gui.gprop.set(GUIProp.CREATEPATH, file.path());
    return file;
  }

  /**
   * Updates the dialog window.
   * @param comp component
   * @param empty allow empty input
   * @return success flag, or {@code false} if specified input is not found
   */
  boolean action(final Object comp, final boolean empty) {
    parsing.action();

    final String in = input.getText().trim();
    final IO io = IO.get(in);
    gui.gprop.set(GUIProp.CREATEPATH, in);
    final boolean dir = io.isDir();

    final String type = parser();
    final boolean r = type.equals(DataText.M_RAW);
    if(comp == parser) {
      parsing.updateType(type);
      if(dir) filter.setText(r ? "*" : "*." + type);
    }

    final boolean ok = empty ? in.isEmpty() || io.exists() :
      !in.isEmpty() && io.exists();

    if(comp == input) setType(in, ok);

    info.setText(null, null);
    filter.setEnabled(dir);
    addRaw.setEnabled(dir && !r && !gui.context.prop.is(Prop.MAINMEM));
    skipCorrupt.setEnabled(!r);
    archives.setEnabled(dir || io.isArchive());
    return ok;
  }

  /**
   * Sets the parsing options.
   */
  void setOptions() {
    final String type = parser();
    gui.set(Prop.PARSER, type);
    gui.set(Prop.CREATEFILTER, filter.getText());
    gui.set(Prop.ADDARCHIVES, archives.isSelected());
    gui.set(Prop.SKIPCORRUPT, skipCorrupt.isSelected());
    gui.set(Prop.ADDRAW, addRaw.isSelected());
    parsing.setOptions(type);
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
    setType(in.path(), true);
  }

  /**
   * Chooses the correct input type.
   * @param in input
   * @param ok ok flag
   */
  void setType(final String in, final boolean ok) {
    // get file path, update input path and database name
    final IO io = IO.get(in);
    if(io instanceof IOFile && !in.isEmpty()) dbname = io.dbname();
    if(!ok) return;

    final boolean dir = io.isDir();
    final boolean archive = io.isArchive();
    filter.setText(io instanceof IOFile && !dir && !archive ? io.name() :
      "*" + IO.XMLSUFFIX);

    // evaluate input type
    String type = null;
    if(!dir && !archive) {
      // input type of single file
      final String path = io.path();
      final int i = path.lastIndexOf('.');
      if(i != -1) {
        // analyze file suffix
        final String suf = path.substring(i).toLowerCase(Locale.ENGLISH);
        if(Token.eq(suf, IO.XMLSUFFIXES)) type = DataText.M_XML;
        else if(Token.eq(suf, IO.HTMLSUFFIXES)) type = DataText.M_HTML;
        else if(Token.eq(suf, IO.CSVSUFFIX)) type = DataText.M_CSV;
        else if(Token.eq(suf, IO.TXTSUFFIXES)) type = DataText.M_TEXT;
        else if(Token.eq(suf, IO.JSONSUFFIX)) type = DataText.M_JSON;
      }
      // unknown suffix: analyze first bytes
      if(type == null) type = guess(io);
    }
    // default parser: XML
    if(type == null) type = DataText.M_XML;

    // choose correct parser (default: XML)
    parser.setSelectedItem(type.toUpperCase(Locale.ENGLISH));
    parsing.updateType(type);
  }

  /**
   * Returns the parsing type.
   * @return type
   */
  String parser() {
    return parser.getSelectedItem().toString().toLowerCase(Locale.ENGLISH);
  }

  /**
   * Guesses the content type of the specified input.
   * @param in input stream
   * @return type
   */
  String guess(final IO in) {
    if(!in.exists()) return null;

    TextInput ti = null;
    try {
      ti = new TextInput(in);
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
    } catch(final IOException ex) {
    } finally {
      if(ti != null) try { ti.close(); } catch(final IOException ex) { }
    }
    // could not evaluate type
    return null;
  }
}
