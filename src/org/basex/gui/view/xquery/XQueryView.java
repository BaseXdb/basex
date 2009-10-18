package org.basex.gui.view.xquery;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.basex.core.Main;
import org.basex.core.Prop;
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
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class XQueryView extends View {
  /** Ok icon. */
  private static final ImageIcon OKICON = BaseXLayout.icon("ok");
  /** Error icon. */
  private static final ImageIcon ERRICON = BaseXLayout.icon("error");

  /** Header string. */
  final BaseXLabel header;
  /** Scroll Pane. */
  final BaseXBack south;
  /** Text Area. */
  final XQueryText text;
  /** Info label. */
  final BaseXLabel info;
  /** Execute button. */
  final BaseXButton go;
  /** Execute Button. */
  final BaseXButton stop;
  /** Filter button. */
  final BaseXButton filter;
  /** Modified flag. */
  boolean modified;

  /**
   * Default constructor.
   * @param man view manager
   */
  public XQueryView(final ViewNotifier man) {
    super(HELPXQUERY, man);

    setLayout(new BorderLayout());
    setBorder(6, 8, 8, 8);
    setFocusable(false);

    header = new BaseXLabel(XQUERYTIT, true, false);

    final BaseXBack back = new BaseXBack(Fill.NONE);
    back.setLayout(new BorderLayout());
    back.add(header, BorderLayout.CENTER);

    final BaseXButton open = BaseXButton.command(GUICommands.XQOPEN, gui);
    final BaseXButton save = BaseXButton.command(GUICommands.XQSAVE, gui);
    final BaseXTextField find = new BaseXTextField(null, gui);
    BaseXLayout.setHeight(find, (int) open.getPreferredSize().getHeight());

    final BaseXButton hist = new BaseXButton(gui, "hist", HELPRECENT);
    hist.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final JPopupMenu popup = new JPopupMenu("History");
        final ActionListener al = new ActionListener() {
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
    back.add(sp, BorderLayout.EAST);
    add(back, BorderLayout.NORTH);

    text = new XQueryText(this);
    add(text, BorderLayout.CENTER);
    text.addSearch(find);

    south = new BaseXBack(Fill.NONE);
    south.setLayout(new BorderLayout());

    info = new BaseXLabel(" ");
    info.setCursor(GUIConstants.CURSORHAND);
    info.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        if(info.getName() == null) return;
        text.setCaret(Integer.parseInt(info.getName()));
        text.requestFocusInWindow();
      }
    });
    south.add(info, BorderLayout.CENTER);
    BaseXLayout.enable(info, false);

    stop = new BaseXButton(gui, "stop", HELPSTOP);
    stop.addKeyListener(this);
    stop.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        BaseXLayout.enable(stop, false);
        gui.stop();
      }
    });
    BaseXLayout.enable(stop, false);

    go = new BaseXButton(gui, "go", HELPGO);
    go.addKeyListener(this);
    go.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        text.query();
      }
    });

    filter = BaseXButton.command(GUICommands.FILTER, gui);
    filter.addKeyListener(this);

    sp = new BaseXBack(Fill.NONE);
    sp.setLayout(new TableLayout(1, 5));
    sp.add(stop);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(go);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(filter);
    south.add(sp, BorderLayout.EAST);
    add(south, BorderLayout.SOUTH);

    Prop.xquery = IO.get("query.xq");
    refreshLayout();
  }

  @Override
  protected void refreshContext(final boolean more, final boolean quick) {
  }

  @Override
  protected void refreshFocus() {
  }

  @Override
  protected void refreshInit() {
  }

  @Override
  protected void refreshLayout() {
    header.setFont(GUIConstants.lfont);
    text.setFont(GUIConstants.mfont);
    refreshMark();
  }

  @Override
  protected void refreshMark() {
    BaseXLayout.enable(go, !gui.prop.is(GUIProp.EXECRT));
    final Nodes marked = gui.context.marked();
    BaseXLayout.enable(filter, !gui.prop.is(GUIProp.FILTERRT) &&
        marked != null && marked.size() != 0);
  }

  @Override
  protected void refreshUpdate() {
  }

  @Override
  public boolean visible() {
    return gui.prop.is(gui.context.data() != null ?
        GUIProp.SHOWXQUERY : GUIProp.SHOWSTARTXQUERY);
  }

  /**
   * Sets a new XQuery.
   * @param file query file
   */
  public void setQuery(final IO file) {
    gui.prop.files(file);
    try {
      setQuery(file.content());
    } catch(final IOException ex) {
      Dialog.error(gui, NOTOPENED);
    }
  }

  /**
   * Sets a new XQuery.
   * @param qu query
   */
  public void setQuery(final byte[] qu) {
    if(!visible()) GUICommands.SHOWXQUERY.execute(gui);
    text.setText(qu);
    text.query();
    modified(false);
  }

  /**
   * Returns the last XQuery input.
   * @return XQuery
   */
  public byte[] getQuery() {
    modified(false);
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
    info.setText(ok ? STATUSOK : inf.replaceAll("Stopped.*", ""));
    info.setIcon(ok ? OKICON : ERRICON);
    info.setToolTipText(ok ? null : inf);

    BaseXLayout.enable(stop, false);
    BaseXLayout.enable(info, !ok);
  }

  /**
   * Show a quit dialog for saving the opened XQuery.
   */
  public void confirm() {
    if(modified && Dialog.confirm(gui, Main.info(XQUERYCONF,
        Prop.xquery.name()))) GUICommands.XQSAVE.execute(gui);
  }

  /**
   * Sets the query modification flag.
   * @param mod modification flag
   */
  void modified(final boolean mod) {
    if(modified != mod) header.setText(XQUERYTIT + (mod ? "*" : ""));
    modified = mod;
  }
}
