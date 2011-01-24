package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.basex.build.file.HTMLParser;
import org.basex.build.xml.CatalogResolverWrapper;
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
import org.basex.util.StringList;

/**
 * Parsing options dialog.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public class DialogParsing extends BaseXBack {
  
  /** Parser. */
  private final BaseXCombo parser;
  /** Internal XML parsing. */
  private final BaseXCheckBox intparse;
  /** Entities mode. */
  private final BaseXCheckBox entities;
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
  /** Main window reference. */
  private final GUI gui;
  
  /**
   * Default constructor.
   * @param g main window reference.
   */
  public DialogParsing(final GUI g) {
    this.gui = g;
    final BaseXBack p2 = new BaseXBack(new TableLayout(11, 1)).border(8);

    // always use internal/external parser, chop whitespaces, ...?
    BaseXBack p = new BaseXBack(new TableLayout(1, 2, 6, 0));

    final BaseXLabel parse = new BaseXLabel(CREATEFORMAT, true, true);
    final StringList parsers = new StringList();
    parsers.add(DataText.M_XML);
    if(HTMLParser.available()) parsers.add(DataText.M_HTML);
    parsers.add(DataText.M_TEXT);
    parsers.add(DataText.M_CSV);

    parser = new BaseXCombo(g, parsers.toArray());
    parser.setSelectedItem(g.context.prop.get(Prop.PARSER));
    p.add(parse);
    p.add(parser);
    p2.add(p);
    p2.add(new BaseXLabel(FORMATINFO, true, false));

    intparse = new BaseXCheckBox(CREATEINTPARSE,
        g.context.prop.is(Prop.INTPARSE), 0, g);
    p2.add(intparse);
    p2.add(new BaseXLabel(INTPARSEINFO, true, false));

    entities = new BaseXCheckBox(CREATEENTITIES, 
        g.context.prop.is(Prop.ENTITY), g);
    p2.add(entities);
    dtd = new BaseXCheckBox(CREATEDTD, g.context.prop.is(Prop.DTD), 12, g);
    p2.add(dtd);

    chop = new BaseXCheckBox(CREATECHOP, g.context.prop.is(Prop.CHOP), 0, g);
    p2.add(chop);
    p2.add(new BaseXLabel(CHOPPINGINFO, false, false).border(0, 0, 8, 0));
    p2.add(new BaseXLabel());

    // CatalogResolving
    final boolean rsen = CatalogResolverWrapper.available();
    final BaseXBack fl = new BaseXBack(new TableLayout(2, 2, 6, 0));
    usecat = new BaseXCheckBox(USECATFILE,
        !g.context.prop.get(Prop.CATFILE).isEmpty(), 0, g);
    usecat.setEnabled(rsen);
    fl.add(usecat);
    fl.add(new BaseXLabel());
    cfile = new BaseXTextField(g.context.prop.get(Prop.CATFILE), g);
    cfile.setEnabled(rsen);
    fl.add(cfile);

    browsec = new BaseXButton(BUTTONBROWSE, g);
    browsec.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { catchoose(); }
    });
    browsec.setEnabled(rsen);
    fl.add(browsec);
    p2.add(fl);
    if(!rsen) {
      final BaseXBack rs = new BaseXBack(new TableLayout(2, 1));
      rs.add(new BaseXLabel(USECATHLP).color(GUIConstants.COLORDARK));
      rs.add(new BaseXLabel(USECATHLP2).color(GUIConstants.COLORDARK));
      p2.add(rs);
    }
    add(p2);
  }
  
  /**
   * Opens a file dialog to choose an XML catalog or directory.
   */
  void catchoose() {
    final GUIProp gprop = gui.gprop;
    final BaseXFileChooser fc = new BaseXFileChooser(CREATETITLE,
        gprop.get(GUIProp.CREATEPATH), gui);
    fc.addFilter(CREATEXMLDESC, IO.XMLSUFFIX);

    final IO file = fc.select(BaseXFileChooser.Mode.FDOPEN);
    if(file != null) cfile.setText(file.path());
  }
  
  /**
   * Reacts on user input.
   */
  void action() {
    final boolean xml = parser.getSelectedItem().toString().equals(
        DataText.M_XML);
    intparse.setEnabled(!usecat.isSelected() && xml);
    entities.setEnabled(intparse.isSelected());
    dtd.setEnabled(intparse.isSelected());

    usecat.setEnabled(!intparse.isSelected() &&
        CatalogResolverWrapper.available() && xml);
    cfile.setEnabled(usecat.isSelected());
    browsec.setEnabled(cfile.isEnabled());
    chop.setEnabled(xml);
  }
  
  /**
   * Closes the tab.
   */
  public void close() {
    gui.set(Prop.CHOP, chop.isSelected());
    gui.set(Prop.ENTITY, entities.isSelected());
    gui.set(Prop.DTD, dtd.isSelected());
    gui.set(Prop.INTPARSE, intparse.isSelected());
    gui.set(Prop.PARSER, parser.getSelectedItem().toString());
    gui.set(Prop.CATFILE, usecat.isSelected() ? cfile.getText().trim() : "");
  }
}
