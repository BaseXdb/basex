package org.basex.gui.view.info;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.cmd.XQuery;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXEditor;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewNotifier;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;
import org.basex.util.list.IntList;
import org.basex.util.list.StringList;

/**
 * This view displays query information.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class InfoView extends View {
  /** Old text. */
  private final TokenBuilder text = new TokenBuilder();
  /** Header label. */
  private final BaseXLabel header;
  /** Timer label. */
  private final BaseXLabel timer;
  /** North label. */
  private final BaseXBack north;
  /** Text Area. */
  private final BaseXEditor area;

  /** Query statistics. */
  private IntList stat = new IntList();
  /** Query statistics strings. */
  private StringList strings;
  /** Focused bar. */
  private int focus = -1;
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
    super(INFOVIEW, man);
    border(6, 6, 6, 6).layout(new BorderLayout());

    north = new BaseXBack(Fill.NONE).layout(new BorderLayout());
    header = new BaseXLabel(QUERY_INFO);
    north.add(header, BorderLayout.NORTH);
    north.add(header, BorderLayout.NORTH);
    timer = new BaseXLabel(" ", true, false);
    north.add(timer, BorderLayout.SOUTH);
    add(north, BorderLayout.NORTH);

    area = new BaseXEditor(false, gui);
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
    return gui.gprop.is(GUIProp.SHOWINFO);
  }

  @Override
  public void visible(final boolean v) {
    gui.gprop.set(GUIProp.SHOWINFO, v);
  }

  @Override
  protected boolean db() {
    return false;
  }

  /**
   * Displays the specified information.
   * @param info info string
   * @param cmd command string
   * @param time time needed
   * @param ok flag indicating if command execution was successful
   */
  public void setInfo(final String info, final Command cmd,
      final String time, final boolean ok) {
    final StringList eval = new StringList();
    final StringList comp = new StringList();
    final StringList plan = new StringList();
    final StringList sl = new StringList();
    final StringList stats = new StringList();
    final IntList il = new IntList();
    String err = "";
    String qu = "";
    String res = "";

    final String[] split = info.split(NL);
    for(int i = 0; i < split.length; ++i) {
      final String line = split[i];
      final int s = line.indexOf(':');
      if(line.startsWith(PARSING_CC) || line.startsWith(COMPILING_CC) ||
          line.startsWith(EVALUATING_CC) || line.startsWith(PRINTING_CC) ||
          line.startsWith(TOTAL_TIME_CC)) {
        final int t = line.indexOf(" ms");
        sl.add(line.substring(0, s).trim());
        il.add((int) (Double.parseDouble(line.substring(s + 1, t)) * 100));
      } else if(line.startsWith(QUERY_C)) {
        qu = line.substring(s + 1).trim();
      } else if(line.startsWith(QUERY_PLAN_C)) {
        while(++i < split.length && !split[i].isEmpty()) plan.add(split[i]);
        --i;
      } else if(line.startsWith(COMPILING_C)) {
        while(++i < split.length && !split[i].isEmpty()) comp.add(split[i]);
      } else if(line.startsWith(RESULT_C)) {
        res = line.substring(s + 1).trim();
      } else if(line.startsWith(EVALUATING_C)) {
        while(split[++i].startsWith(QUERYSEP)) eval.add(split[i]);
        --i;
      } else if(!ok) {
        err += line + NL;
      } else if(line.startsWith(HITS_X_CC) || line.startsWith(UPDATED_CC)
          || line.startsWith(PRINTED_CC)) {
          stats.add("- " + line);
      }
    }

    stat = il;
    strings = sl;
    String total = time;

    final boolean q = cmd instanceof XQuery;
    if(!ok || !q) {
      text.bold();
      if(q) {
        add(QUERY_C + " ", cmd.toString().replaceAll("^.*? ", "").trim());
      } else if(cmd != null) {
        text.bold().add(COMMAND + COLS).norm().addExt(cmd).nline();
      }
      if(ok) {
        text.add(info).nline();
      } else {
        add(COMPILING_C, comp);
        add(QUERY_PLAN_C, plan);
        add(ERROR_C, err.replaceAll(STOPPED_AT + ".*\\r?\\n", ""));
      }
    } else if(sl.size() != 0) {
      text.reset();
      add(EVALUATING_C, eval);
      add(QUERY_C + " ", qu);
      add(COMPILING_C, comp);
      if(comp.size() != 0) add(RESULT_C, res);
      add(TIMING_C, sl);
      add(RESULT_C, stats);
      add(QUERY_PLAN_C, plan);
      final int runs = Math.max(1, gui.context.prop.num(Prop.RUNS));
      total = Performance.getTimer(il.get(il.size() - 1) * 10000L * runs, runs);
    }

    area.setText(text.finish());
    if(total != null) timer.setText(TOTAL_TIME_CC + total);
    repaint();
  }

  /**
   * Resets the info string without repainting the view.
   */
  public void reset() {
    text.reset();
  }

  /**
   * Adds the specified strings.
   * @param head string header
   * @param list list reference
   */
  private void add(final String head, final StringList list) {
    final int runs = Math.max(1, gui.context.prop.num(Prop.RUNS));
    if(list.size() == 0) return;
    text.bold().add(head).norm().nline();
    final int is = list.size();
    for(int i = 0; i < is; ++i) {
      String line = list.get(i);
      if(list == strings) line = " " + QUERYSEP + line + ":  " +
        Performance.getTimer(stat.get(i) * 10000L * runs, runs);
      text.add(line).nline();
    }
    text.hline();
  }

  /**
   * Adds a string.
   * @param head string header
   * @param txt text
   */
  private void add(final String head, final String txt) {
    if(txt.isEmpty()) return;
    text.bold().add(head).norm().add(txt).nline().hline();
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    final int l = stat.size();
    if(l == 0) return;

    focus = -1;
    if(e.getY() < h) {
      for(int i = 0; i < l; ++i) {
        final int bx = w - bw + bs * i;
        if(e.getX() >= bx && e.getX() < bx + bs) focus = i;
      }
    }

    final int runs = Math.max(1, gui.context.prop.num(Prop.RUNS));
    final int f = focus == -1 ? l - 1 : focus;
    timer.setText(strings.get(f) + COLS +
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
    bw = gui.gprop.num(GUIProp.FONTSIZE) * 2 + w / 10;
    bs = bw / (l - 1);

    // find maximum value
    int m = 0;
    for(int i = 0; i < l - 1; ++i) m = Math.max(m, stat.get(i));

    // draw focused bar
    final int by = 10;
    final int bh = h - by;

    for(int i = 0; i < l - 1; ++i) {
      if(i != focus) continue;
      final int bx = w - bw + bs * i;
      g.setColor(color4);
      g.fillRect(bx, by, bs + 1, bh);
    }

    // draw all bars
    for(int i = 0; i < l - 1; ++i) {
      final int bx = w - bw + bs * i;
      g.setColor(color((i == focus ? 3 : 2) + i * 2));
      final int p = Math.max(1, stat.get(i) * bh / m);
      g.fillRect(bx, by + bh - p, bs, p);
      g.setColor(color(8));
      g.drawRect(bx, by + bh - p, bs, p - 1);
    }
  }
}
