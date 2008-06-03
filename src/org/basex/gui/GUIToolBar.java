package org.basex.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import org.basex.gui.layout.BaseXButton;
import org.basex.util.Token;

/**
 * This is the toolbar of the main window.
 * The toolbar contents are defined in {@link GUIConstants#TOOLBAR}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class GUIToolBar extends JToolBar {
  /** Toolbar commands. */
  private final GUICommand[] toolbar;
  
  /**
   * Default Constructor.
   * @param tb toolbar commands
   */
  public GUIToolBar(final GUICommand[] tb) {
    super();
    setFloatable(false);
    toolbar = tb;

    for(final GUICommand cmd : toolbar) {
      if(cmd == null) {
        addSeparator();
      } else {
        final BaseXButton button = newButton(cmd);
        button.setFocusable(false);
        add(button);
      }
    }
    refresh();
  }

  /**
   * Refresh buttons.
   */
  public void refresh() {
    for(int b = 0; b < toolbar.length; b++) {
      if(toolbar[b] == null) continue;
      toolbar[b].refresh((BaseXButton) getComponent(b));
    }
  }

  /**
   * Creates a new button.
   * @param cmd command
   * @return button
   */
  public static BaseXButton newButton(final GUICommand cmd) {
    // the image equals the 'cmd-' prefix and the command in lower case
    final ImageIcon icon = GUI.icon("cmd-" + cmd.toString().toLowerCase());
    final String info = cmd.help();
    final BaseXButton button = new BaseXButton(icon, Token.token(info));
    button.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        cmd.execute();
      }
    });
    button.trim();
    return button;
  }
}
