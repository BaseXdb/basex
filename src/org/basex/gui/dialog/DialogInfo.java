package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import org.basex.core.Prop;
import org.basex.core.proc.InfoDB;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.basex.data.MetaData;
import org.basex.data.XMLSerializer;
import org.basex.data.Data.Type;
import org.basex.gui.GUI;
import org.basex.gui.GUICommands;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXText;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Info database dialog.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DialogInfo extends Dialog {
  /** Index Checkbox. */
  private final BaseXCheckBox[] indexes = new BaseXCheckBox[4];
  /** Full-text indexing. */
  private final BaseXCheckBox[] ft = new BaseXCheckBox[4];
  /** Full-text labels. */
  private final BaseXLabel[] fl = new BaseXLabel[4];
  /** Editable full-text options. */
  private final boolean ftedit;
  /** Button panel. */
  private final BaseXBack buttons;
  /** Optimize flag. */
  public boolean opt;
  /** Optimize button. */
  private Object optimize;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogInfo(final GUI main) {
    super(main, INFODB);

    // first tab
    final BaseXBack tab1 = new BaseXBack();
    tab1.setBorder(new CompoundBorder(new EtchedBorder(),
        new EmptyBorder(8, 8, 8, 8)));
    tab1.setLayout(new BorderLayout());

    final Data data = gui.context.data;
    final MetaData meta = data.meta;

    final BaseXLabel doc = new BaseXLabel(meta.name);
    doc.setFont(getFont().deriveFont(18f));
    doc.setBorder(0, 0, 5, 0);
    tab1.add(doc, BorderLayout.NORTH);

    final byte[] db = InfoDB.db(meta, true, false, true);
    final TokenBuilder info = new TokenBuilder(db);
    if(data.ns.size() != 0) {
      info.high().add(NL + INFONS + NL).norm().add(data.ns.info());
    }

    final BaseXText text = text(info.finish());
    text.setFont(getFont());
    BaseXLayout.setHeight(text, 350);
    tab1.add(text, BorderLayout.CENTER);

    // second tab
    final BaseXBack tab2 = new BaseXBack();
    tab2.setLayout(new GridLayout(2, 1, 0, 8));
    tab2.setBorder(8, 8, 0, 8);
    tab2.add(addIndex(true, data));
    tab2.add(addIndex(false, data));

    // third tab
    BaseXBack tab3 = null;
    final boolean pi = data.meta.pathindex;
    indexes[0] = new BaseXCheckBox(INFOPATHINDEX, pi, 0, this);

    tab3 = new BaseXBack();
    tab3.setLayout(new GridLayout(1, 1));
    tab3.setBorder(8, 8, 8, 8);

    final BaseXBack north = new BaseXBack();
    north.setLayout(new BorderLayout());
    north.add(indexes[0], BorderLayout.WEST);
    final BaseXButton export = new BaseXButton(GUIEXPORT, this);
    export.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final IO file = GUICommands.save(gui, true);
        if(file != null) {
          PrintOutput out = null;
          try {
            out = new PrintOutput(file.path());
            data.path.plan(data, new XMLSerializer(out));
          } catch(final IOException ex) {
            Dialog.error(gui, NOTSAVED);
          } finally {
            if(out != null) try { out.close(); } catch(final Exception x) { }
          }
        }
      }
    });
    if(pi) north.add(export, BorderLayout.EAST);

    BaseXBack p = new BaseXBack();
    p.setLayout(new BorderLayout());
    p.add(north, BorderLayout.NORTH);
    if(pi) p.add(text(data.path.info(data)), BorderLayout.CENTER);
    tab3.add(p);

    // fourth tab
    final BaseXBack tab4 = new BaseXBack();
    tab4.setLayout(new GridLayout(2, 1));
    tab4.setBorder(8, 8, 0, 8);

    p = new BaseXBack();
    p.setLayout(new BorderLayout());

    indexes[1] = new BaseXCheckBox(INFOTEXTINDEX, meta.txtindex, 0, this);
    p.add(indexes[1], BorderLayout.NORTH);

    p.add(text(meta.txtindex ? data.info(Type.TXT) :
      Token.token(TXTINDEXINFO)), BorderLayout.CENTER);
    tab4.add(p);

    p = new BaseXBack();
    p.setLayout(new BorderLayout());

    indexes[2] = new BaseXCheckBox(INFOATTRINDEX, meta.atvindex, 0, this);
    p.add(indexes[2], BorderLayout.NORTH);

    p.add(text(meta.atvindex ? data.info(Type.ATV) :
      Token.token(ATTINDEXINFO)), BorderLayout.CENTER);
    tab4.add(p);

    // fifth tab
    final BaseXBack tab5 = new BaseXBack();
    tab5.setLayout(new GridLayout(1, 1));
    tab5.setBorder(8, 8, 8, 8);

    ftedit = !meta.ftxindex;
    indexes[3] = new BaseXCheckBox(INFOFTINDEX, meta.ftxindex, 0, this);

    for(final BaseXCheckBox b : indexes) b.setEnabled(data instanceof DiskData);

    p = new BaseXBack();
    p.setLayout(ftedit ? new TableLayout(10, 1) : new BorderLayout());
    p.add(indexes[3], BorderLayout.NORTH);

    if(ftedit) {
      p.add(new BaseXLabel(FTINDEXINFO, ftedit, false));
      final String[] cb = { CREATEWC, CREATESTEM, CREATEDC, CREATECS };
      final String[] desc = { FZINDEXINFO, FTSTEMINFO, FTDCINFO, FTCSINFO };
      final boolean[] val = { meta.wildcards, meta.stemming,
          meta.diacritics, meta.casesens };
      for(int f = 0; f < ft.length; f++) {
        ft[f] = new BaseXCheckBox(cb[f], val[f], 0, this);
        fl[f] = new BaseXLabel(desc[f], true, false);
        p.add(ft[f]);
        p.add(fl[f]);
      }
    } else {
      p.add(text(data.info(Type.FTX)), BorderLayout.CENTER);
    }
    tab5.add(p);

    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.addTab(GENERALINFO, tab1);
    tabs.addTab(NAMESINFO, tab2);
    tabs.addTab(INFOPATHINDEX, tab3);
    tabs.addTab(INDEXINFO, tab4);
    tabs.addTab(FTINFO, tab5);

    set(tabs, BorderLayout.CENTER);

    optimize = new BaseXButton(BUTTONOPT, this);
    buttons = newButtons(this,
        new Object[] { optimize, BUTTONOK, BUTTONCANCEL });
    set(buttons, BorderLayout.SOUTH);

    action(null);
    setResizable(true);
    setMinimumSize(getPreferredSize());
    finish(null);
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
    String lbl = tag ? INFOTAGS : INFOATTS;
    if(!data.meta.uptodate) lbl += " (" + INFOOUTOFDATED + ")";
    p.add(new BaseXLabel(lbl, false, true), BorderLayout.NORTH);
    final Type index = tag ? Type.TAG : Type.ATN;
    p.add(text(data.info(index)), BorderLayout.CENTER);
    return p;
  }

  /**
   * Returns a text box.
   * @param txt contents
   * @return text box
   */
  private BaseXText text(final byte[] txt) {
    final BaseXText text = new BaseXText(false, this);
    text.setBorder(new EmptyBorder(5, 5, 5, 5));
    text.setText(txt);
    BaseXLayout.setWidth(text, 550);
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
  public void action(final Object cmp) {
    if(cmp == optimize) {
      opt = true;
      close();
    }
    if(ftedit) {
      final boolean ftx = indexes[3].isSelected();
      for(int f = 0; f < ft.length; f++) {
        ft[f].setEnabled(ftx);
        fl[f].setEnabled(ftx);
      }
    }
    final Data data = gui.context.data;
    enableOK(buttons, BUTTONOPT, !data.meta.uptodate);
  }

  @Override
  public void close() {
    super.close();
    if(ftedit) {
      final Prop prop = gui.context.prop;
      prop.set(Prop.WILDCARDS, ft[0].isSelected());
      prop.set(Prop.STEMMING, ft[1].isSelected());
      prop.set(Prop.DIACRITICS, ft[2].isSelected());
      prop.set(Prop.CASESENS, ft[3].isSelected());
    }
  }
}
