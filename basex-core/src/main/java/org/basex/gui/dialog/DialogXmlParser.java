package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.io.*;

/**
 * CSV parser panel.
 *
 * @author BaseX Team 2005-21, BSD License
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
  /** Use XInclude. */
  private final BaseXCheckBox xinclude;
  /** Catalog file. */
  private final BaseXTextField cfile;
  /** Browse Catalog file. */
  private final BaseXButton browsec;

  /**
   * Constructor.
   * @param dialog dialog reference
   * @param opts main options
   */
  DialogXmlParser(final BaseXDialog dialog, final MainOptions opts) {
    final BaseXBack pp = new BaseXBack(new RowLayout());

    intparse = new BaseXCheckBox(dialog, INT_PARSER, MainOptions.INTPARSE, opts).bold();
    pp.add(intparse);
    pp.add(new BaseXLabel(H_INT_PARSER, true, false));

    dtd = new BaseXCheckBox(dialog, PARSE_DTDS, MainOptions.DTD, opts).bold();
    pp.add(dtd);

    stripNS = new BaseXCheckBox(dialog, STRIP_NS, MainOptions.STRIPNS, opts).bold();
    pp.add(stripNS);

    chopWS = new BaseXCheckBox(dialog, CHOP_WS, MainOptions.CHOP, opts).bold();
    pp.add(chopWS);
    pp.add(new BaseXLabel(H_CHOP_WS, false, false).border(0, 0, 8, 0));
    pp.add(new BaseXLabel());

    // XInclude
    xinclude = new BaseXCheckBox(dialog, USE_XINCLUDE, MainOptions.XINCLUDE, opts).bold();
    pp.add(xinclude);

    // catalog resolver
    final boolean cat = !opts.get(MainOptions.CATFILE).isEmpty();
    usecat = new BaseXCheckBox(dialog, USE_CATALOG_FILE, cat).bold();
    final boolean rsen = CatalogWrapper.available();
    final BaseXBack cr = new BaseXBack(new TableLayout(2, 2, 8, 0));
    usecat.setEnabled(rsen);
    cr.add(usecat);
    cr.add(new BaseXLabel());

    cfile = new BaseXTextField(dialog, opts.get(MainOptions.CATFILE));
    cfile.setEnabled(rsen);
    cr.add(cfile);

    browsec = new BaseXButton(dialog, BROWSE_D);
    browsec.addActionListener(e -> {
      final GUIOptions gopts = dialog.gui.gopts;
      final BaseXFileChooser fc = new BaseXFileChooser(dialog, FILE_OR_DIR,
          gopts.get(GUIOptions.INPUTPATH)).filter(XML_DOCUMENTS, true, IO.XMLSUFFIX);

      final IO file = fc.select(Mode.FDOPEN);
      if(file != null) cfile.setText(file.path());
    });
    browsec.setEnabled(rsen);
    cr.add(browsec);
    pp.add(cr);
    if(!rsen) {
      final BaseXBack rs = new BaseXBack(new RowLayout());
      rs.add(new BaseXLabel(HELP1_USE_CATALOG).color(GUIConstants.dgray));
      rs.add(new BaseXLabel(HELP2_USE_CATALOG).color(GUIConstants.dgray));
      pp.add(rs);
    }

    add(pp, BorderLayout.WEST);
    action(true);
  }

  @Override
  boolean action(final boolean active) {
    final boolean ip = intparse.isSelected();
    final boolean uc = usecat.isSelected();
    intparse.setEnabled(!uc);
    xinclude.setEnabled(!ip);
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
    gui.set(MainOptions.XINCLUDE, xinclude.isSelected());
    gui.set(MainOptions.CATFILE, usecat.isSelected() ? cfile.getText() : "");
  }
}
