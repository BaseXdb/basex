package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import javax.swing.JFrame;
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
  /** Fulltext Indexing mode. */
  private BaseXCheckBox wrdindex;
  /** Main Memory mode. */
  private BaseXCheckBox mainmem;

  /**
   * Default Constructor.
   * @param parent parent frame
   */
  public DialogCreate(final JFrame parent) {
    super(parent, CREATEADVTITLE);

    final BaseXLabel label = new BaseXLabel(CREATEADVLABEL, true);
    set(label, BorderLayout.NORTH);

    // create checkboxes
    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(14, 1, 0, 0));

    chop = new BaseXCheckBox(CREATECHOP, Token.token(CHOPPINGINFO),
        Prop.chop, 0, this);
    p.add(chop);
    p.add(new BaseXLabel(CHOPPINGINFO, 10));

    entities = new BaseXCheckBox(CREATEENTITIES, Token.token(ENTITIESINFO),
        Prop.entity, 0, this);
    p.add(entities);
    p.add(new BaseXLabel(ENTITIESINFO, 10));

    txtindex = new BaseXCheckBox(CREATETXTINDEX, Token.token(TXTINDEXINFO),
        Prop.textindex, 0, this);
    p.add(txtindex);
    p.add(new BaseXLabel(TXTINDEXINFO, 10));

    atvindex = new BaseXCheckBox(CREATEATTINDEX, Token.token(ATTINDEXINFO),
        Prop.attrindex, 0, this);
    p.add(atvindex);
    p.add(new BaseXLabel(ATTINDEXINFO, 10));

    wrdindex = new BaseXCheckBox(CREATEWORDINDEX, Token.token(WORDINDEXINFO),
        Prop.wordindex, 0, this);
    p.add(wrdindex);
    p.add(new BaseXLabel(WORDINDEXINFO, 10));

    ftxindex = new BaseXCheckBox(CREATEFTINDEX, Token.token(FTINDEXINFO),
        Prop.ftindex, 0, this);
    p.add(ftxindex);
    p.add(new BaseXLabel(FTINDEXINFO, 20));

    mainmem = new BaseXCheckBox(CREATEMAINMEM, HELPMMEM, Prop.mainmem, 0, this);
    p.add(mainmem);
    p.add(new BaseXLabel(MMEMINFO, 10));

    set(p, BorderLayout.CENTER);

    // create buttons
    set(BaseXLayout.okCancel(this), BorderLayout.SOUTH);

    finish(parent);
  }

  @Override
  public void close() {
    super.close();
    Prop.chop  = chop.isSelected();
    Prop.entity   = entities.isSelected();
    Prop.textindex = txtindex.isSelected();
    Prop.attrindex = atvindex.isSelected();
    Prop.wordindex = wrdindex.isSelected();
    Prop.ftindex = ftxindex.isSelected();
    Prop.mainmem  = mainmem.isSelected();
  }
}
