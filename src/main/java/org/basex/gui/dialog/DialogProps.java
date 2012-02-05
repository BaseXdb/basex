package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import org.basex.core.cmd.InfoDB;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXEditor;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTabs;
import org.basex.index.IndexToken.IndexType;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Database properties dialog.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogProps extends Dialog {
  /** Index checkboxes. */
  private final BaseXCheckBox[] indexes = new BaseXCheckBox[4];
  /** Button panel. */
  private final BaseXBack buttons;
  /** Optimize button. */
  private final BaseXButton optimize;

  /** Resource panel. */
  final DialogResources resources;
  /** Add panel. */
  final DialogAdd add;

  /** Editable full-text options. */
  private DialogFT ft;
  /** Optimize flag. */
  private boolean opt;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogProps(final GUI main) {
    super(main, DB_PROPS);
    panel.setLayout(new BorderLayout(5, 0));

    optimize = new BaseXButton(OPTIMIZE_D, this);
    buttons = newButtons(optimize, OK, CANCEL);

    // resource tree
    resources = new DialogResources(this);

    // tab: database info
    final Data data = gui.context.data();
    final BaseXBack tabInfo = new BaseXBack(new BorderLayout(0, 8)).border(8);
    final Font f = tabInfo.getFont();
    final BaseXLabel doc = new BaseXLabel(data.meta.name).border(
        0, 0, 5, 0).large();
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
    final BaseXBack tabRes = add.border(8);

    // tab: name indexes
    final BaseXBack tabNames =
        new BaseXBack(new GridLayout(2, 1, 0, 8)).border(8);
    tabNames.add(addIndex(true, data));
    tabNames.add(addIndex(false, data));

    final String[] cb = {
        PATH_INDEX, TEXT_INDEX, ATTRIBUTE_INDEX, FULLTEXT_INDEX };
    final boolean[] val = { data.meta.pathindex, data.meta.textindex,
        data.meta.attrindex, data.meta.ftxtindex };

    final BaseXBack[] panels = new BaseXBack[indexes.length];
    for(int i = 0; i < indexes.length; ++i) {
      indexes[i] = new BaseXCheckBox(cb[i], val[i], 0, this).large();
      indexes[i].setEnabled(data instanceof DiskData);
      panels[i] = new BaseXBack(new BorderLayout());
    }

    // tab: path index
    final BaseXBack tabPath = new BaseXBack(new GridLayout(1, 1)).border(8);
    panels[0].add(indexes[0], BorderLayout.NORTH);
    panels[0].add(text(val[0] ? data.info(IndexType.PATH) :
      Token.token(H_PATH_INDEX)), BorderLayout.CENTER);
    tabPath.add(panels[0]);

    // tab: value indexes
    final BaseXBack tabValues = new BaseXBack(new GridLayout(2, 1)).border(8);
    panels[1].add(indexes[1], BorderLayout.NORTH);
    panels[1].add(text(val[1] ? data.info(IndexType.TEXT) :
      Token.token(H_TEXT_INDEX)), BorderLayout.CENTER);
    tabValues.add(panels[1]);

    panels[2].add(indexes[2], BorderLayout.NORTH);
    panels[2].add(text(val[2] ? data.info(IndexType.ATTRIBUTE) :
      Token.token(H_ATTR_INDEX)), BorderLayout.CENTER);
    tabValues.add(panels[2]);

    // tab: full-text index
    final BaseXBack tabFT = new BaseXBack(new GridLayout(1, 1)).border(8);
    panels[3].add(indexes[3], BorderLayout.NORTH);
    if(!val[3]) ft = new DialogFT(this, false);
    panels[3].add(val[3] ? text(data.info(IndexType.FULLTEXT)) : ft,
        BorderLayout.CENTER);
    tabFT.add(panels[3]);

    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.addTab(GENERAL, tabInfo);
    tabs.addTab(RESOURCES, tabRes);
    tabs.addTab(NAMES, tabNames);
    tabs.addTab(PATH_INDEX, tabPath);
    tabs.addTab(INDEXES, tabValues);
    tabs.addTab(FULLTEXT, tabFT);

    final BaseXBack back = new BaseXBack(new BorderLayout());
    back.add(tabs, BorderLayout.CENTER);
    back.add(buttons, BorderLayout.SOUTH);

    set(resources, BorderLayout.WEST);
    set(back, BorderLayout.CENTER);

    action(null);
    setResizable(true);
    setMinimumSize(getPreferredSize());
    finish(null);
  }

  /**
   * Adds an index panel.
   * @param elem element/attribute flag
   * @param data data reference
   * @return panel
   */
  private BaseXBack addIndex(final boolean elem, final Data data) {
    final BaseXBack p = new BaseXBack(new BorderLayout());
    String lbl = elem ? ELEMENTS : ATTRIBUTES;
    if(!data.meta.uptodate) lbl += " (" + OUT_OF_DATE + ')';
    p.add(new BaseXLabel(lbl, false, true), BorderLayout.NORTH);
    final IndexType index = elem ? IndexType.TAG : IndexType.ATTNAME;
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
    BaseXLayout.setHeight(text, 200);
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
    resources.action(cmp);
    add.action(cmp);

    opt = cmp == optimize;
    if(opt) close();
    if(ft != null) ft.action(indexes[3].isSelected());
    enableOK(buttons, OPTIMIZE_D, !gui.context.data().meta.uptodate);
  }

  @Override
  public void close() {
    super.close();
    if(ft != null) ft.setOptions();
  }

  /**
   * Returns the optimize flag.
   * @return flag
   */
  public boolean optimize() {
    return opt;
  }
}
