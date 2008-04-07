package org.basex.gui;

import static org.basex.Text.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import javax.swing.event.MouseInputAdapter;
import org.basex.core.Prop;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPanel;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * This is the status bar of the main window. It displays progress information
 * and includes a memory status.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class GUIStatus extends BaseXPanel implements Runnable {
  /** Width of the memory status box. */
  private static final int MEMW = 70;
  /** Current status text. */
  private String status = STATUSOK;
  /** Current path. */
  private String oldStatus = STATUSOK;
  /** Current performance info. */
  private String perf = "";
  /** Current focus on memory. */
  protected boolean memfocus;
  /** Error flag. */
  protected boolean error;
  /** Maximum memory. */
  protected long max = 1;
  /** Used memory. */
  protected long used;

  /**
   * Constructor.
   */
  public GUIStatus() {
    super(Token.token("Status Bar"));
    BaseXLayout.setHeight(this, getFont().getSize() + 6);
    final MouseInputAdapter mouse = new MouseInputAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        if(memfocus) {
          Performance.gc(4);
          repaint();

          final Runtime rt = Runtime.getRuntime();
          final long occ = rt.totalMemory();
          max = rt.maxMemory();
          used = occ - rt.freeMemory();
          
          final String inf =
            MEMTOTAL + Performance.formatSize(max, true) + Prop.NL +
            MEMRESERVED + Performance.formatSize(occ, true) + Prop.NL +
            MEMUSED + Performance.formatSize(used, true) + Prop.NL +
            Prop.NL + MEMHELP;

          JOptionPane.showMessageDialog(GUI.get(), inf, MEMTITLE,
              JOptionPane.INFORMATION_MESSAGE);
        }
      }
      @Override
      public void mouseMoved(final MouseEvent e) {
        memfocus = e.getX() > getWidth() - MEMW;
        if(memfocus) GUI.get().focus(GUIStatus.this, HELPMEM);

        GUI.get().setCursor(memfocus ? GUIConstants.CURSORHAND :
          GUIConstants.CURSORARROW);
      }
      @Override
      public void mouseExited(final MouseEvent e) {
        memfocus = false;
        GUI.get().setCursor(GUIConstants.CURSORARROW);
      }
    };
    addMouseListener(mouse);
    addMouseMotionListener(mouse);
  }

  /**
   * Sets the status text.
   * @param stat the text to be set
   */
  public void setText(final String stat) {
    error = false;
    refresh(stat);
  }

  /**
   * Sets the error status text.
   * @param stat the text to be set
   */
  public void setError(final String stat) {
    error = true;
    refresh(stat);
  }

  /**
   * Refreshes the status text.
   * @param txt status text
   */
  private void refresh(final String txt) {
    status = txt;
    oldStatus = status;
    perf = "";
    
    final Runtime rt = Runtime.getRuntime();
    max = rt.maxMemory();
    used = rt.totalMemory() - rt.freeMemory();
    if(txt.equals(STATUSWAIT)) new Thread(this).start();
    repaint();
  }
      
  /**
   * Sets performance information text.
   * @param info the text to be set
   */
  public void setPerformance(final String info) {
    perf = info;
    repaint();
  }

  /**
   * Sets the current path.
   * @param path the path to be set
   */
  public void setPath(final byte[] path) {
    status = path.length == 0 ? oldStatus : STATUSPATH + Token.string(path);
    error = false;
    perf = "";
    repaint();
  }

  /**
   * Thread.
   */
  public void run() {
    Performance.sleep(500);
    repaint();
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    final int ww = getWidth() - MEMW;
    final int hh = getHeight();

    final boolean full = used * 6 / 5 > max;
    g.setFont(getFont());
    g.setColor(GUIConstants.color1);
    g.fillRect(ww, 0, MEMW - 1, hh - 2);
    g.setColor(full ? GUIConstants.colormark4 : GUIConstants.color4);
    g.fillRect(ww + 2, 2, (int) (used * (MEMW - 7) / max), hh - 5);
    BaseXLayout.rect(g, true, ww, 0, ww + MEMW - 3, hh - 2);
    g.setColor(full ? GUIConstants.colormark3 : GUIConstants.color6);

    FontMetrics fm = g.getFontMetrics();
    final String mem = Performance.formatSize(used, true);
    int fw = ww + (MEMW - 4 - fm.stringWidth(mem)) / 2;
    final int h = fm.getHeight() - 2;
    g.drawString(mem, fw, h);

    g.setFont(g.getFont().deriveFont(Font.BOLD));
    fm = g.getFontMetrics();
    final int w = ww - fm.stringWidth(perf) - 10;
    g.setColor(Color.black);
    g.drawString(perf, w, h);

    final String st = status;
    fw = 24;
    int i = -1;
    final int il = st.length();
    while(++i != il) {
      if(fw > w) break;
      fw += fm.charWidth(st.charAt(i));
    }
    g.setColor(error ? GUIConstants.COLORERROR : Color.black);
    g.drawString(st.substring(0, i), 4, h);
    if(fw > w) g.drawString("...", fw - 16, h);
  }
}
