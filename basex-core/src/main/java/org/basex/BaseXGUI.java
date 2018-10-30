package org.basex;

import static org.basex.core.Text.*;

import java.awt.*;
import java.util.*;

import javax.swing.*;

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
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class BaseXGUI extends Main {
  /** Database context. */
  private final Context context = new Context();
  /** Files, specified as arguments. */
  private final StringList files = new StringList(0);

  /**
   * Main method.
   * @param args text files to open: XML documents, queries, etc.
   */
  public static void main(final String... args) {
    try {
      /* Moves main menu bar from application window into default menu at the top of the screen. */
      if (Prop.MAC) System.setProperty("apple.laf.useScreenMenuBar", "true");
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

    // read options
    final GUIOptions gopts = new GUIOptions();
    // initialize look and feel
    init(gopts);
    // initialize fonts and colors
    GUIConstants.init(gopts);

    SwingUtilities.invokeLater(() -> {
      // open main window
      final GUI gui = new GUI(context, gopts);

      // open specified file
      final ArrayList<IOFile> xqfiles = new ArrayList<>();
      for(final String file : files.finish()) {
        if(file.contains(IO.BASEXSUFFIX)) continue;

        final IOFile io = new IOFile(file);
        final boolean xml = file.endsWith(IO.XMLSUFFIX);
        if(xml && BaseXDialog.confirm(gui, Util.info(CREATE_DB_FILE, io))) {
          gopts.setFile(GUIOptions.INPUTPATH, io);
          gopts.set(GUIOptions.DBNAME, io.dbName());
          DialogProgress.execute(gui, new Check(file));
        } else {
          xqfiles.add(io);
        }
      }
      gui.editor.init(xqfiles);
    });
    // guarantee correct shutdown of database context
    Runtime.getRuntime().addShutdownHook(new Thread(context::close));
  }

  /**
   * Initializes the GUI.
   * @param opts gui options
   */
  private static void init(final GUIOptions opts) {
    try {
      // refresh views when windows are resized
      Toolkit.getDefaultToolkit().setDynamicLayout(true);
      // set look and feel
      final String laf = opts.get(GUIOptions.LOOKANDFEEL);
      UIManager.setLookAndFeel(laf.isEmpty() ? UIManager.getSystemLookAndFeelClassName() : laf);
    } catch(final Exception ex) {
      Util.stack(ex);
    }
  }

  @Override
  protected void parseArgs() throws BaseXException {
    final MainParser arg = new MainParser(this);
    while(arg.more()) {
      if(arg.dash()) {
        final char c = arg.next();
        if(c == 'd') {
          // activate debug mode
          Prop.debug = true;
        } else {
          throw arg.usage();
        }
      } else {
        files.add(arg.string());
      }
    }
  }

  @Override
  public String header() {
    return Util.info(S_CONSOLE_X, S_GUI);
  }

  @Override
  public String usage() {
    return S_GUIINFO;
  }
}
