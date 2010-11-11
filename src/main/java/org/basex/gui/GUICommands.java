package org.basex.gui;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import javax.swing.AbstractButton;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.CreateFS;
import org.basex.core.cmd.CreateIndex;
import org.basex.core.cmd.Cs;
import org.basex.core.cmd.DropIndex;
import org.basex.core.cmd.Export;
import org.basex.core.cmd.Mount;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Optimize;
import org.basex.core.cmd.XQuery;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.dialog.Dialog;
import org.basex.gui.dialog.DialogAbout;
import org.basex.gui.dialog.DialogColors;
import org.basex.gui.dialog.DialogCreate;
import org.basex.gui.dialog.DialogCreateFS;
import org.basex.gui.dialog.DialogEdit;
import org.basex.gui.dialog.DialogExport;
import org.basex.gui.dialog.DialogFontChooser;
import org.basex.gui.dialog.DialogHelp;
import org.basex.gui.dialog.DialogInfo;
import org.basex.gui.dialog.DialogInsert;
import org.basex.gui.dialog.DialogMapLayout;
import org.basex.gui.dialog.DialogMountFS;
import org.basex.gui.dialog.DialogOpen;
import org.basex.gui.dialog.DialogPrefs;
import org.basex.gui.dialog.DialogProgress;
import org.basex.gui.dialog.DialogServer;
import org.basex.gui.dialog.DialogTreeOptions;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.view.ViewData;
import org.basex.io.IO;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;
import org.basex.util.Array;
import org.basex.util.Performance;
import org.basex.util.StringList;
import org.basex.util.Token;
import org.basex.util.Util;
import org.deepfs.util.LibraryLoader;

