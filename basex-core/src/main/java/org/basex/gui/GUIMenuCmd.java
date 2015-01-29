package org.basex.gui;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.gui.dialog.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.*;
import org.basex.gui.view.editor.*;
import org.basex.io.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This enumeration encapsulates all commands that are triggered by GUI operations.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public enum GUIMenuCmd implements GUICommand {

  /* DATABASE MENU */

  /** Opens a dialog to create a new database. */
  C_CREATE(NEW + DOTS, "% N", false, false) {
    @Override
    public void execute(final GUI gui) {
      // open file chooser for XML creation
      final DialogNew dialog = new DialogNew(gui);
      if(!dialog.ok()) return;
      final String in = gui.gopts.get(GUIOptions.INPUTPATH);
      final String db = gui.gopts.get(GUIOptions.DBNAME);
      DialogProgress.execute(gui, new CreateDB(db, in.isEmpty() ? null : in));
    }
  },

  /** Opens a dialog to manage databases. */
  C_OPEN_MANAGE(OPEN_MANAGE + DOTS, "% M", false, false) {
    @Override
    public void execute(final GUI gui) {
      if(new DialogManage(gui).nodb() && BaseXDialog.confirm(gui, NEW_DB_QUESTION))
        C_CREATE.execute(gui);
    }
  },

  /** Shows database info. */
  C_INFO(PROPERTIES + DOTS, "% D", true, false) {
    @Override
    public void execute(final GUI gui) {
      new DialogProps(gui);
    }
  },

  /** Exports a database. */
  C_EXPORT(EXPORT + DOTS, null, true, false) {
    @Override
    public void execute(final GUI gui) {
      final DialogExport dialog = new DialogExport(gui);
      if(!dialog.ok()) return;

      final IOFile root = new IOFile(dialog.path());

      // check if existing files will be overwritten
      if(root.exists()) {
        IOFile file = null;
        boolean overwrite = false;
        final Data d = gui.context.data();
        final IntList il = d.resources.docs();
        final int is = il.size();
        for(int i = 0; i < is; i++) {
          file = root.resolve(Token.string(d.text(il.get(i), true)));
          if(file.exists()) {
            if(overwrite) {
              // more than one file will be overwritten; check remaining tests
              file = null;
              break;
            }
            overwrite = true;
          }
        }
        if(overwrite) {
          // show message for overwriting files or directories
          final String msg = file == null ? FILES_REPLACE_X : FILE_EXISTS_X;
          if(file == null) file = root;
          if(!BaseXDialog.confirm(gui, Util.info(msg, file))) return;
        }
      }
      DialogProgress.execute(gui, new Export(root.path()));
    }
  },

  /** Closes the database. */
  C_CLOSE(CLOSE, "% shift W", true, false) {
    @Override
    public void execute(final GUI gui) {
      gui.execute(new Close());
    }
  },

  /** Creates a new file in the editor. */
  C_EDITNEW(NEW, "% T", false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.newFile();
    }
  },

  /** Opens a new file in the editor. */
  C_EDITOPEN(OPEN + DOTS, "% O", false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.open();
    }
  },

  /** Reverts the current editor file. */
  C_EDITREOPEN(REOPEN + DOTS, null, false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().reopen(true);
    }

    @Override
    public boolean enabled(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWEDITOR);
    }
  },

  /** Saves the current file in the editor. */
  C_EDITSAVE(SAVE, "% S", false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.save();
    }

    @Override
    public boolean enabled(final GUI gui) {
      final EditorArea ea = gui.editor == null ? null : gui.editor.getEditor();
      return gui.gopts.get(GUIOptions.SHOWEDITOR) && ea != null && (ea.modified() || !ea.opened());
    }
  },

  /** Saves the current editor file under a new name. */
  C_EDITSAVEAS(SAVE_AS + DOTS, "% shift S", false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.saveAs();
    }

    @Override
    public boolean enabled(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWEDITOR);
    }
  },

  /** Closes the current editor file. */
  C_EDITCLOSE(CLOSE, "% W", false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.close(null);
    }

    @Override
    public boolean enabled(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWEDITOR);
    }
  },

  /** Jumps to the next error. */
  C_NEXTERROR(NEXT_ERROR, "% PERIOD", false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.jumpToError();
    }

    @Override
    public boolean enabled(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWEDITOR);
    }
  },

  /** Adds or removes a comment. */
  C_COMMENT(COMMENT, "% K", false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().comment();
    }

    @Override
    public boolean enabled(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWEDITOR);
    }
  },

  /** Formats text in the editor. */
  C_FORMAT(FORMAT, "% shift F", false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().format();
    }

    @Override
    public boolean enabled(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWEDITOR);
    }
  },

  /** Sorts text. */
  C_SORT(SORT, "% U", false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().sort();
    }

    @Override
    public boolean enabled(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWEDITOR);
    }
  },

  /** Exits the application. */
  C_EXIT(EXIT, "% Q", false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.dispose();
    }
  },

  /* EDIT COMMANDS */

  /** Copies the current path to the clipboard. */
  C_COPYPATH(COPY_PATH, "% shift C", true, false) {
    @Override
    public void execute(final GUI gui) {
      final int pre = gui.context.marked.pres[0];
      BaseXLayout.copy(Token.string(ViewData.path(gui.context.data(), pre)));
    }

    @Override
    public boolean enabled(final GUI gui) {
      // disallow copy of empty node set or root node
      final DBNodes marked = gui.context.marked;
      return marked != null && marked.size() != 0;
    }
  },

  /** Copies the currently marked nodes. */
  C_COPY(COPY, null, true, false) {
    @Override
    public void execute(final GUI gui) {
      final Context ctx = gui.context;
      final DBNodes n = ctx.marked;
      ctx.copied = new DBNodes(n.data, n.pres);
    }

    @Override
    public boolean enabled(final GUI gui) {
      // disallow copy of empty node set or root node
      return updatable(gui.context.marked);
    }
  },

  /** Pastes the copied nodes. */
  C_PASTE(PASTE, null, true, false) {
    @Override
    public void execute(final GUI gui) {
      final StringBuilder sb = new StringBuilder();
      final DBNodes n = gui.context.copied;
      final long ns = n.size();
      for(int i = 0; i < ns; ++i) {
        if(i > 0) sb.append(',');
        sb.append(openPre(n, i));
      }
      gui.context.copied = null;
      gui.execute(new XQuery("insert nodes (" + sb + ") into " +
        openPre(gui.context.marked, 0)));
    }

    @Override
    public boolean enabled(final GUI gui) {
      final Context ctx = gui.context;
      // disallow copy of empty node set or root node
      return updatable(ctx.marked, Data.DOC) && ctx.copied != null;
    }
  },

  /** Deletes the currently marked nodes. */
  C_DELETE(DELETE + DOTS, null, true, false) {
    @Override
    public void execute(final GUI gui) {
      if(!BaseXDialog.confirm(gui, DELETE_NODES)) return;
      final StringBuilder sb = new StringBuilder();
      final DBNodes n = gui.context.marked;
      final long ns = n.size();
      for(int i = 0; i < ns; ++i) {
        if(i > 0) sb.append(',');
        sb.append(openPre(n, i));
      }
      gui.context.marked = new DBNodes(n.data);
      gui.context.copied = null;
      gui.context.focused = -1;
      gui.execute(new XQuery("delete nodes (" + sb + ')'));
    }

    @Override
    public boolean enabled(final GUI gui) {
      // disallow deletion of empty node set or root node
      return updatable(gui.context.marked);
    }
  },

  /** Inserts new nodes. */
  C_INSERT(NEW + DOTS, null, true, false) {
    @Override
    public void execute(final GUI gui) {
      final DBNodes n = gui.context.marked;
      final DialogInsert insert = new DialogInsert(gui);
      if(!insert.ok()) return;

      final StringList sl = insert.result;
      final NodeType type = ANode.type(insert.kind);
      String item = Token.string(type.string()) +
          " { " + quote(sl.get(0)) + " }";

      if(type == NodeType.ATT || type == NodeType.PI) {
        item += " { " + quote(sl.get(1)) + " }";
      } else if(type == NodeType.ELM) {
        item += " { () }";
      }

      gui.context.copied = null;
      gui.execute(new XQuery("insert node " + item + " into " + openPre(n, 0)));
    }

    @Override
    public boolean enabled(final GUI gui) {
      return updatable(gui.context.marked, Data.ATTR, Data.PI, Data.COMM, Data.TEXT);
    }
  },

  /** Opens a dialog to edit the currently marked nodes. */
  C_EDIT(EDIT + DOTS, null, true, false) {
    @Override
    public void execute(final GUI gui) {
      final DBNodes n = gui.context.marked;
      final DialogEdit edit = new DialogEdit(gui, n.pres[0]);
      if(!edit.ok()) return;

      String rename = null;
      String replace = null;
      final int k = edit.kind;
      if(k == Data.ELEM || k == Data.PI || k == Data.ATTR) {
        rename = edit.result.get(0);
        if(k != Data.ELEM) replace = edit.result.get(1);
      } else {
        replace = edit.result.get(0);
      }

      if(rename != null) gui.execute(new XQuery("rename node " +
        openPre(n, 0) + " as " + quote(rename)));
      if(replace != null) gui.execute(new XQuery("replace value of node " +
        openPre(n, 0) + " with " + quote(replace)));
    }

    @Override
    public boolean enabled(final GUI gui) {
      return updatable(gui.context.marked, Data.DOC);
    }
  },

  /** Filters the currently marked nodes. */
  C_FILTER(FILTER_SELECTED, null, true, false) {
    @Override
    public void execute(final GUI gui) {
      final Context ctx = gui.context;
      DBNodes marked = ctx.marked;
      if(marked.size() == 0) {
        final int pre = gui.context.focused;
        if(pre == -1) return;
        marked = new DBNodes(ctx.data(), pre);
      }
      gui.notify.context(marked, false, null);
    }

    @Override
    public boolean enabled(final GUI gui) {
      final DBNodes marked = gui.context.marked;
      return marked != null && marked.size() != 0;
    }
  },

  /** Shows the XQuery view. */
  C_SHOWEDITOR(EDITOR, "% E", false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gopts.invert(GUIOptions.SHOWEDITOR);
      gui.layoutViews();
    }

    @Override
    public boolean selected(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWEDITOR);
    }
  },

  /** Jumps to the currently edited file. */
  C_JUMPFILE(JUMP_TO_FILE, "% J", false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.showProject();
      gui.editor.jumpToFile();
    }

    @Override
    public boolean enabled(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWEDITOR);
    }
  },

  /** Finds files. */
  C_FILESEARCH(FIND_FILES + DOTS, Prop.MAC ? "% shift H" : "% H", false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.showProject();
      gui.editor.findFiles();
    }

    @Override
    public boolean enabled(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWEDITOR);
    }
  },

  /** Shows the XQuery project structure. */
  C_SHOWPROJECT(PROJECT, "% P", false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gopts.invert(GUIOptions.SHOWPROJECT);
      gui.editor.project();
    }

    @Override
    public boolean enabled(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWEDITOR);
    }

    @Override
    public boolean selected(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWPROJECT);
    }
  },

  /** Shows info. */
  C_SHOWINFO(QUERY_INFO, "% I", false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gopts.invert(GUIOptions.SHOWINFO);
      gui.layoutViews();
    }

    @Override
    public boolean selected(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWINFO);
    }
  },

  /** Repository manager. */
  C_PACKAGES(PACKAGES + DOTS, null, false, false) {
    @Override
    public void execute(final GUI gui) {
      new DialogPackages(gui);
    }
  },

  /* VIEW MENU */

  /** Shows the buttons. */
  C_SHOWBUTTONS(BUTTONS, null, false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gopts.invert(GUIOptions.SHOWBUTTONS);
      gui.updateControl(gui.buttons, gui.gopts.get(GUIOptions.SHOWBUTTONS), BorderLayout.CENTER);
    }

    @Override
    public boolean selected(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWBUTTONS);
    }
  },

  /** Show Input Field. */
  C_SHOWINPUT(INPUT_BAR, null, false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.updateControl(gui.nav, gui.gopts.invert(GUIOptions.SHOWINPUT), BorderLayout.SOUTH);
    }

    @Override
    public boolean selected(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWINPUT);
    }
  },

  /** Shows the status bar. */
  C_SHOWSTATUS(STATUS_BAR, null, false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.updateControl(gui.status, gui.gopts.invert(GUIOptions.SHOWSTATUS), BorderLayout.SOUTH);
    }

    @Override
    public boolean selected(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWSTATUS);
    }
  },

  /** Shows the text view. */
  C_SHOWRESULT(RESULT, "% R", false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gopts.invert(GUIOptions.SHOWTEXT);
      gui.layoutViews();
    }

    @Override
    public boolean selected(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWTEXT);
    }
  },

  /** Shows the map. */
  C_SHOWMAP(MAP, "% 1", true, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gopts.invert(GUIOptions.SHOWMAP);
      gui.layoutViews();
    }

    @Override
    public boolean selected(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWMAP);
    }
  },

  /** Shows the tree view. */
  C_SHOWTREE(TREE, "% 2", true, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gopts.invert(GUIOptions.SHOWTREE);
      gui.layoutViews();
    }

    @Override
    public boolean selected(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWTREE);
    }
  },

  /** Shows the tree view. */
  C_SHOWFOLDER(FOLDER, "% 3", true, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gopts.invert(GUIOptions.SHOWFOLDER);
      gui.layoutViews();
    }

    @Override
    public boolean selected(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWFOLDER);
    }
  },

  /** Shows the plot view. */
  C_SHOWPLOT(PLOT, "% 4", true, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gopts.invert(GUIOptions.SHOWPLOT);
      gui.layoutViews();
    }

    @Override
    public boolean selected(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWPLOT);
    }
  },

  /** Shows the table view. */
  C_SHOWTABLE(TABLE, "% 5", true, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gopts.invert(GUIOptions.SHOWTABLE);
      gui.layoutViews();
    }

    @Override
    public boolean selected(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWTABLE);
    }
  },

  /** Shows the explorer view. */
  C_SHOWEXPLORE(EXPLORER, "% 6", true, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gopts.invert(GUIOptions.SHOWEXPLORE);
      gui.layoutViews();
    }

    @Override
    public boolean selected(final GUI gui) {
      return gui.gopts.get(GUIOptions.SHOWEXPLORE);
    }
  },

  /** Fullscreen mode. */
  C_FULL(FULLSCREEN, Prop.MAC ? "% shift F" : "F11", false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.fullscreen();
    }

    @Override
    public boolean selected(final GUI gui) {
      return gui.fullscreen;
    }
  },

  /* OPTION MENU */

  /** Realtime execution on/off. */
  C_RTEXEC(RT_EXECUCTION, null, false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gopts.invert(GUIOptions.EXECRT);
      gui.stop();
      // refresh buttons in input bar
      gui.refreshControls();
      // refresh editor buttons
      gui.editor.refreshMark();
    }

    @Override
    public boolean selected(final GUI gui) {
      return gui.gopts.get(GUIOptions.EXECRT);
    }
  },

  /** Realtime filtering on/off. */
  C_RTFILTER(RT_FILTERING, null, true, true) {
    @Override
    public void execute(final GUI gui) {
      final boolean rt = gui.gopts.invert(GUIOptions.FILTERRT);
      gui.stop();
      // refresh buttons in input bar
      gui.refreshControls();
      // refresh editor buttons
      gui.editor.refreshMark();

      final Context ctx = gui.context;
      final boolean root = ctx.root();
      final Data data = ctx.data();
      if(rt) {
        if(root) {
          gui.notify.mark(new DBNodes(data), null);
        } else {
          final DBNodes mark = ctx.marked;
          ctx.marked = new DBNodes(data);
          gui.notify.context(mark, true, null);
        }
      } else {
        if(!root) {
          gui.notify.context(new DBNodes(data, 0), true, null);
          gui.notify.mark(ctx.current(), null);
        }
      }
    }

    @Override
    public boolean selected(final GUI gui) {
      return gui.gopts.get(GUIOptions.FILTERRT);
    }
  },

  /** Color schema. */
  C_COLOR(COLORS + DOTS, null, false, false) {
    @Override
    public void execute(final GUI gui) {
      DialogColors.show(gui);
    }
  },

  /** Changes the fonts. */
  C_FONTS(FONTS_D, null, false, false) {
    @Override
    public void execute(final GUI gui) {
      DialogFonts.show(gui);
    }
  },

  /** Shows a preference dialog. */
  C_PREFS(PREFERENCES + DOTS, Prop.MAC ? "% COMMA" : "% shift P", false, false) {
    @Override
    public void execute(final GUI gui) {
      DialogPrefs.show(gui);
    }
  },

  /* HELP MENU */

  /** Shows the documentation web page. */
  C_HELP(HELP, "F1", false, false) {
    @Override
    public void execute(final GUI gui) {
      BaseXDialog.browse(gui, Prop.DOC_URL);
    }
  },

  /** Opens the community web page. */
  C_COMMUNITY(COMMUNITY, null, false, false) {
    @Override
    public void execute(final GUI gui) {
      BaseXDialog.browse(gui, Prop.COMMUNITY_URL);
    }
  },

  /** Opens the update web page. */
  C_UPDATES(CHECK_FOR_UPDATES, null, false, false) {
    @Override
    public void execute(final GUI gui) {
      BaseXDialog.browse(gui, Prop.UPDATE_URL);
    }
  },

  /** Shows the "about" information. */
  C_ABOUT(ABOUT + DOTS, null, false, false) {
    @Override
    public void execute(final GUI gui) {
      new DialogAbout(gui);
    }
  },

  /* BROWSE COMMANDS */

  /** Goes one step back. */
  C_GOBACK(GO_BACK, "alt LEFT", true, false) {
    @Override
    public void execute(final GUI gui) {
      gui.notify.hist(false);
    }

    @Override
    public boolean enabled(final GUI gui) {
      return gui.notify.query(true) != null;
    }
  },

  /** Goes one step forward. */
  C_GOFORWARD(GO_FORWARD, "alt RIGHT", true, false) {
    @Override
    public void execute(final GUI gui) {
      gui.notify.hist(true);
    }

    @Override
    public boolean enabled(final GUI gui) {
      return gui.notify.query(false) != null;
    }
  },

  /** Goes one level up. */
  C_GOUP(GO_UP, "alt UP", true, false) {
    @Override
    public void execute(final GUI gui) {
      // skip operation for root context
      final Context ctx = gui.context;
      if(ctx.root()) return;
      // check if all nodes are document nodes
      boolean doc = true;
      final Data data = ctx.data();
      for(final int pre : ctx.current().pres) doc &= data.kind(pre) == Data.DOC;
      final DBNodes nodes;
      if(doc) {
        // if yes, jump to database root
        ctx.invalidate();
        nodes = ctx.current();
      } else {
        // otherwise, jump to parent nodes
        final IntList pres = new IntList();
        for(final int pre : ctx.current().pres) {
          final int k = data.kind(pre);
          pres.add(k == Data.DOC ? pre : data.parent(pre, k));
        }
        nodes = new DBNodes(data, pres.sort().distinct().finish());
        ctx.current(nodes);
      }
      gui.notify.context(nodes, false, null);
    }

    @Override
    public boolean enabled(final GUI gui) {
      return !gui.gopts.get(GUIOptions.FILTERRT) &&
          gui.context.data() != null && !gui.context.root();
    }
  },

  /** Goes to the root node. */
  C_GOHOME(GO_HOME, "alt HOME", true, false) {
    @Override
    public void execute(final GUI gui) {
      // skip operation for root context
      final Context ctx = gui.context;
      if(ctx.root()) return;
      // jump to database root
      ctx.invalidate();
      gui.notify.context(ctx.current(), false, null);
    }

    @Override
    public boolean enabled(final GUI gui) {
      return gui.context.data() != null && !gui.context.root();
    }
  },

   /** Displays the root node in the text view. */
  C_HOME(GO_HOME, null, true, false) {
    @Override
    public void execute(final GUI gui) {
      gui.execute(new XQuery("/"));
    }
  };

  /** States if the command needs a data reference. */
  private final boolean data;
  /** Menu label. */
  private final String label;
  /** Key shortcut. */
  private final Object key;
  /** Indicates if this command has two states. */
  private final boolean toggle;
  /** Shortcut. */
  private final String shortcut;

  /**
   * Constructor.
   * @param l label of the menu item
   * @param k shortcut
   * @param d requires a database to be opened
   * @param t indicates if this command has two states
   */
  GUIMenuCmd(final String l, final String k, final boolean d, final boolean t) {
    label = l;
    key = k;
    data = d;
    toggle = t;
    shortcut = BaseXLayout.addShortcut(l, k);
  }

  @Override
  public boolean enabled(final GUI gui) {
    return !data || gui.context.data() != null;
  }

  @Override
  public boolean selected(final GUI gui) {
    return false;
  }

  @Override
  public final String label() { return label; }

  @Override
  public final boolean toggle() { return toggle; }

  @Override
  public String shortCut() { return shortcut; }

  @Override
  public Object shortcuts() { return key; }

  // STATIC METHODS ===========================================================

  /**
   * Checks if data can be updated.
   * @param n node instance
   * @param no disallowed node types
   * @return result of check
   */
  private static boolean updatable(final DBNodes n, final int... no) {
    if(n == null || (no.length == 0 ? n.size() < 1 : n.size() != 1)) return false;
    final int k = n.data.kind(n.pres[0]);
    for(final int i : no) if(k == i) return false;
    return true;
  }

  /**
   * Returns a quoted string.
   * @param s string to encode
   * @return quoted string
   */
  private static String quote(final String s) {
    return '"' + s.replaceAll("\"", "&quot;") + '"';
  }

  /**
   * Returns a database function for the first node in a node set.
   * @param n node set
   * @param i offset
   * @return function string
   */
  private static String openPre(final DBNodes n, final int i) {
    return Function._DB_OPEN_PRE.get(null, null, Str.get(n.data.meta.name),
        Int.get(n.pres[i])).toString();
  }
}
