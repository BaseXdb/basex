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
  private final BaseXCheckBox chop;
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
  private final BaseXTextField params;

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
  /** CSV: Format. */
  private final BaseXCombo format;
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
  /** ParserProps. */
  private ParserProp props;

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
      props = new ParserProp(prop.get(Prop.PARSEROPT));
    } catch(final IOException ex) {
      props = new ParserProp();
    }

    intparse = new BaseXCheckBox(INT_PARSER, prop.is(Prop.INTPARSE), 0, d);
    dtd = new BaseXCheckBox(PARSE_DTDS, prop.is(Prop.DTD), 0, d);
    chop = new BaseXCheckBox(CHOP_WS, prop.is(Prop.CHOP), 0, d);
    cfile = new BaseXTextField(prop.get(Prop.CATFILE), d);
    browsec = new BaseXButton(BROWSE_D, d);
    usecat = new BaseXCheckBox(USE_CATALOG_FILE, !prop.get(Prop.CATFILE).isEmpty(), 0, d);

    jsonml = new BaseXCheckBox(PARSE_AS_JSONML, props.is(ParserProp.JSONML), 0, d);

    params = new BaseXTextField(prop.get(Prop.HTMLOPT), d);

    lines = new BaseXCheckBox(SPLIT_INPUT_LINES, props.is(ParserProp.LINES), 0, d);
    header = new BaseXCheckBox(FIRST_LINE_HEADER, props.is(ParserProp.HEADER), 0, d);

    separator = new BaseXBack().layout(new TableLayout(1, 2, 6, 0));
    final StringList sl = new StringList();
    sl.add(CSVParser.SEPARATORS).add("");
    sepcombo = new BaseXCombo(d, sl.toArray());
    separator.add(sepcombo);

    String f = "";
    final String s = props.get(ParserProp.SEPARATOR);
    if(Token.eq(s, CSVParser.SEPARATORS)) {
      sepcombo.setSelectedItem(s);
    } else {
      sepcombo.setSelectedIndex(CSVParser.SEPARATORS.length);
      final int ch = Token.toInt(s);
      f = ch > 0 ? String.valueOf((char) ch) : "";
    }
    sepchar = new BaseXTextField(f, d);
    separator.add(sepchar);
    BaseXLayout.setWidth(sepchar, 35);

    format = new BaseXCombo(d, CSVParser.FORMATS);
    format.setSelectedItem(props.get(ParserProp.FORMAT));

    final String enc = props.get(ParserProp.ENCODING);
    cencoding = DialogExport.encoding(d, enc);
    tencoding = DialogExport.encoding(d, enc);
    jencoding = DialogExport.encoding(d, enc);

    xmlopts  = new BaseXBack(new TableLayout(8, 1));
    htmlopts = new BaseXBack(new TableLayout(2, 1));
    jsonopts = new BaseXBack(new TableLayout(2, 1));
    csvopts  = new BaseXBack(new TableLayout(2, 1));
    textopts = new BaseXBack(new TableLayout(2, 1));
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
    xmlopts.add(chop);
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
    htmlopts.add(new BaseXLabel(avl ? H_HTML_PARSER : H_NO_HTML_PARSER).
        border(0, 0, 12, 0));

    if(avl) {
      final BaseXBack p = new BaseXBack(new TableLayout(1, 2, 8, 0));
      p.add(new BaseXLabel(PARAMETERS + COL, true, true));
      p.add(params);
      htmlopts.add(p);
    }

    BaseXBack p = new BaseXBack(new TableLayout(1, 2, 8, 4));
    p.add(new BaseXLabel(ENCODING + COL, true, true));
    p.add(jencoding);
    jsonopts.add(p);
    jsonopts.add(jsonml);

    p = new BaseXBack(new TableLayout(3, 2, 8, 4));
    p.add(new BaseXLabel(ENCODING + COL, true, true));
    p.add(cencoding);
    p.add(new BaseXLabel(SEPARATOR, true, true));
    p.add(separator);
    p.add(new BaseXLabel(XML_FORMAT, true, true));
    p.add(format);
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
        gprop.get(GUIProp.CREATEPATH), gui);
    fc.addFilter(XML_DOCUMENTS, IO.XMLSUFFIX);

    final IO file = fc.select(Mode.FDOPEN);
    if(file != null) cfile.setText(file.path());
  }

  /**
   * Updates the options, depending on the specific type.
   * @param type parsing type
   */
  void updateType(final String type) {
    label.setText(type.toUpperCase(Locale.ENGLISH) + " Parser");

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
    final boolean customsep = s == CSVParser.SEPARATORS.length;
    sepchar.setEnabled(customsep);
    return !customsep || sepchar.getText().length() == 1;
  }

  /**
   * Sets the parsing options.
   * @param type parsing type
   */
  public void setOptions(final String type) {
    final BaseXCombo cb = type.equals(DataText.M_TEXT) ? tencoding :
      type.equals(DataText.M_JSON) ? jencoding : cencoding;
    props.set(ParserProp.ENCODING, cb.getSelectedItem().toString());
    props.set(ParserProp.FORMAT, format.getSelectedItem().toString());
    props.set(ParserProp.HEADER, header.isSelected());
    props.set(ParserProp.LINES, lines.isSelected());
    props.set(ParserProp.JSONML, jsonml.isSelected());
    props.set(ParserProp.SEPARATOR, sepcombo.getSelectedIndex() <
      CSVParser.SEPARATORS.length ? sepcombo.getSelectedItem().toString() :
      String.valueOf((int) sepchar.getText().charAt(0)));

    gui.set(Prop.PARSER, type);
    gui.set(Prop.PARSEROPT, props.toString());
    gui.set(Prop.CHOP, chop.isSelected());
    gui.set(Prop.DTD, dtd.isSelected());
    gui.set(Prop.INTPARSE, intparse.isSelected());
    gui.set(Prop.CATFILE, usecat.isSelected() ? cfile.getText() : "");
    gui.set(Prop.HTMLOPT, params.getText());
  }
}
