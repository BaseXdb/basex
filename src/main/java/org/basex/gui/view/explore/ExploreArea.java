package org.basex.gui.view.explore;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;

import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.index.name.*;
import org.basex.index.stats.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This is a simple user search panel.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Bastian Lemke
 */
final class ExploreArea extends BaseXPanel implements ActionListener {
  /** Component width. */
  private static final int COMPW = 150;
  /** Exact search pattern. */
  private static final String PATEX = "[% = \"%\"]";
  /** Substring search pattern. */
  private static final String PATSUB = "[% contains text \"%\"]";
  /** Numeric search pattern. */
  private static final String PATNUM = "[% >= % and % <= %]";
  /** Simple search pattern. */
  private static final String PATSIMPLE = "[%]";
  /** Main panel. */
  private final ExploreView main;
  /** Main panel. */
  private final BaseXBack panel;
  /** Query field. */
  private final BaseXTextField all;
  /** Last Query. */
  private String last = "";

  /**
   * Default constructor.
   * @param m main panel
   */
  ExploreArea(final ExploreView m) {
    super(m.gui);
    main = m;

    layout(new BorderLayout(0, 5)).mode(Fill.NONE);

    all = new BaseXTextField(gui);
    all.addKeyListener(main);
    all.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        query(false);
      }
    });
    add(all, BorderLayout.NORTH);

    panel = new BaseXBack(Fill.NONE).layout(new TableLayout(32, 2, 10, 5));
    add(panel, BorderLayout.CENTER);
  }

  /**
   * Initializes the panel.
   */
  void init() {
    panel.removeAll();
    panel.revalidate();
    panel.repaint();
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    final Data data = gui.context.data();
    if(!main.visible() || data == null || panel.getComponentCount() != 0) return;

    addKeys(gui.context.data());
    panel.revalidate();
    panel.repaint();
  }

  /**
   * Adds a text field.
   * @param pos position
   */
  private void addInput(final int pos) {
    final BaseXTextField txt = new BaseXTextField(gui);
    BaseXLayout.setWidth(txt, COMPW);
    BaseXLayout.setHeight(txt, txt.getFont().getSize() + 11);
    txt.setMargin(new Insets(0, 0, 0, 10));
    txt.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        query(false);
      }
    });
    txt.addKeyListener(main);
    panel.add(txt, pos);
  }

  /**
   * Adds a category combobox.
   * @param data data reference
   */
  private void addKeys(final Data data) {
    final TokenList tl = new TokenList();
    final int cs = panel.getComponentCount();

    for(int c = 0; c < cs; c += 2) {
      final BaseXCombo combo = (BaseXCombo) panel.getComponent(c);
      if(combo.getSelectedIndex() == 0) continue;
      final String elem = combo.getSelectedItem().toString();
      if(!elem.startsWith("@")) tl.add(Token.token(elem));
    }

    final TokenList tmp = data.paths.desc(tl, true, false);
    final String[] keys = entries(tmp.toArray());
    final BaseXCombo cm = new BaseXCombo(gui, keys);
    cm.addActionListener(this);
    cm.addKeyListener(main);
    if(tmp.isEmpty()) cm.setEnabled(false);
    panel.add(cm);
    panel.add(new BaseXLabel(""));
  }

  /**
   * Adds a combobox.
   * @param values combobox values
   * @param pos position
   */
  private void addCombo(final String[] values, final int pos) {
    final BaseXCombo cm = new BaseXCombo(gui, values);
    BaseXLayout.setWidth(cm, COMPW);
    cm.addActionListener(this);
    cm.addKeyListener(main);
    panel.add(cm, pos);
  }

  /**
   * Adds a combobox.
   * @param min minimum value
   * @param max maximum value
   * @param pos position
   * @param itr integer flag
   */
  private void addSlider(final double min, final double max, final int pos,
      final boolean itr) {
    final BaseXDSlider sl = new BaseXDSlider(min, max, gui, this);
    BaseXLayout.setWidth(sl, COMPW + BaseXDSlider.LABELW);
    sl.itr = itr;
    sl.addKeyListener(main);
    panel.add(sl, pos);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    if(e != null) {
      final Object source = e.getSource();

      // find modified component
      int cp = 0;
      final int cs = panel.getComponentCount();
      for(int c = 0; c < cs; ++c) if(panel.getComponent(c) == source) cp = c;

      if((cp & 1) == 0) {
        // combo box with tags/attributes
        final BaseXCombo combo = (BaseXCombo) source;
        panel.remove(cp + 1);

        final Data data = gui.context.data();
        final boolean selected = combo.getSelectedIndex() != 0;
        if(selected) {
          final String item = combo.getSelectedItem().toString();
          final boolean att = item.startsWith("@");
          final Names names = att ? data.atnindex : data.tagindex;
          final byte[] key = Token.token(att ? item.substring(1) : item);
          final Stats stat = names.stat(names.id(key));
          switch(stat.type) {
            case INTEGER:
              addSlider(stat.min, stat.max, cp + 1, true);
              break;
            case DOUBLE:
              addSlider(stat.min, stat.max, cp + 1, false);
              break;
            case CATEGORY:
              addCombo(entries(stat.cats.keys()), cp + 1);
              break;
            case TEXT:
              addInput(cp + 1);
              break;
            case NONE:
              panel.add(new BaseXLabel(""), cp + 1);
              break;
          }
        } else {
          panel.add(new BaseXLabel(""), cp + 1);
        }
        while(cp + 2 < panel.getComponentCount()) {
          panel.remove(cp + 2);
          panel.remove(cp + 2);
        }
        if(selected) addKeys(data);
        panel.revalidate();
        panel.repaint();
      }
    }
    query(false);
  }

  /**
   * Runs a query.
   * @param force force the execution of a new query
   */
  void query(final boolean force) {
    final TokenBuilder tb = new TokenBuilder();
    final Data data = gui.context.data();

    final int cs = panel.getComponentCount();
    for(int c = 0; c < cs; c += 2) {
      final BaseXCombo com = (BaseXCombo) panel.getComponent(c);
      final int k = com.getSelectedIndex();
      if(k <= 0) continue;
      String key = com.getSelectedItem().toString().replaceAll("^(@?)(.*):", "$1*:");
      final boolean attr = key.startsWith("@");

      final Component comp = panel.getComponent(c + 1);
      String pattern = "";
      String val1 = null;
      String val2 = null;
      if(comp instanceof BaseXTextField) {
        val1 = ((BaseXTextField) comp).getText();
        if(!val1.isEmpty()) {
          if(val1.startsWith("\"")) {
            val1 = val1.replaceAll("\"", "");
            pattern = PATEX;
          } else {
            pattern = attr && data.meta.attrindex ||
              !attr && data.meta.textindex ? PATSUB : PATEX;
          }
        }
      } else if(comp instanceof BaseXCombo) {
        final BaseXCombo combo = (BaseXCombo) comp;
        if(combo.getSelectedIndex() != 0) {
          val1 = combo.getSelectedItem().toString();
          pattern = PATEX;
        }
      } else if(comp instanceof BaseXDSlider) {
        final BaseXDSlider slider = (BaseXDSlider) comp;
        if(slider.min != slider.totMin || slider.max != slider.totMax) {
          final double m = slider.min;
          final double n = slider.max;
          val1 = (long) m == m ? Long.toString((long) m) : Double.toString(m);
          val2 = (long) n == n ? Long.toString((long) n) : Double.toString(n);
          pattern = PATNUM;
        }
      }

      if(attr) {
        key = "descendant-or-self::node()/" + key;
        if(tb.isEmpty()) tb.add("//*");
        if(pattern.isEmpty()) pattern = PATSIMPLE;
      } else {
        tb.add("//" + key);
        key = "text()";
      }
      tb.addExt(pattern, key, val1, key, val2);
    }

    String qu = tb.toString();
    final boolean root = gui.context.root();
    final boolean rt = gui.gprop.is(GUIProp.FILTERRT);
    if(!qu.isEmpty() && !rt && !root) qu = '.' + qu;

    String simple = all.getText().trim();
    if(!simple.isEmpty()) {
      simple = Find.find(simple, gui.context, rt);
      qu = !qu.isEmpty() ? simple + " | " + qu : simple;
    }

    if(qu.isEmpty()) qu = rt || root ? "/" : ".";

    if(!force && last.equals(qu)) return;
    last = qu;
    gui.xquery(qu, false);
  }

  /**
   * Returns the combo box selections
   * and the keys of the specified set.
   * @param key keys
   * @return key array
   */
  private static String[] entries(final byte[][] key) {
    final StringList sl = new StringList();
    sl.add(Util.info(ENTRIES, key.length));
    for(final byte[] k : key) sl.add(Token.string(k));
    sl.sort(true, true, 1);
    return sl.toArray();
  }
}
