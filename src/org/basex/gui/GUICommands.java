package org.basex.gui;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

import javax.swing.AbstractButton;

import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.Commands.CmdUpdate;
import org.basex.core.proc.Close;
import org.basex.core.proc.Copy;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.CreateFS;
import org.basex.core.proc.CreateIndex;
import org.basex.core.proc.Cs;
import org.basex.core.proc.Delete;
import org.basex.core.proc.DropIndex;
import org.basex.core.proc.Export;
import org.basex.core.proc.Insert;
import org.basex.core.proc.Mount;
import org.basex.core.proc.Open;
import org.basex.core.proc.Optimize;
import org.basex.core.proc.Update;
import org.basex.core.proc.XQuery;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.dialog.Dialog;
import org.basex.gui.dialog.DialogAbout;
import org.basex.gui.dialog.DialogColors;
import org.basex.gui.dialog.DialogCreate;
import org.basex.gui.dialog.DialogCreateFS;
import org.basex.gui.dialog.DialogEdit;
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
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.ViewData;
import org.basex.io.IO;
import org.basex.util.Array;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.deepfs.jfuse.JFUSEAdapter;

/**
 * This enumeration encapsulates all commands that are triggered by
 * GUI operations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public enum GUICommands implements GUICommand {

  /* FILE MENU */

  /** Opens a dialog to create a new database. */
  CREATE(false, GUICREATE, "% N", GUICREATETT) {
    @Override
    public void execute(final GUI gui) {
      // open file chooser for XML creation
      final DialogCreate dialog = new DialogCreate(gui);
      if(!dialog.ok()) return;
      final String in = dialog.path();
      final String db = dialog.dbname();
      progress(gui, PROGCREATE, new Process[] { new CreateDB(in, db) });
    }
  },

  /** Opens a dialog to open a database. */
  OPEN(false, GUIOPEN, "% O", GUIOPENTT) {
    @Override
    public void execute(final GUI gui) {
      final DialogOpen dialog = new DialogOpen(gui, false, false);
      if(dialog.ok()) {
        if(new Close().execute(gui.context)) gui.notify.init();
        gui.execute(new Open(dialog.db()));
      } else if(dialog.nodb()) {
        if(Dialog.confirm(gui, NODBQUESTION)) CREATE.execute(gui);
      }
    }
  },

  /** Shows database info. */
  INFO(true, GUIINFO, "% D", GUIINFOTT) {
    @Override
    public void execute(final GUI gui) {
      final DialogInfo info = new DialogInfo(gui);
      if(info.ok()) {
        final Data d = gui.context.data();
        final boolean[] ind = info.indexes();
        if(info.opt) {
          d.meta.txtindex = ind[0];
          d.meta.atvindex = ind[1];
          d.meta.ftxindex = ind[2];
          progress(gui, INFOOPT, new Process[] { new Optimize() });
        } else {
          Process[] proc = new Process[0];
          if(ind[0] != d.meta.pathindex)
            proc = Array.add(proc, cmd(ind[0], CmdIndex.SUMMARY));
          if(ind[1] != d.meta.txtindex)
            proc = Array.add(proc, cmd(ind[1], CmdIndex.TEXT));
          if(ind[2] != d.meta.atvindex)
            proc = Array.add(proc, cmd(ind[2], CmdIndex.ATTRIBUTE));
          if(ind[3] != d.meta.ftxindex)
            proc = Array.add(proc, cmd(ind[3], CmdIndex.FULLTEXT));

          if(proc.length != 0) progress(gui, INFOBUILD, proc);
        }
      }
    }

    /**
     * Returns a process for creating/dropping the specified index.
     * @param create create flag
     * @param index name of index
     * @return process reference
     */
    private Process cmd(final boolean create, final CmdIndex index) {
      return create ? new CreateIndex(index) : new DropIndex(index);
    }
  },

  /** Exports a document. */
  EXPORT(true, GUIEXPORT, null, GUIEXPORTTT) {
    @Override
    public void execute(final GUI gui) {
      final IO file = save(gui, gui.context.data().doc().length == 1);
      if(file != null) gui.execute(new Export(file.path()));
    }
  },

  /** Opens a dialog to drop databases. */
  DROP(false, GUIDROP, null, GUIDROPTT) {
    @Override
    public void execute(final GUI gui) {
      if(new DialogOpen(gui, true, false).nodb()) Dialog.info(gui, INFONODB);
    }
  },

  /** Closes the database. */
  CLOSE(true, GUICLOSE, "% W", GUICLOSETT) {
    @Override
    public void execute(final GUI gui) {
      gui.execute(new Close());
    }
  },
  
  /** Server Dialog. */
  SERVER(false, "Server...", "% serv", "SERVER") {
    @Override
    public void execute(final GUI gui) {
   // open file chooser for XML creation
      new DialogServer(gui);
    }
  },

  /** Opens an XQuery file. */
  XQOPEN(false, GUIXQOPEN, "% R", GUIXQOPENTT) {
    @Override
    public void execute(final GUI gui) {
      gui.query.confirm();

      // open file chooser for XML creation
      final BaseXFileChooser fc = new BaseXFileChooser(XQOPENTITLE,
          gui.prop.get(GUIProp.XQPATH), gui);
      fc.addFilter(CREATEXQDESC, IO.XQSUFFIX);

      final IO file = fc.select(BaseXFileChooser.Mode.FOPEN);
      if(file != null) gui.query.setQuery(file);
    }
  },

  /** Saves the current XQuery. */
  XQSAVE(false, GUIXQSAVE, "% S", GUIXQSAVETT) {
    @Override
    public void execute(final GUI gui) {
      // open file chooser for XML creation
      final String fn = Prop.xquery == null ? null : Prop.xquery.path();
      final BaseXFileChooser fc = new BaseXFileChooser(XQSAVETITLE,
          fn == null ? gui.prop.get(GUIProp.XQPATH) : fn, gui);
      fc.addFilter(CREATEXQDESC, IO.XQSUFFIX);

      final IO file = fc.select(BaseXFileChooser.Mode.FSAVE);
      if(file != null) {
        file.suffix(IO.XQSUFFIX);
        try {
          file.write(gui.query.getQuery());
          gui.query.setQuery(file);
        } catch(final IOException ex) {
          Dialog.error(gui, NOTSAVED);
        }
      }
    }
  },

  /** Saves the result text. */
  SAVE(true, GUIEXPORT, "", GUIEXPORTTT) {
    @Override
    public void execute(final GUI gui) {
      final IO file = save(gui, true);
      if(file != null) {
        try {
          file.write(gui.text.getText());
        } catch(final IOException ex) {
          Dialog.error(gui, NOTSAVED);
        }
      }
    }
  },

  /** Exits the application. */
  EXIT(false, GUIEXIT, null, GUIEXITTT) {
    @Override
    public void execute(final GUI gui) {
      gui.dispose();
    }
  },

  /* EDIT COMMANDS */

  /** Copies the currently marked nodes. */
  COPY(true, GUICOPY, "", GUICOPYTT) {
    @Override
    public void execute(final GUI gui) {
      final Context context = gui.context;
      final Nodes nodes = context.marked();
      context.copy(new Nodes(nodes.nodes, nodes.data));
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      // disallow copy of empty node set or root node
      final Nodes n = gui.context.marked();
      BaseXLayout.enable(button, updatable(n) &&
          (n.size() != 1 || n.nodes[0] != 0));
    }
  },

  /** Copies the current path. */
  COPYPATH(true, GUICPPATH, "% shift C", GUICPPATHTT) {
    @Override
    public void execute(final GUI gui) {
      final int pre = gui.context.marked().nodes[0];
      final byte[] txt = ViewData.path(gui.context.data(), pre);
      // copy path to clipboard
      final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
      clip.setContents(new StringSelection(Token.string(txt)), null);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      // disallow copy of empty node set or root node
      final Nodes marked = gui.context.marked();
      BaseXLayout.enable(button, marked != null && marked.size() != 0);
    }
  },

  /** Pastes the copied nodes. */
  PASTE(true, GUIPASTE, "", GUIPASTETT) {
    @Override
    public void execute(final GUI gui) {
      gui.execute(new Copy());
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final Context context = gui.context;
      // disallow copy of empty node set or root node
      final Nodes n = context.marked();
      boolean s = updatable(n) && context.copied() != null &&
        n.size() != 0 && (n.size() != 1 || n.nodes[0] != 0);
      if(s) {
        final Data d = n.data;
        for(final int pre : n.nodes) {
          if(d.kind(pre) != Data.ELEM) {
            s = false;
            break;
          }
        }
      }
      BaseXLayout.enable(button, s);
    }
  },

  /** Deletes the currently marked nodes. */
  DELETE(true, GUIDELETE, "", GUIDELETETT) {
    @Override
    public void execute(final GUI gui) {
      if(Dialog.confirm(gui, DELETECONF)) {
        gui.execute(new Delete());
      }
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      // disallow deletion of empty node set or root node
      final Nodes n = gui.context.marked();
      BaseXLayout.enable(button, updatable(n) && n.size() != 0);
    }
  },

  /** Inserts new nodes. */
  INSERT(true, GUIINSERT, "", GUIINSERTTT) {
    @Override
    public void execute(final GUI gui) {
      final DialogInsert insert = new DialogInsert(gui);
      if(insert.result == null) return;
      final CmdUpdate type = CmdUpdate.values()[insert.kind];
      gui.execute(new Insert(type, null, insert.result.finish()));
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final Context context = gui.context;
      final Nodes n = context.marked();
      final Data d = context.data();
      BaseXLayout.enable(button, updatable(n) && n.size() == 1 &&
          (d.kind(n.nodes[0]) == Data.ELEM || d.kind(n.nodes[0]) == Data.DOC));
    }
  },

  /** Opens a dialog to edit the currently marked nodes. */
  EDIT(true, GUIEDIT, "", GUIEDITTT) {
    @Override
    public void execute(final GUI gui) {
      final Nodes nodes = gui.context.marked();
      final DialogEdit edit = new DialogEdit(gui, nodes.nodes[0]);
      if(edit.result == null) return;
      final CmdUpdate type = CmdUpdate.values()[edit.kind];
      gui.execute(new Update(type, null, edit.result.finish()));
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final Context context = gui.context;
      final Nodes n = context.marked();
      BaseXLayout.enable(button, updatable(n) && n.size() == 1 &&
          context.data().kind(n.nodes[0]) != Data.DOC);
    }
  },

  /** Filters the currently marked nodes. */
  FILTER(true, GUIFILTER, "% ENTER", GUIFILTERTT) {
    @Override
    public void execute(final GUI gui) {
      final Context context = gui.context;
      Nodes marked = context.marked();
      if(marked.size() == 0) {
        final int pre = gui.focused;
        if(pre == -1) return;
        marked = new Nodes(pre, context.data());
      }
      gui.notify.context(marked, false, null);
      gui.input.requestFocusInWindow();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final Nodes marked = gui.context.marked();
      BaseXLayout.enable(button, marked != null && marked.size() != 0);
    }
  },

  /** Shows the XQuery view. */
  SHOWXQUERY(false, GUISHOWXQUERY, "% E", GUISHOWXQUERYTT) {
    @Override
    public void execute(final GUI gui) {
      if(gui.context.data() == null) gui.prop.invert(GUIProp.SHOWSTARTXQUERY);
      else gui.prop.invert(GUIProp.SHOWXQUERY);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      select(button, gui.prop.is(gui.context.data() != null ?
          GUIProp.SHOWXQUERY : GUIProp.SHOWSTARTXQUERY));
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Shows info. */
  SHOWINFO(false, GUISHOWINFO, "% I", GUISHOWINFOTT) {
    @Override
    public void execute(final GUI gui) {
      if(gui.context.data() == null) gui.prop.invert(GUIProp.SHOWSTARTINFO);
      else gui.prop.invert(GUIProp.SHOWINFO);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      select(button, gui.prop.is(gui.context.data() != null ?
          GUIProp.SHOWINFO : GUIProp.SHOWSTARTINFO));
    }

    @Override
    public boolean checked() { return true; }
  },

  /* VIEW MENU */

  /** Shows the menu. */
  SHOWMENU(false, GUISHOWMENU, null, GUISHOWMENUTT) {
    @Override
    public void execute(final GUI gui) {
      gui.prop.invert(GUIProp.SHOWMENU);
      gui.updateControl(gui.menu, gui.prop.is(GUIProp.SHOWMENU),
          BorderLayout.NORTH);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      select(button, gui.prop.is(GUIProp.SHOWMENU));
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Shows the buttons. */
  SHOWBUTTONS(false, GUISHOWBUTTONS, null, GUISHOWBUTTONSTT) {
    @Override
    public void execute(final GUI gui) {
      gui.prop.invert(GUIProp.SHOWBUTTONS);
      gui.updateControl(gui.buttons, gui.prop.is(GUIProp.SHOWBUTTONS),
          BorderLayout.CENTER);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      select(button, gui.prop.is(GUIProp.SHOWBUTTONS));
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show Input Field. */
  SHOWINPUT(false, GUISHOWINPUT, null, GUISHOWINPUTTT) {
    @Override
    public void execute(final GUI gui) {
      gui.updateControl(gui.nav, gui.prop.invert(GUIProp.SHOWINPUT),
          BorderLayout.SOUTH);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      select(button, gui.prop.is(GUIProp.SHOWINPUT));
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Shows the status bar. */
  SHOWSTATUS(false, GUISHOWSTATUS, null, GUISHOWSTATUSTT) {
    @Override
    public void execute(final GUI gui) {
      gui.updateControl(gui.status, gui.prop.invert(GUIProp.SHOWSTATUS),
          BorderLayout.SOUTH);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      select(button, gui.prop.is(GUIProp.SHOWSTATUS));
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Shows the text view. */
  SHOWTEXT(false, GUISHOWTEXT, "% 1", GUISHOWTEXTTT) {
    @Override
    public void execute(final GUI gui) {
      if(gui.context.data() == null) gui.prop.invert(GUIProp.SHOWSTARTTEXT);
      else gui.prop.invert(GUIProp.SHOWTEXT);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      select(button, gui.prop.is(gui.context.data() != null ?
          GUIProp.SHOWTEXT : GUIProp.SHOWSTARTTEXT));
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Shows the map. */
  SHOWMAP(true, GUISHOWMAP, "% 2", GUISHOWMAPTT) {
    @Override
    public void execute(final GUI gui) {
      gui.prop.invert(GUIProp.SHOWMAP);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      select(button, gui.prop.is(GUIProp.SHOWMAP));
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Shows the tree view. */
  SHOWFOLDER(true, GUISHOWFOLDER, "% 3", GUISHOWFOLDERTT) {
    @Override
    public void execute(final GUI gui) {
      gui.prop.invert(GUIProp.SHOWFOLDER);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      select(button, gui.prop.is(GUIProp.SHOWFOLDER));
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Shows the table view. */
  SHOWTABLE(true, GUISHOWTABLE, "% 4", GUISHOWTABLETT) {
    @Override
    public void execute(final GUI gui) {
      gui.prop.invert(GUIProp.SHOWTABLE);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      select(button, gui.prop.is(GUIProp.SHOWTABLE));
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Shows the plot view. */
  SHOWPLOT(true, GUISHOWPLOT, "% 5", GUISHOWPLOTTT) {
    @Override
    public void execute(final GUI gui) {
      gui.prop.invert(GUIProp.SHOWPLOT);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      select(button, gui.prop.is(GUIProp.SHOWPLOT));
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Shows the explorer view. */
  SHOWEXPLORER(true, GUISHOWEXPLORE, "% 6", GUISHOWEXPLORETT) {
    @Override
    public void execute(final GUI gui) {
      gui.prop.invert(GUIProp.SHOWEXPLORE);
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      select(button, gui.prop.is(GUIProp.SHOWEXPLORE));
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Fullscreen mode. */
  FULL(false, GUIFULL, "F11", GUIFULLTT) {
    @Override
    public void execute(final GUI gui) {
      gui.fullscreen();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      select(button, gui.fullscreen);
    }

    @Override
    public boolean checked() { return true; }
  },

  /* OPTION MENU */

  /** Realtime execution on/off. */
  RTEXEC(false, GUIRTEXEC, null, GUIRTEXECTT) {
    @Override
    public void execute(final GUI gui) {
      gui.prop.invert(GUIProp.EXECRT);
      gui.refreshControls();
      gui.notify.layout();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      select(button, gui.prop.is(GUIProp.EXECRT));
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Realtime filtering on/off. */
  RTFILTER(true, GUIRTFILTER, null, GUIRTFILTERTT) {
    @Override
    public void execute(final GUI gui) {
      final boolean rt = gui.prop.invert(GUIProp.FILTERRT);
      gui.refreshControls();
      gui.notify.layout();

      final Context context = gui.context;
      final boolean root = context.root();
      if(!rt) {
        if(!root) {
          gui.notify.context(new Nodes(0, context.data()), true, null);
          gui.notify.mark(context.current(), null);
        }
      } else {
        if(root) {
          gui.notify.mark(new Nodes(context.data()), null);
        } else {
          final Nodes mark = context.marked();
          context.marked(new Nodes(context.data()));
          gui.notify.context(mark, true, null);
        }
      }
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      select(button, gui.prop.is(GUIProp.FILTERRT));
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Color schema. */
  COLOR(false, GUICOLOR, null, GUICOLORTT) {
    @Override
    public void execute(final GUI gui) {
      new DialogColors(gui);
    }
  },

  /** Changes the fonts. */
  FONTS(false, GUIFONTS, null, GUIFONTSTT) {
    @Override
    public void execute(final GUI gui) {
      new DialogFontChooser(gui);
    }
  },

  /** Map layout. */
  MAPLAYOUT(true, GUIMAPLAYOUT, "% L", GUIMAPLAYOUTTT) {
    @Override
    public void execute(final GUI gui) {
      new DialogMapLayout(gui);
    }
  },

  /** Shows a preference dialog. */
  PREFS(false, GUIPREFS, "% P", GUIPREFSTT) {
    @Override
    public void execute(final GUI gui) {
      new DialogPrefs(gui);
    }
  },
  
  /* DEEPFS MENU */

  /** Opens a dialog to import given directory as DeepFS instance. */
  CREATEFS(false, GUICREATEFS, null, GUICREATEFSTT) {
    @Override
    public void execute(final GUI gui) {
      if(!new DialogCreateFS(gui).ok()) return;
      final GUIProp gprop = gui.prop;
      final String p = gprop.is(GUIProp.FSALL) ? "/"
          : gui.prop.get(GUIProp.FSBACKING).replace('\\', '/');
      final String n = gprop.get(GUIProp.FSDBNAME);
      progress(gui, CREATEFSTITLE, new Process[] { new CreateFS(p, n) });
    }
  },
  
  /** Opens a dialog to use DeepFS instance as Desktop Query Engine. */
  DQE(false, GUIDQE, null, GUIDQETT) {
    @Override
    public void execute(final GUI gui) {
      final DialogOpen dialog = new DialogOpen(gui, false, true);
      if(dialog.ok()) {
        if(new Close().execute(gui.context)) gui.notify.init();
        gui.execute(new Open(dialog.db()));
      } else if(dialog.nodb()) {
        if(Dialog.confirm(gui, NODEEPFSQUESTION)) CREATEFS.execute(gui);
      }
    }
  },
  
  /** Opens a dialog to mount DeepFS instance as Filesystem in USErspace. */
  MOUNTFS(false, GUIMOUNTFS, null, GUIMOUNTFSTT) {
    @Override
    public void execute(final GUI gui) {
      final DialogMountFS dialog = new DialogMountFS(gui);
      if(dialog.ok()) {
        if(new Close().execute(gui.context)) gui.notify.init();
        gui.execute(new Mount(dialog.db(), dialog.mp()));
      } else if(dialog.nodb()) {
        if(Dialog.confirm(gui, NODEEPFSQUESTION)) CREATEFS.execute(gui);
      }
    }
    
    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      // disable mount button, if native library is not available.
      BaseXLayout.enable(button, JFUSEAdapter.loadJFUSELibrary());
    }
  },
 
  /* HELP MENU */

  /** Shows the help window. */
  SHOWHELP(false, GUISHOWHELP, "F1", GUISHOWHELPTT) {
    @Override
    public void execute(final GUI gui) {
      if(!gui.prop.is(GUIProp.SHOWHELP)) {
        gui.prop.set(GUIProp.SHOWHELP, true);
        gui.help = new DialogHelp(gui);
        gui.refreshControls();
      } else {
        gui.help.close();
      }
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      select(button, gui.prop.is(GUIProp.SHOWHELP));
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Shows the "about" information. */
  ABOUT(false, GUIABOUT, null, GUIABOUTTT) {
    @Override
    public void execute(final GUI gui) {
      new DialogAbout(gui);
    }
  },

  /* BROWSE COMMANDS */

  /** Goes one step back. */
  GOBACK(true, GUIGOBACK, "alt LEFT", GUIGOBACKTT) {
    @Override
    public void execute(final GUI gui) {
      gui.notify.hist(false);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final String tt = gui.notify.tooltip(true);
      final boolean en = tt != null;
      BaseXLayout.enable(button, en);
      button.setToolTipText(en && tt.isEmpty() ? GUIGOBACKTT : tt);
    }
  },

  /** Goes one step forward. */
  GOFORWARD(true, GUIGOFORWARD, "alt RIGHT", GUIGOFORWARDTT) {
    @Override
    public void execute(final GUI gui) {
      gui.notify.hist(true);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final String tt = gui.notify.tooltip(false);
      final boolean en = tt != null;
      BaseXLayout.enable(button, en);
      button.setToolTipText(en && tt.isEmpty() ? GUIGOFORWARDTT : tt);
    }
  },

  /** Goes one level up. */
  GOUP(true, GUIGOUP, "alt UP", GUIGOUPTT) {
    @Override
    public void execute(final GUI gui) {
      gui.execute(new Cs(".."));
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      BaseXLayout.enable(button, !gui.context.root());
    }
  },

  /** Goes to the root node. */
  ROOT(true, GUIROOT, "alt HOME", GUIROOTTT) {
    @Override
    public void execute(final GUI gui) {
      gui.execute(new Cs("/"));
    }
  },

  /** Displays the root node in the text view. */
  HOME(true, GUIROOT, null, GUIROOTTT) {
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

  /**
   * Constructor.
   * @param d data reference flag
   * @param e text of the menu item
   * @param k key shortcut
   * @param h help string
   */
  GUICommands(final boolean d, final String e, final String k, final String h) {
    data = d;
    entry = e;
    key = k;
    help = h;
  }

  public abstract void execute(final GUI gui);

  public void refresh(final GUI gui, final AbstractButton button) {
    BaseXLayout.enable(button, !data || gui.context.data() != null);
  }

  public boolean checked() { return false; }

  public String help() { return help; }

  public String desc() { return entry; }

  public String key() { return key; }

  // =========================================================================

  /**
   * Performs a process, showing a progress dialog.
   * @param gui reference to the main window
   * @param t dialog title
   * @param procs processes
   */
  static void progress(final GUI gui, final String t, final Process[] procs) {
    // start database creation thread
    new Thread() {
      @Override
      public void run() {
        for(final Process p : procs) {
          final boolean ci = p instanceof CreateIndex;
          final boolean fs = p instanceof CreateFS;
          final boolean di = p instanceof DropIndex;
          final boolean op = p instanceof Optimize;

          if(!ci && !di && !op) {
            new Close().execute(gui.context);
            gui.notify.init();
          }

          // execute process
          final DialogProgress wait = new DialogProgress(gui, t, !fs, !op, p);
          final Performance perf = new Performance();
          final boolean ok = p.execute(gui.context);
          wait.dispose();

          // return user information
          if(ok) {
            gui.status.setText(Main.info(PROCTIME, perf.getTimer()));
            if(op) Dialog.info(gui, INFOOPTIM);
          } else {
            final String info = p.info();
            Dialog.error(gui, info.equals(PROGERR) ? CANCELCREATE : info);
          }
          // initialize views
          if(!ci && !di) gui.notify.init();
        }
      }
    }.start();
  }

  /**
   * Displays a file save dialog and returns the file name or a null reference.
   * @param gui gui reference
   * @param single file vs directory dialog
   * @return io reference
   */
  public static IO save(final GUI gui, final boolean single) {
    // open file chooser for XML creation
    final BaseXFileChooser fc = new BaseXFileChooser(EXPORTTITLE,
        gui.context.data().meta.file.path(), gui);
    fc.addFilter(CREATEXMLDESC, IO.XMLSUFFIX);

    final IO file = fc.select(single ? BaseXFileChooser.Mode.FSAVE :
      BaseXFileChooser.Mode.DSAVE);
    if(file != null && single) file.suffix(IO.XMLSUFFIX);
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
   * @return result of check
   */
  static boolean updatable(final Nodes n) {
    return n != null && n.data.ns.size() == 0;
  }
}
