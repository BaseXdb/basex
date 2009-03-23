package org.basex.gui;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import javax.swing.AbstractButton;
import org.basex.BaseX;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.Commands.CmdUpdate;
import org.basex.core.proc.Cs;
import org.basex.core.proc.Close;
import org.basex.core.proc.Copy;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.CreateFS;
import org.basex.core.proc.CreateIndex;
import org.basex.core.proc.Delete;
import org.basex.core.proc.DropIndex;
import org.basex.core.proc.Export;
import org.basex.core.proc.Insert;
import org.basex.core.proc.Open;
import org.basex.core.proc.Optimize;
import org.basex.core.proc.Update;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.data.Nodes;
import org.basex.gui.dialog.Dialog;
import org.basex.gui.dialog.DialogAbout;
import org.basex.gui.dialog.DialogColors;
import org.basex.gui.dialog.DialogCreate;
import org.basex.gui.dialog.DialogEdit;
import org.basex.gui.dialog.DialogFontChooser;
import org.basex.gui.dialog.DialogHelp;
import org.basex.gui.dialog.DialogImportFS;
import org.basex.gui.dialog.DialogInfo;
import org.basex.gui.dialog.DialogInsert;
import org.basex.gui.dialog.DialogMapLayout;
import org.basex.gui.dialog.DialogOpen;
import org.basex.gui.dialog.DialogPrefs;
import org.basex.gui.dialog.DialogProgress;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.ViewData;
import org.basex.io.IO;
import org.basex.util.Array;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * This enumeration encapsulates all commands that are triggered by
 * GUI operations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public enum GUICommands implements GUICommand {

  /* FILE MENU */

  /** Create database. */
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

  /** Open database. */
  OPEN(false, GUIOPEN, "% O", GUIOPENTT) {
    @Override
    public void execute(final GUI gui) {
      final DialogOpen dialog = new DialogOpen(gui, false);
      if(dialog.ok()) {
        if(new Close().execute(gui.context)) gui.notify.init();
        gui.execute(new Open(dialog.db()));
      } else if(dialog.nodb()) {
        if(Dialog.confirm(gui, NODBQUESTION)) CREATE.execute(gui);
      }
    }
  },

  /** Drop database. */
  DROP(false, GUIDROP, null, GUIDROPTT) {
    @Override
    public void execute(final GUI gui) {
      if(new DialogOpen(gui, true).nodb()) Dialog.info(gui, INFODB);
    }
  },

  /** Reset database. */
  CLOSE(true, GUICLOSE, "% W", GUICLOSETT) {
    @Override
    public void execute(final GUI gui) {
      gui.execute(new Close());
    }
  },

  /** Open XQuery. */
  XQOPEN(true, GUIXQOPEN, "% R", GUIXQOPENTT) {
    @Override
    public void execute(final GUI gui) {
      // open file chooser for XML creation
      final BaseXFileChooser fc = new BaseXFileChooser(XQOPENTITLE,
          GUIProp.xqpath, gui);
      fc.addFilter(IO.XQSUFFIX, CREATEXQDESC);

      final IO file = fc.select(BaseXFileChooser.Mode.FOPEN);
      if(file != null) {
        Prop.xquery = file;
        try {
          gui.query.setQuery(file.content());
        } catch(final IOException ex) {
          Dialog.error(gui, XQOPERROR);
        }
        GUIProp.xqpath = file.getDir();
      }
    }
  },

  /** Save XQuery. */
  XQSAVE(true, GUIXQSAVE, "% S", GUIXQSAVETT) {
    @Override
    public void execute(final GUI gui) {
      // open file chooser for XML creation
      final String fn = Prop.xquery == null ? null : Prop.xquery.path();
      final BaseXFileChooser fc = new BaseXFileChooser(XQSAVETITLE,
          fn == null ? GUIProp.xqpath : fn, gui);
      fc.addFilter(CREATEXQDESC, IO.XQSUFFIX);

      final IO file = fc.select(BaseXFileChooser.Mode.FSAVE);
      if(file != null) {
        Prop.xquery = file;
        try {
          file.suffix(IO.XQSUFFIX);
          file.write(gui.query.getQuery());
        } catch(final IOException ex) {
          Dialog.error(gui, XQSAVERROR);
        }
        GUIProp.xqpath = file.getDir();
      }
    }
  },

  /** Import filesystem. */
  IMPORTFS(false, GUIIMPORTFS, "% D", GUIIMPORTFSTT) {
    @Override
    public void execute(final GUI gui) {
      if(!new DialogImportFS(gui).ok()) return;
      final String p = GUIProp.fsall ? "/" : GUIProp.fspath.replace('\\', '/');
      final String name = GUIProp.importfsname;
      progress(gui, IMPORTFSTITLE, new Process[] { new CreateFS(p, name) });
    }
  },

  /** Export document. */
  EXPORT(true, GUIEXPORT, "% E", GUIEXPORTTT) {
    @Override
    public void execute(final GUI gui) {
      final IO file = save(gui);
      if(file != null) gui.execute(new Export(file.path()));
    }
  },

  /** Save result text. */
  SAVE(true, GUIEXPORT, "", GUIEXPORTTT) {
    @Override
    public void execute(final GUI gui) {
      final IO file = save(gui);
      if(file != null) {
        try {
          file.write(gui.text.getText());
        } catch(final IOException ex) {
          Dialog.error(gui, XQSAVERROR);
        }
      }
    }
  },

  /** Exit BaseX. */
  EXIT(false, GUIEXIT, null, GUIEXITTT) {
    @Override
    public void execute(final GUI gui) {
      gui.dispose();
    }
  },

  /* EDIT COMMANDS */

  /** Copy the currently marked nodes. */
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
      final Nodes nodes = gui.context.marked();
      BaseXLayout.enable(button, !Prop.mainmem && nodes != null &&
          nodes.size() != 0 && (nodes.size() != 1 || nodes.nodes[0] != 0));
    }
  },

  /** Copy the current path. */
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

  /** Paste the copied nodes. */
  PASTE(true, GUIPASTE, "", GUIPASTETT) {
    @Override
    public void execute(final GUI gui) {
      gui.execute(new Copy("0"));
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final Context context = gui.context;
      // disallow copy of empty node set or root node
      final Nodes nodes = context.marked();
      boolean s = !Prop.mainmem && context.copied() != null && nodes != null &&
        nodes.size() != 0 && (nodes.size() != 1 || nodes.nodes[0] != 0) &&
        nodes.data.ns.size() == 0;
      if(s) {
        final Data d = nodes.data;
        for(final int n : nodes.nodes) {
          if(d.kind(n) != Data.ELEM) {
            s = false;
            break;
          }
        }
      }
      BaseXLayout.enable(button, s);
    }
  },

  /** Delete the currently marked nodes. */
  DELETE(true, GUIDELETE, "", GUIDELETETT) {
    @Override
    public void execute(final GUI gui) {
      if(Dialog.confirm(gui, DELETECONF)) gui.execute(new Delete());
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      // disallow deletion of empty node set or root node
      final Nodes n = gui.context.marked();
      BaseXLayout.enable(button, !Prop.mainmem && n != null && n.size() != 0 &&
          n.data.ns.size() == 0);
    }
  },

  /** Insert new nodes. */
  INSERT(true, GUIINSERT, "", GUIINSERTTT) {
    @Override
    public void execute(final GUI gui) {
      final DialogInsert insert = new DialogInsert(gui);
      if(insert.result == null) return;
      final CmdUpdate type = CmdUpdate.values()[insert.kind];
      gui.execute(new Insert(type, insert.result));
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final Context context = gui.context;
      final Nodes nodes = context.marked();
      final Data d = context.data();
      BaseXLayout.enable(button, !Prop.mainmem && nodes != null &&
        nodes.size() == 1 && (d.kind(nodes.nodes[0]) == Data.ELEM ||
        d.kind(nodes.nodes[0]) == Data.DOC) && nodes.data.ns.size() == 0);
    }
  },

  /** Copy the currently marked nodes. */
  EDIT(true, GUIEDIT, "", GUIEDITTT) {
    @Override
    public void execute(final GUI gui) {
      final Nodes nodes = gui.context.marked();
      final DialogEdit edit = new DialogEdit(gui, nodes.nodes[0]);
      if(edit.result == null) return;
      final CmdUpdate type = CmdUpdate.values()[edit.kind];
      gui.execute(new Update(type, edit.result));
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final Context context = gui.context;
      final Nodes nodes = context.marked();
      BaseXLayout.enable(button, !Prop.mainmem && nodes != null &&
        nodes.size() == 1 && context.data().kind(nodes.nodes[0]) != Data.DOC &&
        nodes.data.ns.size() == 0);
    }
  },

  /** Filter currently marked nodes. */
  FILTER(true, GUIFILTER, null, GUIFILTERTT) {
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
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      final Nodes marked = gui.context.marked();
      BaseXLayout.enable(button, marked != null && marked.size() != 0);
    }
  },

  /** Show search. */
  SHOWSEARCH(true, GUISHOWSEARCH, "% F", GUISHOWSEARCHTT) {
    @Override
    public void execute(final GUI gui) {
      GUIProp.showquery ^= true;
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      BaseXLayout.select(button, GUIProp.showquery);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show info. */
  SHOWINFO(true, GUISHOWINFO, "% G", GUISHOWINFOTT) {
    @Override
    public void execute(final GUI gui) {
      GUIProp.showinfo ^= true;
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      BaseXLayout.select(button, GUIProp.showinfo);
    }

    @Override
    public boolean checked() { return true; }
  },


  /* VIEW MENU */

  /** Show menu. */
  SHOWMENU(false, GUISHOWMENU, null, GUISHOWMENUTT) {
    @Override
    public void execute(final GUI gui) {
      GUIProp.showmenu ^= true;
      gui.updateControl(gui.menu, GUIProp.showmenu, BorderLayout.NORTH);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      BaseXLayout.select(button, GUIProp.showmenu);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show buttons. */
  SHOWBUTTONS(false, GUISHOWBUTTONS, null, GUISHOWBUTTONSTT) {
    @Override
    public void execute(final GUI gui) {
      GUIProp.showbuttons ^= true;
      gui.updateControl(gui.buttons, GUIProp.showbuttons, BorderLayout.CENTER);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      BaseXLayout.select(button, GUIProp.showbuttons);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show Input Field. */
  SHOWINPUT(false, GUISHOWINPUT, null, GUISHOWINPUTTT) {
    @Override
    public void execute(final GUI gui) {
      GUIProp.showinput ^= true;
      gui.updateControl(gui.nav, GUIProp.showinput, BorderLayout.SOUTH);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      BaseXLayout.select(button, GUIProp.showinput);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show Status Bar. */
  SHOWSTATUS(false, GUISHOWSTATUS, null, GUISHOWSTATUSTT) {
    @Override
    public void execute(final GUI gui) {
      GUIProp.showstatus ^= true;
      gui.updateControl(gui.status, GUIProp.showstatus, BorderLayout.SOUTH);
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      BaseXLayout.select(button, GUIProp.showstatus);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show Text View. */
  SHOWTEXT(false, GUISHOWTEXT, "% 1", GUISHOWTEXTTT) {
    @Override
    public void execute(final GUI gui) {
      if(!gui.context.db()) GUIProp.showstarttext ^= true;
      else GUIProp.showtext ^= true;
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      BaseXLayout.select(button, gui.context.db() ? GUIProp.showtext :
        GUIProp.showstarttext);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show map. */
  SHOWMAP(true, GUISHOWMAP, "% 2", GUISHOWMAPTT) {
    @Override
    public void execute(final GUI gui) {
      GUIProp.showmap ^= true;
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      BaseXLayout.select(button, GUIProp.showmap);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show Tree View. */
  SHOWFOLDER(true, GUISHOWFOLDER, "% 3", GUISHOWFOLDERTT) {
    @Override
    public void execute(final GUI gui) {
      GUIProp.showfolder ^= true;
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      BaseXLayout.select(button, GUIProp.showfolder);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show Table View. */
  SHOWTABLE(true, GUISHOWTABLE, "% 4", GUISHOWTABLETT) {
    @Override
    public void execute(final GUI gui) {
      GUIProp.showtable ^= true;
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      BaseXLayout.select(button, GUIProp.showtable);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show Plot View. */
  SHOWPLOT(true, GUISHOWPLOT, "% 5", GUISHOWPLOTTT) {
    @Override
    public void execute(final GUI gui) {
      GUIProp.showplot ^= true;
      gui.layoutViews();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      BaseXLayout.select(button, GUIProp.showplot);
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
      BaseXLayout.select(button, GUIProp.fullscreen);
    }

    @Override
    public boolean checked() { return true; }
  },

  /* OPTION MENU */

  /** Realtime filtering on/off. */
  RTEXEC(true, GUIRTEXEC, null, GUIRTEXECTT) {
    @Override
    public void execute(final GUI gui) {
      GUIProp.execrt ^= true;
      gui.refreshControls();
      gui.notify.layout();
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      BaseXLayout.select(button, GUIProp.execrt);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Realtime filtering on/off. */
  RTFILTER(true, GUIRTFILTER, null, GUIRTFILTERTT) {
    @Override
    public void execute(final GUI gui) {
      GUIProp.filterrt ^= true;
      gui.refreshControls();
      gui.notify.layout();

      final Context context = gui.context;
      final boolean root = context.root();

      if(!GUIProp.filterrt) {
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
      BaseXLayout.select(button, GUIProp.filterrt);
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

  /** Change fonts. */
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

  /** Database path. */
  PREFS(false, GUIPREFS, "% P", GUIPREFSTT) {
    @Override
    public void execute(final GUI gui) {
      new DialogPrefs(gui);
    }
  },

  /* HELP MENU */

  /** Show Help. */
  SHOWHELP(false, GUISHOWHELP, "F1", GUISHOWHELPTT) {
    @Override
    public void execute(final GUI gui) {
      if(!GUIProp.showhelp) {
        GUIProp.showhelp = true;
        gui.help = new DialogHelp(gui);
        gui.refreshControls();
      } else {
        gui.help.close();
      }
    }

    @Override
    public void refresh(final GUI gui, final AbstractButton button) {
      super.refresh(gui, button);
      BaseXLayout.select(button, GUIProp.showhelp);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show Database info. */
  INFO(true, GUIINFO, "% I", GUIINFOTT) {
    @Override
    public void execute(final GUI gui) {
      final DialogInfo info = new DialogInfo(gui);
      if(info.ok()) {
        final MetaData meta = gui.context.data().meta;
        final boolean[] indexes = info.indexes();
        if(info.opt) {
          meta.txtindex = indexes[0];
          meta.atvindex = indexes[1];
          meta.ftxindex = indexes[2];
          progress(gui, INFOOPT, new Process[] { new Optimize() });
        } else {
          Process[] proc = new Process[0];
          if(indexes[0] != meta.txtindex)
            proc = Array.add(proc, cmd(indexes[0], CmdIndex.TEXT));
          if(indexes[1] != meta.atvindex)
            proc = Array.add(proc, cmd(indexes[1], CmdIndex.ATTRIBUTE));
          if(indexes[2] != meta.ftxindex)
            proc = Array.add(proc, cmd(indexes[2], CmdIndex.FULLTEXT));

          if(proc.length != 0) progress(gui, INFOBUILD, proc);
        }
      }
    }

    /**
     * Builds the create command.
     * @param create create flag
     * @param index name of index
     * @return process
     */
    private Process cmd(final boolean create, final CmdIndex index) {
      return create ? new CreateIndex(index) : new DropIndex(index);
    }
  },

  /** Show about information. */
  ABOUT(false, GUIABOUT, null, GUIABOUTTT) {
    @Override
    public void execute(final GUI gui) {
      new DialogAbout(gui);
    }
  },

  /* BROWSE COMMANDS */

  /** Go one step back. */
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
      button.setToolTipText(en && tt.length() == 0 ? GUIGOBACKTT : tt);
    }
  },

  /** Go one step forward. */
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
      button.setToolTipText(en && tt.length() == 0 ? GUIGOFORWARDTT : tt);
    }
  },

  /** Go one level up. */
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

  /** Go to root node. */
  ROOT(true, GUIROOT, "alt HOME", GUIROOTTT) {
    @Override
    public void execute(final GUI gui) {
      gui.execute(new Cs("/"));
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
    BaseXLayout.enable(button, !data || gui.context.db());
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
            gui.status.setText(BaseX.info(PROCTIME, perf.getTimer()));
            if(op) Dialog.info(gui, INFOOPTIM);
          } else {
            Dialog.error(gui, p.info());
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
   * @return io reference
   */
  static IO save(final GUI gui) {
    // open file chooser for XML creation
    final BaseXFileChooser fc = new BaseXFileChooser(EXPORTTITLE,
        GUIProp.createpath, gui);
    fc.addFilter(CREATEXMLDESC, IO.XMLSUFFIX);

    final boolean single = gui.context.data().doc().length == 1;
    final IO file = fc.select(single ? BaseXFileChooser.Mode.FSAVE :
      BaseXFileChooser.Mode.DSAVE);
    if(file == null) return null;

    GUIProp.createpath = file.path();
    if(single) file.suffix(IO.XMLSUFFIX);
    return file;
  }
}
