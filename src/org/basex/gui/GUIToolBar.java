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
  private final GUICommand[] cmd;
  /** Reference to main window. */
  protected final GUI gui;
  
  /**
   * Default Constructor.
   * @param tb toolbar commands
   * @param main reference to the main window
   */
  public GUIToolBar(final GUICommand[] tb, final GUI main) {
    super();
    setFloatable(false);
    cmd = tb;
    gui = main;

    for(final GUICommand c : cmd) {
      if(c == null) {
        addSeparator();
      } else {
        final BaseXButton button = newButton(c, gui);
        button.setFocusable(false);
        add(button);
      }
    }
  }

  /**
   * Refresh buttons.
   */
  public void refresh() {
    for(int b = 0; b < cmd.length; b++) {
      if(cmd[b] != null) cmd[b].refresh(gui, (BaseXButton) getComponent(b));
    }
  }

  /**
   * Creates a new button.
   * @param cmd command
   * @param gui reference to main window
   * @return button
   */
  public static BaseXButton newButton(final GUICommand cmd, final GUI gui) {
    // the image equals the 'cmd-' prefix and the command in lower case
    final ImageIcon icon = GUI.icon("cmd-" + cmd.toString().toLowerCase());
    final String info = cmd.help();
    final BaseXButton button = new BaseXButton(icon, Token.token(info));
    button.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        cmd.execute(gui);
      }
    });
    button.trim();
    return button;
  }
}
