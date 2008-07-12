package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import org.basex.core.Prop;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;
import org.basex.util.Token;

/**
 * Dialog window for specifying options for creating a new database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogCreate extends Dialog {
  /** Database Input. */
  private BaseXTextField input;
  /** Database name. */
  private BaseXTextField dbname;
  /** Internal XML parsing. */
  private BaseXCheckBox intparse;
  /** Whitespace chopping. */
  private BaseXCheckBox chop;
  /** Entities mode. */
  private BaseXCheckBox entities;
  /** Indexing mode. */
  private BaseXCheckBox txtindex;
  /** Indexing mode. */
  private BaseXCheckBox atvindex;
  /** Word Indexing mode. */
  private BaseXCheckBox ftxindex;
  /** Fulltext indexing. */
  private BaseXCheckBox[] ft = new BaseXCheckBox[4];
  /** Fulltext labels. */
  private BaseXLabel[] fl = new BaseXLabel[4];
  /** Buttons. */
  private BaseXBack buttons;

  /**
   * Default Constructor.
   * @param parent parent frame
   */
  public DialogCreate(final JFrame parent) {
    super(parent, CREATEADVTITLE);

    // create checkboxes
    final BaseXBack p1 = new BaseXBack();
    p1.setLayout(new TableLayout(5, 1));
    p1.setBorder(8, 8, 8, 8);
 
    BaseXLabel l = new BaseXLabel(CREATETITLE);
    p1.add(l);

    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(1, 3, 6, 0));

    input = new BaseXTextField(null, this);
    BaseXLayout.setWidth(input, 300);

    final BaseXButton button = new BaseXButton(BUTTONBROWSE, HELPBROWSE, this);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        choose(parent);
      }
    });

    p.add(input);
    p.add(button);
    p1.add(p);

    l = new BaseXLabel(CREATENAME);
    l.setBorder(0, 0, 0, 0);
    p1.add(l);
    dbname = new BaseXTextField(null, this);
    BaseXLayout.setWidth(dbname, 300);
    p1.add(dbname);

    // create checkboxes
    final BaseXBack p2 = new BaseXBack();
    p2.setLayout(new TableLayout(9, 1));
    p2.setBorder(8, 8, 8, 8);

    intparse = new BaseXCheckBox(CREATEINTPARSE, Token.token(INTPARSEINFO),
        Prop.intparse, 0, this);
    p2.add(intparse);
    p2.add(new BaseXLabel(INTPARSEINFO, 8));

    entities = new BaseXCheckBox(CREATEENTITIES, Token.token(ENTITIESINFO),
        Prop.entity, 0, this);
    p2.add(entities);
    p2.add(new BaseXLabel(ENTITIESINFO, 8));

    chop = new BaseXCheckBox(CREATECHOP, Token.token(CHOPPINGINFO),
        Prop.chop, 0, this);
    p2.add(chop);
    p2.add(new BaseXLabel(CHOPPINGINFO, 16));

    // create checkboxes
    final BaseXBack p3 = new BaseXBack();
    p3.setLayout(new TableLayout(10, 1, 0, 0));
    p3.setBorder(8, 8, 8, 8);

    txtindex = new BaseXCheckBox(INFOTXTINDEX, Token.token(TXTINDEXINFO),
        Prop.textindex, 0, this);
    p3.add(txtindex);
    p3.add(new BaseXLabel(TXTINDEXINFO, 8));

    atvindex = new BaseXCheckBox(INFOATVINDEX, Token.token(ATTINDEXINFO),
        Prop.attrindex, 0, this);
    p3.add(atvindex);
    p3.add(new BaseXLabel(ATTINDEXINFO, 8));

    // create checkboxes
    final BaseXBack p4 = new BaseXBack();
    p4.setLayout(new TableLayout(10, 1, 0, 0));
    p4.setBorder(8, 8, 8, 8);

    ftxindex = new BaseXCheckBox(INFOFTINDEX, Token.token(FTINDEXINFO),
        Prop.ftindex, 0, this);
    p4.add(ftxindex);
    p4.add(new BaseXLabel(FTINDEXINFO, 8));

    final String[] cb = { CREATEFZ, CREATESTEM, CREATEDC, CREATECS };
    final String[] desc = { FZINDEXINFO, FTSTEMINFO, FTDCINFO, FTCSINFO };
    final boolean[] val = { Prop.ftfuzzy, Prop.ftstem, Prop.ftdc, Prop.ftcs };
    for(int f = 0; f < ft.length; f++) {
      ft[f] = new BaseXCheckBox(cb[f], Token.token(desc[f]), val[f], 0, this);
      fl[f] = new BaseXLabel(desc[f], 8);
      p4.add(ft[f]);
      //p4.add(fl[f]);
    }

    final JTabbedPane tabs = new JTabbedPane();
    BaseXLayout.addDefaultKeys(tabs, this);
    tabs.addTab(GENERALINFO, p1);
    tabs.addTab(PARSEINFO, p2);
    tabs.addTab(INDEXINFO, p3);
    tabs.addTab(FTINFO, p4);

    set(tabs, BorderLayout.CENTER);

    // create buttons
    buttons = BaseXLayout.okCancel(this);
    set(buttons, BorderLayout.SOUTH);
    action(null);

    finish(parent);
  }
  

  /**
   * Choose an XML document or directory.
   * @param parent parent reference
   */
  public void choose(final JFrame parent) {
    final BaseXFileChooser fc = new BaseXFileChooser(CREATETITLE,
        GUIProp.createpath, parent);
    fc.addFilter(IO.GZSUFFIX, CREATEGZDESC);
    fc.addFilter(IO.ZIPSUFFIX, CREATEZIPDESC);
    fc.addFilter(IO.XMLSUFFIX, CREATEXMLDESC);

    GUIProp.createpath = fc.getDir();
    if(fc.select(BaseXFileChooser.MODE.OPENDIR)) {
      IO file = fc.getFile();
      input.setText(file.path());
      dbname.setText(file.dbname());
    }
  }

  /**
   * Returns the chosen XML file or directory.
   * @return file or directory
   */
  public String input() {
    return input.getText();
  }

  /**
   * Returns the database name.
   * @return file or directory
   */
  public String db() {
    return dbname.getText();
  }

  @Override
  public void action(final String cmd) {
    final boolean ftx = ftxindex.isSelected();
    for(int f = 0; f < ft.length; f++) {
      ft[f].setEnabled(ftx);
      fl[f].setEnabled(ftx);
    }
    ok = dbname.getText().length() != 0;
    BaseXLayout.enableOK(buttons, ok);
  }

  @Override
  public void close() {
    super.close();
    Prop.chop  = chop.isSelected();
    Prop.entity   = entities.isSelected();
    Prop.textindex = txtindex.isSelected();
    Prop.attrindex = atvindex.isSelected();
    Prop.ftindex = ftxindex.isSelected();
    Prop.intparse = intparse.isSelected();
    Prop.ftfuzzy = ft[0].isSelected();
    Prop.ftstem = ft[1].isSelected();
    Prop.ftdc = ft[2].isSelected();
    Prop.ftcs = ft[3].isSelected();
  }
}
