package org.basex.gui.layout;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.gui.listener.*;

/**
 * Project-specific Popup menu implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Lukas Kircher
 */
public final class BaseXPopup extends JPopupMenu {
  /** Reference to the main window. */
  private final GUI gui;
  /** Popup commands. */
  private final GUICommand[] commands;

  /**
   * Constructor.
   * @param comp component reference
   * @param commands associated commands
   */
  public BaseXPopup(final BaseXPanel comp, final GUICommand... commands) {
    this(comp, comp.gui, commands);
  }

  /**
   * Constructor.
   * @param comp component reference
   * @param gui gui reference
   * @param commands associated commands
   */
  public BaseXPopup(final JComponent comp, final GUI gui, final GUICommand... commands) {
    this.gui = gui;
    this.commands = commands;

    // both listeners must be implemented to support different platforms
    comp.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        if(!gui.updating && e.isPopupTrigger())
          show(e.getComponent(), e.getX() - 10, e.getY() - 15);
      }
      @Override
      public void mouseReleased(final MouseEvent e) {
        mousePressed(e);
      }
    });
    comp.addKeyListener((KeyPressedListener) e -> {
      if(!gui.updating && CONTEXT.is(e)) {
        show(e.getComponent(), 10, 10);
      } else {
        for(final GUICommand cmd : commands) {
          if(cmd instanceof GUIPopupCmd) {
            for(final BaseXKeys sc : ((GUIPopupCmd) cmd).shortcuts()) {
              if(sc.is(e)) {
                cmd.execute(gui);
                e.consume();
                return;
              }
            }
          }
        }
      }
    });

    final StringBuilder mnemCache = new StringBuilder();
    for(final GUICommand cmd : commands) {
      if(cmd == null) {
        addSeparator();
      } else {
        final JMenuItem item = GUIMenu.newItem(cmd, gui, mnemCache);
        item.setAccelerator(BaseXLayout.keyStroke(cmd));
        add(item);
      }
    }
  }

  @Override
  public void show(final Component comp, final int x, final int y) {
    final int cl = commands.length;
    for(int c = 0; c < cl; c++) {
      if(commands[c] != null) {
        final AbstractButton button = (AbstractButton) getComponent(c);
        button.setEnabled(commands[c].enabled(gui));
        button.setSelected(commands[c].selected(gui));
      }
    }
    super.show(comp, x, y);
  }
}
