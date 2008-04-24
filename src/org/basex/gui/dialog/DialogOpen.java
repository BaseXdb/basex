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
import org.basex.core.proc.List;
import org.basex.data.MetaData;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXListChooser;
import org.basex.gui.layout.BaseXText;
import org.basex.io.IOConstants;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

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
  private BaseXText detail;
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
    final String[] db = List.list();
    if(db.length == 0) return;
    
    choice = new BaseXListChooser(this, db, HELPOPEN);
    set(choice, BorderLayout.CENTER);
    choice.setSize(130, 356);

    final BaseXBack info = new BaseXBack();
    info.setLayout(new BorderLayout());
    info.setBorder(new CompoundBorder(new EtchedBorder(),
        new EmptyBorder(10, 10, 10, 10)));

    doc = new BaseXLabel(DIALOGINFO);
    doc.setFont(new Font(GUIProp.font, 0, 18));
    doc.setBorder(0, 0, 5, 0);
    info.add(doc, BorderLayout.NORTH);

    detail = new BaseXText(HELPOPENINFO, false, this);
    detail.setFont(GUIConstants.mfont.deriveFont(12f));
    detail.setBorder(new EmptyBorder(5, 5, 5, 5));
    detail.setOpaque(false);
    detail.setEnabled(false);

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

    if(drop) {
      buttons = BaseXLayout.newButtons(this, true,
          new String[] { BUTTONDROP, BUTTONCANCEL },
          new byte[][] { HELPDROP, HELPCANCEL });
    } else {
      buttons = BaseXLayout.newButtons(this, true,
          new String[] { BUTTONRENAME, BUTTONOPEN, BUTTONCANCEL },
          new byte[][] { HELPRENAMEDB, HELPOPENDB, HELPCANCEL });
    }
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
    if(BUTTONRENAME.equals(cmd)) {
      new DialogRename(GUI.get(), choice.getValue());
      choice.setData(List.list());
    } else if(BUTTONOPEN.equals(cmd)) {
      close();
    } else if(BUTTONDROP.equals(cmd)) {
      final String db = choice.getValue();
      if(db.length() == 0) return;
      if(JOptionPane.showConfirmDialog(this, BaseX.info(DROPCONF, db),
          DIALOGINFO, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        Drop.drop(db);
        choice.setData(List.list());
        choice.requestFocusInWindow();
      }
    } else {
      ok = setInfo();
      BaseXLayout.enableOK(buttons, ok);
    }
  }

  @Override
  public void close() {
    if(!ok) return;
    if(mainmem == null) action(BUTTONDROP);
    else dispose();
  }

  /**
   * Refreshes the database information panel.
   * @return true if the current choice is valid. 
   */
  boolean setInfo() {
    final String db = choice.getValue().trim();
    if(db.length() == 0) return false;

    // read the database version
    final File dir = IOConstants.dbpath(db);
    if(!dir.exists()) return false;

    // read the database version and stop reading when version differs
    long len = 0;
    for(final File f : dir.listFiles()) len += f.length();

    final TokenBuilder txt = new TokenBuilder();
    try {
      ok = true;
      final MetaData meta = new MetaData(db);
      final int size = meta.read();

      if(mainmem != null)
        BaseXLayout.enable(mainmem, meta.filesize < (1 << 30));

      txt.add(INFODOC + meta.filename + NL);
      txt.add(INFOTIME + new SimpleDateFormat(
          "dd.MM.yyyy hh:mm:ss").format(new Date(meta.time)) + NL);
      txt.add(INFODOCSIZE + (meta.filesize != 0 ?
          Performance.formatSize(meta.filesize) : "-") + NL);
      txt.add(INFODBSIZE + Performance.formatSize(len) + NL);
      txt.add(INFOENCODING + meta.encoding + NL);
      txt.add(INFONODES + size + NL);
      txt.add(INFOHEIGHT + meta.height + NL + NL);

      txt.add(INFOINDEXES + NL);
      txt.add(" " + INFOTXTINDEX + meta.txtindex + NL);
      txt.add(" " + INFOATVINDEX + meta.atvindex + NL);
      txt.add(" " + INFOFTINDEX + meta.ftxindex + NL);

      txt.add(NL + INFOCREATE + NL);
      txt.add(" " + INFOCHOP + meta.chop + NL);
      txt.add(" " + INFOENTITIES + meta.entity + NL);
    } catch(final IOException e) {
      txt.add(e.getMessage());
      ok = false;
    }
    doc.setText(db);
    detail.setText(txt.finish());
    BaseXLayout.enableOK(buttons, ok);
    return true;
  }
}
