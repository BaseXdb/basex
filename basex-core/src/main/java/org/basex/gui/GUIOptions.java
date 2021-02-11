package org.basex.gui;

import static org.basex.util.Prop.*;

import java.awt.*;
import java.text.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * This class contains options which are used in the GUI.
 * They are also stored in the project's home directory.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class GUIOptions extends Options {
  // DATABASE & PROGRAM PATHS =====================================================================

  /** Comment: written to options file. */
  public static final Comment C_PATHS = new Comment("Paths");

  /** Current path to database input. */
  public static final StringOption INPUTPATH = new StringOption("INPUTPATH", HOMEDIR);
  /** Current path to additional database input files. */
  public static final StringOption DATAPATH = new StringOption("DATAPATH", HOMEDIR);
  /** Current path to current working directory. */
  public static final StringOption WORKPATH = new StringOption("WORKPATH", HOMEDIR);
  /** Current path to database project. */
  public static final StringOption PROJECTPATH = new StringOption("PROJECTPATH", "");

  /** Comment: written to options file. */
  public static final Comment C_LAYOUT = new Comment("Layout");

  /** Default GUI Font. */
  public static final StringOption FONT = new StringOption("FONT", Font.SANS_SERIF);
  /** Default GUI Monospace Font. */
  public static final StringOption MONOFONT = new StringOption("MONOFONT",
      WIN ? "Consolas" : Font.MONOSPACED);
  /** Font TYPE = plain, bold, italics). */
  public static final NumberOption FONTTYPE = new NumberOption("FONTTYPE", 0);
  /** Font size. */
  public static final NumberOption FONTSIZE = new NumberOption("FONTSIZE", 15);
  /** Only display monospace fonts. */
  public static final BooleanOption ONLYMONO = new BooleanOption("ONLYMONO", false);

  /** Red GUI color factor. */
  public static final NumberOption COLORRED = new NumberOption("COLORRED", 15);
  /** Green GUI color factor. */
  public static final NumberOption COLORGREEN = new NumberOption("COLORGREEN", 11);
  /** Blue GUI color factor. */
  public static final NumberOption COLORBLUE = new NumberOption("COLORBLUE", 6);

  /** Comment: written to options file. */
  public static final Comment C_WINDOWS = new Comment("Windows");

  /** Last updated version. */
  public static final StringOption UPDATEVERSION = new StringOption("UPDATEVERSION",
    VERSION.replaceAll(" .*", ""));

  /** GUI layout. */
  public static final StringOption VIEWS = new StringOption("VIEWS", GUIConstants.VIEWS);

  /** GUI height. */
  public static final NumbersOption GUISIZE = new NumbersOption("GUISIZE", 1004, 748);
  /** GUI position. */
  public static final NumbersOption GUILOC = new NumbersOption("GUILOC", 10, 10);
  /** Flag for maximized GUI window. */
  public static final BooleanOption MAXSTATE = new BooleanOption("MAXSTATE", false);

  /** Flag for displaying buttons in the GUI window. */
  public static final BooleanOption SHOWBUTTONS = new BooleanOption("SHOWBUTTONS", true);
  /** Flag for displaying the text field in the GUI window. */
  public static final BooleanOption SHOWINPUT = new BooleanOption("SHOWINPUT", true);
  /** Flag for displaying the status bar in the GUI window. */
  public static final BooleanOption SHOWSTATUS = new BooleanOption("SHOWSTATUS", true);

  /** Flag for activated info view. */
  public static final BooleanOption SHOWINFO = new BooleanOption("SHOWINFO", true);
  /** Flag for activated map view. */
  public static final BooleanOption SHOWMAP = new BooleanOption("SHOWMAP", true);
  /** Flag for activated table view. */
  public static final BooleanOption SHOWTABLE = new BooleanOption("SHOWTABLE", false);
  /** Flag for activated result view. */
  public static final BooleanOption SHOWTEXT = new BooleanOption("SHOWTEXT", true);
  /** Flag for activated tree view. */
  public static final BooleanOption SHOWFOLDER = new BooleanOption("SHOWFOLDER", false);
  /** Flag for activated query view. */
  public static final BooleanOption SHOWEXPLORE = new BooleanOption("SHOWEXPLORE", false);
  /** Flag for activated plot view. */
  public static final BooleanOption SHOWPLOT = new BooleanOption("SHOWPLOT", false);
  /** Flag for activated xquery view. */
  public static final BooleanOption SHOWEDITOR = new BooleanOption("SHOWEDITOR", true);
  /** Flag for activated tree view. */
  public static final BooleanOption SHOWTREE = new BooleanOption("SHOWTREE", false);
  /** Flag for activated project structure. */
  public static final BooleanOption SHOWPROJECT = new BooleanOption("SHOWPROJECT", true);

  /** Preferences tab. */
  public static final NumberOption PREFTAB = new NumberOption("PREFTAB", 0);
  /** Flag for Java look and feel. */
  public static final StringOption LOOKANDFEEL = new StringOption("LOOKANDFEEL", "");
  /** Flag for dissolving name attributes. */
  public static final BooleanOption SHOWNAME = new BooleanOption("SHOWNAME", true);
  /** Flag for scrolling editor tabs. */
  public static final BooleanOption SCROLLTABS = new BooleanOption("SCROLLTABS", true);
  /** Focus follows mouse. */
  public static final BooleanOption MOUSEFOCUS = new BooleanOption("MOUSEFOCUS", false);
  /** XML suffixes. */
  public static final StringOption XMLSUFFIXES =
      new StringOption("XMLSUFFIXES", "xml,xsd,svg,rdf,rss,rng,sch,xhtml");

  /** Sort ascending. */
  public static final BooleanOption ASCSORT = new BooleanOption("ASCSORT", true);
  /** Case sensitive sorting. */
  public static final BooleanOption CASESORT = new BooleanOption("CASESORT", true);
  /** Merge duplicate lines. */
  public static final BooleanOption MERGEDUPL = new BooleanOption("MERGEDUPL", false);
  /** Column. */
  public static final NumberOption COLUMN = new NumberOption("COLUMN", 1);
  /** Unicode order. */
  public static final BooleanOption UNICODE = new BooleanOption("UNICODE", true);

  /** Show line margin. */
  public static final BooleanOption SHOWMARGIN = new BooleanOption("SHOWMARGIN", true);
  /** Line margin. */
  public static final NumberOption MARGIN = new NumberOption("MARGIN", 80);
  /** Insert tabs as spaces. */
  public static final BooleanOption TABSPACES = new BooleanOption("TABSPACES", true);
  /** Indentation. */
  public static final NumberOption INDENT = new NumberOption("INDENT", 2);
  /** Show invisible characters. */
  public static final BooleanOption SHOWINVISIBLE = new BooleanOption("SHOWINVISIBLE", true);
  /** Show newlines. */
  public static final BooleanOption SHOWNL = new BooleanOption("SHOWNL", false);
  /** Show line numbers. */
  public static final BooleanOption SHOWLINES = new BooleanOption("SHOWLINES", true);
  /** Mark current line. */
  public static final BooleanOption MARKLINE = new BooleanOption("MARKLINE", true);
  /** Save before executing file. */
  public static final BooleanOption SAVERUN = new BooleanOption("SAVERUN", false);
  /** Parse project files. */
  public static final BooleanOption PARSEPROJ = new BooleanOption("PARSEPROJ", true);
  /** Automatically add characters. */
  public static final BooleanOption AUTO = new BooleanOption("AUTO", true);
  /** Default file filter. */
  public static final StringOption FILES = new StringOption("FILES", "*.xml, *.xq*");
  /** Show hidden files. */
  public static final BooleanOption SHOWHIDDEN = new BooleanOption("SHOWHIDDEN", false);

  /** Current input mode in global text field (Search, XQuery, Command). */
  public static final NumberOption SEARCHMODE = new NumberOption("SEARCHMODE", 0);
  /** Flag for realtime context filtering. */
  public static final BooleanOption FILTERRT = new BooleanOption("FILTERRT", false);
  /** Flag for realtime query execution. */
  public static final BooleanOption EXECRT = new BooleanOption("EXECRT", false);

  /** Name of new database. */
  public static final StringOption DBNAME = new StringOption("DBNAME", "");
  /** Last insertion type. */
  public static final NumberOption LASTINSERT = new NumberOption("LASTINSERT", 1);

  /** Comment: written to options file. */
  public static final Comment C_VISUALIZATIONS = new Comment("Visualizations");

  /** Show attributes in treemap. */
  public static final BooleanOption MAPATTS = new BooleanOption("MAPATTS", false);
  /** Treemap Offsets. */
  public static final NumberOption MAPOFFSETS = new NumberOption("MAPOFFSETS", 3);
  /** Map algorithm. */
  public static final NumberOption MAPALGO = new NumberOption("MAPALGO", 0);
  /** number of children - size weight in (0;100). */
  public static final NumberOption MAPWEIGHT = new NumberOption("MAPWEIGHT", 0);

  /** Slim rectangles to text length. */
  public static final BooleanOption TREESLIMS = new BooleanOption("TREESLIM", true);
  /** Show attributes in treeview. */
  public static final BooleanOption TREEATTS = new BooleanOption("TREEATTS", false);

  /** Dot sizes in plot. */
  public static final NumberOption PLOTDOTS = new NumberOption("PLOTDOTS", 0);
  /** Logarithmic plot. */
  public static final BooleanOption PLOTXLOG = new BooleanOption("PLOTXLOG", false);
  /** Logarithmic plot. */
  public static final BooleanOption PLOTYLOG = new BooleanOption("PLOTYLOG", false);

  /** Maximum number of bytes to be cached in textual result. */
  public static final NumberOption MAXTEXT = new NumberOption("MAXTEXT", 1 << 23);
  /** Maximum number of items to be displayed. */
  public static final NumberOption MAXRESULTS = new NumberOption("MAXRESULTS", 500000);

  /** Comment: written to options file. */
  public static final Comment C_SEARCH = new Comment("Search");

  /** Last searched strings. */
  public static final StringsOption SEARCHED = new StringsOption("SEARCHED");
  /** Last replaced strings. */
  public static final StringsOption REPLACED = new StringsOption("REPLACED");

  /** Comment: written to options file. */
  public static final Comment C_HISTORY = new Comment("History");

  /** Last command inputs. */
  public static final StringsOption COMMANDS = new StringsOption("COMMANDS");
  /** Last keyword inputs. */
  public static final StringsOption SEARCH = new StringsOption("SEARCH");
  /** Last XQuery inputs. */
  public static final StringsOption XQUERY = new StringsOption("XQUERY");

  /** Comment: files. */
  public static final Comment C_FILES = new Comment("Files");

  /** History of editor files. */
  public static final StringsOption EDITOR = new StringsOption("EDITOR");
  /** Input/output paths. */
  public static final StringsOption INPUTS = new StringsOption("INPUTS");
  /** Files opened in the editor. */
  public static final StringsOption OPEN = new StringsOption("OPEN");
  /** History of project directories. */
  public static final StringsOption PROJECTS = new StringsOption("PROJECTS");
  /** History of filtered project files. */
  public static final StringsOption PROJFILES = new StringsOption("PROJFILES");
  /** History of filtered project file contents. */
  public static final StringsOption PROJCONTS = new StringsOption("PROJCONTS");

  /**
   * Constructor.
   */
  public GUIOptions() {
    super(new IOFile(HOMEDIR + IO.BASEXSUFFIX + "gui"));
    // reset realtime operations
    set(FILTERRT, false);
    set(EXECRT, false);
    gui = true;

    // normalize and clean file paths
    for(final StringOption path : new StringOption[] { WORKPATH, PROJECTPATH })
      setFile(path, new IOFile(get(path)));
    for(final StringsOption input : new StringsOption[] { EDITOR, OPEN, PROJECTS })
      setFiles(input, get(input));
  }

  /**
   * Sets the string array value of an option. Duplicates and orphaned files will be removed.
   * @param option option to be set
   * @param paths file paths to be assigned
   */
  public synchronized void setFiles(final StringsOption option, final String[] paths) {
    final StringList list = new StringList();
    for(final String path : paths) {
      final IOFile file = new IOFile(path);
      if(file.exists()) list.addUnique(file.path());
    }
    set(option, list.finish());
  }

  /**
   * Sets the string value of an option. The file path will be normalized.
   * @param option option to be set
   * @param file file to be assigned
   */
  public synchronized void setFile(final StringOption option, final IOFile file) {
    set(option, file.normalize().path());
  }

  /**
   * Returns the supported XML suffixes.
   * @return XML suffixes
   */
  public synchronized String[] xmlSuffixes() {
    final StringList list = new StringList();
    for(final String suffix : get(XMLSUFFIXES).split("\\W+")) list.add('.' + suffix);
    return list.finish();
  }

  /**
   * Returns a string representation of the number of results.
   * @param results number of results
   * @param bytes number of bytes (ignored if smaller than {@code 1})
   * @return result string
   */
  public String results(final long results, final long bytes) {
    final BiFunction<Long, Integer, String> more = (num, max) -> num >= max ? "\u2265" : "";
    final StringBuilder sb = new StringBuilder();
    final String num = new DecimalFormat("#,###,###").format(results);
    final String text = more.apply(results, get(MAXRESULTS)) + num;
    sb.append(Util.info(results == 1 ? Text.RESULT_X : Text.RESULTS_X, text));
    if(bytes > 0) {
      sb.append(", ");
      sb.append(more.apply(bytes, get(MAXTEXT))).append(Performance.format(bytes));
    }
    return sb.toString();
  }
}
