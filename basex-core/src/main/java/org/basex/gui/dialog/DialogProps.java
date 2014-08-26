package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;

import java.awt.*;

import javax.swing.event.*;

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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DialogProps extends BaseXDialog {
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
    ELEMENTS, ATTRIBUTES, PATH_INDEX, TEXT_INDEX, ATTRIBUTE_INDEX, FULLTEXT_INDEX
  };

  /** Full-text tab. */
  private final BaseXBack tabFT;
  /** Name tab. */
  private final BaseXBack tabNames;
  /** Name tab. */
  private final BaseXBack tabPath;
  /** Name tab. */
  private final BaseXBack tabValues;
  /** Contains the panels that are currently being updated. */
  private final IntList updated = new IntList();
  /** Tabbed pane. */
  private final BaseXTabs tabs;
  /** Resource panel. */
  final DialogResources resources;
  /** Add panel. */
  final DialogAdd add;
  /** Index information. */
  private final TextPanel[] infos = new TextPanel[LABELS.length];

  /** Index labels. */
  private final BaseXLabel[] labels = new BaseXLabel[LABELS.length];
  /** Index buttons. */
  private final BaseXButton[] indxs = new BaseXButton[LABELS.length];
  /** Index panels. */
  private final BaseXBack[] panels = new BaseXBack[LABELS.length];
  /** Editable full-text options. */
  private final DialogFT ft;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogProps(final GUI main) {
    super(main, DB_PROPS);
    main.setCursor(CURSORWAIT);

    panel.setLayout(new BorderLayout(5, 0));

    // resource tree
    resources = new DialogResources(this);

    // tab: resources
    add = new DialogAdd(this);
    ft = new DialogFT(this, false);
    final BaseXBack tabRes = add.border(8);

    final Data data = main.context.data();
    for(int i = 0; i < LABELS.length; ++i) {
      labels[i] = new BaseXLabel(LABELS[i]).large();
      panels[i] = new BaseXBack(new BorderLayout(0, 4));
      infos[i] = new TextPanel(Token.token(PLEASE_WAIT_D), false, this);
      infos[i].setFont(dmfont);
      BaseXLayout.setHeight(infos[i], 200);
      if(i != 1) {
        indxs[i] = new BaseXButton(" ", this);
        indxs[i].setEnabled(!data.inMemory());
      }
    }

    // tab: name indexes
    tabNames = new BaseXBack(new GridLayout(2, 1, 0, 8)).border(8);
    add(0, tabNames, null);
    add(1, tabNames, null);

    // tab: path index
    tabPath = new BaseXBack(new GridLayout(1, 1)).border(8);
    add(2, tabPath, null);

    // tab: value indexes
    tabValues = new BaseXBack(new GridLayout(2, 1, 0, 8)).border(8);
    add(3, tabValues, null);
    add(4, tabValues, null);

    // tab: full-text index
    tabFT = new BaseXBack(new GridLayout(1, 1)).border(8);
    add(5, tabFT, null);

    // tab: database info
    final BaseXBack tabGeneral = new BaseXBack(new BorderLayout(0, 8)).border(8);
    final Font f = tabGeneral.getFont();
    final BaseXLabel doc = new BaseXLabel(data.meta.name).border(0, 0, 6, 0).large();
    BaseXLayout.setWidth(doc, 400);
    tabGeneral.add(doc, BorderLayout.NORTH);

    final String db = InfoDB.db(data.meta, true, false, true);
    final TokenBuilder info = new TokenBuilder(db);
    if(data.nspaces.size() != 0) {
      info.bold().add(NL + NAMESPACES + NL).norm().add(data.nspaces.info());
    }

    final TextPanel text = new TextPanel(info.finish(), false, this);
    text.setFont(f);
    BaseXLayout.setHeight(text, 200);
    tabGeneral.add(new SearchEditor(main, text), BorderLayout.CENTER);

    tabs = new BaseXTabs(this);
    tabs.addTab(RESOURCES, tabRes);
    tabs.addTab(NAMES, tabNames);
    tabs.addTab(PATH_INDEX, tabPath);
    tabs.addTab(INDEXES, tabValues);
    tabs.addTab(FULLTEXT, tabFT);
    tabs.addTab(GENERAL, tabGeneral);

    tabs.addChangeListener(new ChangeListener() {
      @Override
      public synchronized void stateChanged(final ChangeEvent evt) {
        updateInfo();
      }
    });

    set(resources, BorderLayout.WEST);
    set(tabs, BorderLayout.CENTER);

    action(this);
    setResizable(true);

    main.setCursor(CURSORARROW);
    finish(null);
  }

  /**
   * Updates the currently visible index panel.
   */
  private synchronized void updateInfo() {
    final Object o = tabs.getSelectedComponent();
    final IntList il = new IntList();
    if(o == tabNames) {
      il.add(0);
      il.add(1);
    } else if(o == tabPath) {
      il.add(2);
    } else if(o == tabValues) {
      il.add(3);
      il.add(4);
    } else if(o == tabFT) {
      il.add(5);
    }

    final Data data = gui.context.data();
    final boolean[] val = { true, true, true, data.meta.textindex, data.meta.attrindex,
        data.meta.ftxtindex };
    for(int i = 0; i < il.size(); i++) {
      final int idx = il.get(i);
      if(updated.contains(idx)) continue;
      updated.add(idx);
      new Thread() {
        @Override
        public void run() {
          infos[idx].setText(val[idx] ? data.info(TYPES[idx]) : Token.token(HELP[idx]));
          updated.delete(idx);
        }
      }.start();
    }
  }

  /**
   * Adds index information to the specified panel and tab.
   * @param i index offset
   * @param tab panel tab
   * @param info optional info to display
   */
  private void add(final int i, final BaseXBack tab, final BaseXBack info) {
    final BaseXBack idx = new BaseXBack(new BorderLayout(8, 0));
    idx.add(labels[i], BorderLayout.WEST);
    if(indxs[i] != null) idx.add(indxs[i], BorderLayout.EAST);
    panels[i].add(idx, BorderLayout.NORTH);

    final BaseXBack b = info != null ? info : new SearchEditor(gui, infos[i]);
    panels[i].add(b, BorderLayout.CENTER);
    tab.add(panels[i]);
  }

  @Override
  public void action(final Object cmp) {
    if(cmp != null) {
      for(int i = 0; i < LABELS.length; i++) {
        if(cmp != indxs[i]) continue;
        final String label = indxs[i].getText();
        final Command cmd;
        if(label.equals(OPTIMIZE + DOTS)) {
          cmd = new Optimize();
        } else if(label.equals(DROP + DOTS)) {
          cmd = new DropIndex(TYPES[i]);
        } else {
          cmd = new CreateIndex(TYPES[i]);
          ft.setOptions();
        }
        infos[i].setText(PLEASE_WAIT_D);
        DialogProgress.execute(this, cmd);
        return;
      }
    }

    resources.action(cmp);
    add.action(cmp);

    final Data data = gui.context.data();
    final boolean[] val = {
      true, true, true, data.meta.textindex, data.meta.attrindex, data.meta.ftxtindex
    };

    if(cmp == this) {
      final boolean utd = data.meta.uptodate;
      for(int i = 0; i < LABELS.length; ++i) {
        // structural index/statistics?
        final boolean struct = i < 3;
        String lbl = LABELS[i];
        if(struct && !utd) lbl += " (" + OUT_OF_DATE + ')';
        // updates labels and infos
        labels[i].setText(lbl);
        // update button (label, disable/enable)
        if(indxs[i] != null) {
          indxs[i].setText((struct ? OPTIMIZE : val[i] ? DROP : CREATE) + DOTS);
          if(struct) indxs[i].setEnabled(!utd);
        }
      }
      // full-text options
      tabFT.removeAll();
      final int f = 5;
      panels[f].removeAll();
      add(f, tabFT, val[f] ? null : ft);
      panels[f].revalidate();
      panels[f].repaint();
      updateInfo();
    }

    ft.action(true);
  }

  @Override
  public void close() {
    super.close();
    ft.setOptions();
  }
}
