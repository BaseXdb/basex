package org.basex.gui.editor;

import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXBack;
import org.basex.util.list.IntList;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Bar showing the line numbers of a text panel.
 */
public class LineNumberBar extends BaseXBack {
  /** Text offset from the borders of the bar. */
  private final int borderOffset = 3;
  /** Vertical line positions. */
  private final IntList lineYPositions = new IntList();
  /** Text size. */
  private Rectangle2D textBounds;
  /** Last line number. */
  private int lastLineNumber = 1;

  /** Constructor. */
  public LineNumberBar() {
    super();

    setBackground(GUIConstants.LGRAY);
    setForeground(GUIConstants.BLUE);

    setOpaque(true);

    textBounds = calcTextBounds();
  }

  @Override
  public int getWidth() {
    return (int) width();
  }

  @Override
  public void setFont(Font font) {
    super.setFont(font);
    textBounds = calcTextBounds();
  }

  /**
   * Set last line number.
   * @param n new last line number
   */
  public void setLastLineNumber(int n) {
    lastLineNumber = n;
    textBounds = calcTextBounds();
  }

  /**
   * Calculates the space needed to render the text of the last line number.
   * @return text size
   */
  private Rectangle2D calcTextBounds() {
    String lastVisibleLineNumberText = String.valueOf(lastLineNumber);
    return getFontMetrics(getFont()).getStringBounds(lastVisibleLineNumberText, getGraphics());
  }

  /**
   * Calculate the width of the line number bar.
   * @return line number bar width
   */
  private double width() {
    return textBounds.getWidth() + borderOffset + borderOffset + 1;
  }

  /** Empty the list with vertical line positions. */
  public void resetLineNumbers() {
    lineYPositions.reset();
  }

  /**
   * Add a vertical line position.
   * @param y vertical line position
   */
  public void addRenderedTextLine(int y) {
    lineYPositions.add(y);
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    int n = lineYPositions.size();
    for(int i = 0; i < n; i++) {
      g.drawString(String.valueOf(i + 1), borderOffset, lineYPositions.get(i));
    }

    int width = (int) width() - 1;
    g.setColor(GUIConstants.GRAY);
    g.drawLine(width, 0, width, getHeight());
  }
}
