package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.build.CsvProp.*;
import org.basex.build.JsonProp.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.io.*;
import org.basex.io.serial.*;
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
  /** JSON: format. */
  private final BaseXCombo jsonformat;
  /** JSON: encoding. */
  private final BaseXCombo jsonenc;

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
  private final BaseXCombo csven;

  /** Text options panel. */
  private final BaseXBack textopts;
  /** Text: Use lines. */
  private final BaseXCheckBox lines;
  /** Text: encoding. */
  private final BaseXCombo textenc;

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
    jsonenc = DialogExport.encoding(d, jprop.get(JsonProp.ENCODING));
    StringList sl = new StringList();
    final JsonFormat[] formats = JsonFormat.values();
    final int fl = formats.length - 1;
    for(int f = 0; f < fl; f++) sl.add(formats[f].toString());
    String[] sa = sl.toArray();
    jsonformat = new BaseXCombo(d, sa);
    String s = jprop.get(JsonProp.FORMAT);
    if(Token.eq(s, sa)) jsonformat.setSelectedItem(s);

    // html
    html = new BaseXTextField(prop.get(Prop.HTMLPARSER), d);

    // text
    textenc = DialogExport.encoding(d, tprop.get(TextProp.ENCODING));
    lines = new BaseXCheckBox(SPLIT_INPUT_LINES, tprop.is(TextProp.LINES), 0, d);

    // csv
    csven = DialogExport.encoding(d, cprop.get(CsvProp.ENCODING));
    header = new BaseXCheckBox(FIRST_LINE_HEADER, cprop.is(CsvProp.HEADER), 0, d);

    separator = new BaseXBack().layout(new TableLayout(1, 2, 6, 0));
    sl = new StringList();
    for(final CsvSep cs : CsvSep.values()) sl.add(cs.toString());
    sa = sl.toArray();
    sepcombo = new BaseXCombo(d, sl.add("").toArray());
    separator.add(sepcombo);

    String f = "";
    s = cprop.get(CsvProp.SEPARATOR);
    if(Token.eq(s, sa)) {
      sepcombo.setSelectedItem(s);
    } else {
      sepcombo.setSelectedIndex(sa.length);
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

    final boolean avl = HtmlParser.available();
    htmlopts.add(new BaseXLabel(avl ? H_HTML_PARSER : H_NO_HTML_PARSER));

    if(avl) {
      final BaseXBack p = new BaseXBack(new TableLayout(1, 2, 8, 0));
      p.add(new BaseXLabel(PARAMETERS + COL, true, true));
      p.add(html);
      htmlopts.add(p);
    }

    BaseXBack p = new BaseXBack(new TableLayout(2, 2, 8, 4));
    p.add(new BaseXLabel(ENCODING + COL, true, true));
    p.add(jsonenc);
    p.add(new BaseXLabel(FORMAT + COL, true, true));
    p.add(jsonformat);
    jsonopts.add(p);

    p = new BaseXBack(new TableLayout(2, 2, 8, 4));
    p.add(new BaseXLabel(ENCODING + COL, true, true));
    p.add(csven);
    p.add(new BaseXLabel(SEPARATOR, true, true));
    p.add(separator);
    csvopts.add(p);
    csvopts.add(header);

    p = new BaseXBack(new TableLayout(1, 2, 8, 4));
    p.add(new BaseXLabel(ENCODING + COL, true, true));
    p.add(textenc);
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

    final boolean fixedsep = sepcombo.getSelectedIndex() < CsvSep.values().length;
    sepchar.setEnabled(!fixedsep);
    if(fixedsep) {
      cprop.set(CsvProp.SEPARATOR, sepcombo.getSelectedItem().toString());
      try {
        sepchar.setText(new TokenBuilder().add(cprop.separator()).toString());
      } catch(final SerializerException ex) { Util.notexpected(); }
    }
    return fixedsep || sepchar.getText().length() == 1;
  }

  /**
   * Sets the parsing options.
   * @param type parsing type
   */
  public void setOptions(final String type) {
    jprop.set(JsonProp.ENCODING, jsonenc.getSelectedItem().toString());
    jprop.set(JsonProp.FORMAT, jsonformat.getSelectedItem().toString());
    gui.set(Prop.JSONPARSER, jprop.toString());

    tprop.set(TextProp.ENCODING, textenc.getSelectedItem().toString());
    tprop.set(TextProp.LINES, lines.isSelected());
    gui.set(Prop.TEXTPARSER, tprop.toString());

    cprop.set(CsvProp.ENCODING, csven.getSelectedItem().toString());
    cprop.set(CsvProp.HEADER, header.isSelected());
    cprop.set(CsvProp.SEPARATOR, sepcombo.getSelectedIndex() <
      CsvSep.values().length ? sepcombo.getSelectedItem().toString() :
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
