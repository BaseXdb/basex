package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.gui.layout.BaseXLayout.DropHandler;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Panel for importing new database resources.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
final class DialogImport extends BaseXBack {
  /** User feedback. */
  final BaseXLabel info;
  /** Resource to add. */
  final BaseXTextField input;
  /** Browse button. */
  final BaseXButton browse;
  /** Chosen parser. */
  final BaseXCombo parsers;
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
  DialogImport(final BaseXDialog dial, final BaseXBack panel, final DialogParsing parse) {
    gui = dial.gui;
    parsing = parse;

    layout(new TableLayout(10, 1));
    border(8);

    // add options
    add(new BaseXLabel(FILE_OR_DIR + COL, true, true).border(0, 0, 6, 0));

    final String path = gui.gopts.get(GUIOptions.INPUTPATH);
    input = new BaseXTextField(path, dial);
    input.history(GUIOptions.INPUTS, dial);

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
    final StringList ps = new StringList();
    for(final MainParser mp : MainParser.values()) ps.add(mp.name());

    parsers = new BaseXCombo(dial, ps.toArray());
    parsers.setSelectedItem(opts.get(MainOptions.PARSER).name());

    filter = new BaseXTextField(opts.get(MainOptions.CREATEFILTER), dial);
    BaseXLayout.setWidth(filter, 200);

    addRaw = new BaseXCheckBox(ADD_RAW_FILES, MainOptions.ADDRAW, opts, dial);
    skipCorrupt = new BaseXCheckBox(SKIP_CORRUPT_FILES, MainOptions.SKIPCORRUPT, opts, dial);
    archives = new BaseXCheckBox(PARSE_ARCHIVES, MainOptions.ADDARCHIVES, opts, dial);

    final BaseXBack p = new BaseXBack(new TableLayout(2, 2, 20, 0));
    p.add(new BaseXLabel(INPUT_FORMAT, false, true).border(0, 0, 6, 0));
    p.add(new BaseXLabel(FILE_PATTERNS + COL, false, true).border(0, 0, 6, 0));
    p.add(parsers);
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

    final MainParser parser = MainParser.valueOf(parsers.getSelectedItem());
    final boolean raw = parser == MainParser.RAW;
    addRaw.setEnabled(multi && !raw && !gui.context.options.get(MainOptions.MAINMEM));
    skipCorrupt.setEnabled(!raw);

    if(comp == parsers) {
      parsing.setType(parser);
      if(multi) filter.setText(raw ? "*" : "*." + parser);
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
    MainParser type = null;
    // input type of single file
    final String path = io.path();
    final int i = path.lastIndexOf('.');
    if(i != -1) {
      // analyze file suffix
      final String suf = path.substring(i).toLowerCase(Locale.ENGLISH);
      if(Token.eq(suf, IO.XMLSUFFIXES) || Token.eq(suf, IO.XSLSUFFIXES)) type = MainParser.XML;
      else if(Token.eq(suf, IO.HTMLSUFFIXES)) type = MainParser.HTML;
      else if(Token.eq(suf, IO.CSVSUFFIX)) type = MainParser.CSV;
      else if(Token.eq(suf, IO.TXTSUFFIXES)) type = MainParser.TEXT;
      else if(Token.eq(suf, IO.JSONSUFFIX)) type = MainParser.JSON;
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
   * @return type
   */
  private static MainParser guess(final IO in) {
    if(!in.exists() || in instanceof IOUrl) return null;

    try(final BufferInput ti = new BufferInput(in)) {
      int b = ti.read();
      // input starts with opening bracket: may be xml
      if(b == '<') return MainParser.XML;

      for(int c = 0; b >= 0 && ++c < IO.BLOCKSIZE;) {
        // treat as raw data if characters are no ascii
        if(b < ' ' && !Token.ws(b) || b >= 128) return MainParser.RAW;
        b = ti.read();
      }
      // all characters were of type ascii
      return MainParser.TEXT;
    } catch(final IOException ignored) { }
    // could not evaluate type
    return null;
  }
}
