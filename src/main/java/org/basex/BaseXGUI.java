package org.basex;

import static org.basex.core.Text.*;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.gui.*;
import org.basex.gui.dialog.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

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
   * @param args command-line arguments
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
  public BaseXGUI(final String... args) throws BaseXException {
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
    // deactivate real-time execution and filtering
    gprop.set(GUIProp.FILTERRT, false);
    gprop.set(GUIProp.EXECRT, false);
    // cache results to pass them on to all visualizations
    context.prop.set(Prop.CACHEQUERY, true);
    // reduce number of results to save memory
    context.prop.set(Prop.MAXHITS, gprop.num(GUIProp.MAXHITS));

    // initialize fonts and colors
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
          final IOFile io = new IOFile(file);
          boolean xq = file.endsWith(IO.BASEXMLSUFFIX);
          for(final String suf : IO.XQSUFFIXES) xq |= file.endsWith(suf);
          if(xq) {
            gui.editor.open(io);
          } else if(!xml) {
            // only parse first xml file
            gprop.set(GUIProp.CREATEPATH, io.path());
            gprop.set(GUIProp.CREATENAME, io.dbname());
            DialogProgress.execute(gui, new Check(file));
            xml = true;
          }
        }
      }
    });

    // guarantee correct shutdown of database context
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public synchronized void run() {
        context.close();
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
