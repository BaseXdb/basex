package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import org.basex.core.Prop;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.TableLayout;
import org.basex.util.Token;

/**
 * Dialog window for specifying options for creating a new database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogCreate extends Dialog {
  /** Internal XML parsing. */
  private BaseXCheckBox intparse;
  /** Whitespace chopping. */
  private BaseXCheckBox chop;
  /** Entities mode. */
  private BaseXCheckBox entities;
  /** Main Memory mode. */
  private BaseXCheckBox mainmem;
  /** Indexing mode. */
  private BaseXCheckBox txtindex;
  /** Indexing mode. */
  private BaseXCheckBox atvindex;
  /** Word Indexing mode. */
  private BaseXCheckBox ftxindex;
  /** Fuzzy indexing. */
  private BaseXCheckBox fuzzyindex;
  /** Fuzzy indexing label. */
  private BaseXLabel fuzzylabel;

  /**
   * Default Constructor.
   * @param parent parent frame
   */
  public DialogCreate(final JFrame parent) {
    super(parent, CREATEADVTITLE);

    // create checkboxes
    final BaseXBack p1 = new BaseXBack();
    p1.setBorder(8, 8, 8, 8);
    p1.setLayout(new TableLayout(8, 1, 0, 0));

    intparse = new BaseXCheckBox(CREATEINTPARSE, Token.token(INTPARSEINFO),
        Prop.intparse, 0, this);
    p1.add(intparse);
    p1.add(new BaseXLabel(INTPARSEINFO, 8));

    entities = new BaseXCheckBox(CREATEENTITIES, Token.token(ENTITIESINFO),
        Prop.entity, 0, this);
    p1.add(entities);
    p1.add(new BaseXLabel(ENTITIESINFO, 8));

    chop = new BaseXCheckBox(CREATECHOP, Token.token(CHOPPINGINFO),
        Prop.chop, 0, this);
    p1.add(chop);
    p1.add(new BaseXLabel(CHOPPINGINFO, 16));

    mainmem = new BaseXCheckBox(CREATEMAINMEM, HELPMMEM, Prop.mainmem, 0, this);
    p1.add(mainmem);
    p1.add(new BaseXLabel(MMEMINFO, 8));

    // create checkboxes
    final BaseXBack p2 = new BaseXBack();
    p2.setLayout(new TableLayout(10, 1, 0, 0));
    p2.setBorder(8, 8, 8, 8);

    txtindex = new BaseXCheckBox(CREATETXTINDEX, Token.token(TXTINDEXINFO),
        Prop.textindex, 0, this);
    p2.add(txtindex);
    p2.add(new BaseXLabel(TXTINDEXINFO, 8));

    atvindex = new BaseXCheckBox(CREATEATTINDEX, Token.token(ATTINDEXINFO),
        Prop.attrindex, 0, this);
    p2.add(atvindex);
    p2.add(new BaseXLabel(ATTINDEXINFO, 8));

    ftxindex = new BaseXCheckBox(CREATEFTINDEX, Token.token(FTINDEXINFO),
        Prop.ftindex, 0, this);
    p2.add(ftxindex);
    p2.add(new BaseXLabel(FTINDEXINFO, 8));

    fuzzyindex = new BaseXCheckBox(CREATEFZINDEX, Token.token(FZINDEXINFO),
        Prop.fzindex, 0, this);
    p2.add(fuzzyindex);
    fuzzylabel = new BaseXLabel(FZINDEXINFO, 8);
    p2.add(fuzzylabel);

    final JTabbedPane tabs = new JTabbedPane();
    BaseXLayout.addDefaultKeys(tabs, this);
    tabs.addTab(GENERALINFO, p1);
    tabs.addTab(INDEXINFO, p2);

    set(tabs, BorderLayout.CENTER);

    // create buttons
    set(BaseXLayout.okCancel(this), BorderLayout.SOUTH);
    action(null);

    finish(parent);
  }
  

  @Override
  public void action(final String cmd) {
    final boolean ftx = ftxindex.isSelected();
    fuzzyindex.setEnabled(ftx);
    fuzzylabel.setEnabled(ftx);
  }

  @Override
  public void close() {
    super.close();
    Prop.chop  = chop.isSelected();
    Prop.entity   = entities.isSelected();
    Prop.textindex = txtindex.isSelected();
    Prop.attrindex = atvindex.isSelected();
    Prop.ftindex = ftxindex.isSelected();
    Prop.mainmem  = mainmem.isSelected();
    Prop.intparse = intparse.isSelected();
    Prop.fzindex = fuzzyindex.isSelected();
  }
}
