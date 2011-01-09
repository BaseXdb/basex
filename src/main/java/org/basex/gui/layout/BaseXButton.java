package org.basex.gui.layout;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;
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
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class BaseXButton extends JButton {
  /**
   * Constructor for text buttons.
   * @param l button title
   * @param win parent window
   */
  public BaseXButton(final String l, final Window win) {
    super(l);
    setOpaque(false);
    BaseXLayout.addInteraction(this, null, win);
    if(!(win instanceof Dialog)) return;

    final Dialog d = (Dialog) win;
    addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final String text = getText();
        if(text.equals(BUTTONCANCEL)) d.cancel();
        else if(text.equals(BUTTONOK)) d.close();
        else d.action(e.getSource());
      }
    });
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(ESCAPE.is(e)) d.cancel();
      }
    });
  }

  /**
   * Constructor for image buttons.
   * @param gui main window
   * @param img image reference
   * @param hlp help text
   */
  public BaseXButton(final Window gui, final String img, final byte[] hlp) {
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
   * Creates a new button for the specified command.
   * @param cmd command
   * @param gui reference to main window
   * @return button
   */
  public static BaseXButton command(final GUICommand cmd, final GUI gui) {
    // images are defined via the 'cmd-' prefix and the command in lower case
    final BaseXButton button = new BaseXButton(gui,
        cmd.toString().toLowerCase(), Token.token(cmd.help()));
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        cmd.execute(gui);
      }
    });
    return button;
  }

  @Override
  public void setEnabled(final boolean flag) {
    if(flag != isEnabled()) super.setEnabled(flag);
  }
}
