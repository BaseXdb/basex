package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
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
  /** Editable full-text options. */
  private DialogFT ft;
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

    String[] cb = { INFOPATHINDEX, INFOTEXTINDEX, INFOATTRINDEX, INFOFTINDEX };
    boolean[] val = { data.meta.pathindex, data.meta.txtindex,
        data.meta.atvindex, data.meta.ftxindex };
    
    BaseXBack[] panels = new BaseXBack[indexes.length];
    for(int i = 0; i < indexes.length; i++) {
      indexes[i] = new BaseXCheckBox(cb[i], val[i], 0, this);
      indexes[i].setEnabled(data instanceof DiskData);
      panels[i] = new BaseXBack();
      panels[i].setLayout(new BorderLayout());
    }

    // third tab
    final BaseXBack tab3 = new BaseXBack();
    tab3.setLayout(new GridLayout(1, 1));
    tab3.setBorder(8, 8, 8, 8);

    JComponent north = indexes[0];
    if(val[0]) {
      north = new BaseXBack();
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
      north.add(export, BorderLayout.EAST);
    }

    panels[0].add(north, BorderLayout.NORTH);
    panels[0].add(text(meta.pathindex ? data.path.info(data) :
      Token.token(PATHINDEXINFO)), BorderLayout.CENTER);
    tab3.add(panels[0]);

    // fourth tab
    final BaseXBack tab4 = new BaseXBack();
    tab4.setLayout(new GridLayout(2, 1));
    tab4.setBorder(8, 8, 0, 8);

    panels[1].add(indexes[1], BorderLayout.NORTH);
    panels[1].add(text(val[1] ? data.info(Type.TXT) :
      Token.token(TXTINDEXINFO)), BorderLayout.CENTER);
    tab4.add(panels[1]);

    panels[2].add(indexes[2], BorderLayout.NORTH);
    panels[2].add(text(val[2] ? data.info(Type.ATV) :
      Token.token(ATTINDEXINFO)), BorderLayout.CENTER);
    tab4.add(panels[2]);

    // fifth tab
    final BaseXBack tab5 = new BaseXBack();
    tab5.setLayout(new GridLayout(1, 1));
    tab5.setBorder(8, 8, 8, 8);

    panels[3].add(indexes[3], BorderLayout.NORTH);
    if(!val[3]) ft = new DialogFT(this, false);
    panels[3].add(val[3] ? text(data.info(Type.FTX)) : ft,
        BorderLayout.CENTER);
    tab5.add(panels[3]);

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
    if(ft != null) ft.action(indexes[3].isSelected());
    final Data data = gui.context.data;
    enableOK(buttons, BUTTONOPT, !data.meta.uptodate);
  }

  @Override
  public void close() {
    super.close();
    if(ft != null) ft.close();
  }
}
