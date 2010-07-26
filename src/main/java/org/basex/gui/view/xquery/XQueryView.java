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
import org.basex.core.Main;
import org.basex.data.Nodes;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIConstants;
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

/**
 * This view allows the input and evaluation of XQuery queries.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class XQueryView extends View {
  /** Execute Button. */
  final BaseXButton stop;
  /** Info label. */
  final BaseXLabel info;
  /** Text Area. */
  final XQueryText text;
  /** Modified flag. */
  boolean modified;

  /** Header string. */
  private final BaseXLabel header;
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

    setLayout(new BorderLayout());
    setBorder(6, 8, 8, 8);
    setFocusable(false);

    header = new BaseXLabel(XQUERYTIT, true, false);

    final BaseXBack b = new BaseXBack(Fill.NONE);
    b.setLayout(new BorderLayout());
    b.add(header, BorderLayout.CENTER);

    final BaseXButton open = BaseXButton.command(GUICommands.XQOPEN, gui);
    final BaseXButton save = BaseXButton.command(GUICommands.XQSAVEAS, gui);
    final BaseXTextField find = new BaseXTextField(gui);
    BaseXLayout.setHeight(find, (int) open.getPreferredSize().getHeight());

    final BaseXButton hist = new BaseXButton(gui, "hist", HELPRECENT);
    hist.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final JPopupMenu popup = new JPopupMenu("History");
        final ActionListener al = new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent ac) {
            confirm();
            setQuery(IO.get(ac.getActionCommand()));
          }
        };
        for(final String en : gui.prop.strings(GUIProp.QUERIES)) {
          final JMenuItem jmi = new JMenuItem(en);
          jmi.addActionListener(al);
          popup.add(jmi);
        }
        popup.show(hist, 0, hist.getHeight());
      }
    });

    BaseXBack sp = new BaseXBack(Fill.NONE);
    sp.setLayout(new TableLayout(1, 7));
    sp.add(find);
    sp.add(Box.createHorizontalStrut(5));
    sp.add(open);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(save);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(hist);
    b.add(sp, BorderLayout.EAST);
    add(b, BorderLayout.NORTH);

    text = new XQueryText(this);
    add(text, BorderLayout.CENTER);
    text.addSearch(find);

    south = new BaseXBack(Fill.NONE);
    south.setLayout(new BorderLayout());

    info = new BaseXLabel(" ");
    info.setCursor(GUIConstants.CURSORHAND);
    info.setText(OK, Msg.SUCCESS);
    info.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        if(info.getName() == null) return;
        text.setCaret(Integer.parseInt(info.getName()));
        text.requestFocusInWindow();
      }
    });
    south.add(info, BorderLayout.CENTER);

    stop = new BaseXButton(gui, "stop", HELPSTOP);
    stop.addKeyListener(this);
    stop.addActionListener(new ActionListener() {
      @Override
     public void actionPerformed(final ActionEvent e) {
        stop.setEnabled(false);
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

    sp = new BaseXBack(Fill.NONE);
    sp.setLayout(new TableLayout(1, 5));
    sp.setBorder(4, 0, 0, 0);
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
    go.setEnabled(!gui.prop.is(GUIProp.EXECRT));
    final Nodes marked = gui.context.marked;
    filter.setEnabled(!gui.prop.is(GUIProp.FILTERRT) &&
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
    return gui.prop.is(GUIProp.SHOWXQUERY);
  }

  @Override
  public void visible(final boolean v) {
    gui.prop.set(GUIProp.SHOWXQUERY, v);
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
    gui.prop.files(file);
    gui.context.query = file;
    try {
      if(!visible()) GUICommands.SHOWXQUERY.execute(gui);
      modified(false, true);
      final byte[] query = file.content();
      text.setText(query);
      if(gui.prop.is(GUIProp.EXECRT)) text.query();
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
   * Handles info messages resulting from a query execution.
   * @param inf info message
   * @param ok true if query was successful
   */
  public void info(final String inf, final boolean ok) {
    final int error = text.error(inf, ok);
    info.setName(error != -1 ? Integer.toString(error) : null);
    info.setText(ok ? OK : inf.replaceAll(STOPPED + ".*\\r?\\n", ""),
        ok ? Msg.SUCCESS : Msg.ERROR);
    info.setToolTipText(ok ? null : inf);
    stop.setEnabled(false);
  }

  /**
   * Shows a quit dialog for saving the opened XQuery.
   */
  public void confirm() {
    if(gui.context.query != null && modified && Dialog.confirm(gui, Main.info(
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
