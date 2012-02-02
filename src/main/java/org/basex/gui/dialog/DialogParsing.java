package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import org.basex.build.file.CSVParser;
import org.basex.build.file.HTMLParser;
import org.basex.build.file.ParserProp;
import org.basex.build.xml.CatalogWrapper;
import org.basex.core.Prop;
import org.basex.data.DataText;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;
import org.basex.util.list.StringList;

/**
 * Parsing options dialog.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class DialogParsing extends BaseXBack {
  /** Parser. */
  private final BaseXCombo parser;
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

  /** CSV options panel. */
  private final BaseXBack csvopts;
  /** CSV: Use header. */
  private final BaseXCheckBox header;
  /** CSV: Separator. */
  private final BaseXCombo separator;
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
   */
  public DialogParsing(final Dialog d) {
    main = new BaseXBack(new TableLayout(3, 1)).border(8);
    gui = d.gui;

    final Prop prop = gui.context.prop;
    try {
      props = new ParserProp(prop.get(Prop.PARSEROPT));
    } catch(final IOException ex) {
      props = new ParserProp();
    }

    final StringList parsers = new StringList();
    parsers.add(DataText.M_XML);
    if(HTMLParser.available()) parsers.add(DataText.M_HTML);
    parsers.add(DataText.M_CSV).add(DataText.M_TEXT);

    parser = new BaseXCombo(d, parsers.toArray());
    parser.setSelectedItem(prop.get(Prop.PARSER));

    intparse = new BaseXCheckBox(INT_PARSER, prop.is(Prop.INTPARSE), 0, d);
    dtd = new BaseXCheckBox(PARSE_DTDS, prop.is(Prop.DTD), 12, d);
    chop = new BaseXCheckBox(CHOP_WS, prop.is(Prop.CHOP), 0, d);
    cfile = new BaseXTextField(prop.get(Prop.CATFILE), d);
    browsec = new BaseXButton(BROWSE_D, d);
    usecat = new BaseXCheckBox(USE_CATALOG_FILE,
        !prop.get(Prop.CATFILE).isEmpty(), 0, d);

    lines = new BaseXCheckBox("Lines", props.is(ParserProp.LINES), 0, d);
    header = new BaseXCheckBox("Header", props.is(ParserProp.HEADER), 0, d);
    separator = new BaseXCombo(d, CSVParser.SEPARATORS);
    separator.setSelectedItem(props.get(ParserProp.SEPARATOR));
    format = new BaseXCombo(d, CSVParser.FORMATS);
    format.setSelectedItem(props.get(ParserProp.FORMAT));

    cencoding = DialogExport.encoding(d, props.get(ParserProp.ENCODING));
    tencoding = DialogExport.encoding(d, props.get(ParserProp.ENCODING));

    xmlopts = new BaseXBack(new TableLayout(8, 1));
    csvopts = new BaseXBack(new TableLayout(2, 1));
    textopts = new BaseXBack(new TableLayout(3, 1));
    createOptionsPanels();

    setLayout(new TableLayout(1, 1));
    update(parser.getSelectedItem().toString());
    add(main);
  }

  /**
   * Options panels.
   */
  void createOptionsPanels() {
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

    BaseXBack p = new BaseXBack(new TableLayout(3, 2, 8, 4));
    p.add(new BaseXLabel(ENCODING + COL, true, false));
    p.add(cencoding);
    p.add(new BaseXLabel(SEPARATOR, true, false));
    p.add(separator);
    p.add(new BaseXLabel(XML_FORMAT, true, false));
    p.add(format);
    csvopts.add(p);
    p = new BaseXBack(new TableLayout(2, 1));
    p.add(header);
    p.add(new BaseXLabel(FIRST_LINE_HEADER, true, false));
    csvopts.add(p);

    p = new BaseXBack(new TableLayout(1, 2, 8, 4));
    p.add(new BaseXLabel(ENCODING + COL, true, false));
    p.add(tencoding);
    textopts.add(p);
    textopts.add(lines);
    textopts.add(new BaseXLabel(SPLIT_INPUT_LINES, true, false));
  }

  /**
   * Updates the options panel.
   * @param type format type
   */
  void update(final String type) {
    main.removeAll();

    final BaseXBack p = new BaseXBack(new TableLayout(1, 2, 8, 0));
    p.add(new BaseXLabel(INPUT_FORMAT, true, true));
    p.add(parser);
    main.add(p);
    main.add(new BaseXLabel(H_INPUT_FORMAT, true, false));

    if(type.equals(DataText.M_XML)) {
      parseropts = xmlopts;
    } else if(type.equals(DataText.M_HTML)) {
      parseropts = new BaseXBack();
    } else if(type.equals(DataText.M_CSV)) {
      parseropts = csvopts;
    } else if(type.equals(DataText.M_TEXT)) {
      parseropts = textopts;
    }

    main.add(parseropts);
    main.revalidate();
    parser.requestFocusInWindow();
  }

  /**
   * Opens a file dialog to choose an XML catalog or directory.
   */
  void catchoose() {
    final GUIProp gprop = gui.gprop;
    final BaseXFileChooser fc = new BaseXFileChooser(FILE_OR_DIR,
        gprop.get(GUIProp.CREATEPATH), gui);
    fc.addFilter(XML_DOCUMENTS, IO.XMLSUFFIX);

    final IO file = fc.select(BaseXFileChooser.Mode.FDOPEN);
    if(file != null) cfile.setText(file.path());
  }

  /**
   * Reacts on user input.
   * @param cmp component
   */
  void action(final Object cmp) {
    final String type = parser.getSelectedItem().toString();
    if(type.equals(DataText.M_XML)) {
      final boolean ip = intparse.isSelected();
      final boolean uc = usecat.isSelected();
      intparse.setEnabled(!uc);
      usecat.setEnabled(!ip && CatalogWrapper.available());
      cfile.setEnabled(uc);
      browsec.setEnabled(uc);
    }
    if(cmp == parser) update(type);
  }

  /**
   * Sets the parsing options.
   */
  public void setOptions() {
    final String type = parser.getSelectedItem().toString();
    final BaseXCombo cb = type.equals(DataText.M_TEXT) ? tencoding : cencoding;
    props.set(ParserProp.ENCODING, cb.getSelectedItem().toString());
    props.set(ParserProp.FORMAT, format.getSelectedItem().toString());
    props.set(ParserProp.HEADER, header.isSelected());
    props.set(ParserProp.SEPARATOR, separator.getSelectedItem().toString());
    props.set(ParserProp.LINES, lines.isSelected());
    gui.set(Prop.PARSEROPT, props.toString());
    gui.set(Prop.CHOP, chop.isSelected());
    gui.set(Prop.DTD, dtd.isSelected());
    gui.set(Prop.INTPARSE, intparse.isSelected());
    gui.set(Prop.PARSER, parser.getSelectedItem().toString());
    gui.set(Prop.CATFILE, usecat.isSelected() ? cfile.getText() : "");
  }
}
