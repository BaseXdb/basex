package org.basex.gui.layout;

/**
 * This LayoutManager is a simplified version of the TableLayout. The added components
 * will be rendered as columns.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ColumnLayout extends AxisLayout {
  /**
   * Default constructor.
   */
  public ColumnLayout() {
    this(0);
  }

  /**
   * Constructor, specifying gap between components.
   * @param gap gap
   */
  public ColumnLayout(final int gap) {
    super(gap, true);
  }
}
