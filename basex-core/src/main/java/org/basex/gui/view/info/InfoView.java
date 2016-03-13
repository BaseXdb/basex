package org.basex.gui.view.info;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;
import org.basex.gui.view.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This view displays query information.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class InfoView extends View implements LinkListener {
  /** Searchable editor. */
  private final SearchEditor editor;

  /** Header label. */
  private final BaseXHeader header;
  /** Timer label. */
  private final BaseXLabel timer;
  /** Text Area. */
  private final TextPanel area;

  /** Query statistics. */
  private IntList stat = new IntList(4);
  /** Query statistics strings. */
  private StringList strings = new StringList(4);
  /** Current text. */
  private byte[] text = Token.EMPTY;
  /** Old text. */
  private byte[] old;
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
    super(GUIConstants.INFOVIEW, man);
    border(5).layout(new BorderLayout(0, 5));

    header = new BaseXHeader(QUERY_INFO);

    timer = new BaseXLabel(" ", true, false);
    timer.setForeground(GUIConstants.dgray);

    area = new TextPanel(false, gui);
    area.setLinkListener(this);
    editor = new SearchEditor(gui, area);

    final AbstractButton find = editor.button(FIND);
    final BaseXBack buttons = new BaseXBack(false);
    buttons.layout(new TableLayout(1, 3, 8, 0)).border(0, 0, 4, 0);
    buttons.add(find);
    buttons.add(timer);

    final BaseXBack b = new BaseXBack(false).layout(new BorderLayout());
    b.add(buttons, BorderLayout.WEST);
    b.add(header, BorderLayout.EAST);
    add(b, BorderLayout.NORTH);

    final BaseXBack center = new BaseXBack(false).layout(new BorderLayout());
    add(editor, BorderLayout.CENTER);

    center.add(area, BorderLayout.CENTER);
    center.add(editor, BorderLayout.SOUTH);
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
    header.refreshLayout();
    timer.setFont(GUIConstants.font);
    area.setFont(GUIConstants.font);
    editor.bar().refreshLayout();
  }

  @Override
  public boolean visible() {
    return gui.gopts.get(GUIOptions.SHOWINFO);
  }

  @Override
  public void visible(final boolean v) {
    gui.gopts.set(GUIOptions.SHOWINFO, v);
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
  public void setInfo(final String info, final Command cmd, final boolean ok, final boolean reset) {
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
  public void setInfo(final String info, final Command cmd, final String time, final boolean ok,
      final boolean reset) {

    final TokenBuilder tb = new TokenBuilder(text);
    final StringList eval = new StringList(1);
    final StringList comp = new StringList(1);
    final StringList plan = new StringList(1);
    final StringList result = new StringList(1);
    final StringList stack = new StringList(1);
    final StringList error = new StringList(1);
    final StringList origqu = new StringList(1);
    final StringList optqu = new StringList(1);
    final StringList command = new StringList(1);

    final StringList timings = new StringList(5);
    final IntList times = new IntList(5);

    final int runs = Math.max(1, gui.context.options.get(MainOptions.RUNS));
    final String[] split = info.split(NL);
    final int sl = split.length;
    for(int s = 0; s < sl; s++) {
      final String line = split[s];
      if(line.startsWith(PARSING_CC) || line.startsWith(COMPILING_CC) ||
          line.startsWith(EVALUATING_CC) || line.startsWith(PRINTING_CC) ||
          line.startsWith(TOTAL_TIME_CC)) {
        final int t = line.indexOf(" ms");
        final int d = line.indexOf(':');
        final int tm = (int) (Double.parseDouble(line.substring(d + 1, t)) * 100);
        times.add(tm);
        final String key = line.substring(0, d).trim();
        final String val = Performance.getTime(tm * 10000L * runs, runs);
        timings.add(LI + key + COLS + val);
      } else if(line.startsWith(HITS_X_CC) || line.startsWith(UPDATED_CC) ||
          line.startsWith(PRINTED_CC) || line.startsWith(READ_LOCKING_CC) ||
          line.startsWith(WRITE_LOCKING_CC)) {
        result.add(LI + line);
      } else if(line.equals(COMPILING + COL)) {
        while(++s < sl && !split[s].isEmpty()) comp.add(split[s]);
      } else if(line.equals(QUERY + COL)) {
        while(++s < sl && !split[s].isEmpty()) origqu.add(split[s]);
      } else if(line.equals(OPTIMIZED_QUERY + COL)) {
        while(++s < sl && !split[s].isEmpty()) optqu.add(split[s]);
      } else if(line.startsWith(EVALUATING)) {
        while(++s < sl && split[s].startsWith(LI)) {
          eval.add(split[s].substring(2).replaceAll("\\|", "\n"));
        }
      } else if(line.equals(QUERY_PLAN + COL)) {
        while(++s < sl && !split[s].isEmpty()) plan.add(split[s]);
      } else if(line.equals(Text.ERROR + COL)) {
        while(++s < sl && !split[s].isEmpty()) {
          final Pattern p = Pattern.compile(STOPPED_AT + "(.*)" + COL);
          final Matcher m = p.matcher(split[s]);
          if(m.find()) {
            final TokenBuilder tmp = new TokenBuilder();
            tmp.add(STOPPED_AT).uline().add(m.group(1)).uline().add(COL);
            split[s] = tmp.toString();
          }
          error.add(split[s]);
        }
      } else if(line.equals(STACK_TRACE + COL)) {
        while(++s < sl && !split[s].isEmpty()) {
          final TokenBuilder tmp = new TokenBuilder();
          final String sp = split[s].replaceAll("<.*", "");
          final boolean last = !sp.equals(split[s]);
          if(sp.startsWith(LI)) {
            tmp.add(LI).uline().add(sp.substring(2)).uline();
          } else {
            tmp.add(sp);
          }
          stack.add(tmp.toString());
          if(last) break;
        }
      } else if(!ok && !line.isEmpty()) {
        error.add(line);
      }
    }

    stat = times;
    strings = timings;

    final boolean test = cmd instanceof Test, query = cmd instanceof AQuery;
    /* reset old text if:
       a) deletion was requested by the last function call
       b) the result contains execution times
       c) result is not ok and no XQUnit tests are run */
    if(clear || !times.isEmpty() || !(ok || test)) {
      tb.reset();
    } else if(test) {
      // XQUnit tests: adopt trace output
      eval.add(tb.toString().trim());
      tb.reset();
    }

    String inf = null;
    if(!query) {
      if(cmd != null) command.add(cmd.toString());
      if(ok && !info.isEmpty()) {
        if(reset) result.add(info.trim());
        else if(cmd == null) inf = info.trim();
      }
    }

    add(COMMAND + COL, command, tb);
    add(Text.ERROR + COL, error, tb);
    add(STACK_TRACE + COL, stack, tb);
    add(EVALUATING + COL, eval, tb);
    add(COMPILING + COL, comp, tb);
    add(OPTIMIZED_QUERY + COL, optqu, tb);
    add(QUERY + COL, origqu, tb);
    add(RESULT + COL, result, tb);
    add(TIMING + COL, timings, tb);
    add(QUERY_PLAN + COL, plan, tb);
    if(inf != null) tb.add(inf).nline();
    clear = reset;

    // show total time required for running the process
    String total = time;
    if(!times.isEmpty()) {
      total = Performance.getTime(times.get(times.size() - 1) * 10000L * runs, runs);
    }
    if(total != null) timer.setText(TOTAL_TIME_CC + total);
    text = tb.finish();
    repaint();
  }

  /**
   * Adds the specified strings.
   * @param head string header
   * @param list list reference
   * @param tb token builder
   */
  private static void add(final String head, final StringList list, final TokenBuilder tb) {
    if(list.isEmpty()) return;
    tb.bold().add(head).norm().nline();
    for(final String s : list) tb.add(s).nline();
    tb.hline();
  }

  @Override
  public void linkClicked(final String link) {
    gui.editor.jump(link);
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
    if(old != text) {
      old = text;
      area.setText(text);
    }

    super.paintComponent(g);
    final int l = stat.size();
    if(l == 0) return;

    final int fs = GUIConstants.fontSize;
    h = header.getHeight() + 4;
    w = (int) (getWidth() * .98 - fs / 2d - header.getWidth());
    bw = fs * 2 + w / 10;
    bs = bw / (l - 1);

    // find maximum value
    int m = 1;
    for(int i = 0; i < l - 1; ++i) m = Math.max(m, stat.get(i));

    // draw focused bar
    final int by = 8;
    final int bh = h - by;

    for(int i = 0; i < l - 1; ++i) {
      if(i != focus) continue;
      final int bx = w - bw + bs * i;
      g.setColor(GUIConstants.color3);
      g.fillRect(bx, by, bs + 1, bh);
    }

    // draw all bars
    for(int i = 0; i < l - 1; ++i) {
      final int bx = w - bw + bs * i;
      g.setColor(GUIConstants.color((i == focus ? 3 : 2) + i * 2));
      final int p = Math.max(1, stat.get(i) * bh / m);
      g.fillRect(bx, by + bh - p, bs, p);
      g.setColor(GUIConstants.color(8));
      g.drawRect(bx, by + bh - p, bs, p - 1);
    }
  }
}
