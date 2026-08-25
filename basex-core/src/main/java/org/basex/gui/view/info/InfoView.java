package org.basex.gui.view.info;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.*;
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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class InfoView extends View implements LinkListener, QueryTracer {
  /** Key of the section that shows everything. */
  private static final String ALL_KEY = "all";
  /** Key of the section with the command that was run. */
  private static final String COMMAND_KEY = "command";
  /** Keys of the sections, in the order in which they are displayed. */
  private static final String[] SECTIONS = { ALL_KEY, COMMAND_KEY, QueryInfo.ERROR,
      QueryInfo.EVALUATION, QueryInfo.RESULT, QueryInfo.OPTIMIZED_QUERY, QueryInfo.OPTIMIZATION,
      QueryInfo.COMPILATION, QueryInfo.QUERY, QueryInfo.PLAN, QueryInfo.TIMING
  };

  /** Error start. */
  private static final Pattern STOPPED = Pattern.compile(Pattern.quote(STOPPED_AT) + "(.*)" + COL);

  /** Searchable editor. */
  private final SearchEditor editor;

  /** Header label. */
  private final BaseXHeader header;
  /** Sections a user can display. */
  private final BaseXCombo sections;
  /** Info label for total time. */
  private final BaseXLabel label;
  /** Text Area. */
  private final TextPanel text;

  /** Painting flag. */
  private boolean paint;
  /** Currently selected section. */
  private String section = Strings.titleCase(ALL_KEY);
  /** Time measurements (nanoseconds). */
  private LongList times = new LongList(4);
  /** Time strings. */
  private StringList timeStrings = new StringList(4);
  /** Full text. */
  private byte[] all = Token.EMPTY;
  /** New text (can be {@code null}). */
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
    sections = new BaseXCombo(gui, Strings.titleCase(ALL_KEY));
    String maxSection = "";
    for(final String key : SECTIONS) {
      final String name = Strings.titleCase(key);
      if(name.length() > maxSection.length()) maxSection = name;
    }
    sections.setPrototypeDisplayValue(maxSection);
    sections.addActionListener(ev -> {
      while(paint) Performance.sleep(1);

      section = sections.getSelectedItem();
      final byte[] start = new TokenBuilder().bold().add(section).add(COL).norm().nline().finish();
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
    center.add(sections);
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
   * @param time time required for running the command (can be {@code null})
   * @param ok indicates if evaluation was successful
   * @param reset clear text area when method is called next time
   * @return total time (passed on, or updated, argument)
   */
  public String setInfo(final String info, final Command cmd, final String time, final boolean ok,
      final boolean reset) {

    final TokenBuilder tb = new TokenBuilder().add(all);
    final StringList result = new StringList(1);
    final StringList error = new StringList(1);
    final StringList command = new StringList(1);

    // a query hands its information over as data; everything else is read from the info string
    final AQuery query = cmd instanceof final AQuery aq ? aq : null;
    final QueryInfo.Sections qs = query != null ? query.sections() : null;
    final Map<String, StringList> sctns = new LinkedHashMap<>();
    times = qs != null ? qs.times() : new LongList(1);
    if(qs != null) {
      for(final String key : SECTIONS) {
        final QueryInfo.Section sctn = qs.sections().get(key);
        if(sctn != null) sctns.put(Strings.titleCase(key), QueryInfo.lines(sctn));
      }
      if(!ok) addError(query.message(), error);
    } else {
      final String[] split = info.split(NL);
      final int sl = split.length;
      for(int s = 0; s < sl; s++) {
        final String line = split[s];
        if(line.equals(Strings.titleCase(QueryInfo.ERROR) + COL)) {
          boolean stopped = false;
          while(++s < sl && !split[s].isEmpty()) {
            final Matcher matcher = STOPPED.matcher(split[s]);
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
    }

    timeStrings = sctns.getOrDefault(Strings.titleCase(QueryInfo.TIMING), new StringList(0));

    final boolean test = cmd instanceof Test;
    /* reset old text if:
     * a) deletion was requested by the last function call
     * b) the result contains execution times
     * c) result is not ok and no XQUnit tests are run */
    if(clear || !times.isEmpty() || !(ok || test)) {
      tb.reset();
    } else if(test) {
      // XQUnit tests: adopt trace output
      sctns.computeIfAbsent(Strings.titleCase(QueryInfo.EVALUATION), k -> new StringList(1)).
          add(tb.toString().trim());
      tb.reset();
    }

    String inf = null;
    if(query == null) {
      if(cmd != null) command.add(cmd.toString());
      if(ok && !info.isEmpty()) {
        if(reset) result.add(info.trim());
        else if(cmd == null) inf = info.trim();
      }
    }

    final StringList names = new StringList().add(Strings.titleCase(ALL_KEY));
    add(Strings.titleCase(COMMAND_KEY), command, tb, names);
    add(Strings.titleCase(QueryInfo.ERROR), error, tb, names);
    // the result of a command precedes the sections of a query, which has none of its own
    if(!result.isEmpty()) sctns.put(Strings.titleCase(QueryInfo.RESULT), result);
    for(final Entry<String, StringList> sctn : sctns.entrySet()) {
      add(sctn.getKey(), sctn.getValue(), tb, names);
    }
    if(inf != null) {
      final byte[] prev = tb.next();
      tb.add(inf).nline().add(prev);
    }
    clear = reset;

    // show total time required for running a command; a query has measured it itself
    String total = time;
    if(!timeStrings.isEmpty()) {
      total = timeStrings.get(timeStrings.size() - 1).replaceAll(".*" + COLS, "");
    }
    if(total != null) setTime(Strings.titleCase(QueryInfo.TOTAL) + COLS + total);
    all = tb.finish();
    newText = all;

    // refresh combo box, reassign old value
    sections.setItems(names.toArray());
    sections.setSelectedItem(section);

    repaint();
    return total;
  }

  /**
   * Adds the message of a failed query, underlining the position it stopped at and the entries
   * of its stack trace.
   * @param message error message
   * @param error error section
   */
  private static void addError(final String message, final StringList error) {
    boolean stopped = false, trace = false;
    for(final String line : message.split(NL)) {
      if(line.isEmpty()) continue;
      final TokenBuilder tb = new TokenBuilder();
      final Matcher matcher = STOPPED.matcher(line);
      if(line.equals(STACK_TRACE + COL)) {
        trace = true;
        tb.add(line);
      } else if(trace && line.startsWith(LI)) {
        tb.add(LI).uline().add(line.substring(2).replaceAll("<.*", "")).uline();
      } else if(!stopped && matcher.find()) {
        tb.add(STOPPED_AT).uline().add(matcher.group(1)).uline().add(COL);
        stopped = true;
      } else {
        tb.add(line);
      }
      error.add(tb.toString());
    }
  }

  /**
   * Adds the specified strings.
   * @param head string header
   * @param list list reference
   * @param tb token builder
   * @param sections sections a user can display
   */
  private static void add(final String head, final StringList list, final TokenBuilder tb,
      final StringList sections) {
    if(list.isEmpty()) return;
    tb.bold().add(head).add(COL).norm().nline();
    for(final String line : list) tb.add(line).nline();
    tb.hline();
    sections.add(head);
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
      long m = 1;
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
        final int p = (int) Math.max(1, times.get(i) * bh / m);
        g.fillRect(bx, by + bh - p, bs, p);
        g.setColor(GUIConstants.color(c + 2));
        g.drawRect(bx, by + bh - p, bs, p - 1);
      }
    }
    paint = false;
  }

  @Override
  public void printTrace(final String message) {
    setInfo(message, null, true, false);
  }

  @Override
  public boolean moreTraces(final int count) {
    return clear || count <= 50_000 && all.length <= 10_000_000;
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
