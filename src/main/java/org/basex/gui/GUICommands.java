package org.basex.gui;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javax.swing.AbstractButton;

import org.basex.core.Command;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.CreateIndex;
import org.basex.core.cmd.Cs;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.DropIndex;
import org.basex.core.cmd.Export;
import org.basex.core.cmd.Optimize;
import org.basex.core.cmd.XQuery;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.dialog.Dialog;
import org.basex.gui.dialog.DialogAbout;
import org.basex.gui.dialog.DialogAdd;
import org.basex.gui.dialog.DialogColors;
import org.basex.gui.dialog.DialogCreate;
import org.basex.gui.dialog.DialogEdit;
import org.basex.gui.dialog.DialogExport;
import org.basex.gui.dialog.DialogFonts;
import org.basex.gui.dialog.DialogHelp;
import org.basex.gui.dialog.DialogInfo;
import org.basex.gui.dialog.DialogInput;
import org.basex.gui.dialog.DialogInsert;
import org.basex.gui.dialog.DialogManage;
import org.basex.gui.dialog.DialogMapLayout;
import org.basex.gui.dialog.DialogPrefs;
import org.basex.gui.dialog.DialogProgress;
import org.basex.gui.dialog.DialogServer;
import org.basex.gui.dialog.DialogTreeOptions;
import org.basex.gui.view.ViewData;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.query.func.Function;
import org.basex.query.item.ANode;
import org.basex.query.item.Int;
import org.basex.query.item.NodeType;
import org.basex.query.item.Str;
import org.basex.util.Array;
import org.basex.util.Token;
import org.basex.util.Util;
import org.basex.util.list.IntList;
import org.basex.util.list.StringList;

