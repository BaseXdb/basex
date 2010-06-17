package org.basex;

import java.awt.Font;
import java.awt.Toolkit;
import java.util.Enumeration;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.proc.Check;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIMacOSX;
import org.basex.gui.GUIProp;
import org.basex.io.IO;

/**
 * This is the starter class for the graphical frontend.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class BaseXWin {
  /** Mac OS X GUI optimizations. */
  GUIMacOSX osxGUI;

  /**
   * Main method.
   * @param args command-line arguments
   * An initial XML document or query file can be specified as argument
   */
  public static void main(final String[] args) {
    new BaseXWin(args);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  public BaseXWin(final String[] args) {
    // set mac specific properties
    if(Prop.MAC) {
      try {
        osxGUI = new GUIMacOSX();
      } catch(final Exception e) {
        Main.notexpected("Failed to initialize native Mac OS X interface", e);
      }
    }

    // read properties
    final Context ctx = new Context();
    ctx.prop.set(Prop.CACHEQUERY, true);
    Prop.gui = true;
    final GUIProp gprop = new GUIProp();

    GUIConstants.init(gprop);

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // initialize look and feel
        init(gprop);
        // open main window
        final GUI gui = new GUI(ctx, gprop);
        if(osxGUI != null) osxGUI.init(gui);

        // open specified document or database
        if(args.length != 0) {
          final String db = args[0].replace('\\', '/');
          final IO io = IO.get(db);
          if(db.endsWith(IO.XQSUFFIX)) {
            gui.query.setQuery(io);
          } else {
            gui.execute(new Check(db));
            gprop.set(GUIProp.OPENPATH, io.path());
          }
        }
      }
    });
  }

  /**
   * Initializes the GUI.
   * @param prop gui properties
   */
  void init(final GUIProp prop) {
    try {
      // added to handle possible JDK 1.6 bug (thanks to Makoto Yui)
      UIManager.getInstalledLookAndFeels();
      // set specified look & feel
      final boolean java = prop.is(GUIProp.JAVALOOK);
      UIManager.setLookAndFeel(java ?
          UIManager.getCrossPlatformLookAndFeelClassName() :
          UIManager.getSystemLookAndFeelClassName());
      // refresh views when windows are resized
      Toolkit.getDefaultToolkit().setDynamicLayout(true);

      if(java) {
        // use non-bold fonts in Java's look & feel
        final UIDefaults def = UIManager.getDefaults();
        final Enumeration<?> en = def.keys();
        while(en.hasMoreElements()) {
          final Object k = en.nextElement();
          final Object v = def.get(k);
          if(v instanceof Font) def.put(k, ((Font) v).deriveFont(0));
        }
      }
    } catch(final Exception ex) {
      ex.printStackTrace();
    }
  }
}
