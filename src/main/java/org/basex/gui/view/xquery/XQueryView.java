package org.basex.gui.view.xquery;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.basex.data.Nodes;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIMenu;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.dialog.Dialog;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewNotifier;
import org.basex.io.IO;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * This view allows the input and evaluation of XQuery queries.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class XQueryView extends View {
  /** Execute Button. */
  final BaseXButton stop;
  /** Info label. */
  final BaseXLabel info;
  /** Position label. */
  final BaseXLabel pos;
  /** Text Area. */
  final XQueryText text;
  /** Modified flag. */
  boolean modified;
  /** Thread counter. */
  int threadID;

  /** Header string. */
  final BaseXLabel header;
  /** Scroll Pane. */
  private final BaseXBack south;
  /** Execute button. */
  private final BaseXButton go;
  /** Filter button. */
  private final BaseXButton filter;

  /**
   * Default constructor.
   * @param man view manager
   */
  public XQueryView(final ViewNotifier man) {
    super(XQUERYVIEW, HELPXQUERYY, man);

    border(6, 6, 6, 6).layout(new BorderLayout()).setFocusable(false);

    header = new BaseXLabel(XQUERYTIT, true, false);

    final BaseXBack b = new BaseXBack(Fill.NONE).layout(new BorderLayout());
    b.add(header, BorderLayout.CENTER);

    final BaseXButton openB = BaseXButton.command(GUICommands.XQOPEN, gui);
    final BaseXButton saveB = new BaseXButton(gui, "xqsave",
        Token.token(GUISAVETT));
    saveB.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final JPopupMenu pop = new JPopupMenu();
        final StringBuilder mnem = new StringBuilder();
        final JMenuItem sa = GUIMenu.newItem(GUICommands.XQSAVE, gui, mnem);
        final JMenuItem sas = GUIMenu.newItem(GUICommands.XQSAVEAS, gui, mnem);
        GUICommands.XQSAVE.refresh(gui, sa);
        GUICommands.XQSAVEAS.refresh(gui, sas);
        pop.add(sa);
        pop.add(sas);
        pop.show(saveB, 0, saveB.getHeight());
      }
    });
    final BaseXTextField find = new BaseXTextField(gui);
    BaseXLayout.setHeight(find, (int) openB.getPreferredSize().getHeight());

    final BaseXButton hist = new BaseXButton(gui, "hist", HELPRECENT);
    hist.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final JPopupMenu popup = new JPopupMenu();
        final ActionListener al = new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent ac) {
            confirm();
            setQuery(IO.get(ac.getActionCommand()));
          }
        };
        if(gui.gprop.strings(GUIProp.QUERIES).length == 0) {
          popup.add(new JMenuItem("- No recently opened files -"));
        }
        for(final String en : gui.gprop.strings(GUIProp.QUERIES)) {
          final JMenuItem jmi = new JMenuItem(en);
          jmi.addActionListener(al);
          popup.add(jmi);
        }
        popup.show(hist, 0, hist.getHeight());
      }
    });

    final BaseXButton close = BaseXButton.command(GUICommands.XQCLOSE, gui);

    BaseXBack sp = new BaseXBack(Fill.NONE).layout(new TableLayout(1, 9));
    sp.add(find);
    sp.add(Box.createHorizontalStrut(5));
    sp.add(openB);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(saveB);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(hist);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(close);
    b.add(sp, BorderLayout.EAST);
    add(b, BorderLayout.NORTH);

    text = new XQueryText(this);
    add(text, BorderLayout.CENTER);
    text.addSearch(find);

    south = new BaseXBack(Fill.NONE).layout(new BorderLayout(8, 0));
    sp = new BaseXBack(Fill.NONE).layout(new BorderLayout(8, 0));

    // parsing status (ok/error)
    info = new BaseXLabel(" ");
    info.setText(OK, Msg.SUCCESS);
    info.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        if(info.getName() == null) return;
        text.setCaret(Integer.parseInt(info.getName()));
        text.requestFocusInWindow();
        pos.setText(text.pos());
      }
    });
    sp.add(info, BorderLayout.CENTER);

    // col/line information
    pos = new BaseXLabel(" ");

    //pos.setBo
    sp.add(pos, BorderLayout.EAST);

    south.add(sp, BorderLayout.CENTER);

    stop = new BaseXButton(gui, "stop", HELPSTOP);
    stop.addKeyListener(this);
    stop.addActionListener(new ActionListener() {
      @Override
     public void actionPerformed(final ActionEvent e) {
        stop.setEnabled(false);
        info.setText(OK, Msg.SUCCESS);
        gui.stop();
      }
    });
    stop.setEnabled(false);

    go = new BaseXButton(gui, "go", HELPGO);
    go.addKeyListener(this);
    go.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        text.query();
      }
    });

    filter = BaseXButton.command(GUICommands.FILTER, gui);
    filter.addKeyListener(this);

    sp = new BaseXBack(Fill.NONE).border(4, 0, 0, 0).layout(
        new TableLayout(1, 5));
    sp.add(stop);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(go);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(filter);
    south.add(sp, BorderLayout.EAST);
    add(south, BorderLayout.SOUTH);
    refreshLayout();
  }

  @Override
  public void refreshInit() { }

  @Override
  public void refreshFocus() { }

  @Override
  public void refreshMark() {
    go.setEnabled(!gui.gprop.is(GUIProp.EXECRT));
    final Nodes marked = gui.context.marked;
    filter.setEnabled(!gui.gprop.is(GUIProp.FILTERRT) &&
        marked != null && marked.size() != 0);
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) { }

  @Override
  public void refreshLayout() {
    header.setFont(GUIConstants.lfont);
    text.setFont(GUIConstants.mfont);
    refreshMark();
  }

  @Override
  public void refreshUpdate() { }

  @Override
  public boolean visible() {
    return gui.gprop.is(GUIProp.SHOWXQUERY);
  }

  @Override
  public void visible(final boolean v) {
    gui.gprop.set(GUIProp.SHOWXQUERY, v);
  }

  @Override
  protected boolean db() {
    return false;
  }

  /**
   * Sets a new query file.
   * @param file query file
   */
  public void setQuery(final IO file) {
    if(!visible()) GUICommands.SHOWXQUERY.execute(gui);
    try {
      text.setText(file != null ? file.content() : Token.EMPTY);
      gui.context.query = file;
      gui.gprop.files(file);
      modified(false, true);
      if(gui.gprop.is(GUIProp.EXECRT)) text.query();
    } catch(final IOException ex) {
      Dialog.error(gui, NOTOPENED);
    }
  }

  /**
   * Returns the last XQuery input.
   * @return XQuery
   */
  public byte[] getQuery() {
    modified(false, true);
    return text.getText();
  }

  /**
   * Initializes the info message.
   */
  public void reset() {
    ++threadID;
    info.setName(null);
    info.setToolTipText(null);
    info.setText(OK, Msg.SUCCESS);
    stop.setEnabled(false);
  }

  /**
   * Displays a waiting status.
   */
  public void waitInfo() {
    final int thread = ++threadID;
    new Thread() {
      @Override
      public void run() {
        Performance.sleep(200);
        if(thread == threadID) info.setText(INFOWAIT, Msg.SUCCESS);
      }
    }.start();
  }

  /**
   * Evaluates the info message resulting from a query execution.
   * @param inf info message
   * @param ok true if query was successful
   */
  public void info(final String inf, final boolean ok) {
    ++threadID;
    final int error = text.error(inf, ok);
    info.setName(error != -1 ? Integer.toString(error) : null);
    info.setCursor(error !=  -1 ?
        GUIConstants.CURSORHAND : GUIConstants.CURSORARROW);

    info.setText(ok ? OK : inf.replaceAll(STOPPED + ".*\\r?\\n\\[.*?\\] ", ""),
        ok ? Msg.SUCCESS : Msg.ERROR);
    info.setToolTipText(ok ? null : inf);
    stop.setEnabled(false);
  }

  /**
   * Shows a quit dialog for saving the opened XQuery.
   */
  public void confirm() {
    if(gui.context.query != null && modified && Dialog.confirm(gui, Util.info(
        XQUERYCONF, gui.context.query.name()))) GUICommands.XQSAVE.execute(gui);
  }

  /**
   * Returns the modified flag.
   * @return modified flag
   */
  public boolean modified() {
    return modified;
  }

  /**
   * Sets the query modification flag.
   * @param mod modification flag
   * @param force action
   */
  void modified(final boolean mod, final boolean force) {
    if(modified != mod || force) {
      String title = XQUERYTIT;
      if(gui.context.query != null) {
        title += " - " + gui.context.query.name();
        if(mod) title += "*";
      }
      header.setText(title);
      modified = mod;
      gui.refreshControls();
    }
  }
}
