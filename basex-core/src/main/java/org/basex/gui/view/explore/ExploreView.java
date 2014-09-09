package org.basex.gui.view.explore;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.data.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.*;

/**
 * This view allows the input of database queries.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ExploreView extends View {
  /** Header string. */
  private final BaseXHeader header;
  /** Current search panel. */
  private final ExploreArea search;
  /** Filter button. */
  private final AbstractButton filter;

  /**
   * Default constructor.
   * @param man view manager
   */
  public ExploreView(final ViewNotifier man) {
    super(EXPLOREVIEW, man);
    border(5).layout(new BorderLayout(0, 4));

    header = new BaseXHeader(EXPLORER);

    filter = BaseXButton.command(GUIMenuCmd.C_FILTER, gui);
    filter.addKeyListener(this);

    final BaseXBack buttons = new BaseXBack(Fill.NONE);
    buttons.layout(new TableLayout(1, 1, 1, 0)).border(0, 0, 4, 0);
    buttons.add(filter);

    final BaseXBack b = new BaseXBack(Fill.NONE).layout(new BorderLayout());
    b.add(buttons, BorderLayout.WEST);
    b.add(header, BorderLayout.EAST);
    add(b, BorderLayout.NORTH);

    search = new ExploreArea(this);
    add(search, BorderLayout.CENTER);

    refreshLayout();
  }

  @Override
  public void refreshInit() {
    search.init();
  }

  @Override
  public void refreshFocus() { }

  @Override
  public void refreshMark() {
    final DBNodes marked = gui.context.marked;
    filter.setEnabled(!gui.gopts.get(GUIOptions.FILTERRT) &&
        marked != null && marked.size() != 0);
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    refreshMark();
  }

  @Override
  public void refreshUpdate() { }

  @Override
  public void refreshLayout() {
    header.refreshLayout();
    refreshMark();
  }

  @Override
  public boolean visible() {
    return gui.gopts.get(GUIOptions.SHOWEXPLORE);
  }

  @Override
  public void visible(final boolean v) {
    gui.gopts.set(GUIOptions.SHOWEXPLORE, v);
  }

  @Override
  protected boolean db() {
    return true;
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    // overwrite default interactions
  }
}
