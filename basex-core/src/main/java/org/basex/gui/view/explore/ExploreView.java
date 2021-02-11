package org.basex.gui.view.explore;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.*;
import org.basex.query.value.seq.*;

/**
 * This view allows the input of database queries.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ExploreView extends View {
  /** Current search panel. */
  private final ExploreArea search;
  /** Filter button. */
  private final AbstractButton filter;

  /**
   * Default constructor.
   * @param notifier view notifier
   */
  public ExploreView(final ViewNotifier notifier) {
    super(EXPLOREVIEW, notifier);
    border(5).layout(new BorderLayout(0, 4));

    filter = BaseXButton.command(GUIMenuCmd.C_FILTER, gui);

    final BaseXBack buttons = new BaseXBack(false);
    buttons.layout(new ColumnLayout(1)).border(0, 0, 4, 0);
    buttons.add(filter);

    final BaseXBack b = new BaseXBack(false).layout(new BorderLayout());
    b.add(buttons, BorderLayout.WEST);
    b.add(new BaseXHeader(EXPLORER), BorderLayout.EAST);
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
    filter.setEnabled(!gui.gopts.get(GUIOptions.FILTERRT) && marked != null);
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    refreshMark();
  }

  @Override
  public void refreshUpdate() { }

  @Override
  public void refreshLayout() {
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
