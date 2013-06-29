package org.basex.gui.view.info;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;

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
public final class InfoView extends View implements LinkListener {
  /** Search panel. */
  final SearchEditor search;

  /** Old text. */
  private final TokenBuilder text = new TokenBuilder();
  /** Header label. */
  private final BaseXLabel label;
  /** Timer label. */
  private final BaseXLabel timer;
  /** Text Area. */
  private final Editor area;
  /** Buttons. */
  final BaseXBack buttons;

  /** Query statistics. */
  private IntList stat = new IntList(4);
  /** Query statistics strings. */
  private StringList strings = new StringList(4);
  /** Indicates if text has changed. */
  private boolean changed;
  /** Clear text before adding new text. */
  private boolean clear;
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
    border(6, 6, 6, 6).layout(new BorderLayout(0, 4));

    label = new BaseXLabel(QUERY_INFO);
    label.setForeground(GUIConstants.GRAY);

    timer = new BaseXLabel(" ", true, false);
    timer.setForeground(GUIConstants.DGRAY);

    final BaseXButton srch = new BaseXButton(gui, "search", SEARCH);
    buttons = new BaseXBack(Fill.NONE);
    buttons.layout(new TableLayout(1, 2, 8, 0));
    buttons.add(srch);
    buttons.add(timer);

    final BaseXBack b = new BaseXBack(Fill.NONE).layout(new BorderLayout());
    b.add(buttons, BorderLayout.WEST);
    b.add(label, BorderLayout.EAST);
    add(b, BorderLayout.NORTH);

    final BaseXBack center = new BaseXBack(Fill.NONE).layout(new BorderLayout());
    area = new Editor(false, gui);
    area.setLinkListener(this);
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
    label.setFont(lfont);
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
   * Displays the specified info string.
   * @param info string to be displayed
   * @param cmd command that created the output (may be {@code null})
   * @param ok indicates if evaluation was successful
   * @param reset clear text area when method is called next time
   */
  public void setInfo(final String info, final Command cmd, final boolean ok,
      final boolean reset) {
    setInfo(info, cmd, null, ok, reset);
  }

