package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JTabbedPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import org.basex.core.proc.InfoDB;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXText;
import org.basex.gui.layout.TableLayout;
import org.basex.index.IndexToken;
import org.basex.index.IndexToken.TYPE;
import org.basex.util.Token;

/**
 * Info Database Dialog.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogInfo extends Dialog {
  /** Index Checkbox. */
  private final BaseXCheckBox[] indexes = new BaseXCheckBox[3];
  /** Fulltext indexing. */
  private BaseXCheckBox[] ft = new BaseXCheckBox[4];
  /** Fulltext labels. */
  private BaseXLabel[] fl = new BaseXLabel[4];
  /** Editable fulltext options. */
  private boolean ftedit;
  /** Optimize flag. */
  public boolean opt;

  /**
   * Default Constructor.
   * @param gui reference to main frame
   */
  public DialogInfo(final GUI gui) {
    super(gui, INFOTITLE);
    
    // first tab
    final BaseXBack tab1 = new BaseXBack();
    tab1.setBorder(new CompoundBorder(new EtchedBorder(),
        new EmptyBorder(8, 8, 8, 8)));
    tab1.setLayout(new BorderLayout());

    final Data data = GUI.context.data();
    final MetaData meta = data.meta;

    final BaseXLabel doc = new BaseXLabel(meta.dbname);
    doc.setFont(new Font(GUIProp.font, 0, 18));
    doc.setBorder(0, 0, 5, 0);
    tab1.add(doc, BorderLayout.NORTH);

    final BaseXText text = text(InfoDB.db(meta, data.size, false, false));
    BaseXLayout.setHeight(text, 220);
    tab1.add(text, BorderLayout.CENTER);

    // second tab
    final BaseXBack tab2 = new BaseXBack();
    tab2.setLayout(new GridLayout(2, 1, 0, 8));
    tab2.setBorder(8, 8, 0, 8);
    tab2.add(addIndex(true, data));
    tab2.add(addIndex(false, data));

    // third tab
    final BaseXBack tab3 = new BaseXBack();
    tab3.setLayout(new GridLayout(2, 1));
    tab3.setBorder(8, 8, 0, 8);

    BaseXBack p = new BaseXBack();
    p.setLayout(new BorderLayout());

    indexes[0] = new BaseXCheckBox(INFOTEXTINDEX, Token.token(TXTINDEXINFO),
        meta.txtindex, 0, this);
    p.add(indexes[0], BorderLayout.NORTH);

    p.add(text(meta.txtindex ? data.info(IndexToken.TYPE.TXT) :
      Token.token(TXTINDEXINFO)), BorderLayout.CENTER);
    tab3.add(p);

    p = new BaseXBack();
    p.setLayout(new BorderLayout());

    indexes[1] = new BaseXCheckBox(INFOATTRINDEX, Token.token(ATTINDEXINFO),
        meta.atvindex, 0, this);
    p.add(indexes[1], BorderLayout.NORTH);
    
    p.add(text(meta.atvindex ? data.info(IndexToken.TYPE.ATV) :
      Token.token(ATTINDEXINFO)), BorderLayout.CENTER);
    tab3.add(p);

    // fourth tab
    final BaseXBack tab4 = new BaseXBack();
    tab4.setLayout(new GridLayout(1, 1));
    tab4.setBorder(8, 8, 8, 8);

    ftedit = !meta.ftxindex;
    indexes[2] = new BaseXCheckBox(INFOFTINDEX, Token.token(FTINDEXINFO),
        meta.ftxindex, 0, this);
    
    p = new BaseXBack();
    p.setLayout(ftedit ? new TableLayout(10, 1) : new BorderLayout());
    p.add(indexes[2], BorderLayout.NORTH);

    if(ftedit) {
      p.add(new BaseXLabel(FTINDEXINFO, ftedit, false));
      final String[] cb = { CREATEFZ, CREATESTEM, CREATEDC, CREATECS };
      final String[] desc = { FZINDEXINFO, FTSTEMINFO, FTDCINFO, FTCSINFO };
      final boolean[] val = { meta.ftfz, meta.ftst, meta.ftdc, meta.ftcs };
      for(int f = 0; f < ft.length; f++) {
        ft[f] = new BaseXCheckBox(cb[f], Token.token(desc[f]), val[f], 0, this);
        fl[f] = new BaseXLabel(desc[f], true, false);
        p.add(ft[f]);
        p.add(fl[f]);
      }
    } else {
      p.add(text(data.info(IndexToken.TYPE.FTX)), BorderLayout.CENTER);
    }
    tab4.add(p);
    
    final JTabbedPane tabs = new JTabbedPane();
    BaseXLayout.addDefaultKeys(tabs, this);
    tabs.addTab(GENERALINFO, tab1);
    tabs.addTab(NAMESINFO, tab2);
    tabs.addTab(INDEXINFO, tab3);
    tabs.addTab(FTINFO, tab4);

    set(tabs, BorderLayout.CENTER);

    set(BaseXLayout.newButtons(this, true,
        new String[] { BUTTONOPT, BUTTONOK, BUTTONCANCEL },
        new byte[][] { HELPOPT, HELPOK, HELPCANCEL }), BorderLayout.SOUTH);

    action(null);
    setResizable(true);
    setMinimumSize(getPreferredSize());
    finish(gui);
  }

  /**
   * Adds an index panel.
   * @param tag tag/attribute flag 
   * @param data data reference
   * @return panel
   */
  private BaseXBack addIndex(final boolean tag, final Data data) {
    final BaseXBack p = new BaseXBack();
    p.setLayout(new BorderLayout());
    String lbl = tag ? INFOTAGINDEX : INFOATNINDEX;
    if(!data.tags.uptodate) lbl += " (" + INFOOUTOFDATED + ")";
    p.add(new BaseXLabel(lbl, false, true), BorderLayout.NORTH);
    final TYPE index = tag ? IndexToken.TYPE.TAG : IndexToken.TYPE.ATN;
    p.add(text(data.info(index)), BorderLayout.CENTER);
    return p;
  }
  
  /**
   * Returns a text box.
   * @param txt contents
   * @return text box
   */
  private BaseXText text(final byte[] txt) {
    final BaseXText text = new BaseXText(null, false, this);
    text.setBorder(new EmptyBorder(5, 5, 5, 5));
    text.setText(txt);
    text.setFocusable(false);
    BaseXLayout.setWidth(text, 450);
    return text;
  }

  /**
   * Returns an array with the chosen indexes.
   * @return check box
   */
  public boolean[] indexes() {
    final boolean[] in = new boolean[indexes.length];
    for(int i = 0; i < indexes.length; i++) in[i] = indexes[i].isSelected();
    return in;
  }
  
  @Override
  public void action(final String cmd) {
    if(BUTTONOPT.equals(cmd)) {
      //GUI.get().execute(Commands.OPTIMIZE);
      opt = true;
      close();
    }
    if(!ftedit) return;
    final boolean ftx = indexes[2].isSelected();
    for(int f = 0; f < ft.length; f++) {
      ft[f].setEnabled(ftx);
      fl[f].setEnabled(ftx);
    }
  }

  @Override
  public void close() {
    super.close();
    final Data data = GUI.context.data();
    final MetaData meta = data.meta;
    if(!ftedit) return;
    meta.ftfz = ft[0].isSelected();
    meta.ftst = ft[1].isSelected();
    meta.ftcs = ft[2].isSelected();
    meta.ftdc = ft[3].isSelected();
  }
}
