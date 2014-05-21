package org.basex;

import static org.basex.core.Text.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.gui.*;
import org.basex.gui.dialog.*;
import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This is the starter class for the graphical frontend.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BaseXGUI extends Main {
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
    super(args);
    parseArgs();

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
    context.options.set(MainOptions.CACHEQUERY, true);
    // reduce number of results to save memory
    context.options.set(MainOptions.MAXHITS, gopts.get(GUIOptions.MAXHITS));

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
        for(final String file : files) {
          if(file.matches("^.*\\" + IO.BASEXSUFFIX + "[^.]*$")) continue;

          final IOFile io = new IOFile(file);
          final boolean xml = file.endsWith(IO.XMLSUFFIX);
          if(xml && BaseXDialog.confirm(gui, Util.info(Text.CREATE_DB_FILE, io))) {
            gopts.set(GUIOptions.INPUTPATH, io.path());
            gopts.set(GUIOptions.DBNAME, io.dbname());
            DialogProgress.execute(gui, new Check(file));
          } else {
            gui.editor.open(io);
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
  private static void init(final GUIOptions opts) {
    // added to handle possible JDK 1.6 bug (thanks to Makoto Yui)
    final LookAndFeelInfo[] lafis = UIManager.getInstalledLookAndFeels();

    final String laf = opts.get(GUIOptions.LOOKANDFEEL);
    try {
      // refresh views when windows are resized
      Toolkit.getDefaultToolkit().setDynamicLayout(true);
      // set specified look & feel
      if(laf.equals("Metal")) {
        // use non-bold fonts in Java's look & feel
        final UIDefaults def = UIManager.getDefaults();
        for(final Object k : def.keySet()) {
          final Object v = def.get(k);
          if(v instanceof Font) def.put(k, ((Font) v).deriveFont(Font.PLAIN));
        }
      } else if(laf.isEmpty()) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } else {
        for(final LookAndFeelInfo lafi : lafis) {
          if(lafi.getName().equals(laf)) {
            UIManager.setLookAndFeel(lafi.getClassName());
            break;
          }
        }
      }
    } catch(final Exception ex) {
      Util.stack(ex);
    }
  }

  @Override
  protected void parseArgs() throws BaseXException {
    final MainParser arg = new MainParser(this);
    while(arg.more()) {
      if(arg.dash()) throw arg.usage();
      files.add(arg.string());
    }
  }

  @Override
  public String header() {
    return Util.info(S_CONSOLE, S_GUI);
  }

  @Override
  public String usage() {
    return S_GUIINFO;
  }
}