  /**
   * Displays the specified info string.
   * @param info string to be displayed
   * @param cmd command that created the output (may be {@code null})
   * @param time time required for running the command
   * @param ok indicates if evaluation was successful
   * @param reset clear text area when method is called next time
   */
  public void setInfo(final String info, final Command cmd, final String time,
      final boolean ok, final boolean reset) {

    final StringList eval = new StringList(1);
    final StringList comp = new StringList(1);
    final StringList plan = new StringList(1);
    final StringList result = new StringList(1);
    final StringList stack = new StringList(1);
    final StringList err = new StringList(1);
    final StringList origqu = new StringList(1);
    final StringList optqu = new StringList(1);
    final StringList command = new StringList(1);

    final StringList timings = new StringList(5);
    final IntList times = new IntList(5);

    final int runs = Math.max(1, gui.context.prop.num(Prop.RUNS));
    final String[] split = info.split(NL);
    for(int i = 0; i < split.length; ++i) {
      final String line = split[i];
      final int s = line.indexOf(':');
      if(line.startsWith(PARSING_CC) || line.startsWith(COMPILING_CC) ||
          line.startsWith(EVALUATING_CC) || line.startsWith(PRINTING_CC) ||
          line.startsWith(TOTAL_TIME_CC)) {

        final int t = line.indexOf(" ms");
        final int tm = (int) (Double.parseDouble(line.substring(s + 1, t)) * 100);
        times.add(tm);
        final String key = line.substring(0, s).trim();
        final String val = Performance.getTime(tm * 10000L * runs, runs);
        timings.add(LI + key + COLS + val);
      } else if(line.startsWith(QUERY_PLAN)) {
        while(i + 1 < split.length && !split[++i].isEmpty()) plan.add(split[i]);
      } else if(line.startsWith(COMPILING)) {
        while(++i < split.length && !split[i].isEmpty()) comp.add(split[i]);
      } else if(line.startsWith(QUERY)) {
        while(i + 1 < split.length && !split[++i].isEmpty()) origqu.add(split[i]);
      } else if(line.startsWith(OPTIMIZED_QUERY)) {
        while(i + 1 < split.length && !split[++i].isEmpty()) optqu.add(split[i]);
      } else if(line.startsWith(EVALUATING)) {
        while(i + 1 < split.length && split[++i].startsWith(LI)) eval.add(split[i]);
      } else if(line.startsWith(HITS_X_CC) || line.startsWith(UPDATED_CC) ||
          line.startsWith(PRINTED_CC) || line.startsWith(READ_LOCKING_CC) ||
          line.startsWith(WRITE_LOCKING_CC)) {
        result.add(LI + line);
      } else if(line.startsWith(ERROR_C)) {
        while(i + 1 < split.length && !split[++i].isEmpty()) {
          final Pattern p = Pattern.compile(STOPPED_AT + "(.*)" + COL);
          final Matcher m = p.matcher(split[i]);
          if(m.find()) {
            final TokenBuilder tb = new TokenBuilder();
            tb.add(STOPPED_AT).uline().add(m.group(1)).uline().add(COL);
            split[i] = tb.toString();
          }
          err.add(split[i]);
        }
      } else if(line.startsWith(STACK_TRACE_C)) {
        while(i + 1 < split.length && !split[++i].isEmpty()) {
          final TokenBuilder tb = new TokenBuilder();
          if(split[i].startsWith(LI)) {
            tb.add(LI).uline().add(split[i].substring(2)).uline();
          } else {
            tb.add(split[i]);
          }
          stack.add(tb.toString());
        }
      } else if(!ok && !line.isEmpty()) {
        err.add(line);
      }
    }

    stat = times;
    strings = timings;

    if(clear || !times.isEmpty() || !ok) text.reset();

    String inf = null;
    if(!(cmd instanceof AQuery)) {
      if(cmd != null) command.add(cmd.toString());
      if(ok && !info.isEmpty()) {
        if(reset) result.add(info.trim());
        else if(cmd == null) inf = info.trim();
      }
    }

    add(COMMAND + COL, command);
    add(ERROR_C, err);
    add(STACK_TRACE_C, stack);
    add(EVALUATING + COLS, eval);
    add(COMPILING + COLS, comp);
    add(QUERY + COLS, origqu);
    add(OPTIMIZED_QUERY + COLS, optqu);
    add(RESULT + COLS, result);
    add(TIMING + COLS, timings);
    add(QUERY_PLAN + COLS, plan);
    if(inf != null) text.add(inf).nline();
    changed = true;
    clear = reset;

    // show total time required for running the process
    String total = time;
    if(!times.isEmpty()) {
      total = Performance.getTime(times.get(times.size() - 1) * 10000L * runs, runs);
    }
    if(total != null) timer.setText(TOTAL_TIME_CC + total);
    repaint();
  }

  /**
   * Adds the specified strings.
   * @param head string header
   * @param list list reference
   */
  private void add(final String head, final StringList list) {
    if(list.isEmpty()) return;
    text.bold().add(head).norm().nline();
    for(final String s : list) text.add(s).nline();
    text.hline();
  }

  @Override
  public void linkClicked(final String link) {
    gui.editor.error(link + COL, true);
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

    final int f = focus == -1 ? l - 1 : focus;
    timer.setText(strings.get(f).replace(LI, ""));
    repaint();
  }

  @Override
  public void paintComponent(final Graphics g) {
    if(changed) {
      area.setText(text.finish());
      changed = false;
    }

    super.paintComponent(g);
    final int l = stat.size();
    if(l == 0) return;

    int fs = gui.gprop.num(GUIProp.FONTSIZE);
    h = label.getHeight() + 4;
    w = (int) (getWidth() * .98 - fs / 2 - label.getWidth());
    bw = fs * 2 + w / 10;
    bs = bw / (l - 1);

    // find maximum value
    int m = 0;
    for(int i = 0; i < l - 1; ++i) m = Math.max(m, stat.get(i));

    // draw focused bar
    final int by = 8;
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
