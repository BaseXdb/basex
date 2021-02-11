package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;

import java.awt.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;
import org.basex.index.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Database properties dialog.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DialogProps extends BaseXDialog {
  /** Index types. */
  private static final String[] HELP = {
    "", "", H_PATH_INDEX, H_TEXT_INDEX, H_ATTR_INDEX, H_TOKEN_INDEX, ""
  };
  /** Index types. */
  private static final IndexType[] TYPES = {
    IndexType.ELEMNAME, IndexType.ATTRNAME, IndexType.PATH, IndexType.TEXT, IndexType.ATTRIBUTE,
    IndexType.TOKEN, IndexType.FULLTEXT
  };
  /** Label strings. */
  private static final String[] LABELS = {
    ELEMENTS, ATTRIBUTES, PATH_INDEX, TEXT_INDEX, ATTRIBUTE_INDEX, TOKEN_INDEX, FULLTEXT_INDEX
  };

  /** Full-text tab. */
  private final BaseXBack ftPanel;
  /** Name tab. */
  private final BaseXBack namesPanel;
  /** Name tab. */
  private final BaseXBack pathsPanel;
  /** Name tab. */
  private final BaseXBack indexesPanel;
  /** Contains the panels that are currently being updated. */
  private final IntList updated = new IntList();
  /** Tabbed pane. */
  private final BaseXTabs tabs;
  /** Options dialog. */
  private final DialogOptions optionsPanel;
  /** Index information panel. */
  private final TextPanel[] infos = new TextPanel[LABELS.length];
  /** Database info. */
  private final TextPanel dbInfo;
  /** Namespace info. */
  private final TextPanel nsInfo;

  /** Optimize button. */
  private final BaseXButton optimize;
  /** Optimize all button. */
  private final BaseXButton optimizeAll;

  /** Index labels. */
  private final BaseXLabel[] labels = new BaseXLabel[LABELS.length];
  /** Index buttons. */
  private final BaseXButton[] buttons = new BaseXButton[LABELS.length];
  /** Index panels. */
  private final BaseXBack[] panels = new BaseXBack[LABELS.length];
  /** Index creation panels. */
  private final DialogIndex[] indexes = new DialogIndex[LABELS.length];

  /** Add panel. */
  DialogAdd addPanel;
  /** Resource panel. */
  DialogResources resources;

  /**
   * Default constructor.
   * @param gui reference to the main window
   */
  public DialogProps(final GUI gui) {
    super(gui, DB_PROPS);

    panel.setLayout(new BorderLayout(5, 0));

    // resource tree
    resources = new DialogResources(this);
    resources.setPreferredSize(new Dimension(300, 1));
    set(resources, BorderLayout.WEST);

    // tabs
    addPanel = new DialogAdd(this);
    addPanel.border(8);

    final int ll = LABELS.length;
    for(int l = 0; l < ll; l++) {
      labels[l] = new BaseXLabel(LABELS[l]).large();
      panels[l] = new BaseXBack(new BorderLayout(0, 4));
      BaseXLayout.setWidth(panels[l], 600);
      infos[l] = new TextPanel(this, PLEASE_WAIT_D, false);
      infos[l].setFont(dmfont);
    }
    // create/drop buttons for values indexes
    for(int l = IndexType.TEXT.ordinal(); l < ll; l++) {
      buttons[l] = new BaseXButton(this, " ");
      BaseXLayout.setHeight(panels[l], 160);
    }
    // no full-text index in main-memory mode
    final Data data = gui.context.data();
    buttons[IndexType.FULLTEXT.ordinal()].setEnabled(!data.inMemory());

    // alternative panels
    indexes[IndexType.TEXT.ordinal()] = new DialogValues(this, IndexType.TEXT);
    indexes[IndexType.ATTRIBUTE.ordinal()] = new DialogValues(this, IndexType.ATTRIBUTE);
    indexes[IndexType.TOKEN.ordinal()] = new DialogValues(this, IndexType.TOKEN);
    indexes[IndexType.FULLTEXT.ordinal()] = new DialogFT(this, false);

    // name indexes
    namesPanel = new BaseXBack(new GridLayout(2, 1, 0, 8)).border(8);
    namesPanel.add(panels[IndexType.ELEMNAME.ordinal()]);
    namesPanel.add(panels[IndexType.ATTRNAME.ordinal()]);
    // path index
    pathsPanel = panels[IndexType.PATH.ordinal()].border(8);
    // indexes
    indexesPanel = new BaseXBack(new GridLayout(3, 1, 0, 8)).border(8);
    indexesPanel.add(panels[IndexType.TEXT.ordinal()]);
    indexesPanel.add(panels[IndexType.ATTRIBUTE.ordinal()]);
    indexesPanel.add(panels[IndexType.TOKEN.ordinal()]);
    // full-text index
    ftPanel = panels[IndexType.FULLTEXT.ordinal()].border(8);

    // info panel
    final BaseXBack infoPanel = new BaseXBack(new GridLayout(2, 1)).border(8);

    final BaseXBack dbPanel = new BaseXBack(new BorderLayout());
    final BaseXLabel db = new BaseXLabel().border(0, 0, 6, 0).large();
    dbPanel.add(db.setChoppedText(DATABASE + COLS + data.meta.name, 600), BorderLayout.NORTH);

    dbInfo = new TextPanel(this, "", false);
    dbInfo.setFont(infoPanel.getFont());
    dbInfo.setPreferredSize(new Dimension(600, 1));
    dbPanel.add(new SearchEditor(gui, dbInfo), BorderLayout.CENTER);

    final BaseXBack nsPanel = new BaseXBack(new BorderLayout());
    nsPanel.add(new BaseXLabel(NAMESPACES).border(0, 0, 6, 0).large(), BorderLayout.NORTH);

    nsInfo = new TextPanel(this, "", false);
    nsPanel.add(new SearchEditor(gui, nsInfo), BorderLayout.CENTER);

    infoPanel.add(dbPanel);
    infoPanel.add(nsPanel);

    // options panel
    optionsPanel = new DialogOptions(this, data);

    final BaseXBack tabsPanel = new BaseXBack(new BorderLayout());
    tabs = new BaseXTabs(this);
    tabs.addTab(RESOURCES, addPanel);
    tabs.addTab(NAMES, namesPanel);
    tabs.addTab(PATHS, pathsPanel);
    tabs.addTab(INDEXES, indexesPanel);
    tabs.addTab(FULLTEXT, ftPanel);
    tabs.addTab(OPTIONS, optionsPanel);
    tabs.addTab(INFORMATION, infoPanel);

    tabs.addChangeListener(evt -> updateInfo());
    tabsPanel.add(tabs, BorderLayout.CENTER);

    optimize = new BaseXButton(this, OPTIMIZE);
    optimizeAll = new BaseXButton(this, OPTIMIZE_ALL);
    optimizeAll.setEnabled(!gui.context.data().inMemory());
    tabsPanel.add(newButtons(optimize, optimizeAll), BorderLayout.SOUTH);

    set(tabsPanel, BorderLayout.CENTER);

    action(this);
    setResizable(true);
    finish();
  }

  /**
   * Updates the currently visible index panel.
   */
  private synchronized void updateInfo() {
    final Object o = tabs.getSelectedComponent();
    final IntList il = new IntList();
    if(o == namesPanel) {
      il.add(IndexType.ELEMNAME.ordinal());
      il.add(IndexType.ATTRNAME.ordinal());
    } else if(o == pathsPanel) {
      il.add(IndexType.PATH.ordinal());
    } else if(o == indexesPanel) {
      il.add(IndexType.TEXT.ordinal());
      il.add(IndexType.ATTRIBUTE.ordinal());
      il.add(IndexType.TOKEN.ordinal());
    } else if(o == ftPanel) {
      il.add(IndexType.FULLTEXT.ordinal());
    }

    final Data data = gui.context.data();
    final boolean[] val = {
      true, true, true, data.meta.textindex, data.meta.attrindex, data.meta.tokenindex,
      data.meta.ftindex
    };
    final int is = il.size();
    for(int i = 0; i < is; i++) {
      final int idx = il.get(i);
      if(updated.contains(idx)) continue;
      updated.add(idx);

      SwingUtilities.invokeLater(() -> {
        infos[idx].setText(val[idx] ? data.info(TYPES[idx], gui.context.options) :
          Token.token(HELP[idx]));
        updated.removeAll(idx);
      });
    }
  }

  /**
   * Adds index information to the specified panel and tab.
   * @param i index offset
   * @param alt alternative body component (if {@code null}, {@link #infos} will be displayed)
   */
  private void add(final int i, final BaseXBack alt) {
    final BaseXBack header = new BaseXBack(new BorderLayout(8, 0));
    header.add(labels[i], BorderLayout.WEST);
    if(buttons[i] != null) header.add(buttons[i], BorderLayout.EAST);
    panels[i].removeAll();
    panels[i].add(header, BorderLayout.NORTH);
    panels[i].add(alt != null ? alt : new SearchEditor(gui, infos[i]), BorderLayout.CENTER);
    panels[i].revalidate();
    panels[i].repaint();
  }

  @Override
  public void action(final Object cmp) {
    if(resources == null) return;

    final Data data = gui.context.data();
    final boolean[] exists = { true, true, true, data.meta.textindex, data.meta.attrindex,
        data.meta.tokenindex, data.meta.ftindex };

    Command cmd = null;
    if(cmp == optimize) {
      cmd = new Optimize();
    } else if(cmp == optimizeAll) {
      cmd = new OptimizeAll();
      optionsPanel.setOptions(data);
    } else {
      final int bl = buttons.length;
      for(int b = 0; b < bl; b++) {
        if(cmp == buttons[b]) {
          if(exists[b]) {
            cmd = new DropIndex(TYPES[b]);
          } else {
            cmd = new CreateIndex(TYPES[b]);
            indexes[b].setOptions();
          }
          infos[b].setText(PLEASE_WAIT_D);
        }
      }
    }
    if(cmd != null) {
      DialogProgress.execute(this, cmd);
      return;
    }

    resources.action(cmp);
    addPanel.action(cmp);

    final boolean outofdate = !data.meta.uptodate;
    if(cmp == this) {
      final int ll = LABELS.length;
      for(int l = 0; l < ll; ++l) {
        // structural index/statistics?
        // updates labels and infos
        labels[l].setText(l < IndexType.TEXT.ordinal() && outofdate
            ? LABELS[l] + " (" + OUT_OF_DATE + ')' : LABELS[l]);
        // update button
        if(buttons[l] != null) buttons[l].setText(exists[l] ? DROP : CREATE);
        add(l, indexes[l] == null || exists[l] ? null : indexes[l]);
      }
      updateInfo();
    }

    for(final DialogIndex index : indexes) {
      if(index != null) index.action(true);
    }

    dbInfo.setText(InfoDB.db(data.meta, true, false));
    nsInfo.setText(data.nspaces.info());

    optimize.setEnabled(outofdate);
    optimizeAll.setEnabled(!data.inMemory());
  }

  @Override
  public void close() {
    for(final DialogIndex index : indexes) {
      if(index != null) index.setOptions();
    }
    super.close();
  }

  @Override
  public void dispose() {
    resources = null;
    addPanel = null;
    super.dispose();
  }
}
