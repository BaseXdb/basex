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
  private final Context context = new Context();
  /** Files, specified as arguments. */
  private final StringList files = new StringList();
  /** Mac OS X GUI optimizations. */
  GUIMacOSX osxGUI;

  /**
   * Main method.
   * @param args text files to open: XML documents, queries, etc.
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

    // set Mac-specific properties
    if(Prop.MAC) {
      try {
        osxGUI = new GUIMacOSX();
      } catch(final Exception ex) {
        throw new BaseXException("Failed to initialize native Mac OS X interface", ex);
      }
    }

    // read options
    final GUIOptions gopts = new GUIOptions();
    // cache results to pass them on to all visualizations
    context.options.bool(MainOptions.CACHEQUERY, true);
    // reduce number of results to save memory
    context.options.number(MainOptions.MAXHITS, gopts.number(GUIOptions.MAXHITS));

    // initialize fonts and colors
    GUIConstants.init(gopts);

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        // initialize look and feel
        init(gopts);
        // open main window
        final GUI gui = new GUI(context, gopts);
        if(osxGUI != null) osxGUI.init(gui);

        // open specified document or database
        boolean xml = false;
        for(final String file : files) {
          if(file.matches("^.*\\" + IO.BASEXSUFFIX + "[^.]*$")) continue;

          final IOFile io = new IOFile(file);
          boolean xq = file.endsWith(IO.BXSSUFFIX);
          for(final String suf : IO.XQSUFFIXES) xq |= file.endsWith(suf);
          if(xq) {
            gui.editor.open(io, true);
          } else if(!xml) {
            // only parse first xml file
            gopts.string(GUIOptions.INPUTPATH, io.path());
            gopts.string(GUIOptions.DBNAME, io.dbname());
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
   * @param opts gui options
   */
  static void init(final GUIOptions opts) {
    try {
      // added to handle possible JDK 1.6 bug (thanks to Makoto Yui)
      UIManager.getInstalledLookAndFeels();
      // refresh views when windows are resized
      Toolkit.getDefaultToolkit().setDynamicLayout(true);
      // set specified look & feel
      if(opts.bool(GUIOptions.JAVALOOK)) {
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
