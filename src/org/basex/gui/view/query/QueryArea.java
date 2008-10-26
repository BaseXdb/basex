package org.basex.gui.view.query;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import org.basex.core.proc.XQuery;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXText;
import org.basex.query.QueryException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQParser;
import org.basex.util.Action;
import org.basex.util.Token;

/**
 * This class provides a text area for entering queries.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class QueryArea extends QueryPanel {
  /** Error pattern. */
  static final Pattern ERRPATTERN = Pattern.compile(
      ".* line ([0-9]+), column ([0-9]+).*", Pattern.DOTALL);
  /** Info label. */
  BaseXLabel info;
  /** Text Area. */
  BaseXText area;
  /** Scroll Pane. */
  Box south;
  /** Ok icon. */
  ImageIcon okIcon;
  /** Error icon. */
  ImageIcon errIcon;
  /** Last error position. */
  int err;
  
  /**
   * Default constructor.
   * @param view main panel
   */
  QueryArea(final QueryView view) {
    main = view;
    area = new BaseXText(HELPQUERYMODE);
    area.setSyntax(new QuerySyntax());
    area.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        final String xq = Token.string(area.getText());
        final boolean mod = xq.trim().startsWith("module namespace ");
        if(GUIProp.execrt && !mod) {
          query(false);
        } else {
          try {
            final XQParser parser = new XQParser(new XQContext());
            if(mod) parser.module(xq);
            else parser.parse(xq);
            info("", true);
          } catch(final QueryException ex) {
            info(ex.getMessage(), false);
          }
        }
      }
    });

    south = new Box(BoxLayout.X_AXIS);
    info = new BaseXLabel(" ");
    info.setName(Integer.toString(0));
    info.setCursor(GUIConstants.CURSORHAND);
    info.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        area.setCaret(Integer.parseInt(info.getName()));
        area.requestFocusInWindow();
      }
    });
    south.add(info);
    BaseXLayout.enable(info, false);

    initPanel();

    south.add(Box.createHorizontalGlue());
    south.add(stop);
    south.add(Box.createHorizontalStrut(1));
    south.add(go);

    okIcon = GUI.icon("ok");
    errIcon = GUI.icon("error");
  }

  @Override
  public void init() {
    main.add(area, BorderLayout.CENTER);
    main.add(south, BorderLayout.SOUTH);
    area.setText(Token.token(last));
    area.setCaret(0);
    area.setFont(GUIConstants.mfont);
    refresh();
  }

  @Override
  public void finish() { }

  @Override
  void refresh() {
    BaseXLayout.enable(go, !GUIProp.execrt);
  }

  @Override
  void query(final boolean force) {
    String xquery = Token.string(area.getText());
    if(force || !xquery.equals(last)) {
      last = xquery;
      final String xq = xquery.trim();
      if(xq.startsWith("module namespace ")) return;
      if(xq.length() == 0) xquery = ".";
      BaseXLayout.enable(stop, true);
      GUI.get().execute(new XQuery(xquery));
    }
  }
  
  @Override
  void quit() { }

  @Override
  void info(final String inf, final boolean ok) {
    BaseXLayout.enable(stop, false);
    if(inf == null) return;
    
    final String text = ok ? STATUSOK : inf.replaceAll("Stopped.*", "");
    info.setText(text);
    info.setIcon(ok ? okIcon : errIcon);
    BaseXLayout.enable(info, !ok);
    info.setToolTipText(ok ? null : text);

    err = -1;
    if(!ok) {
      final Matcher m = ERRPATTERN.matcher(inf);
      int el = 0;
      int ec = 0;
      if(m.matches()) {
        el = Integer.parseInt(m.group(1));
        ec = Integer.parseInt(m.group(2));
      }

      final int ll = last.length();
      for(int i = 0, l = 1, c = 1; i < ll; c++, i++) {
        if(l == el && c == ec) {
          err = i;
          break;
        }
        if(last.charAt(i) < ' ') {
          l++;
          c = 0;
        }
      }
    }

    area.error(-1);
    info.setName(Integer.toString(err));
    error.delay(500);
  }

  /** Delays the display of error information. */
  Action error = new Action() {
    public void run() {
      area.error(err);
    }
  };
}
