package org.basex;

import static org.basex.Text.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.Enumeration;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import org.basex.core.Prop;
import org.basex.core.proc.Check;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.io.IO;
import org.basex.util.Performance;

/**
 * This is the starter class for the graphical frontend.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXWin {
  /**
   * Constructor.
   * @param args command-line arguments
   */
  protected BaseXWin(final String[] args) {
    // some mac issues
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", NAME);

    // read properties
    Prop.read();
    Prop.gui = true;
    GUIProp.read();

    // show waiting panel and wait some time to allow repainting
    final JFrame wait = waitPanel();
    Performance.sleep(50);
    GUIConstants.init();

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // initialize look and feel
        init();
        // open main window
        final GUI gui = new GUI();

        // open specified document or database
        if(args.length != 0) {
          String db = args[0].replace('\\', '/');
          if(db.endsWith(IO.BASEXSUFFIX)) {
            db = db.replaceAll("^.*/(.*)/.*", "$1");
          }
          gui.execute(new Check(db));
        }
        // close wait panel
        wait.dispose();
      }
    });
  }

  /**
   * Initializes the GUI.
   */
  public static void init() {
    try {
      // ..added to handle possible JDK 1.6 bug (thanks to Makoto Yui)
      UIManager.getInstalledLookAndFeels();
      // set system specific look & feel
      // temporary: avoid GTK problems in Ubuntu 7.10/other distributions
      UIManager.setLookAndFeel(GUIProp.javalook ?
          UIManager.getCrossPlatformLookAndFeelClassName() :
          UIManager.getSystemLookAndFeelClassName());

      if(GUIProp.javalook) {
        // apply non-bold fonts in Java look & feel
        final UIDefaults def = UIManager.getDefaults();
        final Enumeration<?> en = def.keys();
        while(en.hasMoreElements()) {
          final Object k = en.nextElement();
          final Object v = def.get(k);
          if(v instanceof Font) def.put(k, ((Font) v).deriveFont(0));
        }
      }

      // delay tooltip disappearance
      ToolTipManager.sharedInstance().setDismissDelay(20000);
      // refresh views when windows are resized
      Toolkit.getDefaultToolkit().setDynamicLayout(true);

      /*
      for(java.lang.reflect.Field f : java.awt.SystemColor.class.getFields()) {
        final Object o = f.get(null);
        //if(!(o instanceof java.awt.SystemColor)) continue;
        final Color c = (Color) o;
        BaseX.outln("%: %,%,%", f, c.getRed(), c.getGreen(), c.getBlue());
      }*/

      /*
      final UIDefaults def = UIManager.getDefaults();
      final Enumeration<?> en = def.keys();
      while(en.hasMoreElements()) {
        final Object k = en.nextElement();
        final Object v = def.get(k);
        BaseX.outln("%: % (%)", k, v,
            v != null ? v.getClass().getSimpleName() : null);
      }*/
    } catch(final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns a starter window.
   * @return starter window
   */
  private JFrame waitPanel() {
    final JFrame frame = new JFrame(Text.TITLE);
    frame.setUndecorated(true);

    final JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(2, 1));
    panel.setBackground(Color.white);
    panel.setBorder(new CompoundBorder(new MatteBorder(1, 1, 1, 1,
        Color.GRAY), new EmptyBorder(3, 16, 6, 16)));

    JLabel label = new JLabel(WAIT1);
    label.setFont(label.getFont().deriveFont(0));
    panel.add(label);
    label = new JLabel(WAIT2, SwingConstants.CENTER);
    label.setFont(label.getFont().deriveFont(0));
    panel.add(label);

    frame.add(panel);
    frame.pack();

    final Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
    final Dimension p = frame.getSize();
    frame.setLocation((s.width - p.width) >> 1, (s.height - p.height) >> 1);
    frame.setVisible(true);
    return frame;
  }

  /**
   * Main Method.
   * @param args command line arguments.
   * An initial XML document or database file can be specified as argument.
   */
  public static void main(final String[] args) {
    new BaseXWin(args);
  }
}
