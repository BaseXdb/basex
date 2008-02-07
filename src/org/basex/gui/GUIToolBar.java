package org.basex.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLayout;
import org.basex.util.Token;
import static org.basex.gui.GUIConstants.*;

/**
 * This is the toolbar of the main window.
 * The toolbar contents are defined in {@link GUIConstants#TOOLBAR}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class GUIToolBar extends JToolBar implements ActionListener {
  /**
   * Default Constructor.
   */
  public GUIToolBar() {
    super();
    setBorder(new EmptyBorder(4, 2, 0, 0));
    setFloatable(false);
    setOpaque(false);

    for(final GUICommands cmd : TOOLBAR) {
      if(cmd == null) {
        addSeparator();
      } else {
        // the image equals the 'cmd-' prefix and the command in lower case
        final ImageIcon icon = GUI.icon("cmd-" + cmd.name().toLowerCase());
        final String info = cmd.help;
        final BaseXButton button = new BaseXButton(icon, Token.token(info));
        button.setToolTipText(info);
        button.setActionCommand(cmd.name());
        button.addActionListener(this);
        add(button);
      }
    }

    refresh();
  }

  /**
   * Reacts on button clicks.
   * @param e action event
   */
  public void actionPerformed(final ActionEvent e) {
    GUICommands.get(e.getActionCommand()).execute();
  }

  /**
   * Refresh buttons.
   */
  void refresh() {
    final boolean db = GUI.context.db();
    for(int b = 0; b < TOOLBAR.length; b++) {
      if(TOOLBAR[b] == null) continue;
      TOOLBAR[b].refresh((BaseXButton) getComponentAtIndex(b), db);
    }
  }

  /** Separator implementation. */
  static class Separator extends BaseXBack {
    /** Constructor. */
    Separator() {
      BaseXLayout.setWidth(this, 8);
    }

    @Override
    public void paintComponent(final Graphics g) {
      super.paintComponent(g);
      g.setColor(Color.gray);
      g.drawLine(3, 0, 3, getHeight() - 4);
      g.setColor(Color.white);
      g.drawLine(4, 1, 4, getHeight() - 3);
    }
  }
}
