package org.basex.gui.view.query;

import static org.basex.Text.*;
import static org.basex.gui.GUIConstants.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.border.EmptyBorder;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.View;
import org.basex.util.Token;

/**
 * This view allows the input of database queries.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class QueryView extends View {
  /** Reference to the simple query mode. */
  private static final int SIMPLE = 1;
  /** Input mode panels. */
  private static final int NPANELS = SEARCHMODE.length;
  /** Input mode panels. */
  QueryPanel[] panels = new QueryPanel[NPANELS];
  /** Input mode. */
  int mode;

  /** Input mode buttons. */
  private final BaseXButton[] input = new BaseXButton[NPANELS];
  /** Query panel. */
  private QueryPanel search;
  /** Header string. */
  final BaseXLabel header;
  /** Button box. */
  private final BaseXBack back;

  /**
   * Default constructor.
   * @param help help text
   */
  public QueryView(final byte[] help) {
    super(help);
    setLayout(new BorderLayout(0, 4));
    setBorder(8, 8, 8, 8);

    back = new BaseXBack(FILL.NONE);
    back.setLayout(new BorderLayout());
    header = new BaseXLabel(GUIConstants.QUERYVIEW, 10);
    back.add(header, BorderLayout.NORTH);
    
    Box box = new Box(BoxLayout.X_AXIS);
    box.setBorder(new EmptyBorder(0, 0, 8, 0));
    for(int i = 0; i < NPANELS; i++) {
      final int m = i;
      input[i] = new BaseXButton(SEARCHMODE[i], HELPSEARCH[i]);
      input[i].addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          mode = m;
          refreshLayout();
        }
      });
      box.add(input[i]);
      box.add(Box.createHorizontalStrut(6));
      if(i == 0) {
        panels[i] = new QueryArea(this);
      } else if(i == SIMPLE) {
        panels[i] = new QuerySimple(this);
      }
    }
    box.add(Box.createHorizontalStrut(6));

    back.add(box, BorderLayout.CENTER);

    refresh();
  }

  @Override
  public void refreshInit() {
    if(!GUI.context.db()) {
      for(int i = 0; i < NPANELS; i++) panels[i].finish();
    } else {
      refreshLayout();
    }
  }

  @Override
  public void refreshFocus() { }

  @Override
  public void refreshMark() {
    if(search != null) search.refresh();
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) { }

  @Override
  public void refreshUpdate() {
    refreshLayout();
  }

  @Override
  public void refreshLayout() {
    for(int i = 0; i < NPANELS; i++) BaseXLayout.select(input[i], mode == i);
    removeAll();
    add(back, BorderLayout.NORTH);
    search = panels[mode];
    search.init();
    search.refresh();
    if(GUIProp.execrt) search.query(GUIProp.showquery);
    revalidate();
    repaint();
  }

  /**
   * Notifies all panels of the GUI termination.
   */
  public void quit() {
    for(final QueryPanel p : panels) p.quit();
  }

  /**
   * Refreshes the panel components.
   */
  void refresh() {
    BaseXLayout.select(input[mode], true);
    header.setFont(GUIConstants.lfont);
    header.setForeground(COLORS[16]);
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    if(e.isAltDown()) super.keyPressed(e);
  }

  /**
   * Handles info messages resulting from a query execution.
   * @param info info message
   * @param ok true if query was successful
   * @return true if info was processed
   */
  public boolean info(final String info, final boolean ok) {
    if(search != null) search.info(info, ok);
    return search != null && mode != SIMPLE;
  }

  /**
   * Sets a new XQuery request.
   * @param xq XQuery
   */
  public void setXQuery(final byte[] xq) {
    panels[0].last = Token.string(xq);
    mode = 0;
    refreshLayout();
  }

  /**
   * Returns the last XQuery input..
   * @return XQuery
   */
  public byte[] getXQuery() {
    return Token.token(panels[0].last);
  }
}
