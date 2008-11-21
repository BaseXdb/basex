package org.basex.gui.view.query;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import org.basex.core.proc.Find;
import org.basex.core.proc.XPath;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.StatsKey;
import org.basex.gui.GUI;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIToolBar;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXDSlider;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.index.Names;
import org.basex.query.xpath.func.ContainsLC;
import org.basex.util.StringList;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;

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

  /** Content panel. */
  BaseXBack cont;
  /** Main panel. */
  BaseXBack panel;
  /** Query field. */
  BaseXTextField all;
  /** Filter button. */
  BaseXButton filter;
  /** Copy to XQuery button. */
  BaseXButton copy;

  /**
   * Default constructor.
   * @param m main panel
   */
  QuerySimple(final QueryView m) {
    main = m;
    panel = new BaseXBack(GUIConstants.Fill.NONE);
    panel.setLayout(new TableLayout(20, 2, 10, 5));

    all = new BaseXTextField(null);
    all.addKeyListener(main);
    all.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        BaseXLayout.enable(copy, all.getText().length() != 0);
        if(GUIProp.execrt) query(false);
      }
    });

    cont = new BaseXBack(GUIConstants.Fill.NONE);
    cont.setLayout(new BorderLayout(0, 5));
    cont.add(all, BorderLayout.NORTH);
    cont.add(panel, BorderLayout.CENTER);

    final BaseXBack p = new BaseXBack(GUIConstants.Fill.NONE);
    p.setLayout(new BorderLayout());

    initPanel();

    filter = GUIToolBar.newButton(GUICommands.FILTER);
    filter.addKeyListener(main);

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

    final Box box = new Box(BoxLayout.X_AXIS);
    box.add(stop);
    box.add(Box.createHorizontalStrut(1));
    box.add(go);
    box.add(Box.createHorizontalStrut(1));
    box.add(filter);
    box.add(Box.createHorizontalStrut(3));
    box.add(copy);

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
    panel.removeAll();
    addKeys(GUI.context.data());
  }

  @Override
  void refresh() {
    final Nodes marked = GUI.context.marked();
    if(marked == null) return;
    BaseXLayout.enable(filter, !GUIProp.filterrt && marked.size != 0);
    BaseXLayout.enable(go, !GUIProp.execrt);

    all.help(GUI.context.data().fs != null ? HELPSEARCHFS : HELPSEARCHXML);
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
   * @param data data reference
   */
  void addKeys(final Data data) {
    final TokenList sl = new TokenList();
    final int cs = panel.getComponentCount();
    for(int c = 0; c < cs; c += 2) {
      final BaseXCombo combo = (BaseXCombo) panel.getComponent(c);
      if(combo.getSelectedIndex() == 0) continue;
      final String elem = combo.getSelectedItem().toString();
      if(!elem.startsWith("@")) sl.add(Token.token(elem));
    }
    
    final TokenList tmp = data.skel.desc(sl, true, false);
    if(tmp.size == 0) return;

    final String[] keys = entries(tmp.finish());
    final BaseXCombo combo = new BaseXCombo(keys, HELPSEARCHCAT, false);
    combo.addActionListener(this);
    combo.addKeyListener(main);
    panel.add(combo);
    panel.add(new BaseXLabel(""));
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

    // find modified component
    int cp = 0;
    final int cs = panel.getComponentCount();
    for(int c = 0; c < cs; c++) if(panel.getComponent(c) == source) cp = c;

    if((cp & 1) == 0) {
      // ComboBox with tags/attributes
      final BaseXCombo combo = (BaseXCombo) source;
      panel.remove(cp + 1);

      final Data data = GUI.context.data();
      final boolean selected = combo.getSelectedIndex() != 0;
      if(selected) {
        final String item = combo.getSelectedItem().toString();
        final boolean att = item.startsWith("@");
        final Names names = att ? data.atts : data.tags;
        final byte[] key = Token.token(att ? item.substring(1) : item);
        final StatsKey stat = names.stat(names.id(key));
        switch(stat.kind) {
          case INT:
            addSlider(stat.min, stat.max, cp + 1, item.equals("@size"),
                item.equals("@mtime"), true);
            break;
          case DBL:
            addSlider(stat.min, stat.max, cp + 1, false, false, false);
            break;
          case CAT:
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

      panel.validate();
      panel.repaint();
    }
    if(GUIProp.execrt) query(false);
  }

  @Override
  void query(final boolean force) {
    final TokenBuilder tb = new TokenBuilder();
    final Data data = GUI.context.data();

    final int cs = panel.getComponentCount();
    for(int c = 0; c < cs; c += 2) {
      final BaseXCombo com = (BaseXCombo) panel.getComponent(c);
      final int k = com.getSelectedIndex();
      if(k <= 0) continue;
      String key = com.getSelectedItem().toString();
      final boolean attr = key.startsWith("@");

      final Component comp = panel.getComponent(c + 1);
      String pattern = "";
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
          final double m = slider.min;
          final double n = slider.max;
          val1 = (long) m == m ? Long.toString((long) m) : Double.toString(m);
          val2 = (long) n == n ? Long.toString((long) n) : Double.toString(n);
          pattern = PATNUM;
        }
      }

      if(attr) {
        if(tb.size == 0) tb.add("//*");
        if(pattern.length() == 0) pattern = PATSIMPLE;
      } else {
        tb.add("//" + key);
        key = "text()";
      }
      tb.add(pattern, key, val1, key, val2);
    }

    String qu = tb.toString();
    if(qu.length() != 0) {
      if(!GUIProp.filterrt && !GUI.context.root()) qu = "." + qu;
    }

    String simple = all.getText().trim();
    if(simple.length() != 0) {
      simple = Find.find(simple, GUI.context, GUIProp.filterrt);
      qu = qu.length() != 0 ? simple + " | " + qu : simple;
    }

    if(qu.length() == 0) {
      qu = GUIProp.filterrt || GUI.context.root() ? "/" : ".";
    }
    
    if(!force && last.equals(qu)) return;
    last = qu;
    BaseXLayout.enable(copy, last.length() != 0);
    BaseXLayout.enable(stop, true);
    GUI.get().execute(new XPath(qu));
  }

  @Override
  void quit() { }

  @Override
  boolean info(final String info, final boolean ok) {
    BaseXLayout.enable(stop, false);
    return false;
  }

  /**
   * Returns the combo box selections
   * and the keys of the specified set.
   * @param key keys
   * @return key array
   */
  String[] entries(final byte[][] key) {
    final StringList sl = new StringList();
    sl.add("(" + key.length + " entries)");
    for(final byte[] k : key) sl.add(Token.string(k));
    sl.sort();
    return sl.finish();
  }
}
