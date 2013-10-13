package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.io.*;

/**
 * CSV parser panel.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class DialogXmlParser extends DialogParser {
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


  /**
   * Constructor.
   * @param d dialog reference
   * @param opts main options
   */
  DialogXmlParser(final BaseXDialog d, final MainOptions opts) {
    final BaseXBack pp = new BaseXBack(new TableLayout(9, 1));

    intparse = new BaseXCheckBox(INT_PARSER, opts.get(MainOptions.INTPARSE), 0, d);
    pp.add(intparse);
    pp.add(new BaseXLabel(H_INT_PARSER, true, false));

    dtd = new BaseXCheckBox(PARSE_DTDS, opts.get(MainOptions.DTD), 0, d);
    pp.add(dtd);

    stripNS = new BaseXCheckBox(STRIP_NS, opts.get(MainOptions.STRIPNS), 0, d);
    pp.add(stripNS);

    chopWS = new BaseXCheckBox(CHOP_WS, opts.get(MainOptions.CHOP), 0, d);
    pp.add(chopWS);
    pp.add(new BaseXLabel(H_CHOP_WS, false, false).border(0, 0, 8, 0));
    pp.add(new BaseXLabel());

    // catalog resolver
    usecat = new BaseXCheckBox(USE_CATALOG_FILE, !opts.get(MainOptions.CATFILE).isEmpty(), 0, d);
    final boolean rsen = CatalogWrapper.available();
    final BaseXBack cr = new BaseXBack(new TableLayout(2, 2, 8, 0));
    usecat.setEnabled(rsen);
    cr.add(usecat);
    cr.add(new BaseXLabel());

    cfile = new BaseXTextField(opts.get(MainOptions.CATFILE), d);
    cfile.setEnabled(rsen);
    cr.add(cfile);

    browsec = new BaseXButton(BROWSE_D, d);
    browsec.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { catchoose(d.gui); }
    });
    browsec.setEnabled(rsen);
    cr.add(browsec);
    pp.add(cr);
    if(!rsen) {
      final BaseXBack rs = new BaseXBack(new TableLayout(2, 1));
      rs.add(new BaseXLabel(HELP1_USE_CATALOG).color(GUIConstants.DGRAY));
      rs.add(new BaseXLabel(HELP2_USE_CATALOG).color(GUIConstants.DGRAY));
      pp.add(rs);
    }

    add(pp, BorderLayout.WEST);

    final boolean ip = intparse.isSelected();
    final boolean uc = usecat.isSelected();
    intparse.setEnabled(!uc);
    usecat.setEnabled(!ip && CatalogWrapper.available());
    cfile.setEnabled(uc);
    browsec.setEnabled(uc);
  }

  @Override
  boolean action(final boolean active) {
    final boolean ip = intparse.isSelected();
    final boolean uc = usecat.isSelected();
    intparse.setEnabled(!uc);
    usecat.setEnabled(!ip && CatalogWrapper.available());
    cfile.setEnabled(uc);
    browsec.setEnabled(uc);
    return true;
  }

  @Override
  void update() {
  }

  @Override
  void setOptions(final GUI gui) {
    gui.set(MainOptions.CHOP, chopWS.isSelected());
    gui.set(MainOptions.STRIPNS, stripNS.isSelected());
    gui.set(MainOptions.DTD, dtd.isSelected());
    gui.set(MainOptions.INTPARSE, intparse.isSelected());
    gui.set(MainOptions.CATFILE, usecat.isSelected() ? cfile.getText() : "");
  }

  /**
   * Opens a file dialog to choose an XML catalog or directory.
   * @param gui gui reference
   */
  private void catchoose(final GUI gui) {
    final GUIOptions gopts = gui.gopts;
    final BaseXFileChooser fc = new BaseXFileChooser(FILE_OR_DIR,
        gopts.get(GUIOptions.INPUTPATH), gui).filter(XML_DOCUMENTS, IO.XMLSUFFIX);

    final IO file = fc.select(Mode.FDOPEN);
    if(file != null) cfile.setText(file.path());
  }
}
