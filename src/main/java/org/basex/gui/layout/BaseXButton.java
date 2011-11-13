package org.basex.gui.layout;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Locale;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import org.basex.gui.GUI;
import org.basex.gui.GUICommand;
import org.basex.gui.dialog.Dialog;
import org.basex.util.Token;

/**
 * Project specific button implementation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class BaseXButton extends JButton {
  /**
   * Constructor for text buttons.
   * @param l button title
   * @param win parent window
   */
  public BaseXButton(final String l, final Window win) {
    super(l);
    BaseXLayout.addInteraction(this, win);
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
    BaseXLayout.addInteraction(this, gui, hlp);
    if(hlp != null) setToolTipText(Token.string(hlp));

    // trim horizontal button margins
    final Insets in = getMargin();
    in.left /= 4;
    in.right /= 4;
    if(in.top < in.left) setMargin(in);
  }

  /**
   * Sets the label borders.
   * @param t top distance
   * @param l left distance
   * @param b bottom distance
   * @param r right distance
   * @return self reference
   */
  public BaseXButton border(final int t, final int l, final int b,
      final int r) {
    setBorder(new EmptyBorder(t, l, b, r));
    return this;
  }

  /**
   * Creates a new image button for the specified command.
   * @param cmd command
   * @param gui reference to main window
   * @return button
   */
  public static BaseXButton command(final GUICommand cmd, final GUI gui) {
    // images are defined via the 'cmd-' prefix and the command in lower case
    final BaseXButton button = new BaseXButton(gui,
        cmd.toString().toLowerCase(Locale.ENGLISH), Token.token(cmd.help()));
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        cmd.execute(gui);
      }
    });
    return button;
  }

  /**
   * Sets a mnemomic.
   */
  public void setMnemonic() {
    setMnemonics(this);
  }

  /**
   * Sets mnemomics for the specified buttons.
   * @param buttons buttons
   */
  public static void setMnemonics(final AbstractButton... buttons) {
    setMnemonics(new StringBuilder(), buttons);
  }

  /**
   * Sets mnemomics for the specified buttons.
   * @param mnem assigned mnemonics
   * @param buttons buttons
   */
  public static void setMnemonics(final StringBuilder mnem,
      final AbstractButton... buttons) {

    for(final AbstractButton b : buttons) {
      // find and assign unused mnemomic
      final String label = b.getText();
      for(int l = 0; l < label.length(); l++) {
        final char ch = Character.toLowerCase(label.charAt(l));
        if(ch == ' ' || mnem.indexOf(Character.toString(ch)) != -1) continue;
        b.setMnemonic(ch);
        mnem.append(ch);
        break;
      }
    }
  }

  @Override
  public void setEnabled(final boolean flag) {
    if(flag != isEnabled()) super.setEnabled(flag);
  }
}
