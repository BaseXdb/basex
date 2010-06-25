package org.basex.gui.view.info;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import org.basex.core.Prop;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXText;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewNotifier;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.StringList;
import org.basex.util.TokenBuilder;

/**
 * This view displays query information.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class InfoView extends View {
  /** Header label. */
  private final BaseXLabel header;
  /** Timer label. */
  private final BaseXLabel timer;
  /** Timer label. */
  private final BaseXBack north;
  /** Text Area. */
  private final BaseXText area;

  /** Focused bar. */
  private int focus = -1;
  /** Query statistics. */
  private IntList stat = new IntList();
  /** Query statistics strings. */
  private StringList compile;
  /** Query statistics strings. */
  private StringList strings;
  /** Query statistics strings. */
  private StringList evaluate;
  /** Query plan. */
  private StringList plan;
  /** Query. */
  private String query = "";
  /** Compiled Query. */
  private String result = "";
  /** Panel Width. */
  private int w;
  /** Panel Height. */
  private int h;
  /** Bar widths. */
  private int bw;
  /** Bar size. */
  private int bs;

  /**
   * Default constructor.
   * @param man view manager
   */
  public InfoView(final ViewNotifier man) {
    super(INFOVIEW, HELPINFOO, man);
    setMode(Fill.UP);
    setBorder(6, 8, 8, 8);
    setLayout(new BorderLayout());

    north = new BaseXBack(Fill.NONE);
    north.setLayout(new BorderLayout());
    header = new BaseXLabel(INFOTIT);
    north.add(header, BorderLayout.NORTH);
    north.add(header, BorderLayout.NORTH);
    timer = new BaseXLabel(" ", true, false);
    north.add(timer, BorderLayout.SOUTH);
    add(north, BorderLayout.NORTH);

    area = new BaseXText(false, gui);
    add(area, BorderLayout.CENTER);
    refreshLayout();
  }

  @Override
  public void refreshInit() { }

  @Override
  public void refreshFocus() { }

  @Override
  public void refreshMark() { }

  @Override
  public void refreshContext(final boolean more, final boolean quick) { }

  @Override
  public void refreshUpdate() { }

  @Override
  public void refreshLayout() {
    header.setFont(GUIConstants.lfont);
    timer.setFont(GUIConstants.font);
    area.setFont(GUIConstants.font);
  }

  @Override
  public boolean visible() {
    return gui.prop.is(GUIProp.SHOWINFO);
  }

  @Override
  protected boolean db() {
    return false;
  }

  /**
   * Displays the specified query information.
   * @param inf info string
   * @param ok flag indicating if command execution was successful
   */
  public void setInfo(final String inf, final boolean ok) {
    final IntList il = new IntList();
    final StringList sl = new StringList();
    final StringList cmp = new StringList();
    final StringList eval = new StringList();
    final StringList pln = new StringList();
    String err = "";

    final String[] split = inf.split(NL);
    for(int i = 0; i < split.length; i++) {
      final String line = split[i];
      final int s = line.indexOf(':');
      if(line.startsWith(QUERYPARSE) || line.startsWith(QUERYCOMPILE) ||
          line.startsWith(QUERYEVALUATE) || line.startsWith(QUERYPRINT) ||
          line.startsWith(QUERYTOTAL)) {
        final int t = line.indexOf(" ms");
        sl.add(line.substring(0, s).trim());
        il.add((int) (Double.parseDouble(line.substring(s + 1, t)) * 100));
      } else if(line.startsWith(QUERYSTRING)) {
        query = line.substring(s + 1).trim();
      } else if(line.startsWith(QUERYPLAN)) {
        while(++i < split.length && !Character.isLetter(split[i].charAt(0)))
          pln.add(split[i]);
        --i;
      } else if(line.startsWith(QUERYCOMP)) {
        while(!split[++i].contains(QUERYRESULT)) cmp.add(split[i]);
        result = split[i].substring(split[i].indexOf(':') + 1).trim();
      } else if(line.startsWith(QUERYEVAL)) {
        while(split[++i].startsWith(QUERYSEP)) eval.add(split[i]);
        --i;
      } else if(!ok) {
        err += line + NL;
      }
    }

    final TokenBuilder tb = new TokenBuilder();
    String tm = "";

    stat = il;
    strings = sl;
    compile = cmp;
    evaluate = eval;
    plan = pln;

    final int runs = Math.max(1, gui.context.prop.num(Prop.RUNS));
    if(sl.size() != 0) {
      add(tb, QUERYQU, query);
      add(tb, QUERYCOMP, compile);
      if(compile.size() != 0) add(tb, QUERYRESULT, result);
      add(tb, QUERYPLAN, plan);
      add(tb, QUERYEVAL, evaluate);
      add(tb, QUERYTIME, strings);
      tm = strings.get(il.size() - 1) + ": " + Performance.getTimer(
          stat.get(il.size() - 1) * 10000L * runs, runs);
    } else if(!ok) {
      add(tb, INFOERROR, err.replaceAll(STOPPED + ".*\\r?\\n", ""));
      tm = "";
    }

    area.setText(tb.finish());
    timer.setText(tm);
    repaint();
  }

  /**
   * Adds the specified strings..
   * @param tb token builder
   * @param head string header
   * @param list list reference
   */
  private void add(final TokenBuilder tb, final String head,
      final StringList list) {

    final int runs = Math.max(1, gui.context.prop.num(Prop.RUNS));
    if(list.size() == 0) return;
    tb.high().add(head).norm().nl();
    final int is = list.size();
    for(int i = 0; i < is; i++) {
      String line = list.get(i);
      if(list == strings) line = " " + QUERYSEP + line + ":  " +
        Performance.getTimer(stat.get(i) * 10000L * runs, runs);
      tb.add(line).nl();
    }
    tb.hl();
  }

  /**
   * Adds a string.
   * @param tb token builder
   * @param head string header
   * @param txt text
   */
  private void add(final TokenBuilder tb, final String head, final String txt) {
    if(txt.isEmpty()) return;
    tb.high().add(head).norm().add(txt).nl().hl();
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    final int l = stat.size();
    if(l == 0) return;

    focus = -1;
    if(e.getY() < h) {
      for(int i = 0; i < l; i++) {
        final int bx = w - bw + bs * i;
        if(e.getX() >= bx && e.getX() < bx + bs) focus = i;
      }
    }

    final int runs = Math.max(1, gui.context.prop.num(Prop.RUNS));
    final int f = focus == -1 ? l - 1 : focus;
    timer.setText(strings.get(f) + ": " +
        Performance.getTimer(stat.get(f) * 10000L * runs, runs));
    repaint();
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    final int l = stat.size();
    if(l == 0) return;

    h = north.getHeight();
    w = getWidth() - 8;
    bw = gui.prop.num(GUIProp.FONTSIZE) * 2 + w / 10;
    bs = bw / (l - 1);

    // find maximum value
    int m = 0;
    for(int i = 0; i < l - 1; i++) m = Math.max(m, stat.get(i));

    // draw focused bar
    final int by = 10;
    final int bh = h - by;

    for(int i = 0; i < l - 1; i++) {
      if(i != focus) continue;
      final int bx = w - bw + bs * i;
      g.setColor(color4);
      g.fillRect(bx, by, bs + 1, bh);
    }

    // draw all bars
    for(int i = 0; i < l - 1; i++) {
      final int bx = w - bw + bs * i;
      g.setColor(COLORS[(i == focus ? 3 : 2) + i * 2]);
      final int p = Math.max(1, stat.get(i) * bh / m);
      g.fillRect(bx, by + bh - p, bs, p);
      g.setColor(COLORS[8]);
      g.drawRect(bx, by + bh - p, bs, p - 1);
    }
  }
}
