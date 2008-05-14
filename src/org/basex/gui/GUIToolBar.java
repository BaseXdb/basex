package org.basex.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.basex.gui.layout.BaseXButton;
import org.basex.util.Token;
import static org.basex.gui.GUIConstants.*;

/**
 * This is the toolbar of the main window.
 * The toolbar contents are defined in {@link GUIConstants#TOOLBAR}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class GUIToolBar extends JToolBar {
  /**
   * Default Constructor.
   */
  public GUIToolBar() {
    super();
    setFloatable(false);
    setOpaque(false);
    setBorder(new EmptyBorder(2, 2, 0, 0));

    for(final GUICommand cmd : TOOLBAR) {
      if(cmd == null) {
        addSeparator();
      } else {
        // the image equals the 'cmd-' prefix and the command in lower case
        final ImageIcon icon = GUI.icon("cmd-" + cmd.toString().toLowerCase());
        final String info = cmd.help();
        final BaseXButton button = new BaseXButton(icon, Token.token(info));
        button.setToolTipText(info);
        button.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {
            cmd.execute();
          }
        });
        add(button);
      }
    }
    refresh();
  }

  /**
   * Refresh buttons.
   */
  void refresh() {
    for(int b = 0; b < TOOLBAR.length; b++) {
      if(TOOLBAR[b] == null) continue;
      TOOLBAR[b].refresh((BaseXButton) getComponent(b));
    }
  }
}
