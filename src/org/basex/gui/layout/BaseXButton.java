package org.basex.gui.layout;

import static org.basex.Text.*;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import org.basex.gui.GUI;
import org.basex.gui.GUICommand;
import org.basex.gui.dialog.Dialog;
import org.basex.util.Token;

/**
 * Project specific button implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BaseXButton extends JButton {
  /**
   * Default Constructor.
   * @param l button title
   * @param hlp help text
   * @param win parent window
   */
  public BaseXButton(final String l, final byte[] hlp, final Window win) {
    super(l);
    setOpaque(false);
    BaseXLayout.addInteraction(this, hlp, win);
    if(!(win instanceof Dialog)) return;

    final Dialog d = (Dialog) win;
    addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final String text = getText();
        if(text.equals(BUTTONCANCEL)) d.cancel();
        else if(text.equals(BUTTONOK)) d.close();
        else d.action(text);
      }
    });
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) d.cancel();
      }
    });
  }

  /**
   * Default Constructor.
   * @param gui main window
   * @param img image reference
   * @param hlp help text
   */
  public BaseXButton(final GUI gui, final String img, final byte[] hlp) {
    super(BaseXLayout.icon("cmd-" + img));
    setOpaque(false);
    BaseXLayout.addInteraction(this, hlp, gui);
    if(hlp != null) setToolTipText(Token.string(hlp));

    // trim horizontal button margins
    final Insets in = getMargin();
    in.left /= 4;
    in.right /= 4;
    if(in.top < in.left) setMargin(in);
  }

  /**
   * Creates a new button.
   * @param cmd command
   * @param gui reference to main window
   * @return button
   */
  public static BaseXButton command(final GUICommand cmd, final GUI gui) {
    // images are defined via the 'cmd-' prefix and the command in lower case
    final BaseXButton button = new BaseXButton(gui,
        cmd.toString().toLowerCase(), Token.token(cmd.help()));
    button.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        cmd.execute(gui);
      }
    });
    return button;
  }
}
