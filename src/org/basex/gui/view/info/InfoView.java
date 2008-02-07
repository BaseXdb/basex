package org.basex.gui.view.info;

import static org.basex.Text.*;
import static org.basex.gui.GUIConstants.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import org.basex.core.Prop;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.View;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * This view displays query information.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class InfoView extends View implements Runnable {
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
  /** Clicked mouse flag. */
  private boolean up;
  /** Clicked mouse flag. */
  private boolean down;
  /** Panel Width. */
  int w;
  /** Panel Height. */
  int h;
  /** Bar widths. */
  int bw;
  /** Bar size. */
  int bs;
  /** Minimum vertical position. */
  int my;
  /** Vertical start position. */
  int sy;
  /** Maximum vertical size. */
  int hy;

  /**
   * Default constructor.
   * @param help help text
   */
  public InfoView(final byte[] help) {
    super(help);
    setLayout(new BorderLayout(0, 4));
    setMode(FILL.UP);
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
    repaint();
  }

  /**
   * Processes the query info.
   * @param inf info string
   */
  public void setInfo(final byte[] inf) {
    final IntList il = new IntList();
    final StringList sl = new StringList();
    final StringList cmp = new StringList();
    final StringList eval = new StringList();
    final StringList pln = new StringList();

    final String[] split = Token.string(inf).split(Prop.NL);
    for(int i = 0; i < split.length; i++) {
      final String line = split[i];
      final int s = line.indexOf(':');
      if(line.startsWith(QUERYPARSE) || line.startsWith(QUERYCOMPILE) || 
          line.startsWith(QUERYEVALUATE) || line.startsWith(QUERYPRINT) ||
          line.startsWith(QUERYTOTAL)) {
        final int t = line.indexOf(MS);
        sl.add(line.substring(0, s).trim());
        il.add((int) (Double.parseDouble(line.substring(s + 1, t)) * 100));
      } else if(line.startsWith(QUERYSTRING)) {
        query = line.substring(s + 1).trim();
      } else if(line.startsWith(QUERYPLAN)) {
        while(++i < split.length && split[i].length() != 0)
          pln.add(split[i]);
        --i;
      } else if(line.startsWith(QUERYCOMP)) {
        while(!split[++i].contains(QUERYRESULT)) cmp.add(split[i]);
        result = split[i].substring(split[i].indexOf(':') + 1).trim();
      } else if(line.startsWith(QUERYEVAL)) {
        while(split[++i].startsWith(QUERYSEP)) eval.add(split[i]);
      }
    }

    if(sl.size != 0) {
      stat = il;
      strings = sl;
      compile = cmp;
      evaluate = eval;
      plan = pln;
      sy = 0;
      repaint();
    }
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    final int l = stat.size;
    if(l == 0) return;

    focus = -1;
    final int x = e.getX();
    final int y = e.getY();

    for(int i = 0; i < l; i++) {
      final int bx = w - bw + bs * i;
      if(x >= bx && x < bx + bs) focus = i;
    }

    final boolean f = hy - sy > h && x >= w - bw - 30 && x < w - bw - 10 &&
      y >= h - (sy < 0 ? 40 : 20) && y < h - (hy > h ? 0 : 20);
    GUI.get().setCursor(f ? CURSORHAND : CURSORARROW);
    repaint();
  }


  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    int tmp = sy - e.getUnitsToScroll() * 20;
    if(-h - this.getSize().height + my <= tmp && tmp <= 0) {
      sy = tmp;
      repaint();
    }
  }
  
  @Override
  public void mousePressed(final MouseEvent e) {
    super.mousePressed(e);
    if(hy - sy <= h) return;

    final int x = e.getX();
    final int y = e.getY();
    if(hy - sy > h && x >= w - bw - 30 && x < w - bw - 10 &&
      y >= h - (sy < 0 ? 40 : 20) && y < h - (hy > h ? 0 : 20)) {
      if(y < h - 20) {
        down = true;
        new Thread(this).start();
      } else {
        up = true;
        new Thread(this).start();
      }
    }
  }

  /** {@inheritDoc} */
  public void run() {
    while(up || down) {
      if(up && hy > h) {
        sy -= GUIProp.fontsize;
      } else if(down && sy < 0) {
        sy += GUIProp.fontsize;
      } else {
        GUI.get().setCursor(CURSORARROW);
      }
      repaint();
      Performance.sleep(40);
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    down = false;
    up = false;
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    mouseMoved(e);
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    GUI.get().setCursor(CURSORARROW);
    focus = -1;
    repaint();
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    super.keyPressed(e);
    if(hy - sy <= h) return;

    final int fs = GUIProp.fontsize;
    final int c = e.getKeyCode();
    if(c == KeyEvent.VK_DOWN) {
      sy = Math.max(sy + h - hy, sy - fs);
    } else if(c == KeyEvent.VK_PAGE_DOWN) {
      sy = Math.max(sy + h - hy, sy - h);
    } else if(c == KeyEvent.VK_UP) {
      sy = Math.min(0, sy + fs);
    } else if(c == KeyEvent.VK_PAGE_UP) {
      sy = Math.min(0, sy + h);
    } else if(c == KeyEvent.VK_HOME) {
      sy = 0;
    } else if(c == KeyEvent.VK_END) {
      sy += h - hy;
    }
    repaint();
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    BaseXLayout.antiAlias(g);

    my = lfont.getSize() + 5;
    final int fh = GUIProp.fontsize;

    g.setColor(COLORS[16]);
    g.setFont(lfont);
    g.drawString(GUIConstants.INFOVIEW, 8, my);

    g.setColor(Color.black);
    final int l = stat.size;
    if(l == 0) return;

    h = getHeight() - 8;
    w = getWidth() - 8;
    // calculate bar size with a mix of font size and panel width
    bw = fh * 2 + w / 10;
    bs = bw / l;

    hy = sy + my + fh;
    my += 4;
    hy = draw(g, "Query:  ", query, hy, fh);
    hy = draw(g, QUERYCOMP, compile, hy, fh);
    if(compile.size != 0) hy = draw(g, QUERYRESULT, result, hy, fh);
    hy = draw(g, QUERYEVAL, evaluate, hy, fh);
    hy = draw(g, QUERYTIME, strings, hy, fh);
    hy = draw(g, QUERYPLAN, plan, hy, fh) - fh;

    if(hy - sy > h) {
      g.drawImage(hy > h ? ARROWDOWN : ARROWDOWNIN, w - bw - 30, h - 20, this);
      g.drawImage(sy < 0 ? ARROWUP : ARROWUPIN, w - bw - 30, h - 40, this);
    }

    // find maximum value
    int m = 0;
    for(int i = 0; i < l; i++) if(m < stat.get(i)) m = stat.get(i);

    // draw focused bar
    final int by = 15 + 5 * fh / 2;
    final int bh = h - by;

    for(int i = 0; i < l; i++) {
      if(i != focus) continue;
      final int bx = w - bw + bs * i;
      g.setColor(color2);
      g.fillRect(bx, by, bs + 1, bh);
    }

    // draw all bars
    for(int i = 0; i < l; i++) {
      final int bx = w - bw + bs * i;
      g.setColor(COLORS[(i == focus ? 3 : 2) + i * 2]);
      final int p = Math.max(1, stat.get(i) * bh / m);
      g.fillRect(bx, by + bh - p, bs, p);
      g.setColor(COLORBUTTON);
      g.drawRect(bx, by + bh - p, bs, p - 1);
    }

    final int f = focus == -1 ? l - 1 : focus;
    g.setColor(Color.black);
    g.setFont(bfont);
    BaseXLayout.drawRight(g, strings.list[f], w - 2, fh + 5);
    g.setFont(font);
    BaseXLayout.drawRight(g, Performance.getTimer(stat.get(f) * 10000L *
        Prop.runs, Prop.runs), w - 2, fh * 2 + 7);
  }

  /**
   * Draws a string list.
   * @param g graphics reference
   * @param header string header
   * @param list list reference
   * @param yy y position
   * @param fh font height
   * @return resulting y position
   */
  private int draw(final Graphics g, final String header,
      final StringList list, final int yy, final int fh) {
    int y = yy;
    if(list.size != 0) {
      g.setFont(bfont);
      if(my < y) BaseXLayout.chopString(
          g, Token.token(header), 8, y, w - bw - 8);
      y += fh + 4;
      g.setFont(font);
      for(int i = 0; i < list.size; i++) {
        String line = list.list[i];
        if(list == strings) line = QUERYSEP + line + ":  " +
          Performance.getTimer(stat.get(i) * 10000L * Prop.runs, Prop.runs);
        if(my < y) BaseXLayout.chopString(
            g, Token.token(line), 16, y, w - bw - 24);
        y += fh;
      }
      y += fh;
    }
    return y;
  }

  /**
   * Draws a string.
   * @param g graphics reference
   * @param header string header
   * @param content string
   * @param yy y position
   * @param fh font height
   * @return resulting y position
   */
  private int draw(final Graphics g, final String header,
      final String content, final int yy, final int fh) {
    int y = yy;
    if(content.length() != 0) {
      if(my < y) {
        g.setFont(bfont);
        int x = 8;
        x += BaseXLayout.chopString(g, Token.token(header), x, y, w - bw - 8);
        g.setFont(font);
        BaseXLayout.chopString(g, Token.token(content), x, y, w - bw - x - 8);
      }
      y += fh * 2;
    }
    return y;
  }


  @Override
  public void componentResized(final ComponentEvent e) {
    sy = 0;
  }
}
