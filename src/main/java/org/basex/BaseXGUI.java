package org.basex;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import static org.basex.core.Text.*;
import org.basex.core.cmd.Check;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIMacOSX;
import org.basex.gui.GUIProp;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.util.Args;
import org.basex.util.Util;
import org.basex.util.list.StringList;

import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;

/**
 * This is the starter class for the graphical frontend.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BaseXGUI {
  /** Database context. */
  final Context context = new Context();
  /** Files, specified as arguments. */
  final StringList files = new StringList();
  /** Mac OS X GUI optimizations. */
  GUIMacOSX osxGUI;

  /**
   * Main method.
   * @param args command-line arguments.
   * An XML document or query file can be specified as argument
   */
  public static void main(final String... args) {
    try {
      new BaseXGUI(args);
    } catch(final BaseXException ex) {
      Util.errln(ex);
      System.exit(1);
    }
  }

  /**
   * Constructor.
   * @param args command-line arguments
   * @throws BaseXException database exception
   */
  private BaseXGUI(final String... args) throws BaseXException {
    parseArguments(args);

    // set mac specific properties
    if(Prop.MAC) {
      try {
        osxGUI = new GUIMacOSX();
      } catch(final Exception ex) {
        throw new BaseXException("Failed to initialize native Mac OS X interface", ex);
      }
    }

    // read properties
    final GUIProp gprop = new GUIProp();
    context.prop.set(Prop.CACHEQUERY, true);
    context.prop.set(Prop.MAXHITS, gprop.num(GUIProp.MAXHITS));
    GUIConstants.init(gprop);

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        // initialize look and feel
        init(gprop);
        // open main window
        final GUI gui = new GUI(context, gprop);
        if(osxGUI != null) osxGUI.init(gui);

        // open specified document or database
        boolean xml = false;
        for(final String file : files) {
          final String input = file.replace('\\', '/');
          final IOFile io = new IOFile(input);
          boolean xq = false;
          for(final String suf : IO.XQSUFFIXES) xq |= input.endsWith(suf);
          if(xq) {
            gui.editor.open(io);
          } else if(!xml) {
            // only parse first xml file
            gui.execute(new Check(input));
            gprop.set(GUIProp.CREATEPATH, io.path());
            gprop.set(GUIProp.CREATENAME, io.dbname());
            xml = true;
          }
        }
      }
    });
  }

  /**
   * Initializes the GUI.
   * @param prop gui properties
   */
  static void init(final GUIProp prop) {
    try {
      // added to handle possible JDK 1.6 bug (thanks to Makoto Yui)
      UIManager.getInstalledLookAndFeels();
      // refresh views when windows are resized
      Toolkit.getDefaultToolkit().setDynamicLayout(true);
      // set specified look & feel
      if(prop.is(GUIProp.JAVALOOK)) {
        // use non-bold fonts in Java's look & feel
        final UIDefaults def = UIManager.getDefaults();
        final Enumeration<?> en = def.keys();
        while(en.hasMoreElements()) {
          final Object k = en.nextElement();
          final Object v = def.get(k);
          if(v instanceof Font) def.put(k, ((Font) v).deriveFont(Font.PLAIN));
        }
      } else {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
    } catch(final Exception ex) {
      Util.stack(ex);
    }
  }

  /**
   * Parses the command-line arguments, specified by the user.
   * @param args command-line arguments
   * @throws BaseXException database exception
   */
  private void parseArguments(final String[] args) throws BaseXException {
    final Args arg = new Args(args, this, GUIINFO, Util.info(CONSOLE, GUIMODE));
    while(arg.more()) {
      if(arg.dash()) arg.usage();
      files.add(arg.string());
    }
  }
}
