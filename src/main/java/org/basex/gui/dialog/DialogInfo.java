package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.basex.core.cmd.InfoDB;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.basex.data.MetaData;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXEditor;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTabs;
import org.basex.index.IndexToken.IndexType;
import org.basex.io.IO;
import org.basex.io.out.PrintOutput;
import org.basex.io.serial.Serializer;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Info database dialog.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DialogInfo extends Dialog {
  /** Index checkboxes. */
  private final BaseXCheckBox[] indexes = new BaseXCheckBox[4];
  /** Editable full-text options. */
  private DialogFT ft;
  /** Button panel. */
  private final BaseXBack buttons;
  /** Optimize button. */
  private final Object optimize;

  /** Optimize flag. */
  public boolean opt;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogInfo(final GUI main) {
    super(main, INFODB);

    // first tab
    final BaseXBack tab1 = new BaseXBack(new BorderLayout());
    tab1.setBorder(new CompoundBorder(new EtchedBorder(),
        new EmptyBorder(8, 8, 8, 8)));

    final Data data = gui.context.data();
    final MetaData meta = data.meta;

    final Font f = tab1.getFont();
    final BaseXLabel doc = new BaseXLabel(meta.name).border(0, 0, 5, 0);
    doc.setFont(f.deriveFont(f.getSize2D() + 7f));
    tab1.add(doc, BorderLayout.NORTH);

    final byte[] db = InfoDB.db(meta, true, false, true);
    final TokenBuilder info = new TokenBuilder(db);
    if(data.nspaces.size() != 0) {
      info.bold().add(NL + INFONS + NL).norm().add(data.nspaces.info());
    }

    final BaseXEditor text = text(info.finish());
    text.setFont(f);
    tab1.add(text, BorderLayout.CENTER);

    // second tab
    final BaseXBack tab2 = new BaseXBack(new GridLayout(2, 1, 0, 8)).border(8);
    tab2.add(addIndex(true, data));
    tab2.add(addIndex(false, data));

    final String[] cb = {
        INFOPATHINDEX, INFOTEXTINDEX, INFOATTRINDEX, INFOFTINDEX };
    final boolean[] val = { data.meta.pathindex, data.meta.textindex,
        data.meta.attrindex, data.meta.ftxtindex };

    final BaseXBack[] panels = new BaseXBack[indexes.length];
    for(int i = 0; i < indexes.length; ++i) {
      indexes[i] = new BaseXCheckBox(cb[i], val[i], 0, this);
      indexes[i].setEnabled(data instanceof DiskData);
      panels[i] = new BaseXBack(new BorderLayout());
    }

    // third tab
    final BaseXBack tab3 = new BaseXBack(new GridLayout(1, 1)).border(8);
    JComponent north = indexes[0];
    if(val[0]) {
      north = new BaseXBack(new BorderLayout());
      north.add(indexes[0], BorderLayout.WEST);
      final BaseXButton export = new BaseXButton(GUIEXPORT, this);
      export.setMnemonic();
      export.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          final IO file = save(gui, true);
          if(file != null) {
            PrintOutput po = null;
            try {
              po = new PrintOutput(file.path());
              data.pthindex.plan(Serializer.get(po));
            } catch(final IOException ex) {
              Dialog.error(gui, NOTSAVED);
            } finally {
              if(po != null) try { po.close(); } catch(final IOException x) { }
            }
          }
        }
      });
      north.add(export, BorderLayout.EAST);
    }

    panels[0].add(north, BorderLayout.NORTH);
    panels[0].add(text(val[0] ? data.info(IndexType.PATH) :
      Token.token(PATHINDEXINFO)), BorderLayout.CENTER);
    tab3.add(panels[0]);

    // fourth tab
    final BaseXBack tab4 = new BaseXBack(new GridLayout(2, 1)).border(8);
    panels[1].add(indexes[1], BorderLayout.NORTH);
    panels[1].add(text(val[1] ? data.info(IndexType.TEXT) :
      Token.token(TXTINDEXINFO)), BorderLayout.CENTER);
    tab4.add(panels[1]);

    panels[2].add(indexes[2], BorderLayout.NORTH);
    panels[2].add(text(val[2] ? data.info(IndexType.ATTRIBUTE) :
      Token.token(ATTINDEXINFO)), BorderLayout.CENTER);
    tab4.add(panels[2]);

    // fifth tab
    final BaseXBack tab5 = new BaseXBack(new GridLayout(1, 1)).border(8);
    panels[3].add(indexes[3], BorderLayout.NORTH);
    if(!val[3]) ft = new DialogFT(this, false);
    panels[3].add(val[3] ? text(data.info(IndexType.FULLTEXT)) : ft,
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
    buttons = newButtons(this, optimize, BUTTONOK, BUTTONCANCEL);
    set(buttons, BorderLayout.SOUTH);

    action(null);
    setResizable(true);
    setMinimumSize(getPreferredSize());
    finish(null);
  }

  /**
   * Displays a file save dialog and returns the file name or {@code null}
   * if dialog was canceled.
   * @param gui gui reference
   * @param single file vs directory dialog
   * @return io reference
   */
  static IO save(final GUI gui, final boolean single) {
    // open file chooser for XML creation
    final BaseXFileChooser fc = new BaseXFileChooser(GUISAVEAS,
        gui.gprop.get(GUIProp.SAVEPATH), gui);
    fc.addFilter(CREATEXMLDESC, IO.XMLSUFFIX);

    final IO file = fc.select(single ? BaseXFileChooser.Mode.FSAVE :
      BaseXFileChooser.Mode.DSAVE);
    if(file != null) gui.gprop.set(GUIProp.SAVEPATH, file.path());
    return file;
  }

  /**
   * Adds an index panel.
   * @param tag tag/attribute flag
   * @param data data reference
   * @return panel
   */
  private BaseXBack addIndex(final boolean tag, final Data data) {
    final BaseXBack p = new BaseXBack(new BorderLayout());
    String lbl = tag ? INFOTAGS : INFOATTS;
    if(!data.meta.uptodate) lbl += " (" + INFOOUTOFDATED + ")";
    p.add(new BaseXLabel(lbl, false, true), BorderLayout.NORTH);
    final IndexType index = tag ? IndexType.TAG : IndexType.ATTNAME;
    p.add(text(data.info(index)), BorderLayout.CENTER);
    return p;
  }

  /**
   * Returns a text box.
   * @param txt contents
   * @return text box
   */
  private BaseXEditor text(final byte[] txt) {
    final BaseXEditor text = new BaseXEditor(false, this);
    text.setText(txt);
    text.setPreferredSize(new Dimension(550, 160));
    return text;
  }

  /**
   * Returns an array with the chosen indexes.
   * @return check box
   */
  public boolean[] indexes() {
    final boolean[] in = new boolean[indexes.length];
    for(int i = 0; i < indexes.length; ++i) in[i] = indexes[i].isSelected();
    return in;
  }

  @Override
  public void action(final Object cmp) {
    opt = cmp == optimize;
    if(opt) close();
    if(ft != null) ft.action(indexes[3].isSelected());
    enableOK(buttons, BUTTONOPT, !gui.context.data().meta.uptodate);
  }

  @Override
  public void close() {
    super.close();
    if(ft != null) ft.close();
  }
}
