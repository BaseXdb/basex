package org.basex.gui.view.query;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.border.EmptyBorder;
import org.basex.BaseX;
import org.basex.core.Commands;
import org.basex.core.proc.Find;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.StatsKey;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXDSlider;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.view.View;
import org.basex.query.xpath.func.ContainsLC;
import org.basex.util.Set;
import org.basex.util.Token;

/**
 * This is a simple user search panel.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class QuerySimple extends QueryPanel implements ActionListener {
  /** Component width. */
  private static final int COMPW = 150;
  /** Exact search pattern. */
  private static final String PATEX = "[% = \"%\"]";
  /** Substring search pattern. */
  private static final String PATSUB = "[" + ContainsLC.NAME + "(%, \"%\")]";
  /** Numeric search pattern. */
  private static final String PATNUM = "[% >= % and % <= %]";
  /** Simple search pattern. */
  private static final String PATSIMPLE = "[%]";

  /** Main panel. */
  QueryView main;
  /** Content panel. */
  BaseXBack cont;
  /** Main panel. */
  BaseXBack panel;
  /** Query field. */
  BaseXCombo all;
  /** Filter button. */
  BaseXButton filter;
  /** Execute button. */
  BaseXButton exec;
  /** Copy to XQuery button. */
  BaseXButton copy;
  /** Query keys. */
  String[] keys;

  /**
   * Default constructor.
   * @param m main panel
   */
  QuerySimple(final QueryView m) {
    main = m;
    panel = new BaseXBack(GUIConstants.FILL.NONE);
    panel.setLayout(new TableLayout(20, 2, 10, 5));

    all = new BaseXCombo(new String[] {}, null, true);
    all.addKeyListener(main);
    all.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        if(GUIProp.execrt) query(false);
      }
    });

    cont = new BaseXBack(GUIConstants.FILL.NONE);
    cont.setLayout(new BorderLayout(0, 5));
    cont.add(all, BorderLayout.NORTH);
    cont.add(panel, BorderLayout.CENTER);

    final BaseXBack p = new BaseXBack(GUIConstants.FILL.NONE);
    p.setLayout(new BorderLayout());

    final Box box = new Box(BoxLayout.X_AXIS);

    copy = new BaseXButton(BUTTONTOXPATH, HELPTOXPATH);
    copy.addKeyListener(main);
    copy.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        main.panels[0].last = main.panels[1].last;
        main.mode = 0;
        main.refreshLayout();
      }
    });
    BaseXLayout.enable(copy, false);
    box.add(copy);
    box.add(Box.createHorizontalStrut(4));

    filter = new BaseXButton(BUTTONFILTER, HELPFILTER);
    filter.addKeyListener(main);
    filter.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        View.notifyContext(GUI.context.marked(), false);
      }
    });
    box.add(filter);
    box.add(Box.createHorizontalStrut(4));

    exec = new BaseXButton(BUTTONEXEC, HELPEXEC, null);
    exec.addKeyListener(main);
    exec.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        query(true);
      }
    });
    box.add(exec);

    p.add(box, BorderLayout.EAST);
    cont.add(p, BorderLayout.SOUTH);
  }

  @Override
  void init() {
    if(GUIProp.showquery) create();
    main.add(cont, BorderLayout.CENTER);
    refresh();
  }

  /**
   * Create search categories.
   */
  private void create() {
    // <LK> initialize statistics
    //set.init();
    keys = keys(GUI.context.data().stats);
    panel.removeAll();
    addKeys(0);
  }

  @Override
  void refresh() {
    final Nodes marked = GUI.context.marked();
    if(marked == null) return;
    BaseXLayout.enable(filter, !GUIProp.filterrt && marked.size != 0);
    BaseXLayout.enable(exec, !GUIProp.execrt);

    all.help = GUI.context.data().deepfs ? HELPSEARCHFS : HELPSEARCHXML;
    if(GUIProp.showquery && panel.getComponentCount() == 0) {
      create();
      main.revalidate();
      main.repaint();
    }
  }

  @Override
  void finish() {
    panel.removeAll();
  }

  /**
   * Adds a text field.
   * @param pos position
   */
  void addInput(final int pos) {
    final BaseXTextField text = new BaseXTextField(HELPCATINPUT, null);
    BaseXLayout.setWidth(text, COMPW);
    BaseXLayout.setHeight(text, text.getFont().getSize() + 11);
    text.setMargin(new Insets(0, 0, 0, 10));
    text.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        if(GUIProp.execrt) query(false);
      }
    });
    text.addKeyListener(main);
    panel.add(text, pos);
  }

  /**
   * Adds a category combobox.
   * @param pos position
   */
  void addKeys(final int pos) {
    final BaseXCombo combo = new BaseXCombo(keys, HELPSEARCHCAT, false);
    combo.addActionListener(this);
    combo.addKeyListener(main);
    panel.add(combo, pos);
    panel.add(new BaseXLabel(""), pos + 1);
  }

  /**
   * Adds a combobox.
   * @param values combobox values
   * @param pos position
   */
  void addCombo(final String[] values, final int pos) {
    final BaseXCombo combo = new BaseXCombo(values, HELPCAT, false);
    BaseXLayout.setWidth(combo, COMPW);
    combo.addActionListener(this);
    combo.addKeyListener(main);
    panel.add(combo, pos);
  }

  /**
   * Adds a combobox.
   * @param min minimum value
   * @param max maximum value
   * @param pos position
   * @param kb kilobyte flag
   * @param itr integer flag
   * @param date date flag
   */
  void addSlider(final double min, final double max, final int pos,
      final boolean kb, final boolean date, final boolean itr) {
    final BaseXDSlider slider = new BaseXDSlider(min, max, HELPDS, this);
    BaseXLayout.setWidth(slider, COMPW + BaseXDSlider.LABELW);
    slider.kb = kb;
    slider.date = date;
    slider.itr = itr;
    slider.addKeyListener(main);
    panel.add(slider, pos);
  }

  /** {@inheritDoc} */
  public void actionPerformed(final ActionEvent e) {
    if(e == null) {
      if(GUIProp.execrt) query(false);
      return;
    }

    final Object source = e.getSource();

    int cp = 0;
    final int cs = panel.getComponentCount();
    for(int c = 0; c < cs; c++) if(panel.getComponent(c) == source) cp = c;

    if((cp & 1) == 0) {
      // ComboBox with tags/attributes
      final BaseXCombo combo = (BaseXCombo) source;

      panel.remove(cp + 1);
      if(combo.getSelectedIndex() != 0) {
        final String item = combo.getSelectedItem().toString();
        final StatsKey key = GUI.context.data().stats.get(Token.token(item));
        switch(key.kind) {
          case INT:
            addSlider(key.min, key.max, cp + 1, item.equals("@size"),
                item.equals("@mtime"), true);
            break;
          case DBL:
            addSlider(key.min, key.max, cp + 1, false, false, false);
            break;
          case CAT:
            addCombo(keys(key.cats), cp + 1);
            break;
          case TEXT:
            addInput(cp + 1);
            break;
          case NONE:
            //final BaseXLabel label = new BaseXLabel("(no texts available)");
            final BaseXLabel label = new BaseXLabel("");
            label.setBorder(new EmptyBorder(3, 0, 0, 0));
            panel.add(label, cp + 1);
            break;
        }
        if(cp + 2 == cs) addKeys(cp + 2);
        panel.validate();
        panel.repaint();
      } else {
        panel.add(new BaseXLabel(""), cp + 1);
        if(cp + 4 == cs && ((BaseXCombo) panel.getComponent(cp + 2)).
            getSelectedIndex() == 0) {
          panel.remove(cp + 2);
          panel.remove(cp + 2);
          panel.validate();
          panel.repaint();
        }
      }
    }
    if(GUIProp.execrt) query(false);
  }

  @Override
  void query(final boolean force) {
    final StringBuilder sb = new StringBuilder();
    final int cs = panel.getComponentCount();
    final Data data = GUI.context.data();
    final boolean fsxml = data.deepfs;

    boolean first = true;
    for(int c = 0; c < cs; c += 2) {
      final int k = ((BaseXCombo) panel.getComponent(c)).getSelectedIndex();
      if(k == 0) continue;
      String key = keys[k];
      final boolean attr = keys[k].startsWith("@");

      final Component comp = panel.getComponent(c + 1);
      String pattern = null;
      String val1 = null;
      String val2 = null;
      if(comp instanceof BaseXTextField) {
        val1 = ((BaseXTextField) comp).getText();
        if(val1.length() != 0) {
          if(val1.startsWith("\"")) {
            val1 = val1.replaceAll("\"", "");
            pattern = PATEX;
          } else {
            pattern = attr && data.meta.atvindex ||
              !attr && data.meta.txtindex ? PATEX : PATSUB;
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
          final double m = (long) (slider.min * 100) / 100.0;
          final double n = (long) (slider.max * 100 + 99) / 100.0;
          val1 = (long) m == m ? Long.toString((long) m) : Double.toString(m);
          val2 = (long) n == n ? Long.toString((long) n) : Double.toString(n);
          pattern = PATNUM;
        }
      }
      if(pattern == null) pattern = PATSIMPLE;

      if(fsxml) key = "descendant-or-self::" + (attr ? "*/" : "") + key;
      else {
        if(!attr) key = "self::" + key;
        if(!first) sb.append("/descendant-or-self::node()");
      }
      sb.append(BaseX.info(pattern, key, val1, key, val2));
      first = false;
    }

    String qu = sb.toString();
    if(qu.length() != 0) {
      final Nodes curr = GUI.context.current();
      final boolean r = GUIProp.filterrt || curr.size == 1 && curr.pre[0] == 0;
      qu = (r ? "/" : "") + "descendant-or-self::" +
        (fsxml ? "file" : "*") + qu;
    }

    String simple = all.getText().trim();
    if(simple.length() != 0) {
      simple = Find.find(simple, GUI.context, GUIProp.filterrt);
      qu = qu.length() != 0 ? simple + " | " + qu : simple;
    }

    if(qu.length() == 0) {
      final boolean root = GUIProp.filterrt;
      final Nodes current = GUI.context.current();
      qu = root || current.size == 1 && current.pre[0] < 1 ? "/" : ".";
    }

    if(!force && last.equals(qu)) return;
    last = qu;
    BaseXLayout.enable(copy, last.length() != 0);
    GUI.get().execute(Commands.XPATH, qu);
  }

  @Override
  void quit() { }

  @Override
  void info(final String info, final boolean ok) { }

  /**
   * Returns a string array with the number of distinct keys
   * and the keys of the specified set.
   * @param set set structure
   * @return key array
   */
  String[] keys(final Set set) {
    final byte[][] tmp = set.keys();
    final int tl = tmp.length;
    final String[] vals = new String[tl + 1];
    for(int t = 0; t < tl; t++) vals[t + 1] = Token.string(tmp[t]);
    vals[0] = "";
    Arrays.sort(vals);
    vals[0] = "(" + tl + " entries)";
    return vals;
  }
}
