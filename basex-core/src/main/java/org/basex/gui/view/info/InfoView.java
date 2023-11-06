package org.basex.gui.view.info;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.listener.*;
import org.basex.gui.text.*;
import org.basex.gui.view.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This view displays query information.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class InfoView extends View implements LinkListener, QueryTracer {
  /** Categories. */
  private static final String[] CATEGORIES = { ALL, COMMAND, Text.ERROR, EVALUATING, COMPILING,
      OPTIMIZING, OPTIMIZED_QUERY, QUERY, RESULT, TIMING, QUERY_PLAN
  };

  /** Searchable editor. */
  private final SearchEditor editor;

  /** Header label. */
  private final BaseXHeader header;
  /** Categories. */
  private final BaseXCombo cats;
  /** Info label for total time. */
  private final BaseXLabel label;
  /** Text Area. */
  private final TextPanel text;

  /** Painting flag. */
  private boolean paint;
  /** Category chosen by user. */
  private String cat = ALL;
  /** Time measurements. */
  private IntList times = new IntList(4);
  /** Time strings. */
  private StringList timeStrings = new StringList(4);
  /** Full text. */
  private byte[] all = Token.EMPTY;
  /** New text. */
  private byte[] newText;
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
   * @param notifier view notifier
   */
  public InfoView(final ViewNotifier notifier) {
    super(GUIConstants.INFOVIEW, notifier);
    border(5).layout(new BorderLayout(0, 5));

    header = new BaseXHeader(INFO);

    // first assign values, then assign maximal width
    cats = new BaseXCombo(gui, ALL);
    String maxString = "";
    for(final String c : CATEGORIES) {
      if(c.length() > maxString.length()) maxString = c;
    }
    cats.setPrototypeDisplayValue(maxString);
    cats.addActionListener(ev -> {
      while(paint) Thread.yield();

      cat = cats.getSelectedItem();
      final byte[] start = new TokenBuilder().bold().add(cat).add(COL).norm().nline().finish();
      final byte[] end = new TokenBuilder().bold().finish();
      final int s = Token.indexOf(all, start);
      if(s != -1) {
        final int e = Token.indexOf(all, end, s + start.length);
        newText = Token.substring(all, s, e != -1 ? e : all.length);
      } else {
        newText = all;
      }
      repaint();
    });

    label = new BaseXLabel(" ").resize(1.25f);

    text = new TextPanel(gui, false);
    text.setLinkListener(this);
    editor = new SearchEditor(gui, text);

    final BaseXToolBar buttons = new BaseXToolBar();
    buttons.add(editor.button());

    final BaseXBack center = new BaseXBack(false).layout(new ColumnLayout(10));
    center.add(cats);
    center.add(label);

    final BaseXBack north = new BaseXBack(false).layout(new BorderLayout(10, 10));
    north.add(buttons, BorderLayout.WEST);
    north.add(center, BorderLayout.CENTER);
    north.add(header, BorderLayout.EAST);
    add(north, BorderLayout.NORTH);

    add(editor, BorderLayout.CENTER);
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
    text.setFont(GUIConstants.font);
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
   * @param cmd command that created the output (can be {@code null})
   * @param ok indicates if evaluation was successful
   * @param reset clear text area when method is called next time
   */
  public void setInfo(final String info, final Command cmd, final boolean ok, final boolean reset) {
    setInfo(info, cmd, null, ok, reset);
  }

  /**
   * Displays the specified info string.
   * @param info string to be displayed
   * @param cmd command that created the output (can be {@code null})
   * @param time time required for running the command
   * @param ok indicates if evaluation was successful
   * @param reset clear text area when method is called next time
   * @return total time (passed on, or updated, argument)
   */
  public String setInfo(final String info, final Command cmd, final String time, final boolean ok,
      final boolean reset) {

    final TokenBuilder tb = new TokenBuilder().add(all);
    final StringList eval = new StringList(1);
    final StringList comp = new StringList(1);
    final StringList opt = new StringList(1);
    final StringList plan = new StringList(1);
    final StringList result = new StringList(1);
    final StringList error = new StringList(1);
    final StringList origqu = new StringList(1);
    final StringList optqu = new StringList(1);
    final StringList command = new StringList(1);

    final StringList strings = new StringList(5);
    final IntList tms = new IntList(5);

    final int runs = Math.max(1, gui.context.options.get(MainOptions.RUNS));
    final String[] split = info.split(NL);
    final int sl = split.length;
    for(int s = 0; s < sl; s++) {
      final String line = split[s];
      if(line.startsWith(PARSING_CC) || line.startsWith(COMPILING_CC) ||
          line.startsWith(OPTIMIZING_CC) || line.startsWith(EVALUATING_CC) ||
          line.startsWith(PRINTING_CC) || line.startsWith(TOTAL_TIME_CC)) {
        final int t = line.indexOf(" ms");
        final int d = line.indexOf(':');
        final int tm = (int) (Double.parseDouble(line.substring(d + 1, t)) * 100);
        tms.add(tm);
        final String key = line.substring(0, d).trim();
        final String val = Performance.getTime(tm * 10000L * runs, runs);
        strings.add(LI + key + COLS + val);
      } else if(line.startsWith(NUMBER_CC) || line.startsWith(UPDATED_CC) ||
          line.startsWith(PRINTED_CC) || line.startsWith(READ_LOCKING_CC) ||
          line.startsWith(WRITE_LOCKING_CC)) {
        result.add(LI + line);
      } else if(line.equals(COMPILING + COL)) {
        while(++s < sl && !split[s].isEmpty()) comp.add(split[s]);
      } else if(line.equals(OPTIMIZING + COL)) {
        while(++s < sl && !split[s].isEmpty()) opt.add(split[s]);
      } else if(line.equals(QUERY + COL)) {
        while(++s < sl && !split[s].isEmpty()) origqu.add(split[s]);
      } else if(line.equals(OPTIMIZED_QUERY + COL)) {
        while(++s < sl && !split[s].isEmpty()) optqu.add(split[s]);
      } else if(line.startsWith(EVALUATING)) {
        while(++s < sl && split[s].startsWith(LI)) {
          eval.add(split[s].substring(2).replace('|', '\n'));
        }
      } else if(line.equals(QUERY_PLAN + COL)) {
        while(++s < sl && !split[s].isEmpty()) plan.add(split[s]);
      } else if(line.equals(Text.ERROR + COL)) {
        boolean stopped = false;
        while(++s < sl && !split[s].isEmpty()) {
          final Matcher matcher = Pattern.compile(STOPPED_AT + "(.*)" + COL).matcher(split[s]);
          if(!stopped && matcher.find()) {
            final TokenBuilder tmp = new TokenBuilder();
            tmp.add(STOPPED_AT).uline().add(matcher.group(1)).uline().add(COL);
            split[s] = tmp.toString();
            stopped = true;
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
          error.add(tmp.toString());
          if(last) break;
        }
      } else if(!ok && !line.isEmpty()) {
        error.add(line);
      }
    }

    times = tms;
    timeStrings = strings;

    final boolean test = cmd instanceof Test, query = cmd instanceof AQuery;
    /* reset old text if:
     * a) deletion was requested by the last function call
     * b) the result contains execution times
     * c) result is not ok and no XQUnit tests are run */
    if(clear || !tms.isEmpty() || !(ok || test)) {
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

    final StringList list = new StringList().add(ALL);
    add(COMMAND, command, tb, list);
    add(Text.ERROR, error, tb, list);
    add(EVALUATING, eval, tb, list);
    add(RESULT, result, tb, list);
    add(TIMING, strings, tb, list);
    add(OPTIMIZED_QUERY, optqu, tb, list);
    add(OPTIMIZING, opt, tb, list);
    add(COMPILING, comp, tb, list);
    add(QUERY, origqu, tb, list);
    add(QUERY_PLAN, plan, tb, list);
    if(inf != null) tb.add(inf).nline();
    clear = reset;

    // show total time required for running a command
    String total = time;
    if(!tms.isEmpty()) {
      total = Performance.getTime(tms.get(tms.size() - 1) * 10000L * runs, runs);
    }
    if(total != null) setTime(TOTAL_TIME_CC + total);
    all = tb.finish();
    newText = all;

    // refresh combo box, reassign old value
    cats.setItems(list.toArray());
    cats.setSelectedItem(cat);

    repaint();
    return total;
  }

  /**
   * Adds the specified strings.
   * @param head string header
   * @param list list reference
   * @param tb token builder
   * @param cats categories to choose from
   */
  private static void add(final String head, final StringList list, final TokenBuilder tb,
      final StringList cats) {
    if(list.isEmpty()) return;
    tb.bold().add(head).add(COL).norm().nline();
    for(final String line : list) tb.add(line).nline();
    tb.hline();
    cats.add(head);
  }

  @Override
  public void linkClicked(final String link) {
    gui.editor.jump(link);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    final int l = times.size();
    if(l == 0) return;

    int f = -1;
    if(e.getY() < h) {
      for(int i = 0; i < l; ++i) {
        final int bx = w - bw + bs * i;
        if(e.getX() >= bx && e.getX() < bx + bs) f = i;
      }
    }
    if(f != focus) {
      setTime(timeStrings.get(f == -1 ? l - 1 : f).replace(LI, ""));
      repaint();
      focus = f;
    }
  }

  @Override
  public void paintComponent(final Graphics g) {
    paint = true;
    if(newText != null) {
      text.setText(newText);
      newText = null;
    }

    super.paintComponent(g);
    final int l = times.size();
    if(l != 0) {
      h = header.getHeight();
      w = getWidth() - header.getWidth() - 16;
      bw = 100;
      bs = bw / (l - 1);

      // find maximum value
      int m = 1;
      for(int i = 0; i < l - 1; ++i) m = Math.max(m, times.get(i));

      // draw focused bar
      final int by = 8, bh = h - by;
      for(int i = 0; i < l - 1; ++i) {
        if(i != focus) continue;
        final int bx = w - bw + bs * i;
        g.setColor(GUIConstants.color3);
        g.fillRect(bx, by, bs + 1, bh);
      }

      // draw all bars
      for(int i = 0; i < l - 1; ++i) {
        final int bx = w - bw + bs * i, c = (i == focus ? 4 : 2) + i;
        g.setColor(GUIConstants.color(c));
        final int p = Math.max(1, times.get(i) * bh / m);
        g.fillRect(bx, by + bh - p, bs, p);
        g.setColor(GUIConstants.color(c + 2));
        g.drawRect(bx, by + bh - p, bs, p - 1);
      }
    }
    paint = false;
  }

  @Override
  public boolean print(final String info) {
    if(clear || all.length < 50000) setInfo(info, null, true, false);
    return true;
  }

  /**
   * Displays the specified runtime.
   * @param info info string with measured time
   */
  private void setTime(final String info) {
    final StringBuilder sb = new StringBuilder().append(info);
    final long ms = Long.parseLong(info.replaceAll("^.+: |\\..+", ""));
    if(ms >= 60000) {
      // append hh:mm:ss format if time exceeds 1 minute
      final long seconds = ms / 1000, minutes = seconds % 3600 / 60, hours = seconds / 3600;
      final String time = String.format("%02d:%02d:%02d", hours, minutes, seconds % 60);
      sb.append(" (").append(time).append(')');
    }
    label.setText(sb.toString());
  }
}
