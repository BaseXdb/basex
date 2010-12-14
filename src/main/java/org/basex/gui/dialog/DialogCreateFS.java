/*
package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.cmd.List;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;
import org.basex.util.StringList;
import org.basex.util.Util;

/**
 * Dialog window for specifying the options for importing a file system.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen

public final class DialogCreateFS extends Dialog {
  /** Available databases.
  private final StringList db;
  /** Database info.
  private final BaseXLabel info;
  /** Parsing complete filesystem.
  private final BaseXCheckBox all;
  /** Browse button.
  private final BaseXButton browse;
  /** ID3 parsing.
  private final BaseXCheckBox meta;
  /** Context inclusion.
  private final BaseXCheckBox cont;
  /** XML inclusion.
  private final BaseXCheckBox xml;
  /** Button panel.
  private final BaseXBack buttons;
  /** ComboBox.
  private final BaseXCombo maxsize;

  /** Path summary flag.
  private final BaseXCheckBox pathindex;
  /** Text index flag.
  private final BaseXCheckBox txtindex;
  /** Attribute value index flag.
  private final BaseXCheckBox atvindex;
  /** Full-text index flag.
  private final BaseXCheckBox ftxindex;
  /** Editable full-text options.
  private final DialogFT ft;

  /** Directory path.
  final BaseXTextField path;
  /** Database name.
  final BaseXTextField dbname;

  /**
   * Default constructor.
   * @param main reference to the main window

  public DialogCreateFS(final GUI main) {
    super(main, CREATEFSTITLE);
    db = List.list(main.context);

    // create panels
    final BaseXBack p1 = new BaseXBack(new BorderLayout()).border(8);
    BaseXBack p = new BaseXBack(new TableLayout(7, 2, 6, 0));

    final Prop prop = gui.context.prop;
    final GUIProp gprop = gui.gprop;

    p.add(new BaseXLabel(IMPORTFSTEXT, false, true).border(0, 0, 4, 0));
    p.add(new BaseXLabel());

    path = new BaseXTextField(gprop.get(GUIProp.FSPATH), this);
    path.addKeyListener(keys);
    p.add(path);

    browse = new BaseXButton(BUTTONBROWSE, this);
    browse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final IO file = new BaseXFileChooser(DIALOGFC, path.getText(),
            main).select(BaseXFileChooser.Mode.DOPEN);
        if(file != null) {
          path.setText(file.path());
          dbname.setText(file.dbname().replaceAll("[^\\w-]", ""));
        }
      }
    });
    p.add(browse);
    p.add(new BaseXLabel(CREATENAME, false, true).border(8, 0, 4, 0));

    all = new BaseXCheckBox(IMPORTALL, gprop.is(GUIProp.FSALL), this);
    all.setToolTipText(IMPORTALLINFO);
    all.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        action(null);
      }
    });
    p.add(all);

    dbname = new BaseXTextField(gprop.get(GUIProp.FSNAME), this);
    dbname.addKeyListener(keys);
    p.add(dbname);
    p1.add(p, BorderLayout.CENTER);

    info = new BaseXLabel(" ");
    p1.add(info, BorderLayout.SOUTH);

    // Metadata panel
    final BaseXBack p2 = new BaseXBack(new TableLayout(4, 1)).border(8);

    // Include metadata checkbox
    BaseXLabel label = new BaseXLabel(IMPORTFSTEXT1, false, true);
    p2.add(label);
    meta = new BaseXCheckBox(IMPORTMETA, prop.is(Prop.FSMETA), 12, this);
    p2.add(meta);

    label = new BaseXLabel(IMPORTFSTEXT2, false, true);
    p2.add(label);

    p = new BaseXBack(new BorderLayout());

    cont = new BaseXCheckBox(IMPORTCONT, prop.is(Prop.FSCONT), this);
    cont.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        action(null);
      }
    });
    p.add(cont, BorderLayout.WEST);

    xml = new BaseXCheckBox(IMPORTXML, prop.is(Prop.FSXML), this);
    xml.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        action(null);
      }
    });
    p.add(xml, BorderLayout.SOUTH);

    maxsize = new BaseXCombo(this, IMPORTFSMAX);

    final int m = prop.num(Prop.FSTEXTMAX);
    int i = -1;
    while(++i < IMPORTFSMAXSIZE.length - 1) {
      if(IMPORTFSMAXSIZE[i] == m) break;
    }
    maxsize.setSelectedIndex(i);

    p.add(maxsize, BorderLayout.EAST);
    BaseXLayout.setWidth(p, p2.getPreferredSize().width);
    p2.add(p);

    final BaseXBack p3 = new BaseXBack(new TableLayout(6, 1, 0, 0)).border(8);
    txtindex = new BaseXCheckBox(INFOTEXTINDEX,
        prop.is(Prop.TEXTINDEX), 0, this);
    p3.add(txtindex);
    p3.add(new BaseXLabel(TXTINDEXINFO, true, false));

    atvindex = new BaseXCheckBox(INFOATTRINDEX,
        prop.is(Prop.ATTRINDEX), 0, this);
    p3.add(atvindex);
    p3.add(new BaseXLabel(ATTINDEXINFO, true, false));

    pathindex = new BaseXCheckBox(INFOPATHINDEX,
        prop.is(Prop.PATHINDEX), 0, this);
    p3.add(pathindex);
    p3.add(new BaseXLabel(PATHINDEXINFO, true, false));

    final BaseXBack p4 = new BaseXBack(new TableLayout(2, 1, 0, 0)).border(8);
    ftxindex = new BaseXCheckBox(INFOFTINDEX, prop.is(Prop.FTINDEX), 0, this);
    p4.add(ftxindex);

    ft = new DialogFT(this, true);
    p4.add(ft);

    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.addTab(GENERALINFO, p1);
    tabs.addTab(METAINFO, p2);
    tabs.addTab(INDEXINFO, p3);
    tabs.addTab(FTINFO, p4);
    set(tabs, BorderLayout.CENTER);

    // create buttons
    buttons = okCancel(this);
    set(buttons, BorderLayout.SOUTH);

    action(null);
    finish(null);
  }

  @Override
  public void action(final Object cmp) {
    ft.action(ftxindex.isSelected());

    final boolean sel = !all.isSelected();
    path.setEnabled(sel);
    browse.setEnabled(sel);
    maxsize.setEnabled(cont.isSelected());

    final Prop prop = gui.context.prop;
    final GUIProp gprop = gui.gprop;
    final String nm = dbname.getText().trim();
    final boolean cNam = !nm.isEmpty();
    if(cNam) gprop.set(GUIProp.FSNAME, nm);
    ok = cNam;

    boolean cAll = all.isSelected();
    if(cAll) gprop.set(GUIProp.FSPATH, path.getText());

    if(!cAll && cNam) {
      final String p = path.getText().trim();
      final IO file = IO.get(p);
      cAll = !p.isEmpty() && file.exists();
    }
    ok &= cAll;

    String inf = null;

    Msg icon = Msg.ERROR;
    if(!ok) {
      if(!cAll) inf = PATHWHICH;
      if(!cNam) inf = DBWHICH;
    } else {
      ok = Command.validName(nm);
      if(!ok) {
        inf = Util.info(INVALID, EDITNAME);
      } else if(db.contains(nm)) {
        inf = prop.is(Prop.FUSE) ? RENAMEOVERBACKING : RENAMEOVER;
        icon = Msg.WARN;
      }
    }

    if(ok) {
      gprop.set(GUIProp.FSALL, all.isSelected());
      gprop.set(GUIProp.FSPATH, path.getText());
      gprop.set(GUIProp.FSNAME, dbname.getText());
    }

    info.setText(inf, icon);
    enableOK(buttons, BUTTONOK, ok);
  }

  @Override
  public void close() {
    if(!ok) return;
    super.close();

    gui.set(Prop.FSCONT, cont.isSelected());
    gui.set(Prop.FSMETA, meta.isSelected());
    gui.set(Prop.FSXML, xml.isSelected());
    gui.set(Prop.FSTEXTMAX, IMPORTFSMAXSIZE[maxsize.getSelectedIndex()]);
    gui.set(Prop.PATHINDEX, pathindex.isSelected());
    gui.set(Prop.TEXTINDEX, txtindex.isSelected());
    gui.set(Prop.ATTRINDEX, atvindex.isSelected());
    gui.set(Prop.FTINDEX, ftxindex.isSelected());
    ft.close();
  }
}
*/