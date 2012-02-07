package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.index.IndexToken.IndexType;
import org.basex.util.*;

/**
 * Database properties dialog.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogProps extends Dialog {
  /** Index types. */
  private static final String[] HELP = {
    "", "", H_PATH_INDEX, H_TEXT_INDEX, H_ATTR_INDEX, ""
  };
  /** Index types. */
  private static final IndexType[] TYPES = {
    IndexType.TAG, IndexType.ATTNAME, IndexType.PATH,
    IndexType.TEXT, IndexType.ATTRIBUTE, IndexType.FULLTEXT
  };
  /** Label strings. */
  private static final String[] LABELS = {
      ELEMENTS, ATTRIBUTES, PATH_INDEX, TEXT_INDEX,
      ATTRIBUTE_INDEX, FULLTEXT_INDEX };
  /** Index labels. */
  private final BaseXLabel[] labels = new BaseXLabel[LABELS.length];
  /** Index buttons. */
  private final BaseXButton[] indxs = new BaseXButton[LABELS.length];
  /** Index information. */
  private final BaseXEditor[] infos = new BaseXEditor[LABELS.length];
  /** Index panels. */
  private final BaseXBack[] panels = new BaseXBack[LABELS.length];
  /** Full-text tab. */
  private final BaseXBack tabFT;
  /** Editable full-text options. */
  private final DialogFT ft;

  /** Resource panel. */
  final DialogResources resources;
  /** Add panel. */
  final DialogAdd add;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogProps(final GUI main) {
    super(main, DB_PROPS);
    panel.setLayout(new BorderLayout(5, 0));

    // resource tree
    resources = new DialogResources(this);

    // tab: database info
    final Data data = gui.context.data();
    final BaseXBack tabInfo = new BaseXBack(new BorderLayout(0, 8)).border(8);
    final Font f = tabInfo.getFont();
    final BaseXLabel doc = new BaseXLabel(data.meta.name).border(
        0, 0, 6, 0).large();
    BaseXLayout.setWidth(doc, 400);
    tabInfo.add(doc, BorderLayout.NORTH);

    final String db = InfoDB.db(data.meta, true, false, true);
    final TokenBuilder info = new TokenBuilder(db);
    if(data.nspaces.size() != 0) {
      info.bold().add(NL + NAMESPACES + NL).norm().add(data.nspaces.info());
    }

    final BaseXEditor text = text(info.finish());
    text.setFont(f);
    tabInfo.add(text, BorderLayout.CENTER);

    // tab: resources
    add = new DialogAdd(this);
    ft = new DialogFT(this, false);
    final BaseXBack tabRes = add.border(8);

    for(int i = 0; i < LABELS.length; ++i) {
      String lbl = LABELS[i];
      if(!data.meta.uptodate) lbl += " (" + OUT_OF_DATE + ')';
      labels[i] = new BaseXLabel(lbl).large();
      panels[i] = new BaseXBack(new BorderLayout(0, 4));
      infos[i] = new BaseXEditor(false, this);
      BaseXLayout.setHeight(infos[i], 200);
      if(i >= 2) {
        indxs[i] = new BaseXButton("", this);
        indxs[i].setEnabled(data instanceof DiskData);
      }
    }

    // tab: name indexes
    final BaseXBack tabNames =
        new BaseXBack(new GridLayout(2, 1, 0, 8)).border(8);
    add(0, tabNames, null);
    add(1, tabNames, null);

    // tab: path index
    final BaseXBack tabPath = new BaseXBack(new GridLayout(1, 1)).border(8);
    add(2, tabPath, null);

    // tab: value indexes
    final BaseXBack tabValues =
        new BaseXBack(new GridLayout(2, 1, 0, 8)).border(8);
    add(3, tabValues, null);
    add(4, tabValues, null);

    // tab: full-text index
    tabFT = new BaseXBack(new GridLayout(1, 1)).border(8);
    add(5, tabFT, null);

    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.addTab(GENERAL, tabInfo);
    tabs.addTab(RESOURCES, tabRes);
    tabs.addTab(NAMES, tabNames);
    tabs.addTab(PATH_INDEX, tabPath);
    tabs.addTab(INDEXES, tabValues);
    tabs.addTab(FULLTEXT, tabFT);

    set(resources, BorderLayout.WEST);
    set(tabs, BorderLayout.CENTER);

    action(this);
    setResizable(true);
    setMinimumSize(getPreferredSize());
    finish(null);
  }

  /**
   * Adds index information to the specified panel and tab.
   * @param p index offset
   * @param tab panel tab
   * @param info optional info to display
   */
  private void add(final int p, final BaseXBack tab, final BaseXBack info) {
    final BaseXBack idx = new BaseXBack(new BorderLayout(8, 0));
    idx.add(labels[p], BorderLayout.WEST);
    if(indxs[p] != null) idx.add(indxs[p], BorderLayout.EAST);
    panels[p].add(idx, BorderLayout.NORTH);
    panels[p].add(info != null ? info : infos[p], BorderLayout.CENTER);
    tab.add(panels[p]);
  }

  /**
   * Returns a text box.
   * @param txt contents
   * @return text box
   */
  private BaseXEditor text(final byte[] txt) {
    final BaseXEditor text = new BaseXEditor(false, this);
    text.setText(txt);
    BaseXLayout.setHeight(text, 200);
    return text;
  }

  @Override
  public void action(final Object cmp) {
    for(int i = 0; i < LABELS.length; i++) {
      if(indxs[i] == null || cmp != indxs[i]) continue;
      final Command cmd = indxs[i].getText().equals(DROP + DOTS) ?
          new DropIndex(TYPES[i]) : new CreateIndex(TYPES[i]);
      DialogProgress.execute(this, "", cmd);
      return;
    }

    resources.action(cmp);
    add.action(cmp);

    final Data data = gui.context.data();
    final boolean[] val = {
        true, true, data.meta.pathindex, data.meta.textindex,
        data.meta.attrindex, data.meta.ftxtindex
    };

    if(cmp == this) {
      for(int i = 0; i < LABELS.length; ++i) {
        String lbl = LABELS[i];
        if(i < 3 && !data.meta.uptodate) lbl += " (" + OUT_OF_DATE + ')';
        labels[i].setText(lbl);
        infos[i].setText(val[i] ? data.info(TYPES[i]) : Token.token(HELP[i]));
        if(indxs[i] != null) indxs[i].setText((val[i] ? DROP : CREATE) + DOTS);
      }
      // full-text options
      tabFT.removeAll();
      panels[5].removeAll();
      add(5, tabFT, val[5] ? null : ft);
      panels[5].revalidate();
      panels[5].repaint();
    }

    ft.action();
  }

  @Override
  public void close() {
    super.close();
    ft.setOptions();
  }
}
