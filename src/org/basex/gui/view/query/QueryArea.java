package org.basex.gui.view.query;

import static org.basex.gui.GUIConstants.*;
import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import org.basex.core.Commands;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTextArea;
import org.basex.gui.view.View;

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
  BaseXTextArea area;
  /** Filter/Execute Button. */
  BaseXButton filter;
  /** Scroll Pane. */
  JScrollPane sp;
  /** Scroll Pane. */
  BaseXBack south;

  /**
   * Default constructor.
   * @param view main panel
   */
  QueryArea(final QueryView view) {
    main = view;
    area = new BaseXTextArea(HELPQUERYMODE);
    area.setFont(GUIConstants.mfont);
    area.addKeyListener(main);
    sp = new JScrollPane(area);
    south = new BaseXBack(GUIConstants.FILL.NONE);
    south.setLayout(new BorderLayout(8, 8));
    
    initPanel();
    area.init(GUIProp.xquerycmd);
  }

  @Override
  public void init() {
    main.add(sp, BorderLayout.CENTER);
    main.add(south, BorderLayout.SOUTH);
    area.setText(last);
    area.setFont(GUIConstants.mfont);
    refresh();
  }

  @Override
  public void finish() { }

  /**
   * Initializes the components.
   */
  void initPanel() {
    area.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        query(false);
      }
    });

    final Box box = new Box(BoxLayout.X_AXIS);

    info = new BaseXLabel("");
    info.setFont(info.getFont().deriveFont((float) 13));
    info.setIcon(GUI.icon(IMGERROR));
    BaseXLayout.enable(info, false);
    south.add(info, BorderLayout.CENTER);

    filter = new BaseXButton(BUTTONFILTER, HELPFILTER, null);
    filter.addKeyListener(main);
    filter.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        View.notifyContext(GUI.context.marked(), false);
      }
    });
    box.add(filter);
    
    south.add(box, BorderLayout.EAST);
  }

  @Override
  void refresh() {
    Nodes nodes = GUI.context.marked();
    final boolean marked = nodes != null && nodes.size != 0;
    BaseXLayout.enable(filter, !GUIProp.filterrt && marked);
  }

  @Override
  void query(final boolean force) {
    final String text = area.getText();
    if(force || !text.equals(last)) {
      last = text;
      GUI.get().execute(Commands.XQUERY,
          text.trim().length() == 0 ? "." : text);
    }
  }

  @Override
  void quit() {
    GUIProp.xquerycmd = area.strings();
  }

  @Override
  void info(final String inf, final boolean ok) {
    final String text = ok ? "" : inf.replaceAll("Stopped.*", "");
    info.setText(text);
    info.setToolTipText(ok ? null : text);
    BaseXLayout.enable(info, !ok);
    if(ok) return;

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
        if(i > 0 && i + 1 < ll && last.charAt(i) < ' ') i--;
        final int p = i;
        while(i < ll && last.charAt(i) >= ' ') i++;
        area.mark(p, i + 1);
        return;
      }
      if(last.charAt(i) < ' ') {
        l++;
        c = 0;
      }
    }
  }
}

