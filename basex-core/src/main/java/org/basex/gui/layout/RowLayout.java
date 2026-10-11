package org.basex.gui.layout;

/**
 * This LayoutManager is a simplified version of the TableLayout. The added components
 * will be rendered as rows.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RowLayout extends AxisLayout {
  /**
   * Default constructor.
   */
  public RowLayout() {
    this(0);
  }

  /**
   * Constructor, specifying gap between components.
   * @param gap gap
   */
  public RowLayout(final int gap) {
    super(gap, false);
  }
}
