package org.basex.gui.layout;

import java.awt.*;

import javax.swing.*;

/**
 * Project specific TabbedPane implementation.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class BaseXTabs extends JTabbedPane {
  /**
   * Default constructor.
   * @param win parent window
   */
  public BaseXTabs(final Window win) {
    BaseXLayout.addInteraction(this, win);
  }

  /**
   * Adds a component with a custom tab header.
   * @param content tab content
   * @param header tab header
   * @param index index
   */
  public void add(final Component content, final Component header, final int index) {
    add(content, index);
    setSelectedComponent(content);
    setTabComponentAt(getSelectedIndex(), header);
  }

  /* Tab image.
  private Image tabImage;
  /* Dragged mouse position.
  private Point dragPos;
  /* Index of dragged tab.
  private int dragIndex;

  /*
   * Adds drag and drop support.
  public void dragDrop() {
    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseDragged(final MouseEvent e) {
        if(dragIndex < 0) return;

        dragPos = e.getPoint();
        if(tabImage == null) {
          final Rectangle bounds = getUI().getTabBounds(BaseXTabs.this, dragIndex);
          final BufferedImage bi = new BufferedImage(getWidth(), getHeight(),
              BufferedImage.TYPE_INT_ARGB);
          Graphics g = bi.getGraphics();
          g.setClip(bounds);
          setDoubleBuffered(false);
          paintComponent(g);
          tabImage = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
          g = tabImage.getGraphics();
          g.drawImage(bi, 0, 0, bounds.width, bounds.height, bounds.x, bounds.y,
              bounds.x + bounds.width, bounds.y + bounds.height, BaseXTabs.this);
        }
        repaint();
      }
    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        dragIndex = getUI().tabForCoordinate(BaseXTabs.this, e.getX(), e.getY());
        if(dragIndex < 0) return;
      }

      @Override
      public void mouseReleased(final MouseEvent e) {
        if(tabImage != null) {
          final int tab = Math.min(getTabCount() - 2,
              getUI().tabForCoordinate(BaseXTabs.this, e.getX(), 10));
          if(tab >= 0) {
            final Component comp = getComponentAt(dragIndex);
            final Component head = getTabComponentAt(dragIndex);
            removeTabAt(dragIndex);
            add(comp, head, tab);
          }
        }
        dragIndex = -1;
        tabImage = null;
        repaint();
      }
    });
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    if(tabImage != null) g.drawImage(tabImage, dragPos.x, dragPos.y, this);
  }
  */
}
