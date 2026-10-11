package org.basex.gui;

import static org.basex.core.Text.*;

import java.awt.*;
import java.util.function.Supplier;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.gui.dialog.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.TextEditor.*;
import org.basex.gui.view.*;
import org.basex.gui.view.editor.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.func.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;
import org.basex.util.options.Options.*;

/**
 * This enumeration encapsulates all commands that are triggered by GUI operations.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum GUIMenuCmd implements GUICommand {

  /* DATABASE MENU */

  /** Opens a dialog to create a new database. */
  C_CREATE(NEW + ELLIPSIS, "% N", Requires.NOTHING) {
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
  C_OPEN_MANAGE(OPEN_MANAGE + ELLIPSIS, "% M", Requires.NOTHING) {
    @Override
    public void execute(final GUI gui) {
      if(new DialogManage(gui).nodb() && BaseXDialog.confirm(gui, NEW_DB_QUESTION))
        C_CREATE.execute(gui);
    }
  },

  /** Shows database info. */
  C_PROPERTIES(PROPERTIES + ELLIPSIS, "% shift M", Requires.DATA) {
    @Override
    public void execute(final GUI gui) {
      new DialogProps(gui);
    }
  },

  /** Exports a database. */
  C_EXPORT(EXPORT + ELLIPSIS, null, Requires.DATA) {
    @Override
    public void execute(final GUI gui) {
      final DialogExport dialog = new DialogExport(gui);
      if(!dialog.ok()) return;

      final IOFile root = new IOFile(dialog.path());

      // check if existing files will be overwritten
      if(root.exists()) {
        IOFile file = null;
        boolean overwrite = false;
        final Data data = gui.context.data();
        final IntList docs = data.resources.docs();
        final int ds = docs.size();
        for(int d = 0; d < ds; d++) {
          file = root.resolve(Token.string(data.text(docs.get(d), true)));
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
  C_CLOSE(CLOSE, Prop.MAC ? null : "% Q", Requires.DATA) {
    @Override
    public void execute(final GUI gui) {
      gui.execute(new Close());
    }
  },

  /** Creates a new file in the editor. */
  C_EDIT_NEW(NEW, "% T", Requires.NOTHING) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.newFile();
    }
  },

  /** Opens a new file in the editor. */
  C_EDIT_OPEN(OPEN + ELLIPSIS, "% O", Requires.NOTHING) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.open();
    }
  },

  /** Reverts the current editor file. */
  C_EDIT_REVERT(REVERT + ELLIPSIS, null, Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().reopen(true);
    }

    @Override
    boolean available(final GUI gui) {
      final EditorArea ea = gui.editor.getEditor();
      return ea.opened() && ea.modified();
    }
  },

  /** Saves the current file in the editor. */
  C_EDIT_SAVE(SAVE, "% S", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.save();
    }

    @Override
    boolean available(final GUI gui) {
      final EditorArea ea = gui.editor.getEditor();
      return ea.modified() || !ea.opened();
    }
  },

  /** Saves the current editor file under a new name. */
  C_EDIT_SAVE_AS(SAVE_AS + ELLIPSIS, "% shift S", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.saveAs();
    }
  },

  /** Saves a copy of the current editor file. */
  C_EDIT_SAVE_COPY_AS(SAVE_COPY_AS + ELLIPSIS, null, Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.saveCopyAs();
    }
  },

  /** Closes the current editor file. */
  C_EDIT_CLOSE(CLOSE, "% W", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.close(null);
    }
  },

  /** Closes all editor files. */
  C_EDIT_CLOSE_ALL(CLOSE_ALL, "% shift W", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.closeAll();
    }
  },

  /** Runs the currently opened query. */
  C_GO(RUN_QUERY, "% ENTER", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.run();
    }
  },

  /** Stops the currently opened query. */
  C_STOP(STOP, "shift ESCAPE", Requires.NOTHING) {
    @Override
    public void execute(final GUI gui) {
      gui.stop();
    }
  },

  /** Edits external variables. */
  C_EXTERNAL_VARIABLES(EXTERNAL_VARIABLES + ELLIPSIS, "% shift E", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      new DialogBindings(gui);
    }
  },

  /** Indent result. */
  C_INDENT_RESULT(INDENT_RESULT, "% shift I", Requires.NOTHING, GUIOptions.INDENTRESULT) {
    @Override
    void toggled(final GUI gui, final boolean on) {
      final SerializerOptions sopts = gui.context.options.get(MainOptions.SERIALIZER);
      sopts.put(SerializerOptions.INDENT, on ? YesNo.YES : YesNo.NO);
      gui.layoutViews();
    }
  },

  /** Jumps to the next error. */
  C_NEXT_ERROR(NEXT_ERROR, "% PERIOD", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.markError(true);
    }
  },

  /** Wraps long lines. */
  C_WORD_WRAP(WORD_WRAP, null, Requires.VISIBLE_EDITOR, GUIOptions.WORDWRAP) {
    @Override
    void toggled(final GUI gui, final boolean on) {
      gui.notify.layout();
    }
  },

  /** Adds or removes a comment. */
  C_COMMENT(COMMENT, "% K", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().comment();
    }
  },

  /** Formats text in the editor. */
  C_FORMAT(FORMAT, "% shift F", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().format();
    }
  },

  /** Sorts text. */
  C_SORT(SORT + ELLIPSIS, "% U", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().sort();
    }
  },

  /** Lower case. */
  C_LOWER_CASE(LOWER_CASE, "% shift L", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().toCase(Case.LOWER);
    }
  },

  /** Upper case. */
  C_UPPER_CASE(UPPER_CASE, "% shift U", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().toCase(Case.UPPER);
    }
  },

  /** Title case. */
  C_TITLE_CASE(TITLE_CASE, "% shift T", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().toCase(Case.TITLE);
    }
  },

  /** Jump to matching bracket. */
  C_JUMP_TO_BRACKET(JUMP_TO_BRACKET, "% shift B", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().bracket();
    }
  },

  /** Undoes the last modification. */
  C_UNDO(UNDO, () -> BaseXKeys.UNDOSTEP) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().history(true);
    }

    @Override
    boolean available(final GUI gui) {
      return gui.editor.getEditor().hasHistory(true);
    }
  },

  /** Redoes the last modification. */
  C_REDO(REDO, () -> BaseXKeys.REDOSTEP) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().history(false);
    }

    @Override
    boolean available(final GUI gui) {
      return gui.editor.getEditor().hasHistory(false);
    }
  },

  /** Opens the search bar. */
  C_FIND(FIND + ELLIPSIS, () -> BaseXKeys.FIND) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().find();
    }

    @Override
    boolean available(final GUI gui) {
      return gui.editor.getEditor().searchable();
    }
  },

  /** Jumps to the next search hit. */
  C_FIND_NEXT(FIND_NEXT, () -> BaseXKeys.FINDNEXT) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().search(true);
    }

    @Override
    boolean available(final GUI gui) {
      return gui.editor.getEditor().searchable();
    }
  },

  /** Jumps to the previous search hit. */
  C_FIND_PREVIOUS(FIND_PREVIOUS, () -> BaseXKeys.FINDPREV) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().search(false);
    }

    @Override
    boolean available(final GUI gui) {
      return gui.editor.getEditor().searchable();
    }
  },

  /** Jumps to a specific line. */
  C_GO_TO_LINE(GO_TO_LINE + ELLIPSIS, "% L", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().gotoLine();
    }

    @Override
    boolean available(final GUI gui) {
      return gui.editor.getEditor().searchable();
    }
  },

  /** Jumps to a declaration. */
  C_DECLARATIONS(DECLARATIONS + ELLIPSIS, "% shift O", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.getEditor().gotoDeclaration();
    }

    @Override
    boolean available(final GUI gui) {
      return gui.editor.getEditor().hasDeclarations();
    }
  },

  /** Exits the application. */
  C_EXIT(EXIT, null, Requires.NOTHING) {
    @Override
    public void execute(final GUI gui) {
      gui.quit();
    }
  },

  /* EDIT COMMANDS */

  /** Copies the current database path to the clipboard. */
  C_COPY_PATH(COPY_PATH, "% shift C", Requires.DATA) {
    @Override
    public void execute(final GUI gui) {
      final int pre = gui.context.marked.pre(0);
      BaseXLayout.toClipboard(Token.string(ViewData.path(gui.context.data(), pre)));
    }

    @Override
    boolean available(final GUI gui) {
      // disallow copy of empty node set or root node
      return updatable(gui.context.marked);
    }
  },

  /** Copies the currently marked nodes. */
  C_COPY_NODES(COPY, null, Requires.DATA) {
    @Override
    public void execute(final GUI gui) {
      final Context ctx = gui.context;
      final DBNodes n = ctx.marked;
      ctx.copied = new DBNodes(n.data(), n.pres());
    }

    @Override
    boolean available(final GUI gui) {
      // disallow copy of empty node set or root node
      return updatable(gui.context.marked);
    }
  },

  /** Pastes the copied nodes. */
  C_PASTE_NODES(PASTE, null, Requires.DATA) {
    @Override
    public void execute(final GUI gui) {
      final StringBuilder sb = new StringBuilder();
      final DBNodes n = gui.context.copied;
      final long ns = n.size();
      for(int i = 0; i < ns; i++) {
        if(i > 0) sb.append(',');
        sb.append(openPre(n, i));
      }
      gui.context.copied = null;
      gui.execute(new XQuery("insert nodes (" + sb + ") into " +
        openPre(gui.context.marked, 0)));
    }

    @Override
    boolean available(final GUI gui) {
      final Context ctx = gui.context;
      // disallow copy of empty node set or root node
      return updatable(ctx.marked, Data.DOC) && ctx.copied != null;
    }
  },

  /** Deletes the currently marked nodes. */
  C_DELETE_NODES(DELETE + ELLIPSIS, null, Requires.DATA) {
    @Override
    public void execute(final GUI gui) {
      if(!BaseXDialog.confirm(gui, DELETE_NODES)) return;
      final StringBuilder sb = new StringBuilder();
      final DBNodes n = gui.context.marked;
      final long ns = n.size();
      for(int i = 0; i < ns; i++) {
        if(i > 0) sb.append(',');
        sb.append(openPre(n, i));
      }
      gui.context.marked = new DBNodes(n.data());
      gui.context.copied = null;
      gui.context.focused = -1;
      gui.execute(new XQuery("delete nodes (" + sb + ')'));
    }

    @Override
    boolean available(final GUI gui) {
      return updatable(gui.context.marked, Data.DOC);
    }
  },

  /** Inserts new nodes. */
  C_NEW_NODES(NEW + ELLIPSIS, null, Requires.DATA) {
    @Override
    public void execute(final GUI gui) {
      final DBNodes n = gui.context.marked;
      final DialogInsert insert = new DialogInsert(gui);
      if(!insert.ok()) return;

      final StringList sl = insert.result;
      final Kind kind = XNode.type(insert.kind).kind();
      final TokenBuilder item = new TokenBuilder();
      item.add(kind.description()).add(" { ").add(quote(sl.get(0))).add(" }");

      if(kind.oneOf(Kind.ATTRIBUTE, Kind.PROCESSING_INSTRUCTION)) {
        item.add(" { ").add(quote(sl.get(1))).add(" }");
      } else if(kind == Kind.ELEMENT) {
        item.add(" { () }");
      }

      gui.context.copied = null;
      gui.execute(new XQuery("insert node " + item.add(" into ").add(openPre(n, 0)).toString()));
    }

    @Override
    boolean available(final GUI gui) {
      return updatable(gui.context.marked, Data.ATTR, Data.PI, Data.COMM, Data.TEXT);
    }
  },

  /** Opens a dialog to edit the currently marked nodes. */
  C_EDIT_NODES(EDIT + ELLIPSIS, null, Requires.DATA) {
    @Override
    public void execute(final GUI gui) {
      final DBNodes n = gui.context.marked;
      final DialogEdit edit = new DialogEdit(gui, n.pre(0));
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
    boolean available(final GUI gui) {
      return updatable(gui.context.marked, Data.DOC);
    }
  },

  /** Filters the currently marked nodes. */
  C_FILTER_NODES(FILTER_SELECTED, null, Requires.DATA) {
    @Override
    public void execute(final GUI gui) {
      final Context ctx = gui.context;
      DBNodes marked = ctx.marked;
      if(marked.isEmpty()) {
        final int pre = gui.context.focused;
        if(pre == -1) return;
        marked = new DBNodes(ctx.data(), pre);
      }
      gui.notify.context(marked, false, null);
    }

    @Override
    boolean available(final GUI gui) {
      return updatable(gui.context.marked);
    }
  },

  /** Shows the editor view. */
  C_SHOW_EDITOR(EDITOR, "% E", Requires.NOTHING, GUIOptions.SHOWEDITOR),

  /** Jumps to the currently edited file. */
  C_JUMP_TO_FILE(JUMP_TO_FILE, "% J", Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.showProject();
      gui.editor.jumpToFile();
    }
  },

  /** Finds files. */
  C_FIND_CONTENTS(FIND_CONTENTS + ELLIPSIS, Prop.MAC ? "% shift H" : "% H",
      Requires.VISIBLE_EDITOR) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.showProject();
      gui.editor.findFiles();
    }
  },

  /** Shows the XQuery project structure. */
  C_SHOW_PROJECT(PROJECT, "% P", Requires.VISIBLE_EDITOR, GUIOptions.SHOWPROJECT) {
    @Override
    void toggled(final GUI gui, final boolean on) {
      gui.editor.toggleProject();
    }
  },

  /** Shows info. */
  C_SHOW_INFO(INFO, "% I", Requires.NOTHING, GUIOptions.SHOWINFO),

  /** Repository manager. */
  C_PACKAGES(PACKAGES + ELLIPSIS, null, Requires.NOTHING) {
    @Override
    public void execute(final GUI gui) {
      new DialogPackages(gui);
    }
  },

  /* VIEW MENU */

  /** Shows the buttons. */
  C_SHOW_BUTTONS(BUTTONS, null, Requires.NOTHING, GUIOptions.SHOWBUTTONS) {
    @Override
    void toggled(final GUI gui, final boolean on) {
      gui.updateControl(gui.buttons, on, BorderLayout.CENTER);
    }
  },

  /** Show Input Field. */
  C_SHOW_INPUT_BAR(INPUT_BAR, null, Requires.NOTHING, GUIOptions.SHOWINPUT) {
    @Override
    void toggled(final GUI gui, final boolean on) {
      gui.updateControl(gui.nav, on, BorderLayout.SOUTH);
    }
  },

  /** Shows the text view. */
  C_SHOW_RESULT(RESULT, "% R", Requires.NOTHING, GUIOptions.SHOWTEXT),

  /** Shows the map. */
  C_SHOW_MAP(MAP, "% 1", Requires.DATA, GUIOptions.SHOWMAP),

  /** Shows the tree view. */
  C_SHOW_TREE(TREE, "% 2", Requires.DATA, GUIOptions.SHOWTREE),

  /** Shows the folder view. */
  C_SHOW_FOLDER(FOLDER, "% 3", Requires.DATA, GUIOptions.SHOWFOLDER),

  /** Shows the plot view. */
  C_SHOW_PLOT(PLOT, "% 4", Requires.DATA, GUIOptions.SHOWPLOT),

  /** Shows the table view. */
  C_SHOW_TABLE(TABLE, "% 5", Requires.DATA, GUIOptions.SHOWTABLE),

  /** Shows the explorer view. */
  C_SHOW_EXPLORE(EXPLORER, "% 6", Requires.DATA, GUIOptions.SHOWEXPLORE),

  /** Shows used memory. */
  C_SHOW_MEM(USED_MEM + ELLIPSIS, null, Requires.NOTHING) {
    @Override
    public void execute(final GUI gui) {
      DialogMem.show(gui);
    }
  },

  /* OPTION MENU */

  /** Real-time execution on/off. */
  C_RT_EXECUTION(RT_EXECUCTION, null, Requires.NOTHING, GUIOptions.EXECRT) {
    @Override
    void toggled(final GUI gui, final boolean on) {
      gui.stop();
    }
  },

  /** Real-time filtering on/off. */
  C_RT_FILTERING(RT_FILTERING, null, Requires.DATA, GUIOptions.FILTERRT) {
    @Override
    void toggled(final GUI gui, final boolean on) {
      gui.stop();

      final Context ctx = gui.context;
      final boolean root = ctx.root();
      final Data data = ctx.data();
      if(on) {
        if(root) {
          gui.notify.mark(new DBNodes(data), null);
        } else {
          final DBNodes mark = ctx.marked;
          ctx.marked = new DBNodes(data);
          gui.notify.context(mark, true, null);
        }
      } else if(!root) {
        gui.notify.context(new DBNodes(data, 0), true, null);
        gui.notify.mark(ctx.current(), null);
      }
    }
  },

  /** Shows a preference dialog. */
  C_PREFERENCES(PREFERENCES + ELLIPSIS, Prop.MAC ? null : "% shift P", Requires.NOTHING) {
    @Override
    public void execute(final GUI gui) {
      DialogPrefs.show(gui);
    }
  },

  /* HELP MENU */

  /** Shows the documentation web page. */
  C_HELP(HELP, "F1", Requires.NOTHING) {
    @Override
    public void execute(final GUI gui) {
      BaseXDialog.browse(gui, DOCS_URL);
    }
  },

  /** Opens the community web page. */
  C_COMMUNITY(COMMUNITY, null, Requires.NOTHING) {
    @Override
    public void execute(final GUI gui) {
      BaseXDialog.browse(gui, COMMUNITY_URL);
    }
  },

  /** Opens the update web page. */
  C_CHECK_FOR_UPDATES(CHECK_FOR_UPDATES, null, Requires.NOTHING) {
    @Override
    public void execute(final GUI gui) {
      gui.checkVersion(true);
    }
  },

  /** Shows the "about" information. */
  C_ABOUT(ABOUT + ELLIPSIS, null, Requires.NOTHING) {
    @Override
    public void execute(final GUI gui) {
      DialogAbout.show(gui);
    }
  },

  /* BROWSE COMMANDS */

  /** Goes one step back. */
  C_GO_BACK(GO_BACK, Prop.MAC ? null : "alt LEFT", Requires.DATA) {
    @Override
    public void execute(final GUI gui) {
      gui.notify.hist(false);
    }

    @Override
    boolean available(final GUI gui) {
      return gui.notify.query(true) != null;
    }
  },

  /** Goes one step forward. */
  C_GO_FORWARD(GO_FORWARD, Prop.MAC ? null : "alt RIGHT", Requires.DATA) {
    @Override
    public void execute(final GUI gui) {
      gui.notify.hist(true);
    }

    @Override
    boolean available(final GUI gui) {
      return gui.notify.query(false) != null;
    }
  },

  /** Goes one level up. */
  C_GO_UP(GO_UP, null, Requires.DATA) {
    @Override
    public void execute(final GUI gui) {
      // skip operation for root context
      final Context ctx = gui.context;
      if(ctx.root()) return;
      // check if all nodes are document nodes
      boolean doc = true;
      final Data data = ctx.data();
      for(final int pre : ctx.current().pres()) doc &= data.kind(pre) == Data.DOC;
      final DBNodes nodes;
      if(doc) {
        // if yes, jump to database root
        ctx.invalidate();
        nodes = ctx.current();
      } else {
        // otherwise, jump to parent nodes
        final IntList pres = new IntList();
        for(final int pre : ctx.current().pres()) {
          final int k = data.kind(pre);
          pres.add(k == Data.DOC ? pre : data.parent(pre, k));
        }
        nodes = new DBNodes(data, pres.ddo().finish());
        ctx.current(nodes);
      }
      gui.notify.context(nodes, false, null);
    }

    @Override
    boolean available(final GUI gui) {
      return !gui.context.root();
    }
  },

  /** Goes to the root node. */
  C_GO_HOME(GO_HOME, Prop.MAC ? null : "alt HOME", Requires.DATA) {
    @Override
    public void execute(final GUI gui) {
      // jump to database root
      final Context ctx = gui.context;
      if(!ctx.root()) {
        ctx.invalidate();
        gui.notify.context(ctx.current(), false, null);
      }
      // highlight root nodes in all views
      final Data data = ctx.data();
      gui.notify.mark(new DBNodes(data, true, data.resources.docs().toArray()), null);
    }
  };

  /** Menu label. */
  private final String label;
  /** Key shortcut (can be {@code null}). */
  private final Object key;
  /** Requirement for enabling the command. */
  private final Requires requires;
  /** Option that is toggled by the command (can be {@code null}). */
  private final BooleanOption option;
  /** Shortcut (can be {@code null}). */
  private final String shortcut;
  /** Supplier of a shortcut, resolved after class initialization (can be {@code null}). */
  private final Supplier<BaseXKeys> textKey;

  /**
   * Constructor.
   * @param label    label of the menu item
   * @param key      shortcut (can be {@code null})
   * @param requires requirement for enabling the command
   */
  GUIMenuCmd(final String label, final String key, final Requires requires) {
    this(label, key, requires, null);
  }

  /**
   * Constructor.
   * @param label    label of the menu item
   * @param key      shortcut (can be {@code null})
   * @param requires requirement for enabling the command
   * @param option   option that is toggled by the command (can be {@code null})
   */
  GUIMenuCmd(final String label, final String key, final Requires requires,
      final BooleanOption option) {
    this.label = label;
    this.key = key;
    this.requires = requires;
    this.option = option;
    shortcut = BaseXLayout.addShortcut(label, key);
    textKey = null;
  }

  /**
   * Constructor for commands that share their shortcut with the text panel.
   * @param label label of the menu item
   * @param key   supplier of the shortcut
   */
  GUIMenuCmd(final String label, final Supplier<BaseXKeys> key) {
    this.label = label;
    textKey = key;
    this.key = null;
    requires = Requires.VISIBLE_EDITOR;
    option = null;
    shortcut = null;
  }

  /**
   * Inverts the option of a toggle command.
   * @param gui reference to the main window
   */
  @Override
  public void execute(final GUI gui) {
    toggled(gui, gui.gopts.invert(option));
  }

  /**
   * Called after the option of a toggle command has been inverted.
   * @param gui reference to the main window
   * @param on new option value
   */
  void toggled(final GUI gui, @SuppressWarnings("unused") final boolean on) {
    gui.layoutViews();
  }

  @Override
  public final boolean enabled(final GUI gui) {
    return switch(requires) {
      case DATA -> gui.context.data() != null;
      case VISIBLE_EDITOR -> gui.gopts.get(GUIOptions.SHOWEDITOR) && gui.editor.getEditor() != null;
      case NOTHING -> true;
    } && available(gui);
  }

  /**
   * Checks if the command is available once its requirement is met.
   * @param gui reference to the main window
   * @return result of check
   */
  boolean available(@SuppressWarnings("unused") final GUI gui) {
    return true;
  }

  @Override
  public final boolean selected(final GUI gui) {
    return option != null && gui.gopts.get(option);
  }

  @Override
  public final String label() { return label; }

  @Override
  public final boolean toggle() { return option != null; }

  @Override
  public final String shortCut() {
    return textKey == null ? shortcut : BaseXLayout.addShortcut(label, textKey.get());
  }

  @Override
  public final Object shortcuts() {
    return textKey == null ? key : new BaseXKeys[] { textKey.get() };
  }

  // STATIC METHODS ===============================================================================

  /**
   * Checks if data can be updated.
   * @param node node instance (can be {@code null})
   * @param kinds disallowed node kinds
   * @return result of check
   */
  private static boolean updatable(final DBNodes node, final int... kinds) {
    if(node == null || (kinds.length == 0 ? node.isEmpty() : node.size() != 1)) return false;
    final int k = node.data().kind(node.pre(0));
    for(final int kind : kinds) {
      if(k == kind) return false;
    }
    return true;
  }

  /**
   * Returns a quoted string.
   * @param string string to encode
   * @return quoted string
   */
  private static String quote(final String string) {
    return '"' + string.replace("\"", "&quot;") + '"';
  }

  /**
   * Returns a database function for the specified node in a node set.
   * @param nodes node set
   * @param index node index
   * @return function string
   */
  private static String openPre(final DBNodes nodes, final int index) {
    return Function._DB_GET_PRE.args(nodes.data().meta.name, nodes.pre(index)).trim();
  }

  /** Requirements for enabling a command. */
  private enum Requires {
    /** No requirement. */ NOTHING,
    /** Opened database. */ DATA,
    /** Visible editor. */ VISIBLE_EDITOR
  }
}
