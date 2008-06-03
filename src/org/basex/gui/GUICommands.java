package org.basex.gui;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.io.IOException;
import javax.swing.AbstractButton;
import javax.swing.JOptionPane;
import org.basex.BaseX;
import org.basex.core.Command;
import org.basex.core.CommandParser;
import org.basex.core.Commands;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.Create;
import org.basex.core.proc.Proc;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.data.Nodes;
import org.basex.gui.dialog.DialogAbout;
import org.basex.gui.dialog.DialogColors;
import org.basex.gui.dialog.DialogCreate;
import org.basex.gui.dialog.DialogEdit;
import org.basex.gui.dialog.DialogFontChooser;
import org.basex.gui.dialog.DialogImportFS;
import org.basex.gui.dialog.DialogInfo;
import org.basex.gui.dialog.DialogInsert;
import org.basex.gui.dialog.DialogMapLayout;
import org.basex.gui.dialog.DialogOpen;
import org.basex.gui.dialog.DialogPrefs;
import org.basex.gui.dialog.DialogProgress;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.View;
import org.basex.io.IO;
import org.basex.util.Performance;

/**
 * This enumeration encapsulates all commands that are triggered by
 * GUI operations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public enum GUICommands implements GUICommand {

  /* FILE MENU */

  /** Create database. */
  CREATE(false, GUICREATE, "ctrl N", GUICREATETT) {
    @Override
    public void execute() {
      // open file chooser for XML creation
      final GUI main = GUI.get();

      final BaseXFileChooser fc = new BaseXFileChooser(CREATETITLE,
          GUIProp.createpath, main);
      fc.addFilter(IO.GZSUFFIX, CREATEGZDESC);
      fc.addFilter(IO.ZIPSUFFIX, CREATEZIPDESC);
      fc.addFilter(IO.XMLSUFFIX, CREATEXMLDESC);

      if(fc.select(BaseXFileChooser.MODE.OPEN)) {
        if(!new DialogCreate(main).ok()) return;
        final IO file = fc.getFile();
        build(CREATETITLE, Commands.CREATEXML + " \"" + file.path() + "\"");
      }
      GUIProp.createpath = fc.getDir();
    }
  },

  /** Open database. */
  OPEN(false, GUIOPEN, "ctrl O", GUIOPENTT) {
    @Override
    public void execute() {
      final GUI main = GUI.get();
      final DialogOpen dialog = new DialogOpen(main, false);
      if(dialog.ok()) {
        final String db = dialog.db();
        if(db == null) return;
        if(IO.dbpath(db).exists()) {
          Proc.execute(GUI.context, Commands.CLOSE);
          View.notifyInit();
        }
        main.execute(Commands.OPEN, db);
      } else if(dialog.nodb()) {
        if(JOptionPane.showConfirmDialog(GUI.get(), NODBQUESTION, DIALOGINFO,
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
          CREATE.execute();
      }
    }
  },

  /** Drop database. */
  DROP(false, GUIDROP, null, GUIDROPTT) {
    @Override
    public void execute() {
      if(new DialogOpen(GUI.get(), true).nodb()) {
        JOptionPane.showMessageDialog(GUI.get(), OPENNODBINFO, DIALOGINFO,
            JOptionPane.INFORMATION_MESSAGE);
      }
    }
  },

  /** Reset database. */
  CLOSE(true, GUICLOSE, "ctrl F4", GUICLOSETT) {
    @Override
    public void execute() {
      GUI.get().execute(Commands.CLOSE);
    }
  },

  /** Open XQuery. */
  XQOPEN(true, GUIXQOPEN, "ctrl R", GUIXQOPENTT) {
    @Override
    public void execute() {
      // open file chooser for XML creation
      final GUI main = GUI.get();

      final BaseXFileChooser fc = new BaseXFileChooser(XQOPENTITLE,
          GUIProp.createpath, main);
      fc.addFilter(IO.XQSUFFIX, CREATEXQDESC);

      if(fc.select(BaseXFileChooser.MODE.OPEN)) {
        try {
          final IO file = fc.getFile();
          main.query.setXQuery(file.content(), file.path());
        } catch(final IOException ex) {
          JOptionPane.showMessageDialog(main, XQOPERROR,
              DIALOGINFO, JOptionPane.ERROR_MESSAGE);
        }
      }
      GUIProp.createpath = fc.getDir();
    }
  },

  /** Save XQuery. */
  XQSAVE(true, GUIXQSAVE, "ctrl S", GUIXQSAVETT) {
    @Override
    public void execute() {
      // open file chooser for XML creation
      final GUI main = GUI.get();

      final String fn = main.query.getFilename();
      final BaseXFileChooser fc = new BaseXFileChooser(XQSAVETITLE,
          fn == null ? GUIProp.createpath : fn, main);
      fc.addFilter(IO.XQSUFFIX, CREATEXQDESC);

      if(fc.select(BaseXFileChooser.MODE.SAVE)) {
        try {
          final IO file = fc.getFile();
          file.suffix(IO.XQSUFFIX);
          file.write(main.query.getXQuery());
        } catch(final IOException ex) {
          JOptionPane.showMessageDialog(main, XQSAVERROR,
              DIALOGINFO, JOptionPane.ERROR_MESSAGE);
        }
      }
      GUIProp.createpath = fc.getDir();
    }
  },

  /** Import filesystem. */
  IMPORTFS(false, GUIIMPORTFS, null, GUIIMPORTFSTT) {
    @Override
    public void execute() {
      final GUI main = GUI.get();
      if(!new DialogImportFS(main).ok()) return;
      final String p = GUIProp.fsall ? "/" : GUIProp.fspath.replace('\\', '/');
      final String name = GUIProp.importfsname;
      build(IMPORTFSTITLE, Commands.CREATEFS + " " + name + " \"" + p + "\"");
    }
  },

  /** Export document. */
  EXPORT(true, GUIEXPORT, "ctrl E", GUIEXPORTTT) {
    @Override
    public void execute() {
      // open file chooser for XML creation
      final GUI main = GUI.get();

      final BaseXFileChooser fc = new BaseXFileChooser(CREATETITLE,
          GUIProp.createpath, main);
      fc.addFilter(IO.XMLSUFFIX, CREATEXMLDESC);

      if(fc.select(BaseXFileChooser.MODE.SAVE)) {
        final IO file = fc.getFile();
        file.suffix(IO.XMLSUFFIX);
        try {
          file.write(main.text.getText());
        } catch(final IOException ex) {
          JOptionPane.showMessageDialog(main, XQSAVERROR,
              DIALOGINFO, JOptionPane.ERROR_MESSAGE);
        }
        //main.execute(Commands.EXPORT, "\"" + file + "\"");
      }
      GUIProp.createpath = fc.getDir();
    }
  },

  /** Exit BaseX. */
  EXIT(false, GUIEXIT, null, GUIEXITTT) {
    @Override
    public void execute() {
      GUI.get().quit();
    }
  },


  /* EDIT COMMANDS */

  /** Copy the currently marked nodes. */
  COPY(true, GUICOPY, "ctrl C", GUICOPYTT) {
    @Override
    public void execute() {
      final Context context = GUI.context;
      context.copy(context.marked().copy());
    }

    @Override
    public void refresh(final AbstractButton button) {
      // disallow copy of empty node set or root node
      final Nodes nodes = GUI.context.marked();
      BaseXLayout.enable(button, !Prop.mainmem && nodes != null &&
          nodes.size != 0 && (nodes.size != 1 || nodes.pre[0] != 0));
    }
  },

  /** Paste the copied nodes. */
  PASTE(true, GUIPASTE, "ctrl V", GUIPASTETT) {
    @Override
    public void execute() {
      GUI.get().execute(Commands.COPY, "0");
    }

    @Override
    public void refresh(final AbstractButton button) {
      final Context context = GUI.context;
      // disallow copy of empty node set or root node
      final Nodes nodes = context.marked();
      boolean s = !Prop.mainmem && context.copied() != null && nodes != null &&
        nodes.size != 0 && (nodes.size != 1 || nodes.pre[0] != 0);
      if(s) {
        final Data d = nodes.data;
        for(int n = 0; n < nodes.size; n++) {
          if(d.kind(nodes.pre[n]) != Data.ELEM) {
            s = false;
            break;
          }
        }
      }
      BaseXLayout.enable(button, s);
    }
  },

  /** Delete the currently marked nodes. */
  DELETE(true, GUIDELETE, "DELETE", GUIDELETETT) {
    @Override
    public void execute() {
      if(JOptionPane.showConfirmDialog(GUI.get(), DELETECONF, DELETETITLE,
          JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        GUI.get().execute(Commands.DELETE);
    }

    @Override
    public void refresh(final AbstractButton button) {
      // disallow deletion of empty node set or root node
      final Context context = GUI.context;
      final Nodes nodes = context.marked();
      BaseXLayout.enable(button, !Prop.mainmem && nodes != null &&
          nodes.size != 0 && (nodes.size != 1 || nodes.pre[0] != 0));
    }
  },

  /** Insert new nodes. */
  INSERT(true, GUIINSERT, "F7", GUIINSERTTT) {
    @Override
    public void execute() {
      final DialogInsert insert = new DialogInsert(GUI.get());
      if(insert.query == null) return;
      GUI.get().execute(Commands.INSERT, insert.query);
    }

    @Override
    public void refresh(final AbstractButton button) {
      final Context context = GUI.context;
      final Nodes nodes = context.marked();
      final Data d = context.data();
      BaseXLayout.enable(button, !Prop.mainmem && nodes != null &&
          nodes.size == 1 && (d.kind(nodes.pre[0]) == Data.ELEM ||
              d.kind(nodes.pre[0]) == Data.DOC));
    }
  },

  /** Copy the currently marked nodes. */
  EDIT(true, GUIEDIT, "F2", GUIEDITTT) {
    @Override
    public void execute() {
      final Nodes nodes = GUI.context.marked();
      final DialogEdit edit = new DialogEdit(GUI.get(), nodes.pre[0]);
      if(edit.query == null) return;
      GUI.get().execute(Commands.UPDATE, edit.query);
    }

    @Override
    public void refresh(final AbstractButton button) {
      final Context context = GUI.context;
      final Nodes nodes = context.marked();
      BaseXLayout.enable(button, !Prop.mainmem && nodes != null &&
          nodes.size == 1 && context.data().kind(nodes.pre[0]) != Data.DOC);
    }
  },

  /** Select current nodes. */
  SELECT(true, GUISELECT, "ctrl A", GUISELECTTT) {
    @Override
    public void execute() {
      View.notifyMark(GUI.context.current().copy());
    }

    @Override
    public void refresh(final AbstractButton button) {
      BaseXLayout.enable(button, GUI.context.current() != null);
    }
  },

  /** Filter currently marked nodes. */
  FILTER(true, GUIFILTER, null, GUIFILTERTT) {
    @Override
    public void execute() {
      final Context context = GUI.context;
      Nodes marked = context.marked();
      if(marked.size == 0) {
        final int pre = View.focused;
        if(pre == -1) return;
        marked = new Nodes(pre, context.data());
      }
      View.notifyContext(marked, false);
    }

    @Override
    public void refresh(final AbstractButton button) {
      final Nodes marked = GUI.context.marked();
      BaseXLayout.enable(button, marked != null && marked.size != 0);
    }
  },

  /** Show search. */
  SHOWSEARCH(true, GUISHOWSEARCH, "ctrl F", GUISHOWSEARCHTT) {
    @Override
    public void execute() {
      GUIProp.showquery ^= true;
      GUI.get().layoutViews();
    }

    @Override
    public void refresh(final AbstractButton button) {
      super.refresh(button);
      BaseXLayout.select(button, GUIProp.showquery);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show info. */
  SHOWINFO(true, GUISHOWINFO, "ctrl G", GUISHOWINFOTT) {
    @Override
    public void execute() {
      GUIProp.showinfo ^= true;
      Prop.allInfo = GUIProp.showinfo;
      Prop.info = GUIProp.showinfo;
      GUI.get().layoutViews();
    }

    @Override
    public void refresh(final AbstractButton button) {
      super.refresh(button);
      BaseXLayout.select(button, GUIProp.showinfo);
    }

    @Override
    public boolean checked() { return true; }
  },


  /* VIEW MENU */

  /** Show menu. */
  SHOWMENU(false, GUISHOWMENU, null, GUISHOWMENUTT) {
    @Override
    public void execute() {
      GUIProp.showmenu ^= true;
      final GUI main = GUI.get();
      main.updateControl(main.menu, GUIProp.showmenu, BorderLayout.NORTH);
    }

    @Override
    public void refresh(final AbstractButton button) {
      super.refresh(button);
      BaseXLayout.select(button, GUIProp.showmenu);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show buttons. */
  SHOWBUTTONS(false, GUISHOWBUTTONS, null, GUISHOWBUTTONSTT) {
    @Override
    public void execute() {
      GUIProp.showbuttons ^= true;
      final GUI main = GUI.get();
      main.updateControl(main.buttons, GUIProp.showbuttons,
          BorderLayout.CENTER);
    }

    @Override
    public void refresh(final AbstractButton button) {
      super.refresh(button);
      BaseXLayout.select(button, GUIProp.showbuttons);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show Input Field. */
  SHOWINPUT(false, GUISHOWINPUT, null, GUISHOWINPUTTT) {
    @Override
    public void execute() {
      GUIProp.showinput ^= true;
      final GUI main = GUI.get();
      main.updateControl(main.nav, GUIProp.showinput, BorderLayout.SOUTH);
    }

    @Override
    public void refresh(final AbstractButton button) {
      super.refresh(button);
      BaseXLayout.select(button, GUIProp.showinput);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show Status Bar. */
  SHOWSTATUS(false, GUISHOWSTATUS, null, GUISHOWSTATUSTT) {
    @Override
    public void execute() {
      GUIProp.showstatus ^= true;
      final GUI main = GUI.get();
      main.updateControl(main.status, GUIProp.showstatus,
          BorderLayout.SOUTH);
    }

    @Override
    public void refresh(final AbstractButton button) {
      super.refresh(button);
      BaseXLayout.select(button, GUIProp.showstatus);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show Text View. */
  SHOWTEXT(false, GUISHOWTEXT, "ctrl 1", GUISHOWTEXTTT) {
    @Override
    public void execute() {
      if(!GUI.context.db()) GUIProp.showstarttext ^= true;
      else GUIProp.showtext ^= true;
      GUI.get().layoutViews();
    }

    @Override
    public void refresh(final AbstractButton button) {
      super.refresh(button);
      BaseXLayout.select(button, GUI.context.db() ? GUIProp.showtext :
        GUIProp.showstarttext);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show map. */
  SHOWMAP(true, GUISHOWMAP, "ctrl 2", GUISHOWMAPTT) {
    @Override
    public void execute() {
      GUIProp.showmap ^= true;
      GUI.get().status.setPerformance("");
      GUI.get().layoutViews();
    }

    @Override
    public void refresh(final AbstractButton button) {
      super.refresh(button);
      BaseXLayout.select(button, GUIProp.showmap);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show Tree View. */
  SHOWTREE(true, GUISHOWTREE, "ctrl 3", GUISHOWTREETT) {
    @Override
    public void execute() {
      GUIProp.showtree ^= true;
      GUI.get().layoutViews();
    }

    @Override
    public void refresh(final AbstractButton button) {
      super.refresh(button);
      BaseXLayout.select(button, GUIProp.showtree);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show Table View. */
  SHOWTABLE(true, GUISHOWTABLE, "ctrl 4", GUISHOWTABLETT) {
    @Override
    public void execute() {
      GUIProp.showtable ^= true;
      GUI.get().layoutViews();
    }

    @Override
    public void refresh(final AbstractButton button) {
      super.refresh(button);
      BaseXLayout.select(button, GUIProp.showtable);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Fullscreen mode. */
  FULL(false, GUIFULL, "F11", GUIFULLTT) {
    @Override
    public void execute() {
      GUI.get().fullscreen();
    }

    @Override
    public void refresh(final AbstractButton button) {
      super.refresh(button);
      BaseXLayout.select(button, GUIProp.fullscreen);
    }

    @Override
    public boolean checked() { return true; }
  },

  /* OPTION MENU */

  /** Realtime filtering on/off. */
  INPUTMODE(true, GUIINPUTMODE, null, GUIINPUTMODETT) {
    @Override
    public void execute() {
      //GUIProp.searchmode = ++GUIProp.searchmode % 3;
      GUI.get().refreshControls();
    }
  },

  /** Realtime filtering on/off. */
  RTEXEC(true, GUIRTEXEC, null, GUIRTEXECTT) {
    @Override
    public void execute() {
      final GUI gui = GUI.get();

      GUIProp.execrt ^= true;
      gui.refreshControls();
      View.notifyLayout();
    }

    @Override
    public void refresh(final AbstractButton button) {
      super.refresh(button);
      BaseXLayout.select(button, GUIProp.execrt);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Realtime filtering on/off. */
  RTFILTER(true, GUIRTFILTER, null, GUIRTFILTERTT) {
    @Override
    public void execute() {
      final GUI gui = GUI.get();

      GUIProp.filterrt ^= true;
      gui.refreshControls();
      View.notifyLayout();

      final Context context = GUI.context;
      final Nodes curr = context.current();
      final boolean root = curr.size == 1 && curr.pre[0] == 0;

      if(!GUIProp.filterrt) {
        if(!root) {
          View.notifyContext(new Nodes(0, context.data()), true);
          View.notifyMark(curr);
        }
      } else {
        if(root) {
          View.notifyMark(new Nodes(context.data()));
        } else {
          final Nodes mark = context.marked();
          context.marked(new Nodes(context.data()));
          View.notifyContext(mark, true);
        }
      }
    }

    @Override
    public void refresh(final AbstractButton button) {
      super.refresh(button);
      BaseXLayout.select(button, GUIProp.filterrt);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Color schema. */
  COLOR(false, GUICOLOR, null, GUICOLORTT) {
    @Override
    public void execute() {
      DialogColors.get(GUI.get()).setVisible(true);
    }
  },

  /** Change fonts. */
  FONTS(false, GUIFONTS, null, GUIFONTSTT) {
    @Override
    public void execute() {
      DialogFontChooser.get(GUI.get()).setVisible(true);
    }
  },

  /** Map layout. */
  MAPLAYOUT(true, GUIMAPLAYOUT, "ctrl L", GUIMAPLAYOUTTT) {
    @Override
    public void execute() {
      DialogMapLayout.get(GUI.get()).setVisible(true);
    }
  },

  /** Database path. */
  PREFS(false, GUIPREFS, "ctrl P", GUIPREFSTT) {
    @Override
    public void execute() {
      new DialogPrefs(GUI.get());
    }
  },

  /* HELP MENU */

  /** Show Help. */
  SHOWHELP(false, GUISHOWHELP, "F1", GUISHOWHELPTT) {
    @Override
    public void execute() {
      if(!GUI.context.db()) GUIProp.showstarthelp ^= true;
      else GUIProp.showhelp ^= true;
      GUI.get().layoutViews();
    }

    @Override
    public void refresh(final AbstractButton button) {
      super.refresh(button);
      BaseXLayout.select(button, GUI.context.db() ? GUIProp.showhelp :
        GUIProp.showstarthelp);
    }

    @Override
    public boolean checked() { return true; }
  },

  /** Show Database info. */
  INFO(true, GUIINFO, "ctrl I", GUIINFOTT) {
    @Override
    public void execute() {
      final DialogInfo info = new DialogInfo(GUI.get());
      if(info.ok()) {
        final MetaData meta = GUI.context.data().meta;
        final boolean[] indexes = info.indexes();
        final StringBuilder sb = new StringBuilder();
        meta.fzindex = indexes[3];
        if(indexes[0] != meta.txtindex) cmd(indexes[0], Create.TXT, sb);
        if(indexes[1] != meta.atvindex) cmd(indexes[1], Create.ATV, sb);
        if(indexes[2] != meta.ftxindex) cmd(indexes[2], Create.FTX, sb);
        build(INFOBUILD, sb.toString());
      }
    }

    /**
     * Builds the create command.
     * @param create create flag
     * @param index name of index
     * @param sb string builder
     */
    private void cmd(final boolean create, final String index,
        final StringBuilder sb) {
      final Commands cmd = create ? Commands.CREATEINDEX : Commands.DROPINDEX;
      if(sb.length() != 0) sb.append(";");
      sb.append(cmd + " " + index);
    }
  },

  /** Show about information. */
  ABOUT(false, GUIABOUT, null, GUIABOUTTT) {
    @Override
    public void execute() {
      new DialogAbout(GUI.get());
    }
  },

  /* BROWSE COMMANDS */

  /** Go one step back. */
  GOBACK(true, GUIGOBACK, "alt LEFT", GUIGOBACKTT) {
    @Override
    public void execute() {
      View.notifyHist(false);
    }

    @Override
    public void refresh(final AbstractButton button) {
      final int h = View.hist;
      final boolean enabled = h > 0;
      BaseXLayout.enable(button, enabled);
      final String tt = enabled ? View.QUERYHIST[h - 1] : null;
      button.setToolTipText(enabled && tt.equals("") ? GUIGOBACKTT : tt);
    }
  },

  /** Go one step forward. */
  GOFORWARD(true, GUIGOFORWARD, "alt RIGHT", GUIGOFORWARDTT) {
    @Override
    public void execute() {
      View.notifyHist(true);
    }

    @Override
    public void refresh(final AbstractButton button) {
      final int h = View.hist;
      final boolean enabled = h < View.maxhist;
      BaseXLayout.enable(button, enabled);
      final String tt = enabled ? View.QUERYHIST[h + 1] : null;
      button.setToolTipText(enabled && tt.equals("") ? GUIGOFORWARDTT : tt);
    }
  },

  /** Go one level up. */
  GOUP(true, GUIGOUP, "alt UP", GUIGOUPTT) {
    @Override
    public void execute() {
      GUI.get().execute(Commands.CD, "..");
    }

    @Override
    public void refresh(final AbstractButton button) {
      final Nodes current = GUI.context.current();
      final boolean enabled = current != null && (current.size != 1 ||
          current.pre[0] != 0);
      BaseXLayout.enable(button, enabled);
    }
  },

  /** Go to root node. */
  ROOT(true, GUIROOT, "alt HOME", GUIROOTTT) {
    @Override
    public void execute() {
      GUI.get().execute(Commands.CD, "/");
    }

    @Override
    public void refresh(final AbstractButton button) {
      final Nodes current = GUI.context.current();
      final boolean enabled = current != null && (current.size != 1 ||
          current.pre[0] != 0);
      BaseXLayout.enable(button, enabled);
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
   * @param e string entry
   * @param k key shortcut
   * @param h help string
   */
  GUICommands(final boolean d, final String e, final String k, final String h) {
    data = d;
    entry = e;
    key = k;
    help = h;
  }

  /** {@inheritDoc} */
  public abstract void execute();

  /** {@inheritDoc} */
  public void refresh(final AbstractButton button) {
    BaseXLayout.enable(button, !data || GUI.context.db());
  }

  /** {@inheritDoc} */
  public boolean checked() { return false; }

  /** {@inheritDoc} */
  public String help() { return help; }

  /** {@inheritDoc} */
  public String desc() { return entry; }

  /** {@inheritDoc} */
  public String key() { return key; }

  // =========================================================================

  /**
   * Runs a building progress.
   * @param title dialog title
   * @param command command string
   */
  static void build(final String title, final String command) {
    // start database creation thread
    new Thread() {
      @Override
      public void run() {
        final GUI main = GUI.get();

        final CommandParser cp = new CommandParser(command);
        while(cp.more()) {
          final Command cmd = cp.next();
          final Commands cc = cmd.name;
          final Proc proc = cmd.proc(GUI.context);

          if(cc != Commands.CREATEINDEX && cc != Commands.DROPINDEX)
            main.execute(Commands.CLOSE);

          Performance.sleep(100);
          final DialogProgress wait = cc == Commands.DROPINDEX ? null :
              new DialogProgress(main, title, cc != Commands.CREATEFS);

          // start dialog window thread
          if(wait != null) {
            new Thread() {
              @Override
              public void run() {
                while(true) {
                  Performance.sleep(100);
                  if(wait.stopped()) proc.stop();
                  if(!wait.isVisible()) return;
                  wait.setProgress(proc);
                }
              }
            }.start();
          }

          // create database
          final Performance perf = new Performance();
          final boolean ok = proc.execute();
          // get server info
          final String inf = proc.info();

          // close status dialog
          if(wait != null) wait.dispose();

          // return user information
          if(ok) {
            main.status.setText(BaseX.info(PROCTIME, perf.getTimer()));
          } else {
            JOptionPane.showMessageDialog(main, inf,
                DIALOGINFO, JOptionPane.WARNING_MESSAGE);
          }
          // initialize views
          if(cc != Commands.DROPINDEX && cc != Commands.CREATEINDEX)
            View.notifyInit();
        }
      }
    }.start();
  }
}
