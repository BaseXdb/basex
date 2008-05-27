package org.basex.gui;

import static org.basex.gui.GUIConstants.*;
import static org.basex.Text.*;
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
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import org.basex.BaseX;
import org.basex.Text;
import org.basex.core.Command;
import org.basex.core.CommandParser;
import org.basex.core.Commands;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.Find;
import org.basex.core.proc.Proc;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewContainer;
import org.basex.gui.view.ViewPanel;
import org.basex.gui.view.info.InfoView;
import org.basex.gui.view.map.MapView;
import org.basex.gui.view.query.QueryView;
import org.basex.gui.view.real.RealView;
import org.basex.gui.view.table.TableView;
import org.basex.gui.view.text.TextView;
import org.basex.gui.view.tree.TreeView;
import org.basex.gui.view.xpath.XPathView;
import org.basex.io.CachedOutput;
import org.basex.util.Performance;
import org.basex.util.Token;

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
  /** Query field. */
  public final BaseXCombo input;
  /** Filter button. */
  public final BaseXButton filter;

  /** Top panel. */
  final BaseXBack top;
  /** Result panel. */
  final GUIMenu menu;
  /** Button panel. */
  final BaseXBack buttons;
  /** Query panel. */
  final BaseXBack nav;

  /** Search view. */
  final QueryView query;
  /** Text view. */
  final TextView text;
  /** Info view. */
  final InfoView info;
  /** Help view. */
  final TextView help;

  /** Current input Mode. */
  final BaseXCombo mode;
  /** Execution Button. */
  final BaseXButton exec;
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
  /** JPopupMenu. */
  public JPopupMenu pop = new JPopupMenu();

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
    setIconImage(image(IMGICON));

    // set window size
    final Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
    final int w = GUIProp.guisize[0];
    final int h = GUIProp.guisize[1];
    int x = Math.max(0, GUIProp.guiloc[0]);
    int y = Math.max(0, GUIProp.guiloc[1]);
    if(x > s.width - w) x = Math.max(0, s.width - w);
    if(y > s.height - h) y = Math.max(0, s.height - h);
    
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
    toolbar = new GUIToolBar();
    buttons.add(toolbar, BorderLayout.WEST);

    final BaseXBack p = new BaseXBack();

    hits = new BaseXLabel(" ");
    hits.setFont(fnt);
    BaseXLayout.setWidth(hits, 150);
    hits.setHorizontalAlignment(SwingConstants.RIGHT);
    p.add(hits);
    p.add(Box.createHorizontalStrut(4));

    filter = new BaseXButton(BUTTONFILTER, HELPFILTER, null);
    filter.setToolTipText(Token.string(HELPFILTER));
    filter.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        GUICommands.FILTER.execute();
      }
    });
    filter.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        addKeys(e);
      }
    });
    p.add(filter);

    buttons.add(p, BorderLayout.EAST);
    if(GUIProp.showbuttons) control.add(buttons, BorderLayout.CENTER);

    nav = new BaseXBack();
    nav.setLayout(new BorderLayout(6, 0));
    nav.setBorder(2, 2, 0, 4);

    mode = new BaseXCombo(new String[] {
        BUTTONSEARCH, BUTTONXPATH, BUTTONCMD }, HELPMODE, false);
    mode.setSelectedIndex(2);
    
    mode.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final int t = GUIProp.searchmode;
        final int i = mode.getSelectedIndex();
        if(i == t || !mode.isEnabled()) return;

        saveMode(t);
        input.history(i == 0 ? GUIProp.search : i == 1 ?  GUIProp.xpath :
          GUIProp.commands);
        input.setText("");

        input.help = i == 0 ? context.data().deepfs ? HELPSEARCHFS :
          HELPSEARCHXML : i == 1 ? HELPXPATH : HELPCMD;

        exec.setEnabled(i == 2 || !GUIProp.execrt);
        GUIProp.searchmode = i;
      }
    });
    mode.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        addKeys(e);
      }
    });
    
    nav.add(mode, BorderLayout.WEST);

    input = new BaseXCombo(GUIProp.commands, null, true); 

    final Font f = input.getFont();
    input.setFont(f.deriveFont((float) f.getSize() + 2));

    input.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        addKeys(e);
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
          /* <AW> Here you could set the current popup menu entry
           * as input text (input.setText(...))
          if(pop.isVisible()) {
          }*/
          execute();
        }
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        if(e.getKeyChar() == 0xFFFF || e.isControlDown()) return;
        
        if(GUIProp.searchmode == 2 || !context.db()) {
          refreshPop(e.getKeyCode());
        }

        // skip commands
        if(GUIProp.execrt && GUIProp.searchmode != 2 && context.db() &&
            !input.getText().startsWith("!")) execute();
      }
    });
    nav.add(input, BorderLayout.CENTER);

    exec = new BaseXButton(BUTTONEXEC, null);
    exec.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        execute();
      }
    });
    exec.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        addKeys(e);
      }
    });
    //nav.add(Box.createHorizontalStrut(6));
    nav.add(exec, BorderLayout.EAST);
    
    if(GUIProp.showinput) control.add(nav, BorderLayout.SOUTH);
    top.add(control, BorderLayout.NORTH);

    // create views
    query = new QueryView(null);
    text = new TextView(FILL.DOWN, TEXTTIT, HELPTEXT);
    help = new TextView(FILL.DOWN, HELPTIT, null);
    info = new InfoView(HELPINFO);
    final ViewPanel textpanel = new ViewPanel(text, TEXTVIEW);
    final ViewPanel helppanel = new ViewPanel(help, HELPVIEW);

    // create panels for closed and opened database mode
    final ViewPanel[][] panels = { {
      textpanel, helppanel }, {
        new ViewPanel(new TreeView(HELPTREE), TREEVIEW),
        new ViewPanel(new RealView(), REALVIEW),
        new ViewPanel(new XPathView(), XPATHVIEW),
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
    Prop.allInfo = GUIProp.showinfo;
    Prop.info = GUIProp.showinfo;
    Prop.chop = true;

    // start logo animation as thread 
    new Thread() {
      @Override
      public void run() {
        views.run();
      }
    }.start();
  }
  
  /**
   * Creates the JPopupMenu.
   * @param c current key
   */
  public void refreshPop(final int c) {
    if(pop.isVisible()) pop.setVisible(false);
    if(c == KeyEvent.VK_ESCAPE || c == KeyEvent.VK_ENTER) return;
    
    final String in = input.getText().toLowerCase();
    if(in.length() == 0) return;
    
    pop.removeAll();
    final String[] all = Commands.list();
    for (int i = 0; i < all.length; i++) {
      if(all[i].startsWith(in)) {
        final JMenuItem jmi = new JMenuItem(all[i]);
        jmi.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent ac) {
            input.setText(ac.getActionCommand());
            pop.setVisible(false);
          }
        });
        pop.add(jmi);
      }
    }
    if(pop.getComponentCount() != 0) {
      input.add(pop);
      pop.show(input, 0, input.getHeight());
      input.requestFocusInWindow();
    }
  }

  /**
   * Browse in views.
   * @param e key event
   */
  public void addKeys(final KeyEvent e) {
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
    saveMode(context.db() ? GUIProp.searchmode : 2);

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
   * Sets a new cursor.
   * @param c cursor to be set
   */
  public void cursor(final Cursor c) {
    cursor(c, false);
  }

  /**
   * Sets a new cursor.
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
      if(in.length() > i) execute(in.substring(i));
    } else {
      execute(Commands.XPATH, GUIProp.searchmode == 1 ? in :
        Find.find(in, context, GUIProp.filterrt));
    }
  }
  
  /**
   * Launches the specified command.
   * @param cmd command to be launched
   * @param arg arguments
   */
  public void execute(final Commands cmd, final String... arg) {
    final StringBuilder sb = new StringBuilder(cmd.toString());
    for(final String a : arg) sb.append(" " + a);
    execute(sb.toString());
  }

  /** Thread counter. */
  int threadID;
  /** Current process. */
  Proc proc;

  /**
   * Checks if the specified thread is obsolete.
   * @param thread thread to be checked
   * @return result of check
   */
  protected boolean obsolete(final int thread) {
    boolean obs = thread != threadID;
    if(obs) proc = null;
    return obs;
  }
  
  /**
   * Launches the specified command string.
   * @param command commands to be launched
   */
  public void execute(final String command) {
    if(View.working) return;
    final int thread = ++threadID;

    new Thread() {
      @Override
      public void run() {
        if(threadID != thread) return;
        
        // wait when command is still running
        while(proc != null) {
          proc.stop();
          Performance.sleep(50);
          if(threadID != thread) return;
        }

        cursor(CURSORWAIT);

        try {
          // parse command string
          final CommandParser cp = new CommandParser(command);
          while(cp.more()) {
            if(obsolete(thread)) return;

            final Command cmd = cp.next();
            final Commands cc = cmd.name;

            // exit command
            if(cc == Commands.EXIT || cc == Commands.QUIT) {
              quit();
              return;
            }
            proc = cmd.proc(context);
            if(cc.updating()) View.working = true;

            // cache some variables before executing the command
            final Performance perf = new Performance();
            final Nodes current = context.current();
            final Data data = context.data();
            final Proc p = proc;

            // execute command
            final boolean ok = p.execute();
            if(!ok && p.info().length() == 0) {
              proc = null;
              return;
            }
            
            if(cc.updating()) View.working = false;
            if(obsolete(thread)) return;

            Result result = p.result();
            Nodes nodes = result instanceof Nodes ? (Nodes) result : null;
            
            /* convert xquery result to a flat nodeset
             * solve problems with TextView first
            if(result instanceof XQResult) {
              final Nodes nod = ((XQResult) result).nodes(data);
              if(nod != null) result = nod;
            }*/

            // cached resulting text output
            final CachedOutput out = new CachedOutput(TextView.MAX);

            if(cc.printing() && ok) {
              if(!GUIProp.showstarttext && data == null ||
                 !GUIProp.showtext && data != null && nodes == null) {
                GUICommands.SHOWTEXT.execute();
              }
              // retrieve text result
              if(text.isValid()) {
                p.output(out);
                out.addInfo();
              }
            }            

            final String time = perf.getTimer();
            final String inf = p.info();
            if(obsolete(thread)) return;

            // check if query feedback was evaluated in the query view
            final boolean feedback = cc.printing() && data != null &&
              GUIProp.showquery && cc == Commands.XQUERY && query.info(inf, ok);
            
            final Data ndata = context.data();
            if(ndata != data) {
              // database reference has changed - notify views
              View.notifyInit();
            } else if(cc.updating()) {
              // update command
              View.notifyUpdate();
            } else if(result != null) {
              if(context.current() != current || GUIProp.filterrt) {
                // refresh context
                if(nodes != null) {
                  if(GUIProp.filterrt) {
                    View.ftPos = nodes.ftpos;
                    View.ftPoi = nodes.ftpoin;
                  }
                  View.notifyContext((Nodes) result, GUIProp.filterrt);
                }
              } else if(context.marked() != null) {
                // refresh highlight
                Nodes marked = context.marked();
                // nodes as result?
                if(nodes != null) {
                  marked = nodes;
                } else if(marked.size != 0) {
                  // any other result - remove old marks
                  marked = new Nodes(data);
                }
                // highlights have changed.. refresh views
                if(marked != context.marked()) {
                  View.ftPos = marked.ftpos;
                  View.ftPoi = marked.ftpoin;
                  View.notifyMark(marked);
                }
              }
            }
            if(obsolete(thread)) return;

            // print result
            if(ok && cc.printing() && nodes == null) {
              text.setText(out.buffer(), out.size(), false);
              //text.updateHeader(null);
            }

            // show number of hits
            setHits(result == null ? 0 : result.size());

            // show query info
            if(GUIProp.showinfo) info.setInfo(result != null ?
                Token.token(inf) : Token.EMPTY);

            if(ok) {
              // show status info
              status.setText(BaseX.info(PROCTIME, time));
            } else {
              // show error info
              if(feedback) status.setText(STATUSOK);
              else status.setError(inf);
              break;
            }
          }
        } catch(final IllegalArgumentException ex) {
          // unknown command or wrong argument
          JOptionPane.showMessageDialog(GUI.this, ex.getMessage(),
              DIALOGINFO, JOptionPane.INFORMATION_MESSAGE);
          status.setText(STATUSOK);
        } catch(final Exception ex) {
          // unexpected error
          //BaseX.debug(ex);
          ex.printStackTrace();
          String msg = ex.toString();
          if(msg.length() == 0) msg = ex.getMessage();

          JOptionPane.showMessageDialog(GUI.this, BaseX.info(PROCERR,
              command, msg), DIALOGINFO, JOptionPane.ERROR_MESSAGE);
        }

        cursor(CURSORARROW, true);
        proc = null;
      }
    }.start();
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
    pop.setVisible(false);
  }
  
  /**
   * Refreshes the input mode.
   */
  protected void refreshMode() {
    final Data data = context.data();
    final boolean db = data != null;
    final int t = mode.getSelectedIndex();
    final int s = !db ? 2 : GUIProp.searchmode;

    input.help = s == 0 ? data.deepfs ? HELPSEARCHFS : HELPSEARCHXML :
      s == 1 ? HELPXPATH : HELPCMD;
    mode.setEnabled(db);
    exec.setEnabled(s == 2 || !GUIProp.execrt);

    if(s != t) {
      saveMode(t);
      input.history(s == 0 ? GUIProp.search : s == 1 ?  GUIProp.xpath :
        GUIProp.commands);
      mode.setSelectedIndex(s);
      input.setText("");
      input.requestFocusInWindow();
    }
  }
  
  /**
   * Saves the last input mode.
   * @param i input mode
   */
  protected void saveMode(final int i) {
    if(i == 0) GUIProp.search = input.history();
    else if(i == 1) GUIProp.xpath = input.history();
    else GUIProp.commands = input.history();
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

  /**
   * Returns the specified image.
   * @param name name of image
   * @return image
   */
  public static Image image(final String name) {
    return Toolkit.getDefaultToolkit().getImage(imageURL(name));
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
