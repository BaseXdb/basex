package org.basex.gui.view.info;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.*;
import org.basex.gui.editor.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This view displays query information.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class InfoView extends View {
  /** Search panel. */
  final SearchEditor search;

  /** Old text. */
  private final TokenBuilder text = new TokenBuilder();
  /** Header label. */
  private final BaseXLabel header;
  /** Timer label. */
  private final BaseXLabel timer;
  /** North label. */
  private final BaseXBack title;
  /** Text Area. */
  private final Editor area;
  /** Buttons. */
  final BaseXBack buttons;

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

    title = new BaseXBack(Fill.NONE).layout(new BorderLayout());
    header = new BaseXLabel(QUERY_INFO);
    title.add(header, BorderLayout.NORTH);
    timer = new BaseXLabel(" ", true, false);
    title.add(timer, BorderLayout.SOUTH);

    final BaseXButton srch = new BaseXButton(gui, "search", SEARCH);
    buttons = new BaseXBack(Fill.NONE);
    buttons.layout(new TableLayout(1, 1, 1, 0));
    buttons.add(srch);

    final BaseXBack north = new BaseXBack(Fill.NONE).layout(new BorderLayout());
    north.add(buttons, BorderLayout.EAST);
    north.add(title, BorderLayout.CENTER);
    add(north, BorderLayout.NORTH);

    final BaseXBack center = new BaseXBack(Fill.NONE).layout(new BorderLayout(0, 2));
    area = new Editor(false, gui);
    search = new SearchEditor(gui, area).button(srch);
    add(search, BorderLayout.CENTER);

    center.add(area, BorderLayout.CENTER);
    center.add(search, BorderLayout.SOUTH);
    add(center, BorderLayout.CENTER);
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
    header.setFont(lfont);
    timer.setFont(font);
    area.setFont(font);
    search.panel().refreshLayout();
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
   * @param cmd command
   * @param time time required
   * @param ok flag indicating if command execution was successful
   */
  public void setInfo(final String info, final Command cmd, final String time,
      final boolean ok) {

    final StringList eval = new StringList();
    final StringList comp = new StringList();
    final StringList plan = new StringList();
    final StringList sl = new StringList();
    final StringList stats = new StringList();
    final IntList il = new IntList();
    final StringList err = new StringList();
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
        while(i + 1 < split.length && !split[++i].isEmpty()) plan.add(split[i]);
      } else if(line.startsWith(COMPILING_C)) {
        while(++i < split.length && !split[i].isEmpty()) comp.add(split[i]);
      } else if(line.startsWith(RESULT_C)) {
        res = line.substring(s + 1).trim();
      } else if(line.startsWith(EVALUATING_C)) {
        while(i + 1 < split.length && split[++i].startsWith(QUERYSEP)) eval.add(split[i]);
      } else if(line.startsWith(HITS_X_CC) || line.startsWith(UPDATED_CC) ||
          line.startsWith(PRINTED_CC) || line.startsWith(LOCKING_CC)) {
        stats.add(LI + line);
      } else if(line.startsWith(ERROR_C)) {
        while(i + 1 < split.length && !split[++i].isEmpty()) err.add(split[i]);
      } else if(!ok && !line.isEmpty()) {
        err.add(line);
      }
    }

    stat = il;
    strings = sl;
    String total = time;

    if(!il.isEmpty()) {
      text.reset();
      add(EVALUATING_C, eval);
      add(QUERY_C + ' ', qu);
      add(COMPILING_C, comp);
      if(!comp.isEmpty()) add(RESULT_C, res);
      add(RESULT_C, stats);
      add(TIMING_C, sl);
      add(QUERY_PLAN_C, plan);
      final int runs = Math.max(1, gui.context.prop.num(Prop.RUNS));
      total = Performance.getTime(il.get(il.size() - 1) * 10000L * runs, runs);
    } else {
      if(ok) {
        add(cmd);
        text.add(info).nline();
      } else {
        add(ERROR_C, err);
        add(EVALUATING_C, eval);
        add(cmd);
        add(COMPILING_C, comp);
        if(!comp.isEmpty()) add(RESULT_C, res);
        add(QUERY_PLAN_C, plan);
      }
    }

    area.setText(text.finish());
    if(total != null) timer.setText(TOTAL_TIME_CC + total);
    repaint();
  }

  /**
   * Adds the command representation.
   * @param cmd command
   */
  private void add(final Command cmd) {
    if(cmd instanceof XQuery) {
      add(QUERY_C + ' ', cmd.args[0].trim());
    } else if(cmd != null) {
      text.bold().add(COMMAND + COLS).norm().addExt(cmd).nline();
    }
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
    if(list.isEmpty()) return;
    text.bold().add(head).norm().nline();
    final int is = list.size();
    for(int i = 0; i < is; ++i) {
      String line = list.get(i);
      if(list == strings) line = ' ' + QUERYSEP + line + ":  " +
        Performance.getTime(stat.get(i) * 10000L * runs, runs);
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
    if(!txt.isEmpty()) text.bold().add(head).norm().add(txt).nline().hline();
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
        Performance.getTime(stat.get(f) * 10000L * runs, runs));
    repaint();
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    final int l = stat.size();
    if(l == 0) return;

    h = title.getHeight();
    w = getWidth() - 10 - buttons.getWidth();
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
      g.setColor(color3);
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