/**
 * This enumeration encapsulates all commands that are triggered by
 * GUI operations.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public enum GUICommands implements GUICommand {

  /* DATABASE MENU */

  /** Opens a dialog to create a new database. */
  CREATE(GUICREATE + DOTS, "% N", GUICREATETT, false, false) {
    @Override
    public void execute(final GUI gui) {
      // open file chooser for XML creation
      final DialogCreate dialog = new DialogCreate(gui);
      if(!dialog.ok()) return;
      final String in = gui.gprop.get(GUIProp.CREATEPATH);
      final String db = gui.gprop.get(GUIProp.CREATENAME);
      DialogProgress.execute(dialog, PROGCREATE,
          new CreateDB(db, in.isEmpty() ? null : in));
    }
  },

  /** Opens a dialog to manage databases. */
  MANAGE(GUIMANAGE + DOTS, "% O", GUIMANAGETT, false, false) {
    @Override
    public void execute(final GUI gui) {
      if(new DialogManage(gui, true).nodb()) Dialog.warn(gui, INFONODB);
    }
  },

  /** Opens a dialog to add new documents. */
  ADD(GUIADD + DOTS, null, GUIADDTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      final DialogAdd dialog = new DialogAdd(gui);
      if(dialog.ok()) DialogProgress.execute(dialog, "", dialog.cmd());
    }
  },

  /** Opens a dialog to delete documents. */
  DROP(GUIDROP + DOTS, null, GUIDROPTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      final DialogInput d = new DialogInput("", DROPTITLE, gui, 0);
      if(d.ok()) DialogProgress.execute(d, "", new Delete(d.input()));
    }
  },

  /** Exports a database. */
  EXPORT(GUIEXPORT + DOTS, null, GUIEXPORTTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      final DialogExport dialog = new DialogExport(gui);
      if(!dialog.ok()) return;

      final IOFile root = new IOFile(dialog.path());

      // check if existing files will be overwritten
      if(root.exists()) {
        IO file = null;
        boolean overwrite = false;
        final Data d = gui.context.data();
        final IntList il = d.docs();
        for(int i = 0, is = il.size(); i < is; i++) {
          file = root.merge(Token.string(d.text(il.get(i), true)));
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
          final String msg = file == null ? DIRREPLACE : FILEREPLACE;
          if(file == null) file = root;
          if(!Dialog.confirm(gui, Util.info(msg, file))) return;
        }
      }
      gui.execute(new Export(root.path()));
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      b.setEnabled(gui.context.data() != null && !gui.context.data().empty());
    }
  },

  /** Shows database info. */
  INFO(GUIPROPS + DOTS, "% D", GUIPROPSTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      final DialogInfo info = new DialogInfo(gui);
      if(info.ok()) {
        final Data d = gui.context.data();
        final boolean[] ind = info.indexes();
        if(info.opt) {
          d.meta.textindex = ind[0];
          d.meta.attrindex = ind[1];
          d.meta.ftindex   = ind[2];
          DialogProgress.execute(info, INFOOPT, new Optimize());
        } else {
          Command[] cmd = {};
          if(ind[0] != d.meta.pathindex)
            cmd = Array.add(cmd, cmd(ind[0], CmdIndex.PATH));
          if(ind[1] != d.meta.textindex)
            cmd = Array.add(cmd, cmd(ind[1], CmdIndex.TEXT));
          if(ind[2] != d.meta.attrindex)
            cmd = Array.add(cmd, cmd(ind[2], CmdIndex.ATTRIBUTE));
          if(ind[3] != d.meta.ftindex)
            cmd = Array.add(cmd, cmd(ind[3], CmdIndex.FULLTEXT));

          DialogProgress.execute(info, PROGINDEX, cmd);
        }
      }
    }

    /**
     * Returns a command for creating/dropping the specified index.
     * @param create create flag
     * @param index name of index
     * @return command instance
     */
    private Command cmd(final boolean create, final CmdIndex index) {
      return create ? new CreateIndex(index) : new DropIndex(index);
    }
  },

  /** Closes the database. */
  CLOSE(GUICLOSE, "% W", GUICLOSETT, true, false) {
    @Override
    public void execute(final GUI gui) {
      gui.execute(new Close());
    }
  },

  /** Server Dialog. */
  SERVER(GUISERVER + DOTS, null, GUISERVERTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      // open file chooser for XML creation
      new DialogServer(gui);
    }
  },

  /** Opens a query file. */
  EDITNEW(GUIXQNEW + DOTS, "% shift N", GUIXQNEWTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.newFile();
    }
  },

  /** Opens a new editor file. */
  EDITOPEN(GUIXQOPEN + DOTS, "% R", GUIXQOPENTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.open();
    }
  },

  /** Saves the current editor file. */
  EDITSAVE(GUISAVE, "% S", GUISAVETT, false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.save();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      b.setEnabled(gui.editor != null && gui.editor.saveable());
    }
  },

  /** Saves the current editor file under a new name. */
  EDITSAVEAS(GUISAVEAS + DOTS, "% shift S", GUISAVETT, false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.saveAs();
    }
  },

  /** Closes the current editor file. */
  EDITCLOSE(GUIXQCLOSE, "% shift W", GUIXQCLOSETT, false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.editor.close(null);
    }
  },

  /** Exits the application. */
  EXIT(GUIEXIT, "% Q", GUIEXITTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.dispose();
    }
  },

  /* EDIT COMMANDS */

  /** Copies the current path. */
  COPYPATH(GUICPPATH, "% shift C", GUICPPATHTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      final int pre = gui.context.marked.list[0];
      final byte[] txt = ViewData.path(gui.context.data(), pre);
      // copy path to clipboard
      final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
      clip.setContents(new StringSelection(Token.string(txt)), null);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      // disallow copy of empty node set or root node
      final Nodes marked = gui.context.marked;
      b.setEnabled(marked != null && marked.size() != 0);
    }
  },

  /** Copies the currently marked nodes. */
  COPY(GUICOPY, "", GUICOPYTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      final Context ctx = gui.context;
      final Nodes n = ctx.marked;
      ctx.copied = new Nodes(n.list, n.data);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      // disallow copy of empty node set or root node
      b.setEnabled(updatable(gui.context.marked));
    }
  },

  /** Pastes the copied nodes. */
  PASTE(GUIPASTE, "", GUIPASTETT, true, false) {
    @Override
    public void execute(final GUI gui) {
      final StringBuilder sb = new StringBuilder();
      final Nodes n = gui.context.copied;
      for(int i = 0; i < n.size(); ++i) {
        if(i > 0) sb.append(',');
        sb.append(openPre(n, i));
      }
      gui.context.copied = null;
      gui.execute(new XQuery("insert nodes (" + sb + ") into " +
        openPre(gui.context.marked, 0)));
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      final Context ctx = gui.context;
      // disallow copy of empty node set or root node
      b.setEnabled(updatable(ctx.marked, Data.DOC) && ctx.copied != null);
    }
  },

  /** Deletes the currently marked nodes. */
  DELETE(GUIDELETE + DOTS, "", GUIDELETETT, true, false) {
    @Override
    public void execute(final GUI gui) {
      if(!Dialog.confirm(gui, DELETECONF)) return;
      final StringBuilder sb = new StringBuilder();
      final Nodes n = gui.context.marked;
      for(int i = 0; i < n.size(); ++i) {
        if(i > 0) sb.append(',');
        sb.append(openPre(n, i));
      }
      gui.context.marked = new Nodes(n.data);
      gui.context.copied = null;
      gui.context.focused = -1;
      gui.execute(new XQuery("delete nodes (" + sb + ")"));
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      // disallow deletion of empty node set or root node
      b.setEnabled(updatable(gui.context.marked));
    }
  },

  /** Inserts new nodes. */
  INSERT(GUIINSERT + DOTS, "", GUIINSERTTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      final Nodes n = gui.context.marked;
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
    public void refresh(final GUI gui, final AbstractButton b) {
      b.setEnabled(updatable(gui.context.marked,
          Data.ATTR, Data.PI, Data.COMM, Data.TEXT));
    }
  },

  /** Opens a dialog to edit the currently marked nodes. */
  EDIT(GUIEDIT + DOTS, "", GUIEDITTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      final Nodes n = gui.context.marked;
      final DialogEdit edit = new DialogEdit(gui, n.list[0]);
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
    public void refresh(final GUI gui, final AbstractButton b) {
      b.setEnabled(updatable(gui.context.marked, Data.DOC));
    }
  },

  /** Filters the currently marked nodes. */
  FILTER(GUIFILTER, "", GUIFILTERTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      final Context ctx = gui.context;
      Nodes marked = ctx.marked;
      if(marked.size() == 0) {
        final int pre = gui.context.focused;
        if(pre == -1) return;
        marked = new Nodes(pre, ctx.data());
      }
      gui.notify.context(marked, false, null);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      final Nodes marked = gui.context.marked;
      b.setEnabled(marked != null && marked.size() != 0);
    }
  },

  /** Shows the XQuery view. */
  SHOWXQUERY(GUISHOWXQUERY, "% E", GUISHOWXQUERYTT, false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gprop.invert(GUIProp.SHOWEDITOR);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.gprop.is(GUIProp.SHOWEDITOR));
    }
  },

  /** Shows info. */
  SHOWINFO(GUISHOWINFO, "% I", GUISHOWINFOTT, false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gprop.invert(GUIProp.SHOWINFO);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.gprop.is(GUIProp.SHOWINFO));
    }
  },

  /* VIEW MENU */

  /** Shows the menu. */
  SHOWMENU(GUISHOWMENU, null, GUISHOWMENUTT, false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gprop.invert(GUIProp.SHOWMENU);
      gui.updateControl(gui.menu, gui.gprop.is(GUIProp.SHOWMENU),
          BorderLayout.NORTH);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.gprop.is(GUIProp.SHOWMENU));
    }
  },

  /** Shows the buttons. */
  SHOWBUTTONS(GUISHOWBUTTONS, null, GUISHOWBUTTONSTT, false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gprop.invert(GUIProp.SHOWBUTTONS);
      gui.updateControl(gui.buttons, gui.gprop.is(GUIProp.SHOWBUTTONS),
          BorderLayout.CENTER);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.gprop.is(GUIProp.SHOWBUTTONS));
    }
  },

  /** Show Input Field. */
  SHOWINPUT(GUISHOWINPUT, null, GUISHOWINPUTTT, false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.updateControl(gui.nav, gui.gprop.invert(GUIProp.SHOWINPUT),
          BorderLayout.SOUTH);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.gprop.is(GUIProp.SHOWINPUT));
    }
  },

  /** Shows the status bar. */
  SHOWSTATUS(GUISHOWSTATUS, null, GUISHOWSTATUSTT, false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.updateControl(gui.status, gui.gprop.invert(GUIProp.SHOWSTATUS),
          BorderLayout.SOUTH);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.gprop.is(GUIProp.SHOWSTATUS));
    }
  },

  /** Shows the text view. */
  SHOWTEXT(GUISHOWTEXT, "% 1", GUISHOWTEXTTT, false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gprop.invert(GUIProp.SHOWTEXT);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.gprop.is(GUIProp.SHOWTEXT));
    }
  },

  /** Shows the map. */
  SHOWMAP(GUISHOWMAP, "% 2", GUISHOWMAPTT, true, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gprop.invert(GUIProp.SHOWMAP);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.gprop.is(GUIProp.SHOWMAP));
    }
  },

  /** Shows the tree view. */
  SHOWTREE(GUISHOWTREE, "% 3", GUISHOWTREETT, true, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gprop.invert(GUIProp.SHOWTREE);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.gprop.is(GUIProp.SHOWTREE));
    }
  },

  /** Shows the tree view. */
  SHOWFOLDER(GUISHOWFOLDER, "% 4", GUISHOWFOLDERTT, true, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gprop.invert(GUIProp.SHOWFOLDER);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.gprop.is(GUIProp.SHOWFOLDER));
    }
  },

  /** Shows the plot view. */
  SHOWPLOT(GUISHOWPLOT, "% 5", GUISHOWPLOTTT, true, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gprop.invert(GUIProp.SHOWPLOT);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.gprop.is(GUIProp.SHOWPLOT));
    }
  },

  /** Shows the table view. */
  SHOWTABLE(GUISHOWTABLE, "% 6", GUISHOWTABLETT, true, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gprop.invert(GUIProp.SHOWTABLE);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.gprop.is(GUIProp.SHOWTABLE));
    }
  },

  /** Shows the explorer view. */
  SHOWEXPLORE(GUISHOWEXPLORE, "% 7", GUISHOWEXPLORETT, true, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gprop.invert(GUIProp.SHOWEXPLORE);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.gprop.is(GUIProp.SHOWEXPLORE));
    }
  },

  /** Fullscreen mode. */
  FULL(GUIFULL, Prop.MAC ? "% shift F" : "F11", GUIFULLTT, false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.fullscreen();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.fullscreen);
    }
  },

  /* OPTION MENU */

  /** Realtime execution on/off. */
  RTEXEC(GUIRTEXEC, null, GUIRTEXECTT, false, true) {
    @Override
    public void execute(final GUI gui) {
      gui.gprop.invert(GUIProp.EXECRT);
      gui.refreshControls();
      gui.notify.layout();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.gprop.is(GUIProp.EXECRT));
    }
  },

  /** Realtime filtering on/off. */
  RTFILTER(GUIRTFILTER, null, GUIRTFILTERTT, true, true) {
    @Override
    public void execute(final GUI gui) {
      final boolean rt = gui.gprop.invert(GUIProp.FILTERRT);
      gui.refreshControls();
      gui.notify.layout();

      final Context ctx = gui.context;
      final boolean root = ctx.root();
      if(!rt) {
        if(!root) {
          gui.notify.context(new Nodes(0, ctx.data()), true, null);
          gui.notify.mark(ctx.current(), null);
        }
      } else {
        if(root) {
          gui.notify.mark(new Nodes(ctx.data()), null);
        } else {
          final Nodes mark = ctx.marked;
          ctx.marked = new Nodes(ctx.data());
          gui.notify.context(mark, true, null);
        }
      }
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.gprop.is(GUIProp.FILTERRT));
    }
  },

  /** Color schema. */
  COLOR(GUICOLOR + DOTS, null, GUICOLORTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      new DialogColors(gui);
    }
  },

  /** Changes the fonts. */
  FONTS(GUIFONTS, null, GUIFONTSTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      new DialogFonts(gui);
    }
  },

  /** Map layout. */
  MAPLAYOUT(GUIMAPLAYOUT, null, GUIMAPLAYOUTTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      new DialogMapLayout(gui);
    }
  },

  /** TreeView options. */
  TREEOPTIONS(GUITREEOPTIONS, null, GUITREEOPTIONSTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      new DialogTreeOptions(gui);
    }
  },

  /** Shows a preference dialog. */
  PREFS(GUIPREFS + DOTS, Prop.MAC ? "% COMMA" : "% P", GUIPREFSTT, false,
      false) {
    @Override
    public void execute(final GUI gui) {
      new DialogPrefs(gui);
    }
  },

  /* HELP MENU */

  /** Shows the help window. */
  SHOWHELP(GUISHOWHELP, "F1", GUISHOWHELPTT, false, true) {
    @Override
    public void execute(final GUI gui) {
      if(!gui.gprop.is(GUIProp.SHOWHELP)) {
        gui.gprop.set(GUIProp.SHOWHELP, true);
        gui.help = new DialogHelp(gui);
        gui.refreshControls();
      } else {
        gui.help.close();
      }
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      b.setSelected(gui.gprop.is(GUIProp.SHOWHELP));
    }
  },

  /** Opens the community web page. */
  SHOWCOMMUNITY(GUISHOWCOMMUNITY, null, GUISHOWCOMMUNITYTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      Dialog.browse(gui, COMMUNITY_URL);
    }
  },

  /** Opens the community web page. */
  SHOWDOC(GUISHOWDOC, null, GUISHOWDOCTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      Dialog.browse(gui, DOC_URL);
    }
  },

  /** Opens the update web page. */
  SHOWUPDATES(GUISHOWUPDATES, null, GUISHOWUPDATESTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      Dialog.browse(gui, UPDATE_URL);
    }
  },

  /** Shows the "about" information. */
  ABOUT(GUIABOUT + DOTS, null, GUIABOUTTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      new DialogAbout(gui);
    }
  },

  /* BROWSE COMMANDS */

  /** Goes one step back. */
  GOBACK(GUIGOBACK, "alt LEFT", GUIGOBACK, true, false) {
    @Override
    public void execute(final GUI gui) {
      gui.notify.hist(false);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      final String tt = gui.notify.tooltip(true);
      b.setEnabled(tt != null);
      b.setToolTipText(tt != null && tt.isEmpty() ? GUIGOBACK : tt);
    }
  },

  /** Goes one step forward. */
  GOFORWARD(GUIGOFORWARD, "alt RIGHT", GUIGOFORWARD, true, false) {
    @Override
    public void execute(final GUI gui) {
      gui.notify.hist(true);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      final String tt = gui.notify.tooltip(false);
      b.setEnabled(tt != null);
      b.setToolTipText(tt != null && tt.isEmpty() ? GUIGOFORWARD : tt);
    }
  },

  /** Goes one level up. */
  GOUP(GUIGOUP, "alt UP", GUIGOUPTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      // skip operation for root context
      final Context ctx = gui.context;
      if(ctx.root()) return;
      // check if all nodes are document nodes
      boolean doc = true;
      final Data data = ctx.data();
      for(final int pre : ctx.current().list) doc &= data.kind(pre) == Data.DOC;
      if(doc) {
        // if yes, jump to database root
        ctx.update();
        gui.notify.context(ctx.current(), false, null);
      } else {
        // otherwise, jump to parent nodes
        gui.execute(new Cs(".."));
      }
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      b.setEnabled(!gui.gprop.is(GUIProp.FILTERRT) &&
          gui.context.data() != null && !gui.context.root());
    }
  },

  /** Goes to the root node. */
  GOHOME(GUIROOT, "alt HOME", GUIROOTTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      // skip operation for root context
      final Context ctx = gui.context;
      if(ctx.root()) return;
      // jump to database root
      ctx.update();
      gui.notify.context(ctx.current(), false, null);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      b.setEnabled(gui.context.data() != null && !gui.context.root());
    }
  },

  /** Displays the root node in the text view. */
  HOME(GUIROOT, null, GUIROOTTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      gui.execute(new XQuery("/"));
    }
  };

  /** States if the command needs a data reference. */
  private final boolean data;
  /** Menu entry. */
  private final String entry;
  /** Key shortcut. */
  private final String key;
  /** Help string. */
  private final String help;
  /** Flag for commands that can be (un)checked. */
  private final boolean checked;

  /**
   * Constructor.
   * @param e text of the menu item
   * @param k key shortcut
   * @param h help string
   * @param d data reference flag
   * @param c checked flag
   */
  GUICommands(final String e, final String k, final String h, final boolean d,
      final boolean c) {
    entry = e;
    key = k;
    help = h;
    data = d;
    checked = c;
  }

  @Override
  public void refresh(final GUI gui, final AbstractButton b) {
    b.setEnabled(!data || gui.context.data() != null);
  }

  @Override
  public final boolean checked() { return checked; }

  @Override
  public String help() { return help; }

  @Override
  public String label() { return entry; }

  @Override
  public String key() { return key; }

  // STATIC METHODS ===========================================================

  /**
   * Checks if data can be updated.
   * @param n node instance
   * @param no disallowed node types
   * @return result of check
   */
  static boolean updatable(final Nodes n, final int... no) {
    if(n == null || (no.length == 0 ? n.size() < 1 : n.size() != 1))
      return false;

    final int k = n.data.kind(n.list[0]);
    for(final int i : no) if(k == i) return false;
    return true;
  }

  /**
   * Returns a quoted string.
   * @param s string to encode
   * @return quoted string
   */
  static String quote(final String s) {
    return "\"" + s.replaceAll("\\\"", "&quot;") + "\"";
  }

  /**
   * Returns a database function for the first node in a node set.
   * @param n node set
   * @param i offset
   * @return function string
   */
  static String openPre(final Nodes n, final int i) {
    return Function._DB_OPEN_PRE.get(null, Str.get(n.data.meta.name),
        Int.get(n.list[i])).toString();
  }
}
