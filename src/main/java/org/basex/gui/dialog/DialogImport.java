package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.event.*;
import java.util.*;

import javax.swing.border.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.io.*;
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
    DataText.M_CSV, DataText.M_TEXT
  };

  /** User feedback. */
  final BaseXLabel info;
  /** Resource to add. */
  final BaseXTextField input;
  /** Browse button. */
  final BaseXButton browse;
  /** DB name. */
  String dbname;

  /** Dialog reference. */
  private final GUI gui;
  /** Parsing options. */
  private final DialogParsing parsing;
  /** Add contents of archives. */
  private final BaseXCheckBox archives;
  /** Skip corrupt files. */
  private final BaseXCheckBox skip;
  /** Add remaining files as raw files. */
  private final BaseXCheckBox raw;
  /** Document filter. */
  private final BaseXTextField filter;
  /** Parser. */
  private final BaseXCombo parser;

  /**
   * Constructor.
   * @param dialog dialog reference
   * @param panel feature panel
   * @param parse parsing dialog
   */
  public DialogImport(final Dialog dialog, final BaseXBack panel,
      final DialogParsing parse) {

    gui = dialog.gui;
    parsing = parse;

    layout(new TableLayout(8, 1));
    border(8);

    // add options
    add(new BaseXLabel(FILE_OR_DIR + COL, true, true).border(0, 0, 6, 0));

    input = new BaseXTextField(gui.gprop.get(GUIProp.CREATEPATH), dialog);
    input.addKeyListener(dialog.keys);
    browse = new BaseXButton(BROWSE_D, dialog);
    browse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { choose(); }
    });
    final BaseXBack b = new BaseXBack(new TableLayout(1, 2, 8, 0));
    b.add(input);
    b.add(browse);
    add(b);

    final Prop prop = gui.context.prop;
    skip = new BaseXCheckBox(SKIP_CORRUPT_FILES,
        prop.is(Prop.SKIPCORRUPT), dialog);
    add(skip);

    archives = new BaseXCheckBox(PARSE_ARCHIVES, prop.is(Prop.ADDARCHIVES),
        dialog);
    add(archives);

    final StringList parsers = new StringList(PARSING.length);
    final String type = prop.get(Prop.PARSER);
    for(final String p : PARSING) parsers.add(p.toUpperCase(Locale.ENGLISH));
    parser = new BaseXCombo(dialog, parsers.toArray());
    parser.setSelectedItem(type.toUpperCase(Locale.ENGLISH));

    filter = new BaseXTextField(prop.get(Prop.CREATEFILTER), dialog);
    BaseXLayout.setWidth(filter, 200);
    raw = new BaseXCheckBox(ADD_RAW_FILES, prop.is(Prop.ADDRAW), dialog);
    raw.setBorder(new EmptyBorder(12, 0, 0, 0));

    // add additional options
    add(panel);

    final BaseXBack p = new BaseXBack(new TableLayout(2, 2, 16, 0));
    p.add(new BaseXLabel(INPUT_FORMAT, false, true).border(12, 0, 6, 0));
    p.add(new BaseXLabel(FILE_PATTERNS + COL, false, true).border(12, 0, 6, 0));
    p.add(parser);
    p.add(filter);
    add(p);
    add(raw);

    // add info label
    info = new BaseXLabel(" ").border(24, 0, 6, 0);

    parsing.updateType(parser());
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
    final BaseXFileChooser fc = new BaseXFileChooser(FILE_OR_DIR,
        gui.gprop.get(GUIProp.CREATEPATH), gui);
    fc.addFilter(XML_DOCUMENTS, IO.XMLSUFFIX);
    fc.addFilter(JSON_DOCUMENTS, IO.JSONSUFFIX);
    fc.addFilter(HTML_DOCUMENTS, IO.HTMLSUFFIXES);
    fc.addFilter(CSV_DOCUMENTS, IO.CSVSUFFIX);
    fc.addFilter(PLAIN_TEXT, IO.TXTSUFFIX);
    fc.addFilter(GZIP_ARCHIVES, IO.GZSUFFIX);
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
    if(comp == parser) {
      final String type = parser();
      parsing.updateType(type);
      filter.setText("*." + type);
    }

    final String in = input.getText().trim();
    final IO io = IO.get(in);
    gui.gprop.set(GUIProp.CREATEPATH, in);

    info.setText(null, null);
    final boolean ok = empty ? in.isEmpty() || io.exists() :
      !in.isEmpty() && io.exists();
    final boolean dir = ok && io.isDir();
    filter.setEnabled(dir);
    raw.setEnabled(dir && !gui.context.prop.is(Prop.MAINMEM));
    return ok;
  }

  /**
   * Sets the parsing options.
   */
  public void setOptions() {
    final String type = parser();
    gui.set(Prop.PARSER, type);
    gui.set(Prop.CREATEFILTER, filter.getText());
    gui.set(Prop.ADDARCHIVES, archives.isSelected());
    gui.set(Prop.SKIPCORRUPT, skip.isSelected());
    gui.set(Prop.ADDRAW, raw.isSelected());
    parsing.setOptions(type);
  }

  /**
   * Opens a file dialog to choose an XML catalog or directory.
   */
  void choose() {
    final IOFile in = inputFile();
    if(in == null) return;
    final String path = in.path();
    input.setText(path);
    dbname = in.dbname();

    final int i = path.lastIndexOf('.');
    if(i == -1) return;
    final String suf = path.substring(i).toLowerCase(Locale.ENGLISH);

    String type = null;
    if(Token.eq(suf, IO.XMLSUFFIX)) type = DataText.M_XML;
    if(Token.eq(suf, IO.HTMLSUFFIXES)) type = DataText.M_HTML;
    if(Token.eq(suf, IO.CSVSUFFIX)) type = DataText.M_CSV;
    if(Token.eq(suf, IO.TXTSUFFIX)) type = DataText.M_TEXT;
    if(Token.eq(suf, IO.JSONSUFFIX)) type = DataText.M_JSON;

    if(type != null) {
      parser.setSelectedItem(type.toUpperCase(Locale.ENGLISH));
      parsing.updateType(type);
      filter.setText("*." + type);
    }
  }

  /**
   * Returns the parsing type.
   * @return type
   */
  private String parser() {
    return parser.getSelectedItem().toString().toLowerCase(Locale.ENGLISH);
  }
}
