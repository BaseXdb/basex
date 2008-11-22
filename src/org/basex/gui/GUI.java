package org.basex.gui;

import static org.basex.Text.*;
import static org.basex.gui.GUIConstants.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.HashMap;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import org.basex.BaseX;
import org.basex.Text;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Find;
import org.basex.core.proc.XPath;
import org.basex.core.proc.XQuery;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewContainer;
import org.basex.gui.view.ViewPanel;
import org.basex.gui.view.info.InfoView;
import org.basex.gui.view.map.MapView;
import org.basex.gui.view.plot.PlotView;
import org.basex.gui.view.query.QueryView;
import org.basex.gui.view.real.RealView;
import org.basex.gui.view.table.TableView;
import org.basex.gui.view.text.TextView;
import org.basex.gui.view.tree.TreeView;
import org.basex.io.CachedOutput;
import org.basex.query.QueryException;
import org.basex.util.Action;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class is the main window of the GUI. It is the central instance
 * for user interactions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class GUI extends JFrame {
  /** Database Context. */
  public static Context context = new Context();
  /** Singleton Instance. */
  private static GUI instance;

  /** Status line. */
  public final GUIStatus status;
  /** Content panel, containing all views. */
  public final ViewContainer views;
  /** Input field. */
  public final GUIInput input;
  /** Filter button. */
  public final BaseXButton filter;
  /** History button. */
  public final BaseXButton hist;

  /** Top panel. */
  final BaseXBack top;
  /** Result panel. */
  final GUIMenu menu;
  /** Button panel. */
  final BaseXBack buttons;
  /** Query panel. */
  final BaseXBack nav;

  /** Search view. */
  public final QueryView query;
  /** Text view. */
  public final TextView text;
  /** Info view. */
  public final InfoView info;
  /** Help view. */
  public final TextView help;

  /** Current input Mode. */
  final BaseXCombo mode;
  /** Execution Button. */
  final BaseXButton go;
  /** Fullscreen Window. */
  private JFrame fullscr;

  /** Control panel. */
  private final BaseXBack control;
  /** Results label. */
  private final BaseXLabel hits;
  /** Buttons. */
  private final GUIToolBar toolbar;
  /** Menu panel height. */
  private int menuHeight;

  /**
   * Singleton Constructor.
   * @return window reference
   */
  public static GUI get() {
    if(instance == null) instance = new GUI();
    return instance;
  }

  /**
   * Default Constructor.
   */
  private GUI() {
    setTitle(Text.TITLE);

    // set program icon
    setIconImage(image("icon"));

    // set window size
    final Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
    final int w = GUIProp.guisize[0];
    final int h = GUIProp.guisize[1];
    int x = Math.max(0, GUIProp.guiloc[0]);
    int y = Math.max(0, GUIProp.guiloc[1]);
    if(x > scr.width - w) x = Math.max(0, scr.width - w);
    if(y > scr.height - h) y = Math.max(0, scr.height - h);

    setBounds(x, y, w, h);
    if(GUIProp.maxstate) setExtendedState(MAXIMIZED_BOTH);

    top = new BaseXBack();
    top.setLayout(new BorderLayout());

    // add header
    control = new BaseXBack();
    control.setLayout(new BorderLayout());
    control.setBorder(0, 0, 0, 1);

    // add menu bar
    menu = new GUIMenu();
    if(GUIProp.showmenu) setJMenuBar(menu);

    final Font fnt = new Font(GUIProp.font, 1, 15);

    buttons = new BaseXBack();
    buttons.setLayout(new BorderLayout());
    toolbar = new GUIToolBar(TOOLBAR);
    buttons.add(toolbar, BorderLayout.WEST);

    hits = new BaseXLabel(" ");
    hits.setFont(fnt);
    BaseXLayout.setWidth(hits, 150);
    hits.setHorizontalAlignment(SwingConstants.RIGHT);

    BaseXBack b = new BaseXBack();
    b.add(hits);

    buttons.add(b, BorderLayout.EAST);
    if(GUIProp.showbuttons) control.add(buttons, BorderLayout.CENTER);

    nav = new BaseXBack();
    nav.setLayout(new BorderLayout(5, 0));
    nav.setBorder(2, 2, 0, 2);

    mode = new BaseXCombo(new String[] {
        BUTTONSEARCH, BUTTONXPATH, BUTTONCMD }, HELPMODE, false);
    mode.setSelectedIndex(2);

    mode.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final int s = mode.getSelectedIndex();
        if(s == GUIProp.searchmode || !mode.isEnabled()) return;

        GUIProp.searchmode = s;
        input.setText("");
        refreshControls();
      }
    });
    mode.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        checkKeys(e);
      }
    });
    nav.add(mode, BorderLayout.WEST);

    input = new GUIInput(this);

    hist = new BaseXButton(icon("cmd-hist"), HELPHIST);
    hist.trim();
    hist.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final JPopupMenu popup = new JPopupMenu("History");
        final ActionListener al = new ActionListener() {
          public void actionPerformed(final ActionEvent ac) {
            input.setText(ac.getActionCommand());
            input.requestFocusInWindow();
            popup.setVisible(false);
          }
        };
        final int i = !context.db() ? 2 : GUIProp.searchmode;
        final String[] hs = i == 0 ? GUIProp.search : i == 1 ?  GUIProp.xpath :
          GUIProp.commands;
        for(final String en : hs) {
          final JMenuItem jmi = new JMenuItem(en);
          jmi.addActionListener(al);
          popup.add(jmi);
        }
        popup.show(hist, 0, hist.getHeight());
      }
    });

    b = new BaseXBack();
    b.setLayout(new BorderLayout(5, 0));
    b.add(hist, BorderLayout.WEST);
    b.add(input, BorderLayout.CENTER);
    nav.add(b, BorderLayout.CENTER);

    go = new BaseXButton(icon("cmd-go"), HELPGO);
    go.trim();
    go.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        execute();
      }
    });
    go.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        checkKeys(e);
      }
    });

    filter = GUIToolBar.newButton(GUICommands.FILTER);
    filter.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        checkKeys(e);
      }
    });

    b = new BaseXBack();
    b.setLayout(new TableLayout(1, 3));
    b.add(go);
    b.add(Box.createHorizontalStrut(1));
    b.add(filter);
    nav.add(b, BorderLayout.EAST);

    if(GUIProp.showinput) control.add(nav, BorderLayout.SOUTH);
    top.add(control, BorderLayout.NORTH);

    // create views
    query = new QueryView(null);
    text = new TextView(Fill.DOWN, TEXTTIT, HELPTEXT);
    help = new TextView(Fill.DOWN, HELPTIT, null);
    info = new InfoView(HELPINFO);
    final ViewPanel textpanel = new ViewPanel(text, TEXTVIEW);
    final ViewPanel helppanel = new ViewPanel(help, HELPVIEW);

    // create panels for closed and opened database mode
    final ViewPanel[][] panels = { {
      textpanel, helppanel }, {
        new ViewPanel(new TreeView(HELPTREE), TREEVIEW),
        new ViewPanel(new RealView(), REALVIEW),
        new ViewPanel(new PlotView(null), PLOTVIEW),
        new ViewPanel(new TableView(HELPTABLE), TABLEVIEW),
        new ViewPanel(new MapView(HELPMAP), MAPVIEW),
        new ViewPanel(query, QUERYVIEW),
        new ViewPanel(info, INFOVIEW),
        helppanel,
        textpanel
      }
    };
    views = new ViewContainer(panels);
    views.setViews(false);

    top.add(views, BorderLayout.CENTER);
    setContentBorder();

    // add status bar
    status = new GUIStatus();
    if(GUIProp.showstatus) top.add(status, BorderLayout.SOUTH);

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent e) {
        quit();
      }
    });
    add(top);

    setVisible(true);
    views.updateViews();
    refreshControls();

    Prop.xqerrcode = false;
    Prop.chop = true;
    input.requestFocusInWindow();

    // start logo animation as thread
    new Action() {
      public void run() {
        views.run();
      }
    }.execute();
  }

  /**
   * Browse in views.
   * @param e key event
   */
  public void checkKeys(final KeyEvent e) {
    if(!e.isAltDown() || !context.db()) return;
    final int code = e.getKeyCode();

    // browse back/forward
    if(code == KeyEvent.VK_LEFT) {
      GUICommands.GOBACK.execute();
    } else if(code == KeyEvent.VK_RIGHT) {
      GUICommands.GOFORWARD.execute();
    } else if(code == KeyEvent.VK_UP) {
      GUICommands.GOUP.execute();
    } else if(code == KeyEvent.VK_HOME) {
      GUICommands.ROOT.execute();
    }
  }

  /**
   * Closes the database, writes the property files and quits.
   */
  void quit() {
    GUIProp.maxstate = getExtendedState() == MAXIMIZED_BOTH;
    if(!GUIProp.maxstate) {
      GUIProp.guiloc[0] = getX();
      GUIProp.guiloc[1] = getY();
      GUIProp.guisize[0] = getWidth();
      GUIProp.guisize[1] = getHeight();
    }
    query.quit();

    context.close();
    Prop.write();
    GUIProp.write();
    dispose();
  }

  /**
   * Sets a cursor.
   * @param c cursor to be set
   */
  public void cursor(final Cursor c) {
    cursor(c, false);
  }

  /**
   * Sets a cursor, forcing a new look if necessary.
   * @param c cursor to be set
   * @param force new cursor
   */
  public void cursor(final Cursor c, final boolean force) {
    final Cursor cc = getCursor();
    if(cc != c && (cc != CURSORWAIT || force)) setCursor(c);
  }

  /**
   * Queries the current input, depending on the current input mode.
   */
  protected void execute() {
    final boolean cmd = GUIProp.searchmode == 2 || !context.db();
    final String in = input.getText();

    if(cmd || in.startsWith("!")) {
      // run as command: command mode or exclamation mark as first character
      final int i = cmd ? 0 : 1;
      if(in.length() > i) {
        try {
          for(final Process p : new CommandParser(in.substring(i)).parse()) {
            if(!exec(p)) break;
          }
        } catch(final QueryException ex) {
          final boolean db = context.db();
          if(!GUIProp.showstarttext && !db || !GUIProp.showtext && db) {
            GUICommands.SHOWTEXT.execute();
          }
          final byte[] inf = new TokenBuilder(ex.getMessage()).finish();
          text.setText(inf, inf.length, false);
        }
      }
    } else {
      execute(new XPath(GUIProp.searchmode == 1 ? in :
        Find.find(in, context, GUIProp.filterrt)));
    }
  }
  
  /**
   * Launches the specified process in a thread.
   * @param pr process to be launched
   */
  public void execute(final Process pr) {
    if(View.working) return;
    new Action() {
      public void run() { exec(pr); }
    }.execute();
  }
  
  /** Thread counter. */
  private int threadID;
  /** Current process. */
  private Process proc;


  /**
   * Stops the current process.
   */
  public void stop() {
    if(proc != null) proc.stop();
    cursor(CURSORARROW, true);
    proc = null;
  }

  
  /**
   * Launches the specified process.
   * @param pr process to be launched
   * @return success flag
   */
  public boolean exec(final Process pr) {
    final int thread = ++threadID;

    // wait when command is still running
    while(proc != null) {
      proc.stop();
      Performance.sleep(50);
      if(threadID != thread) return true;
    }

    cursor(CURSORWAIT);
    try {
      if(pr.updating()) View.working = true;

      // cache some variables before executing the command
      final Performance perf = new Performance();
      final Nodes current = context.current();
      final Data data = context.data();
      proc = pr;

      // execute command
      final boolean ok = pr.execute(context);
      if(!ok && pr.info().length() == 0) {
        proc = null;
        return false;
      }

      if(pr.updating()) View.working = false;
      final Result result = pr.result();
      final Nodes nodes = result instanceof Nodes ? (Nodes) result : null;

      /* convert xquery result to a flat nodeset
       * solve problems with TextView first
      if(result instanceof XQResult) {
        final Nodes nod = ((XQResult) result).nodes(data);
        if(nod != null) result = nod;
      }*/

      // cached resulting text output
      final String inf = pr.info();

      if(ok && pr.printing() && nodes == null) {
        if(!GUIProp.showstarttext && data == null ||
           !GUIProp.showtext && data != null) {
          GUICommands.SHOWTEXT.execute();
        }
        // retrieve text result
        final CachedOutput out = new CachedOutput(TextView.MAX);
        if(ok) pr.output(out);
        else out.println(inf);
        out.addInfo();
        text.setText(out.buffer(), out.size(), false);
      }

      // check if query feedback was processed in the query view
      final boolean feedback = data != null && GUIProp.showquery &&
        query.info(pr instanceof XQuery ? inf : null, ok);

      if(!ok) {
        // show error info
        if(feedback) status.setText(STATUSOK);
        else status.setError(inf);
        cursor(CURSORARROW, true);
        proc = null;
        return false;
      }

      final Data ndata = context.data();
      final String time = perf.getTimer();
      Nodes marked = context.marked();
      if(ndata != data) {
        // database reference has changed - notify views
        View.notifyInit();
      } else if(pr.updating()) {
        // update command
        View.notifyUpdate();
      } else if(result != null) {
        if(context.current() != current || GUIProp.filterrt) {
          // refresh context
          if(nodes != null) {
            /*
            if(GUIProp.filterrt) {
            View.ftPos = nodes.ftpos;
            View.ftPoi = nodes.ftpoin;
            }*/
            View.notifyContext((Nodes) result, GUIProp.filterrt, null);
          }
        } else if(marked != null) {
          // refresh highlight
          if(nodes != null) {
            // use query result 
            marked = nodes;
          } else if(marked.size != 0) {
            // remove old highlight
            marked = new Nodes(data);
          }
          // highlights have changed.. refresh views
          if(!marked.same(context.marked())) {
            // View.ftPoi = marked.ftpoin;
            View.notifyMark(marked, null);
          }
          if(thread != threadID) {
            proc = null;
            return true;
          }
        }
      }
      
      // show number of hits
      setHits(result == null ? 0 : result.size());

      // show query info
      if(GUIProp.showinfo) info.setInfo(result != null ?
          Token.token(inf) : Token.EMPTY);

      // show status info
      status.setText(BaseX.info(PROCTIME, time));
      
    } catch(final Exception ex) {
      // unexpected error
      ex.printStackTrace();
      String msg = ex.toString();
      if(msg.length() == 0) msg = ex.getMessage();

      JOptionPane.showMessageDialog(GUI.this, BaseX.info(PROCERR, pr, msg),
          DIALOGINFO, JOptionPane.ERROR_MESSAGE);
    }

    cursor(CURSORARROW, true);
    proc = null;
    return true;
  }

  /**
   * Sets the border of the content area.
   */
  void setContentBorder() {
    final int n = control.getComponentCount();
    final int n2 = top.getComponentCount();

    if(n == 0 && n2 == 2) {
      views.setBorder(0, 0, 0, 0);
    } else {
      views.setBorder(new CompoundBorder(new EmptyBorder(3, 1, 3, 1),
          new EtchedBorder()));
    }
  }

  /**
   * Refreshes the layout.
   */
  public void updateLayout() {
    init();
    repaint();
    View.notifyLayout();
  }

  /**
   * Updates the control panel.
   * @param comp component to be updated
   * @param show true if component is visible
   * @param layout component layout.
   */
  void updateControl(final JComponent comp, final boolean show,
      final String layout) {

    if(comp == status) {
      if(!show) top.remove(comp);
      else top.add(comp, layout);
    } else if(comp == menu) {
      if(!show) menuHeight = menu.getHeight();
      final int s = show ? menuHeight : 0;
      BaseXLayout.setHeight(menu, s);
      menu.setSize(menu.getWidth(), s);
    } else {
      if(!show) control.remove(comp);
      else control.add(comp, layout);
    }
    setContentBorder();
    (fullscr == null ? getRootPane() : fullscr).validate();
    refreshControls();
  }

  /**
   * Updates the view layout.
   */
  public void layoutViews() {
    views.updateViews();
    refreshControls();
    repaint();
  }

  /**
   * Refreshes the menu and the buttons.
   */
  public void refreshControls() {
    final Nodes marked = context.marked();
    if(marked != null) setHits(marked.size);

    BaseXLayout.enable(filter, marked != null && marked.size != 0);
    refreshMode();
    toolbar.refresh();
    menu.refresh();

    final int i = !context.db() ? 2 : GUIProp.searchmode;
    final String[] hs = i == 0 ? GUIProp.search : i == 1 ? GUIProp.xpath :
      GUIProp.commands;
    hist.setEnabled(hs.length != 0);
  }

  /**
   * Refreshes the input mode.
   */
  protected void refreshMode() {
    final Data data = context.data();
    final boolean db = data != null;
    final int t = mode.getSelectedIndex();
    final int s = !db ? 2 : GUIProp.searchmode;

    final boolean inf = GUIProp.showinfo;
    Prop.allInfo = inf;
    Prop.xmlplan = inf;
    Prop.info = inf;

    input.help(s == 0 ? data.fs != null ? HELPSEARCHFS : HELPSEARCHXML :
      s == 1 ? HELPXPATH : HELPCMD);
    mode.setEnabled(db);
    go.setEnabled(s == 2 || !GUIProp.execrt);

    if(s != t) {
      mode.setSelectedIndex(s);
      input.setText("");
      input.requestFocusInWindow();
    }
  }

  /**
   * Sets hits information.
   * @param n number of hits
   */
  public void setHits(final int n) {
    hits.setText(n + " " + HITS);
  }

  /**
   * Focuses the specified component if the input field is not focused.
   * @param comp component to be focused
   */
  public void checkFocus(final JComponent comp) {
    if(GUIProp.mousefocus) comp.requestFocusInWindow();
  }

  /**
   * Displays some help in the help view.
   * @param comp component reference
   * @param txt text to be shown
   */
  public void focus(final Component comp, final byte[] txt) {
    if(txt != null) {
      final boolean db = context.db();
      if(!db && GUIProp.showstarthelp || db && GUIProp.showhelp)
        help.setText(txt, txt.length, true);
    }
    if(GUIProp.mousefocus && comp != null && comp.isEnabled())
      comp.requestFocusInWindow();
  }

  /**
   * Returns the specified image as icon.
   * @param name name of icon
   * @return icon
   */
  public static ImageIcon icon(final String name) {
    return new ImageIcon(image(name));
  }

  /** Cached images. */
  private static final HashMap<String, Image> IMAGES =
    new HashMap<String, Image>();
  
  /**
   * Returns the specified image.
   * @param name name of image
   * @return image
   */
  public static Image image(final String name) {
    Image img = IMAGES.get(name);
    if(img != null) return img;
    img = Toolkit.getDefaultToolkit().getImage(imageURL(name));
    IMAGES.put(name, img);
    return img;
  }

  /**
   * Returns the image url.
   * @param name name of image
   * @return url
   */
  public static URL imageURL(final String name) {
    final String path = "img/" + name + ".png";
    final URL url = GUI.class.getResource(path);
    if(url == null) BaseX.errln("Not found: " + path);
    return url;
  }

  /**
   * Turns fullscreen mode on/off.
   */
  public void fullscreen() {
    fullscreen(!GUIProp.fullscreen);
  }

  /**
   * Turns fullscreen mode on/off.
   * @param full fullscreen mode
   */
  public void fullscreen(final boolean full) {
    if(full ^ fullscr == null) {
      if(!GUIProp.showmenu) GUICommands.SHOWMENU.execute();
      return;
    }

    if(full) {
      control.remove(buttons);
      control.remove(nav);
      getRootPane().remove(menu);
      top.remove(status);
      remove(top);
      fullscr = new JFrame();
      fullscr.setIconImage(getIconImage());
      fullscr.setTitle(getTitle());
      fullscr.setUndecorated(true);
      fullscr.setJMenuBar(menu);
      fullscr.add(top);
      fullscr.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    } else {
      fullscr.removeAll();
      fullscr.dispose();
      fullscr = null;
      if(!GUIProp.showbuttons) control.add(buttons, BorderLayout.CENTER);
      if(!GUIProp.showinput) control.add(nav, BorderLayout.SOUTH);
      if(!GUIProp.showstatus) top.add(status, BorderLayout.SOUTH);
      setJMenuBar(menu);
      add(top);
    }

    GUIProp.showmenu = !full;
    GUIProp.showbuttons = !full;
    GUIProp.showinput = !full;
    GUIProp.showstatus = !full;
    GUIProp.fullscreen = full;

    GraphicsEnvironment.getLocalGraphicsEnvironment().
      getDefaultScreenDevice().setFullScreenWindow(fullscr);
    setContentBorder();
    refreshControls();
    updateControl(menu, !full, BorderLayout.NORTH);
    setVisible(!full);
  }
}
