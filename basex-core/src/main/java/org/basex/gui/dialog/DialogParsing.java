package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import org.basex.build.file.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Parsing options dialog.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class DialogParsing extends BaseXBack {
  /** Format label. */
  private final BaseXLabel label;
  /** Tabulators. */
  private final BaseXTabs tabs;

  /** Internal XML parsing. */
  private final BaseXCheckBox intparse;
  /** DTD mode. */
  private final BaseXCheckBox dtd;
  /** Whitespace chopping. */
  private final BaseXCheckBox chopWS;
  /** Namespace stripping. */
  private final BaseXCheckBox stripNS;
  /** Use XML Catalog. */
  private final BaseXCheckBox usecat;
  /** Catalog file. */
  private final BaseXTextField cfile;
  /** Browse Catalog file. */
  private final BaseXButton browsec;

  /** JSON options panel. */
  private final BaseXBack jsonopts;
  /** JSON: Use JsonML format. */
  private final BaseXCheckBox jsonml;
  /** JSON: encoding. */
  private final BaseXCombo jencoding;

  /** HTML options panel. */
  private final BaseXBack htmlopts;
  /** Parameters. */
  private final BaseXTextField html;

  /** CSV options panel. */
  private final BaseXBack csvopts;
  /** CSV: Use header. */
  private final BaseXCheckBox header;
  /** CSV: Separator. */
  private final BaseXBack separator;
  /** CSV: Separator. */
  private final BaseXCombo sepcombo;
  /** CSV: Separator (numeric). */
  private final BaseXTextField sepchar;
  /** CSV: encoding. */
  private final BaseXCombo cencoding;

  /** Text options panel. */
  private final BaseXBack textopts;
  /** Text: Use lines. */
  private final BaseXCheckBox lines;
  /** Text: encoding. */
  private final BaseXCombo tencoding;

  /** Main panel. */
  private final BaseXBack main;
  /** Main window reference. */
  private final GUI gui;

  /** Options panel. */
  private BaseXBack parseropts;
  /** XML options panel. */
  private final BaseXBack xmlopts;

  /** Text parser options. */
  TextProp tprop;
  /** Text parser options. */
  CsvProp cprop;
  /** Text parser options. */
  JsonProp jprop;

  /**
   * Default constructor.
   * @param d dialog reference
   * @param t tabs
   */
  public DialogParsing(final BaseXDialog d, final BaseXTabs t) {
    main = new BaseXBack(new TableLayout(2, 1)).border(8);
    gui = d.gui;
    tabs = t;

    label = new BaseXLabel(" ").border(0, 0, 12, 0).large();

    final Prop prop = gui.context.prop;
    try {
      tprop = new TextProp(prop.get(Prop.TEXTPARSER));
    } catch(final IOException ex) { tprop = new TextProp(); }
    try {
      cprop = new CsvProp(prop.get(Prop.CSVPARSER));
    } catch(final IOException ex) { cprop = new CsvProp(); }
    try {
      jprop = new JsonProp(prop.get(Prop.JSONPARSER));
    } catch(final IOException ex) { jprop = new JsonProp(); }

    intparse = new BaseXCheckBox(INT_PARSER, prop.is(Prop.INTPARSE), 0, d);
    dtd = new BaseXCheckBox(PARSE_DTDS, prop.is(Prop.DTD), 0, d);
    chopWS = new BaseXCheckBox(CHOP_WS, prop.is(Prop.CHOP), 0, d);
    stripNS = new BaseXCheckBox(STRIP_NS, prop.is(Prop.STRIPNS), 0, d);
    cfile = new BaseXTextField(prop.get(Prop.CATFILE), d);
    browsec = new BaseXButton(BROWSE_D, d);
    usecat = new BaseXCheckBox(USE_CATALOG_FILE, !prop.get(Prop.CATFILE).isEmpty(), 0, d);

    // json
    jencoding = DialogExport.encoding(d, jprop.get(JsonProp.ENCODING));
    jsonml = new BaseXCheckBox(PARSE_AS_JSONML,
        jprop.get(JsonProp.FORMAT).equals(DataText.M_JSONML), 0, d);

    // html
    html = new BaseXTextField(prop.get(Prop.HTMLPARSER), d);

    // text
    tencoding = DialogExport.encoding(d, tprop.get(TextProp.ENCODING));
    lines = new BaseXCheckBox(SPLIT_INPUT_LINES, tprop.is(TextProp.LINES), 0, d);

    // csv
    cencoding = DialogExport.encoding(d, cprop.get(CsvProp.ENCODING));
    header = new BaseXCheckBox(FIRST_LINE_HEADER, cprop.is(CsvProp.HEADER), 0, d);

    separator = new BaseXBack().layout(new TableLayout(1, 2, 6, 0));
    final StringList sl = new StringList();
    sl.add(CsvProp.SEPARATORS).add("");
    sepcombo = new BaseXCombo(d, sl.toArray());
    separator.add(sepcombo);

    String f = "";
    final String s = cprop.get(CsvProp.SEPARATOR);
    if(Token.eq(s, CsvProp.SEPARATORS)) {
      sepcombo.setSelectedItem(s);
    } else {
      sepcombo.setSelectedIndex(CsvProp.SEPARATORS.length);
      final int ch = Token.toInt(s);
      f = ch > 0 ? String.valueOf((char) ch) : "";
    }
    sepchar = new BaseXTextField(f, d);
    separator.add(sepchar);
    BaseXLayout.setWidth(sepchar, 35);

    xmlopts  = new BaseXBack(new TableLayout(9, 1));
    htmlopts = new BaseXBack(new TableLayout(2, 1, 0, 8));
    jsonopts = new BaseXBack(new TableLayout(2, 1, 0, 8));
    csvopts  = new BaseXBack(new TableLayout(2, 1, 0, 8));
    textopts = new BaseXBack(new TableLayout(2, 1, 0, 8));
    createOptionsPanels();

    setLayout(new TableLayout(1, 1));
    add(main);
  }

  /**
   * Options panels.
   */
  private void createOptionsPanels() {
    xmlopts.add(intparse);
    xmlopts.add(new BaseXLabel(H_INT_PARSER, true, false));
    xmlopts.add(dtd);
    xmlopts.add(stripNS);
    xmlopts.add(chopWS);
    xmlopts.add(new BaseXLabel(H_CHOP_WS, false, false).border(0, 0, 8, 0));
    xmlopts.add(new BaseXLabel());

    // catalog resolver
    final boolean rsen = CatalogWrapper.available();
    final BaseXBack fl = new BaseXBack(new TableLayout(2, 2, 8, 0));
    usecat.setEnabled(rsen);
    fl.add(usecat);
    fl.add(new BaseXLabel());
    cfile.setEnabled(rsen);
    fl.add(cfile);
    browsec.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { catchoose(); }
    });
    browsec.setEnabled(rsen);
    fl.add(browsec);
    xmlopts.add(fl);
    if(!rsen) {
      final BaseXBack rs = new BaseXBack(new TableLayout(2, 1));
      rs.add(new BaseXLabel(HELP1_USE_CATALOG).color(GUIConstants.DGRAY));
      rs.add(new BaseXLabel(HELP2_USE_CATALOG).color(GUIConstants.DGRAY));
      xmlopts.add(rs);
    }

    final boolean avl = HTMLParser.available();
    htmlopts.add(new BaseXLabel(avl ? H_HTML_PARSER : H_NO_HTML_PARSER));

    if(avl) {
      final BaseXBack p = new BaseXBack(new TableLayout(1, 2, 8, 0));
      p.add(new BaseXLabel(PARAMETERS + COL, true, true));
      p.add(html);
      htmlopts.add(p);
    }

    BaseXBack p = new BaseXBack(new TableLayout(1, 2, 8, 0));
    p.add(new BaseXLabel(ENCODING + COL, true, true));
    p.add(jencoding);
    jsonopts.add(p);
    jsonopts.add(jsonml);

    p = new BaseXBack(new TableLayout(2, 2, 8, 4));
    p.add(new BaseXLabel(ENCODING + COL, true, true));
    p.add(cencoding);
    p.add(new BaseXLabel(SEPARATOR, true, true));
    p.add(separator);
    csvopts.add(p);
    csvopts.add(header);

    p = new BaseXBack(new TableLayout(1, 2, 8, 4));
    p.add(new BaseXLabel(ENCODING + COL, true, true));
    p.add(tencoding);
    textopts.add(p);
    textopts.add(lines);

    final boolean ip = intparse.isSelected();
    final boolean uc = usecat.isSelected();
    intparse.setEnabled(!uc);
    usecat.setEnabled(!ip && CatalogWrapper.available());
    cfile.setEnabled(uc);
    browsec.setEnabled(uc);
  }

  /**
   * Opens a file dialog to choose an XML catalog or directory.
   */
  void catchoose() {
    final GUIProp gprop = gui.gprop;
    final BaseXFileChooser fc = new BaseXFileChooser(FILE_OR_DIR,
        gprop.get(GUIProp.INPUTPATH), gui).filter(XML_DOCUMENTS, IO.XMLSUFFIX);

    final IO file = fc.select(Mode.FDOPEN);
    if(file != null) cfile.setText(file.path());
  }

  /**
   * Updates the options, depending on the specific type.
   * @param type parsing type
   */
  void updateType(final String type) {
    label.setText(Util.info(PARSER_X, type.toUpperCase(Locale.ENGLISH)));

    if(type.equals(DataText.M_XML)) {
      parseropts = xmlopts;
    } else if(type.equals(DataText.M_HTML)) {
      parseropts = htmlopts;
    } else if(type.equals(DataText.M_JSON)) {
      parseropts = jsonopts;
    } else if(type.equals(DataText.M_CSV)) {
      parseropts = csvopts;
    } else if(type.equals(DataText.M_TEXT)) {
      parseropts = textopts;
    }

    main.removeAll();
    main.add(label);
    if(parseropts != null) main.add(parseropts);
    main.revalidate();
    tabs.setEnabledAt(1, !type.equals(DataText.M_RAW));
  }

  /**
   * Reacts on user input.
   * @return result of check
   */
  boolean action() {
    final boolean ip = intparse.isSelected();
    final boolean uc = usecat.isSelected();
    intparse.setEnabled(!uc);
    usecat.setEnabled(!ip && CatalogWrapper.available());
    cfile.setEnabled(uc);
    browsec.setEnabled(uc);

    final int s = sepcombo.getSelectedIndex();
    final boolean customsep = s == CsvProp.SEPARATORS.length;
    sepchar.setEnabled(customsep);
    return !customsep || sepchar.getText().length() == 1;
  }

  /**
   * Sets the parsing options.
   * @param type parsing type
   */
  public void setOptions(final String type) {
    jprop.set(JsonProp.ENCODING, jencoding.getSelectedItem().toString());
    jprop.set(JsonProp.FORMAT, jsonml.isSelected() ? DataText.M_JSONML : DataText.M_JSON);
    gui.set(Prop.JSONPARSER, jprop.toString());

    tprop.set(TextProp.ENCODING, tencoding.getSelectedItem().toString());
    tprop.set(TextProp.LINES, lines.isSelected());
    gui.set(Prop.TEXTPARSER, tprop.toString());

    cprop.set(CsvProp.ENCODING, cencoding.getSelectedItem().toString());
    cprop.set(CsvProp.HEADER, header.isSelected());
    cprop.set(CsvProp.SEPARATOR, sepcombo.getSelectedIndex() <
      CsvProp.SEPARATORS.length ? sepcombo.getSelectedItem().toString() :
      String.valueOf((int) sepchar.getText().charAt(0)));
    gui.set(Prop.CSVPARSER, cprop.toString());

    gui.set(Prop.PARSER, type);
    gui.set(Prop.CHOP, chopWS.isSelected());
    gui.set(Prop.STRIPNS, stripNS.isSelected());
    gui.set(Prop.DTD, dtd.isSelected());
    gui.set(Prop.INTPARSE, intparse.isSelected());
    gui.set(Prop.CATFILE, usecat.isSelected() ? cfile.getText() : "");
    gui.set(Prop.HTMLPARSER, html.getText());
  }
}
