package org.basex.gui;

import static org.basex.gui.GUIConstants.*;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.MouseInputAdapter;
import org.basex.BaseX;
import org.basex.BaseXWin;
import org.basex.core.Commands;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.Find;
import org.basex.core.proc.Link;
import org.basex.core.proc.Proc;
import org.basex.gui.layout.BaseXTextField;

/**
 * This class offers a simple search window for creating symbolic
 * links in a 'basex' sub-directory of your home directory.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class GUISearch extends JFrame {
  /** Database Context. */
  public static final Context CONTEXT = new Context();
  /** Flag for opening a file explorer. */
  private static final boolean SHOWFILES = true;
  /** Flag for realtime search. */
  private static final boolean REALTIME = false;
  /** Name of file system database instance. */
  private static final String FSDATABASE = "FS";
  
  /** Current process. */
  Proc proc;
  /** Last search. */
  String last = "";

  /**
   * Constructor.
   */
  GUISearch() {
    super("BaseX Search");
    setUndecorated(true);
    setAlwaysOnTop(true);
    setIconImage(GUI.image(IMGICON));
    setLayout(new BorderLayout());

    final BaseXTextField input = new BaseXTextField(null);
    input.setBorder(new CompoundBorder(new EtchedBorder(),
        new EmptyBorder(1, 4, 1, 4)));

    input.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        if(!REALTIME && e.getKeyCode() != KeyEvent.VK_ENTER) return;

        String text = input.getText().trim();
        if(!text.startsWith("/")) text = Find.find(text, CONTEXT, true);
        if(!text.equals(last)) {
          last = text;
          if(proc != null) proc.stop();

          // open window with results..
          if(SHOWFILES && !Prop.UNIX) {
            try {
              final Runtime run = Runtime.getRuntime();
              run.exec("explorer " + Link.LINKDIR.replace("/", "\\"));
            } catch(final IOException ex) {
              ex.printStackTrace();
            }
          }

          proc = Proc.get(CONTEXT, Commands.LINK, text);
          proc.execute();
        }
      }
    });
    final MouseInputAdapter mouse = new MouseInputAdapter() {
      /** Window position. */ Point p;

      @Override
      public void mousePressed(final MouseEvent e) {
        p = input.getLocationOnScreen();
        p.x += e.getX();
        p.y += e.getY();
      }
      @Override
      public void mouseDragged(final MouseEvent e) {
        final Point n = input.getLocationOnScreen();
        n.x += e.getX();
        n.y += e.getY();

        final Point w = getLocation();
        setLocation(w.x + n.x - p.x, w.y + n.y - p.y);
        p = n;
      }
    };

    input.addMouseMotionListener(mouse);
    input.addMouseListener(mouse);

    final JLabel label = new JLabel(new ImageIcon(GUI.image(IMGBASEX)));
    label.setBorder(new CompoundBorder(new EtchedBorder(),
        new EmptyBorder(2, 2, 2, 2)));
    label.addMouseMotionListener(mouse);
    label.addMouseListener(mouse);
    label.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        int w = getWidth() + 100;
        if(w > 600) w -= 500;
        setSize(w, getHeight());
        validate();
        repaint();
      }
    });

    add(label, BorderLayout.WEST);
    add(input, BorderLayout.CENTER);
    pack();
    setSize(GUIProp.searchwidth, getHeight());
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLocation(GUIProp.searchloc[0], GUIProp.searchloc[1]);
    setVisible(true);
    input.requestFocusInWindow();

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent e) {
        GUIProp.searchloc[0] = getX();
        GUIProp.searchloc[1] = getY();
        GUIProp.searchwidth = getWidth();
        GUIProp.write();
      }
    });
  }

  /**
   * Main Method.
   * @param args command line arguments (ignored).
   */
  public static void main(final String[] args) {
    // read properties
    Prop.read();
    GUIProp.read();
    GUIConstants.init();
    Link.delete();

    final Proc proc = Proc.get(CONTEXT, Commands.OPEN, FSDATABASE);
    if(!proc.execute()) {
      BaseX.errln(proc.info());
    } else {
      BaseXWin.init();
      new GUISearch();
    }
  }
}
