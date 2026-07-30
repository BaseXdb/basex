package org.basex;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;
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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BaseXGUI extends Main {
  /** Database context. */
  private final Context context = new Context();
  /** Files, specified as arguments. */
  private final StringList files = new StringList(0);

  /**
   * Main method.
   * @param args text files to open (XML documents, queries, text files)
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

    // delegate files to a GUI instance that is already running
    final String[] paths = files.finish();
    if(GUIInstance.delegate(paths)) return;

    // initialize scaling and look and feel
    final GUIOptions gopts = new GUIOptions();
    scale(gopts);
    init(gopts);
    // adopt fonts and colors of the look and feel
    GUIConstants.init(gopts);

    // create splash screen
    final JFrame splash = splash();

    SwingUtilities.invokeLater(() -> {
      // open main window and close splash screen
      final GUI gui;
      try {
        gui = new GUI(context, gopts);
      } finally {
        splash.dispose();
      }

      // open specified files
      gui.editor.init(filter(gui, paths));

      // open files that are delegated by other GUI instances
      GUIInstance.listen(gui, paths, delegated -> SwingUtilities.invokeLater(() -> {
        // request focus first: dialogs of a background process will not receive key events
        focus(gui);
        for(final IOFile file : filter(gui, delegated)) gui.editor.open(file);
      }));
    });

    // guarantee correct shutdown of database context
    Runtime.getRuntime().addShutdownHook(new Thread(context::close));
  }

  /**
   * Returns the files to be opened in the editor and creates databases for XML documents.
   * @param gui reference to the main window
   * @param paths paths to the files
   * @return editor files
   */
  private static ArrayList<IOFile> filter(final GUI gui, final String[] paths) {
    final ArrayList<IOFile> xqfiles = new ArrayList<>();
    for(final String file : paths) {
      if(file.endsWith(IO.BASEXSUFFIX)) continue;

      final IOFile io = new IOFile(file);
      if(file.endsWith(IO.XMLSUFFIX) && BaseXDialog.confirm(gui, Util.info(CREATE_DB_FILE, io))) {
        gui.gopts.setFile(GUIOptions.INPUTPATH, io);
        gui.gopts.set(GUIOptions.DBNAME, io.dbName());
        DialogProgress.execute(gui, new Check(file));
      } else {
        xqfiles.add(io);
      }
    }
    return xqfiles;
  }

  /**
   * Moves the main window to the foreground.
   * @param gui reference to the main window
   */
  private static void focus(final GUI gui) {
    // inject key event: Windows grants the focus only to the process that received the last input
    if(Prop.WIN) {
      try {
        final Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyRelease(KeyEvent.VK_CONTROL);
      } catch(final AWTException ex) {
        Util.debug(ex);
      }
    }
    gui.setExtendedState(gui.getExtendedState() & ~Frame.ICONIFIED);
    gui.toFront();
  }

  /**
   * Assigns the scaling factor of the user interface.
   * @param opts gui options
   */
  private static void scale(final GUIOptions opts) {
    // the property must be assigned before the graphics environment is initialized
    final int scale = opts.get(GUIOptions.UISCALE);
    if(scale > 0) System.setProperty("sun.java2d.uiScale", Double.toString(scale / 100.0d));
  }

  /**
   * Creates a splash screen.
   * @return splash screen
   */
  private static JFrame splash() {
    final JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    f.setAlwaysOnTop(true);
    f.setUndecorated(true);
    f.setIconImage(BaseXImages.get("logo_small"));
    f.add(new JLabel(BaseXImages.icon("logo_large")));
    f.pack();
    f.setLocationRelativeTo(null);
    f.setVisible(true);
    return f;
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
