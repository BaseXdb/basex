package org.basex.gui.view.query;

import static org.basex.Text.*;
import static org.basex.gui.GUIConstants.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.BoxLayout;
import org.basex.core.Commands;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXText;
import org.basex.query.QueryException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQParser;
import org.basex.query.xquery.item.Uri;
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
  /** Main panel. */
  QueryView main;
  /** Text Area. */
  BaseXText area;
  /** Execute Button. */
  BaseXButton exec;
  /** Scroll Pane. */
  BaseXBack south;

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
        String xq = Token.string(area.getText());
        if(!xq.equals(last)) {
          last = xq;
          boolean module = xq.trim().startsWith("module namespace ");
          if(xq.trim().length() == 0) xq = ".";
          if(GUIProp.execrt && !module) {
            GUI.get().execute(Commands.XQUERY, xq);
          } else {
            final XQContext ctx = new XQContext();
            final XQParser parser = new XQParser(ctx);
            try {
              parser.parse(xq, ctx.file, module ? Uri.EMPTY : null);
              info("", true);
            } catch(final QueryException ex) {
              info(ex.getMessage(), false);
            }
          }
        }
      }
    });
    south = new BaseXBack(GUIConstants.FILL.NONE);
    south.setLayout(new BorderLayout(8, 8));
    initPanel();
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

  /**
   * Initializes the components.
   */
  void initPanel() {
    final Box box = new Box(BoxLayout.X_AXIS);

    info = new BaseXLabel("");
    info.setFont(info.getFont().deriveFont((float) 13));
    info.setIcon(GUI.icon(IMGERROR));
    info.setName(Integer.toString(0));
    info.setCursor(GUIConstants.CURSORHAND);
    info.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        area.setCaret(Integer.parseInt(info.getName()));
        area.requestFocusInWindow();
      }
    });
    BaseXLayout.enable(info, false);
    south.add(info, BorderLayout.CENTER);

    exec = new BaseXButton(GUI.icon("go"), HELPEXEC);
    exec.trim();
    exec.addKeyListener(main);
    exec.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        query(true);
      }
    });
    box.add(exec);

    south.add(box, BorderLayout.EAST);
  }

  @Override
  void refresh() {
    BaseXLayout.enable(exec, !GUIProp.execrt);
  }

  @Override
  void query(final boolean force) {
    final String xq = Token.string(area.getText());
    if(force || !xq.equals(last)) {
      last = xq;
      if(xq.trim().length() == 0) return;
      GUI.get().execute(Commands.XQUERY, xq);
    }
  }

  @Override
  void quit() { }

  @Override
  void info(final String inf, final boolean ok) {
    final String text = ok ? "" : inf.replaceAll("Stopped.*", "");
    info.setText(text);
    info.setToolTipText(ok ? null : text);
    BaseXLayout.enable(info, !ok);

    err = -1;
    if(!ok) {
      final Matcher m = ERRPATTERN.matcher(inf);
      int el = 0;
      int ec = 0;
      if(m.matches()) {
        el = Integer.parseInt(m.group(1));
        ec = Integer.parseInt(m.group(2));
      }

      int l = 1;
      int c = 1;
      final int ll = last.length();
      for(int i = 0; i < ll; c++, i++) {
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
    error.sleep(500);
  }

  /** Last error position. */
  int err;

  /** Delays the display of error information. */
  Action error = new Action() {
    @Override
    public void action() {
      area.error(err);
    }
  };
}
