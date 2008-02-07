package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.core.proc.Drop;
import org.basex.data.MetaData;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXListChooser;
import org.basex.gui.layout.BaseXTextArea;
import org.basex.io.IOConstants;
import org.basex.util.Performance;

/**
 * Open Database Dialog.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogOpen extends Dialog {
  /** List of currently available databases. */
  private BaseXListChooser choice;
  /** Information panel. */
  private BaseXLabel doc;
  /** Information panel. */
  private BaseXTextArea detail;
  /** Buttons. */
  private BaseXBack buttons;
  /** Main Memory flag. */
  private BaseXCheckBox mainmem;

  /**
   * Default Constructor.
   * @param parent parent frame
   * @param drop show drop dialog
   */
  public DialogOpen(final JFrame parent, final boolean drop) {
    super(parent, drop ? DROPTITLE : OPENTITLE);

    // create database chooser
    final String[] db = DialogOpen.getDatabases();
    
    if(db.length == 0) return;
    
    choice = new BaseXListChooser(this, db, HELPOPEN);
    set(choice, BorderLayout.CENTER);
    choice.setSize(130, 348);

    final BaseXBack info = new BaseXBack();
    info.setLayout(new BorderLayout());
    info.setBorder(new CompoundBorder(new EtchedBorder(),
        new EmptyBorder(10, 10, 10, 10)));

    doc = new BaseXLabel(DIALOGINFO);
    doc.setFont(new Font(GUIProp.font, 0, 20));
    doc.setBorder(0, 0, 5, 0);
    info.add(doc, BorderLayout.NORTH);

    detail = new BaseXTextArea(HELPOPENINFO);
    detail.setFont(GUIConstants.mfont.deriveFont(13f));
    detail.setBorder(new EmptyBorder(5, 5, 5, 5));
    detail.setOpaque(false);
    detail.setFocusable(false);
    if(db.length == 0) detail.setText(OPENNODBINFO);

    BaseXLayout.setWidth(detail, 480);
    info.add(detail, BorderLayout.CENTER);

    final BaseXBack pp = new BaseXBack();
    pp.setBorder(new EmptyBorder(0, 12, 0, 0));
    pp.setLayout(new BorderLayout());
    pp.add(info, BorderLayout.CENTER);

    // create buttons
    final BaseXBack p = new BaseXBack();
    p.setLayout(new BorderLayout());
    if(!drop) {
      mainmem = new BaseXCheckBox(CREATEMAINMEM, HELPMMEM, Prop.mainmem, this);
      p.add(mainmem, BorderLayout.WEST);
    }

    buttons = BaseXLayout.newButtons(this, true,
        new String[] { drop ? BUTTONDROP : BUTTONOPEN, BUTTONCANCEL },
        new byte[][] { drop ? HELPDROP : HELPOPENDB, HELPCANCEL });
    p.add(buttons, BorderLayout.EAST);
    pp.add(p, BorderLayout.SOUTH);

    set(pp, BorderLayout.EAST);
    setInfo();
    
    finish(parent);
  }

  /**
   * Returns the database name.
   * @return database name
   */
  public String db() {
    Prop.mainmem = mainmem.isEnabled() && mainmem.isSelected();
    final String db = choice.getValue();
    return ok && db.length() > 0 ? db : null;
  }

  /**
   * Returns if no databases have been found.
   * @return result of check
   */
  public boolean nodb() {
    return choice == null;
  }

  @Override
  public void action(final String cmd) {
    if(BUTTONOPEN.equals(cmd)) {
      close();
    } else if(BUTTONDROP.equals(cmd)) {
      final String db = choice.getValue();
      if(db.length() == 0) return;
      if(JOptionPane.showConfirmDialog(this, BaseX.info(DROPCONF, db),
          DIALOGINFO, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        Drop.drop(db);
        choice.setData(DialogOpen.getDatabases());
        choice.requestFocusInWindow();
      }
    } else {
      setInfo();
    }
  }

  @Override
  public void close() {
    if(mainmem == null) action(BUTTONDROP);
    else if(ok) dispose();
  }

  /**
   * Returns the list of available databases.
   * @return available databases.
   */
  static String[] getDatabases() {
    // create database list
    int n = 0;
    final File file = new File(Prop.dbpath);
    // no database directory found...
    if(!file.exists()) return new String[] {};

    for(final File f : file.listFiles()) if(f.isDirectory()) n++;
    final String[] db = new String[n];
    n = 0;
    for(final File f : file.listFiles()) {
      if(f.isDirectory()) db[n++] = f.getName();
    }
    return db;
  }

  /**
   * Refreshes the database information panel.
   */
  void setInfo() {
    final String db = choice.getValue().trim();
    if(db.length() == 0) return;

    doc.setText(db);

    // read the database version and stop reading when version differs
    final File dir = IOConstants.dbpath(db);
    long len = 0;
    for(final File f : dir.listFiles()) len += f.length();

    final StringBuilder txt = new StringBuilder();
    try {
      ok = true;
      final MetaData meta = new MetaData(db);
      final int size = meta.read();

      if(mainmem != null)
        BaseXLayout.enable(mainmem, meta.filesize < (1 << 30));

      txt.append(INFODOC + meta.filename + NL);
      txt.append(INFOTIME + new SimpleDateFormat(
          "dd.MM.yyyy hh:mm:ss").format(new Date(meta.time)) + NL);
      txt.append(INFODOCSIZE + (meta.filesize != 0 ?
          Performance.formatSize(meta.filesize) : "-") + NL);
      txt.append(INFODBSIZE + Performance.formatSize(len) + NL);
      txt.append(INFOENCODING + meta.encoding + NL);
      txt.append(INFONODES + size + NL);
      txt.append(INFOHEIGHT + meta.height + NL + NL);

      txt.append(INFOINDEXES + NL);
      txt.append(" " + INFOTXTINDEX + meta.txtindex + NL);
      txt.append(" " + INFOATVINDEX + meta.atvindex + NL);
      txt.append(" " + INFOWORDINDEX + meta.wrdindex + NL);
      txt.append(" " + INFOFTINDEX + meta.ftxindex + NL);

      txt.append(NL + INFOCREATE + NL);
      txt.append(" " + INFOCHOP + meta.chop + NL);
      txt.append(" " + INFOENTITIES + meta.entity + NL + NL);
    } catch(final IOException e) {
      txt.append(e.getMessage());
      ok = false;
    }
    detail.setText(txt.toString());
    BaseXLayout.enableOK(buttons, ok);
  }
}