/**
 * This enumeration encapsulates all commands that are triggered by
 * GUI operations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public enum GUICommands implements GUICommand {

  /* FILE MENU */

  /** Opens a dialog to create a new database. */
  CREATE(GUICREATE + DOTS, "% N", GUICREATETT, false, false) {
    @Override
    public void execute(final GUI gui) {
      // open file chooser for XML creation
      final DialogCreate dialog = new DialogCreate(gui);
      if(!dialog.ok()) return;
      final String in = dialog.path();
      final String db = dialog.dbname();
      progress(gui, PROGCREATE, new Command[] { new CreateDB(db, in) });
    }
  },

  /** Opens a dialog to open a database. */
  OPEN(GUIOPEN + DOTS, "% O", GUIOPENTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      final DialogOpen dialog = new DialogOpen(gui, false, false);
      if(dialog.ok()) {
        close(gui);
        gui.execute(new Open(dialog.db()), false);
      } else if(dialog.nodb()) {
        if(Dialog.confirm(gui, NODBQUESTION)) CREATE.execute(gui);
      }
    }
  },

  /** Shows database info. */
  INFO(GUIINFO + DOTS, "% D", GUIINFOTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      final DialogInfo info = new DialogInfo(gui);
      if(info.ok()) {
        final Data d = gui.context.data;
        final boolean[] ind = info.indexes();
        if(info.opt) {
          d.meta.txtindex = ind[0];
          d.meta.atvindex = ind[1];
          d.meta.ftxindex = ind[2];
          progress(gui, INFOOPT, new Command[] { new Optimize() });
        } else {
          Command[] cmd = new Command[0];
          if(ind[0] != d.meta.pthindex)
            cmd = Array.add(cmd, cmd(ind[0], CmdIndex.PATH));
          if(ind[1] != d.meta.txtindex)
            cmd = Array.add(cmd, cmd(ind[1], CmdIndex.TEXT));
          if(ind[2] != d.meta.atvindex)
            cmd = Array.add(cmd, cmd(ind[2], CmdIndex.ATTRIBUTE));
          if(ind[3] != d.meta.ftxindex)
            cmd = Array.add(cmd, cmd(ind[3], CmdIndex.FULLTEXT));

          if(cmd.length != 0) progress(gui, PROGINDEX, cmd);
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

  /** Exports a document. */
  EXPORT(GUIEXPORT + DOTS, null, GUIEXPORTTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      final DialogExport dialog = new DialogExport(gui);
      if(!dialog.ok()) return;

      final IO root = IO.get(dialog.path());

      // check if existing files will be overwritten
      if(root.exists()) {
        IO file = null;
        boolean overwrite = false;
        final Data d = gui.context.data;
        for(final int pre : d.doc().toArray()) {
          file = root.merge(Token.string(d.text(pre, true)));
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

      gui.execute(new Export(root.path()), false);
    }
  },

  /** Opens a dialog to drop databases. */
  DROP(GUIDROP + DOTS, null, GUIDROPTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      if(new DialogOpen(gui, true, false).nodb()) Dialog.warn(gui, INFONODB);
    }
  },

  /** Closes the database. */
  CLOSE(GUICLOSE, "% W", GUICLOSETT, true, false) {
    @Override
    public void execute(final GUI gui) {
      gui.execute(new Close(), false);
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

  /** Opens an XQuery file. */
  XQOPEN(GUIXQOPEN + DOTS, "% R", GUIXQOPENTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      gui.query.confirm();

      // open file chooser for XML creation
      final BaseXFileChooser fc = new BaseXFileChooser(GUIOPEN,
          gui.gprop.get(GUIProp.XQPATH), gui);
      fc.addFilter(CREATEXQDESC, IO.XQSUFFIX);

      final IO file = fc.select(BaseXFileChooser.Mode.FOPEN);
      if(file != null) gui.query.setQuery(file);
    }
  },

  /** Saves the current XQuery. */
  XQSAVE(GUISAVE, "% S", GUISAVETT, false, false) {
    @Override
    public void execute(final GUI gui) {
      final IO file = gui.context.query;
      if(file == null) {
        XQSAVEAS.execute(gui);
      } else {
        try {
          file.write(gui.query.getQuery());
          gui.gprop.files(file);
        } catch(final IOException ex) {
          Dialog.error(gui, NOTSAVED);
        }
      }
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      b.setEnabled(gui.query.modified());
    }
  },

  /** Saves the current XQuery. */
  XQSAVEAS(GUISAVEAS + DOTS, "% shift S", GUISAVETT, false, false) {
    @Override
    public void execute(final GUI gui) {
      // open file chooser for XML creation
      final String fn = gui.context.query == null ? null :
        gui.context.query.path();
      final BaseXFileChooser fc = new BaseXFileChooser(GUISAVEAS,
          fn == null ? gui.gprop.get(GUIProp.XQPATH) : fn, gui);
      fc.addFilter(CREATEXQDESC, IO.XQSUFFIX);

      final IO file = fc.select(BaseXFileChooser.Mode.FSAVE);
      if(file == null) return;
      gui.context.query = file;
      XQSAVE.execute(gui);
    }
  },

  /** Exits the application. */
  EXIT(GUIEXIT, null, GUIEXITTT, false, false) {
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
      final byte[] txt = ViewData.path(gui.context.data, pre);
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
        sb.append(fndb(n, i));
      }
      gui.context.copied = null;
      gui.execute(new XQuery("insert nodes (" + sb + ") into " +
        fndb(gui.context.marked, 0)), false);
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
      if(Dialog.confirm(gui, DELETECONF)) {
        final StringBuilder sb = new StringBuilder();
        final Nodes n = gui.context.marked;
        for(int i = 0; i < n.size(); ++i) {
          if(i > 0) sb.append(',');
          sb.append(fndb(n, i));
        }
        gui.context.marked = new Nodes(n.data);
        gui.context.copied = null;
        gui.context.focused = -1;
        gui.execute(new XQuery("delete nodes (" + sb + ")"), false);
      }
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
      final Type type = Nod.type(insert.kind);
      String item = Token.string(type.nam) + " { " + quote(sl.get(0)) + " }";

      if(type == Type.ATT || type == Type.PI) {
        item += " { " + quote(sl.get(1)) + " }";
      } else if(type == Type.ELM) {
        item += " { () }";
      }

      gui.context.copied = null;
      gui.execute(new XQuery("insert node " + item + " into " + fndb(n, 0)),
          false);
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
        fndb(n, 0) + " as " + quote(rename)), false);
      if(replace != null) gui.execute(new XQuery("replace value of node " +
        fndb(n, 0) + " with " + quote(replace)), false);
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
        marked = new Nodes(pre, ctx.data);
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
      gui.gprop.invert(GUIProp.SHOWXQUERY);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      select(b, gui.gprop.is(GUIProp.SHOWXQUERY));
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
      select(b, gui.gprop.is(GUIProp.SHOWINFO));
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
      select(b, gui.gprop.is(GUIProp.SHOWMENU));
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
      select(b, gui.gprop.is(GUIProp.SHOWBUTTONS));
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
      select(b, gui.gprop.is(GUIProp.SHOWINPUT));
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
      select(b, gui.gprop.is(GUIProp.SHOWSTATUS));
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
      select(b, gui.gprop.is(GUIProp.SHOWTEXT));
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
      select(b, gui.gprop.is(GUIProp.SHOWMAP));
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
      select(b, gui.gprop.is(GUIProp.SHOWTREE));
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
      select(b, gui.gprop.is(GUIProp.SHOWFOLDER));
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
      select(b, gui.gprop.is(GUIProp.SHOWPLOT));
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
      select(b, gui.gprop.is(GUIProp.SHOWTABLE));
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
      select(b, gui.gprop.is(GUIProp.SHOWEXPLORE));
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
      select(b, gui.fullscreen);
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
      select(b, gui.gprop.is(GUIProp.EXECRT));
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
          gui.notify.context(new Nodes(0, ctx.data), true, null);
          gui.notify.mark(ctx.current, null);
        }
      } else {
        if(root) {
          gui.notify.mark(new Nodes(ctx.data), null);
        } else {
          final Nodes mark = ctx.marked;
          ctx.marked = new Nodes(ctx.data);
          gui.notify.context(mark, true, null);
        }
      }
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      super.refresh(gui, b);
      select(b, gui.gprop.is(GUIProp.FILTERRT));
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
      new DialogFontChooser(gui);
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

  /* DEEPFS MENU */

  /** Opens a dialog to import given directory as DeepFS instance. */
  CREATEFS(GUICREATEFS + DOTS, "% M", GUICREATEFSTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      if(!new DialogCreateFS(gui).ok()) return;
      final GUIProp gprop = gui.gprop;
      final String p = gprop.is(GUIProp.FSALL) ? "/"
          : gui.gprop.get(GUIProp.FSBACKING).replace('\\', '/');
      final String n = gprop.get(GUIProp.FSDBNAME);
      progress(gui, CREATEFSTITLE, new Command[] { new CreateFS(n, p) });
    }
  },

  /** Opens a dialog to use DeepFS instance as Desktop Query Engine. */
  DQE(GUIDQE + DOTS, null, GUIDQETT, false, false) {
    @Override
    public void execute(final GUI gui) {
      final DialogOpen dialog = new DialogOpen(gui, false, true);
      if(dialog.ok()) {
        close(gui);
        gui.execute(new Open(dialog.db()), false);
      } else if(dialog.nodb()) {
        if(Dialog.confirm(gui, NODEEPFSQUESTION)) CREATEFS.execute(gui);
      }
    }
  },

  /** Opens a dialog to mount DeepFS instance as Filesystem in USErspace. */
  MOUNTFS(GUIMOUNTFS + DOTS, null, GUIMOUNTFSTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      final DialogMountFS dialog = new DialogMountFS(gui);
      if(dialog.ok()) {
        close(gui);
        gui.execute(new Mount(dialog.db(), dialog.mp()), false);
      } else if(dialog.nodb()) {
        if(Dialog.confirm(gui, NODEEPFSQUESTION)) CREATEFS.execute(gui);
      }
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      // disable mount button, if native library is not available
      b.setEnabled(LibraryLoader.load(LibraryLoader.DEEPFUSELIBNAME));
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
      select(b, gui.gprop.is(GUIProp.SHOWHELP));
    }
  },

  /** Opens the community webpage. */
  SHOWCOMMUNITY(GUISHOWCOMMUNITY, null, GUISHOWCOMMUNITYTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      final String url = COMMUNITY_URL;
      try {
        Desktop.getDesktop().browse(new URI(url));
      } catch(final Exception ex) {
        Dialog.error(gui, Util.info(INFOBROSERERR, url));
      }
    }
  },

  /** Opens the update webpage. */
  SHOWUPDATES(GUISHOWUPDATES, null, GUISHOWUPDATESTT, false, false) {
    @Override
    public void execute(final GUI gui) {
      final String url = UPDATE_URL;
      try {
        Desktop.getDesktop().browse(new URI(url));
      } catch(final Exception ex) {
        Dialog.error(gui, Util.info(INFOBROSERERR, url));
      }
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
      final boolean en = tt != null;
      b.setEnabled(en);
      b.setToolTipText(en && tt.isEmpty() ? GUIGOBACK : tt);
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
      final boolean en = tt != null;
      b.setEnabled(en);
      b.setToolTipText(en && tt.isEmpty() ? GUIGOFORWARD : tt);
    }
  },

  /** Goes one level up. */
  GOUP(GUIGOUP, "alt UP", GUIGOUPTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      final Context ctx = gui.context;
      if(!ctx.root()) {
        boolean root = true;
        for(final int pre : ctx.current.list) {
          root &= ctx.data.kind(pre) == Data.DOC;
        }
        gui.execute(new Cs(root ? "/" : ".."), false);
      }
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      b.setEnabled(!gui.context.root());
    }
  },

  /** Goes to the root node. */
  GOHOME(GUIROOT, "alt HOME", GUIROOTTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      if(!gui.context.root()) gui.execute(new Cs("/"), false);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton b) {
      b.setEnabled(!gui.context.root());
    }
  },

  /** Displays the root node in the text view. */
  HOME(GUIROOT, null, GUIROOTTT, true, false) {
    @Override
    public void execute(final GUI gui) {
      gui.execute(new XQuery("/"), false);
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
    final boolean e = !data || gui.context.data != null;
    if(b.isEnabled() != e) b.setEnabled(e);
  }

  @Override
  public final boolean checked() { return checked; }

  @Override
  public String help() { return help; }

  @Override
  public String desc() { return entry; }

  @Override
  public String key() { return key; }

  // STATIC METHODS ===========================================================

  /**
   * Runs the specified commands, showing a progress dialog.
   * @param gui reference to the main window
   * @param t dialog title
   * @param cmds commands to be run
   */
  static void progress(final GUI gui, final String t, final Command[] cmds) {
    // start database creation thread
    new Thread() {
      @Override
      public void run() {
        for(final Command cmd : cmds) {
          final boolean ci = cmd instanceof CreateIndex;
          final boolean fs = cmd instanceof CreateFS;
          final boolean di = cmd instanceof DropIndex;
          final boolean op = cmd instanceof Optimize;
          if(!ci && !di && !op) close(gui);

          // execute command
          final DialogProgress wait = new DialogProgress(gui, t, !fs, !op, cmd);
          final Performance perf = new Performance();
          String info;
          boolean ok = true;
          try {
            cmd.execute(gui.context);
            info = cmd.info();
          } catch(final BaseXException ex) {
            info = ex.getMessage();
            ok = false;
          } finally {
            wait.dispose();
          }
          final String time = perf.toString();
          gui.info.setInfo(info, cmd, time, ok);
          gui.info.reset();
          gui.status.setText(Util.info(PROCTIME, time));
          if(!ok) Dialog.error(gui, info.equals(PROGERR) ? CANCELCREATE : info);

          // initialize views
          if(!ci && !di) gui.notify.init();
        }
      }
    }.start();
  }

  /**
   * Closes the current database and initializes the GUI.
   * @param gui gui reference
   */
  static void close(final GUI gui) {
    try {
      new Close().execute(gui.context);
    } catch(final BaseXException ex) {
      /* Ignored. */
    }
    gui.notify.init();
  }

  /**
   * Displays a file save dialog and returns the file name or a null reference.
   * @param gui gui reference
   * @param single file vs directory dialog
   * @return io reference
   */
  public static IO save(final GUI gui, final boolean single) {
    // open file chooser for XML creation
    final BaseXFileChooser fc = new BaseXFileChooser(GUISAVEAS,
        gui.gprop.get(GUIProp.SAVEPATH), gui);
    fc.addFilter(CREATEXMLDESC, IO.XMLSUFFIX);

    final IO file = fc.select(single ? BaseXFileChooser.Mode.FSAVE :
      BaseXFileChooser.Mode.DSAVE);
    if(file != null) gui.gprop.set(GUIProp.SAVEPATH, file.path());
    return file;
  }

  /**
   * Selects or de-selects the specified component.
   * @param but component
   * @param select selection flag
   */
  static void select(final AbstractButton but, final boolean select) {
    if(but.isSelected() != select) but.setSelected(select);
  }

  /**
   * Checks if data can be updated (disk mode, nodes defined, no namespaces).
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
  static String fndb(final Nodes n, final int i) {
    return NAMELC + ":db('" + n.data.meta.name + "', " + n.list[i] + ")";
  }
}
