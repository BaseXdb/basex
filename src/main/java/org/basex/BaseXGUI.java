package org.basex;

import static org.basex.core.Text.*;

import java.awt.Font;
import java.awt.Toolkit;
import java.util.Enumeration;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Check;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIMacOSX;
import org.basex.gui.GUIProp;
import org.basex.io.IO;
import org.basex.util.Args;
import org.basex.util.Util;

/**
 * This is the starter class for the graphical frontend.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class BaseXGUI {
  /** Mac OS X GUI optimizations. */
  protected GUIMacOSX osxGUI;
  /** File, specified as argument. */
  protected String file;

  /**
   * Main method.
   * @param args command-line arguments
   * An XML document or query file can be specified as argument
   */
  public static void main(final String[] args) {
    new BaseXGUI(args);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  public BaseXGUI(final String[] args) {
    if(!parseArguments(args)) return;

    // set mac specific properties
    if(Prop.MAC) {
      try {
        osxGUI = new GUIMacOSX();
      } catch(final Exception ex) {
        Util.notexpected("Failed to initialize native Mac OS X interface", ex);
      }
    }

    // read properties
    final Context ctx = new Context();
    ctx.prop.set(Prop.CACHEQUERY, true);
    Prop.gui = true;
    final GUIProp gprop = new GUIProp();

    GUIConstants.init(gprop);

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        // initialize look and feel
        init(gprop);
        // open main window
        final GUI gui = new GUI(ctx, gprop);
        if(osxGUI != null) osxGUI.init(gui);

        // open specified document or database
        if(file != null) {
          final String input = file.replace('\\', '/');
          final IO io = IO.get(input);
          boolean xq = false;
          for(final String suf : IO.XQSUFFIXES) xq |= input.endsWith(suf);
          if(xq) {
            gui.query.setQuery(io);
          } else {
            gui.execute(new Check(input));
            gprop.set(GUIProp.CREATEPATH, io.path());
            gprop.set(GUIProp.CREATENAME, io.dbname());
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
          if(v instanceof Font) def.put(k, ((Font) v).deriveFont(Font.PLAIN));
        }
      }
    } catch(final Exception ex) {
      Util.stack(ex);
    }
  }

  /**
   * Parses the command-line arguments, specified by the user.
   * @param args command-line arguments
   * @return success flag
   */
  private boolean parseArguments(final String[] args) {
    final Args arg = new Args(args, this, GUIINFO, Util.info(CONSOLE, GUIMODE));
    while(arg.more()) {
      if(arg.dash()) {
        arg.check(false);
      } else {
        file = file == null ? arg.string() : file + " " + arg.string();
      }
    }
    return arg.finish();
  }
}
