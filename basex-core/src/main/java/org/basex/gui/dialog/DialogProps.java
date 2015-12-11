package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;

import java.awt.*;

import javax.swing.*;
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
 * @author BaseX Team 2005-15, BSD License
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
  /** Index information panel. */
  private final TextPanel[] infos = new TextPanel[LABELS.length];

  /** Index labels. */
  private final BaseXLabel[] labels = new BaseXLabel[LABELS.length];
  /** Index buttons. */
  private final BaseXButton[] buttons = new BaseXButton[LABELS.length];
  /** Index panels. */
  private final BaseXBack[] panels = new BaseXBack[LABELS.length];
  /** Index creation panels. */
  private final DialogIndex[] creations = new DialogIndex[LABELS.length];

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
    final BaseXBack tabRes = add.border(8);

    final Data data = main.context.data();
    final int ll = LABELS.length;
    for(int l = 0; l < ll; ++l) {
      labels[l] = new BaseXLabel(LABELS[l]).large();
      panels[l] = new BaseXBack(new BorderLayout(0, 4));
      infos[l] = new TextPanel(Token.token(PLEASE_WAIT_D), false, this);
      infos[l].setFont(dmfont);
      BaseXLayout.setHeight(infos[l], 200);
      // show no optimize button for attribute name panel
      if(l != 1) buttons[l] = new BaseXButton(" ", this);
      // disallow creation/removal of full-text index
      if(l == 5) buttons[l].setEnabled(!data.inMemory());
    }
    // alternative panels
    creations[3] = new DialogValues(this, true);
    creations[4] = new DialogValues(this, false);
    creations[5] = new DialogFT(this, false);

    // tab: name indexes
    tabNames = new BaseXBack(new GridLayout(2, 1, 0, 8)).border(8);
    tabNames.add(panels[0]);
    tabNames.add(panels[1]);
    // tab: path index
    tabPath = new BaseXBack(new GridLayout(1, 1)).border(8);
    tabPath.add(panels[2]);
    // tab: value indexes
    tabValues = new BaseXBack(new GridLayout(2, 1, 0, 8)).border(8);
    tabValues.add(panels[3]);
    tabValues.add(panels[4]);
    // tab: full-text index
    tabFT = new BaseXBack(new GridLayout(1, 1)).border(8);
    tabFT.add(panels[5]);

    // tab: database info
    final BaseXBack tabGeneral = new BaseXBack(new BorderLayout(0, 8)).border(8);
    final Font f = tabGeneral.getFont();
    final BaseXLabel doc = new BaseXLabel(data.meta.name).border(0, 0, 6, 0).large();
    BaseXLayout.setWidth(doc, 400);
    tabGeneral.add(doc, BorderLayout.NORTH);

    final String db = InfoDB.db(data.meta, true, false, true);
    final TokenBuilder info = new TokenBuilder(db);
    if(!data.nspaces.isEmpty()) {
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
    final boolean[] val = {
      true, true, true, data.meta.textindex, data.meta.attrindex, data.meta.ftindex
    };
    final int is = il.size();
    for(int i = 0; i < is; i++) {
      final int idx = il.get(i);
      if(updated.contains(idx)) continue;
      updated.add(idx);
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          infos[idx].setText(val[idx] ? data.info(TYPES[idx], gui.context.options) :
            Token.token(HELP[idx]));
          updated.delete(idx);
        }
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
    if(cmp != null) {
      final int ll = LABELS.length;
      for(int l = 0; l < ll; l++) {
        if(cmp != buttons[l]) continue;
        final String label = buttons[l].getText();
        final Command cmd;
        if(label.equals(OPTIMIZE)) {
          cmd = new Optimize();
        } else if(label.equals(DROP)) {
          cmd = new DropIndex(TYPES[l]);
        } else {
          cmd = new CreateIndex(TYPES[l]);
          for(final DialogIndex di : creations) if(di != null) di.setOptions();
        }
        infos[l].setText(PLEASE_WAIT_D);
        DialogProgress.execute(this, cmd);
        return;
      }
    }

    resources.action(cmp);
    add.action(cmp);

    final Data data = gui.context.data();
    final boolean[] exists = {
      true, true, true, data.meta.textindex, data.meta.attrindex, data.meta.ftindex
    };

    if(cmp == this) {
      final boolean outdated = !data.meta.uptodate;
      final int ll = LABELS.length;
      for(int l = 0; l < ll; ++l) {
        // structural index/statistics?
        final boolean stats = l < 3;
        // updates labels and infos
        labels[l].setText(stats && outdated ? LABELS[l] + " (" + OUT_OF_DATE + ')' : LABELS[l]);
        // update button (label, disable/enable)
        if(buttons[l] != null) {
          buttons[l].setText(stats ? OPTIMIZE : exists[l] ? DROP : CREATE);
          if(stats) buttons[l].setEnabled(outdated);
        }
        add(l, creations[l] == null || exists[l] ? null : creations[l]);
      }
      updateInfo();
    }

    for(final DialogIndex di : creations) {
      if(di != null) di.action(true);
    }
  }

  @Override
  public void close() {
    super.close();
    for(final DialogIndex di : creations) {
      if(di != null) di.setOptions();
    }
  }
}
