package org.basex.gui.layout;

import static java.awt.event.KeyEvent.*;
import java.awt.Event;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.basex.core.Prop;

/**
 * This class offers system-dependent key mappings.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BaseXKeys {
  /** Mac OS flag. */
  private static final boolean MAC = Prop.MAC;
  /** Shortcut key (CTRL/META). */
  public static final int SC =
    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
  /** Shift key. */
  private static final int SHF = Event.SHIFT_MASK;
  /** Alt key. */
  private static final int ALT = Event.ALT_MASK;

  /** Find search term. */
  public static final int[] FIND = { SC, VK_F };
  /** Find next hit. */
  public static final int[] FINDNEXT = { SC, VK_G };
  /** Find previous hit. */
  public static final int[] FINDPREV = { SC | SHF, VK_G };
  /** Select all. */
  public static final int[] SELECTALL = { SC, VK_A };
  /** Browse back. */
  public static final int[] GOBACK = { MAC ? SC : ALT, VK_LEFT };
  /** Browse forward. */
  public static final int[] GOFORWARD = { MAC ? SC : ALT, VK_RIGHT };
  /** Browse up. */
  public static final int[] GOUP = { MAC ? SC : ALT, VK_LEFT };
  /** Browse home. */
  public static final int[] GOHOME = { MAC ? SC : ALT, VK_RIGHT };
  /** Copy. */
  public static final int[] COPY = { SC, VK_C };
  /** Cut. */
  public static final int[] CUT = { SC, VK_X };
  /** Paste. */
  public static final int[] PASTE = { SC, VK_V };
  /** Undo. */
  public static final int[] UNDO = { SC, VK_Z };
  /** Redo. */
  public static final int[] REDO = { MAC ? SC | SHF : SC, MAC ? VK_Z : VK_Y };
  /** Word Right. */
  public static final int[] WORDRIGHT = { MAC ? ALT : SC, VK_RIGHT };
  /** Word Left. */
  public static final int[] WORDLEFT = { MAC ? ALT : SC, VK_LEFT };
  /** Right. */
  public static final int[] RIGHT = { 0, VK_RIGHT };
  /** Left. */
  public static final int[] LEFT = { 0, VK_LEFT };
  /** Up. */
  public static final int[] UP = { 0, VK_UP };
  /** Down. */
  public static final int[] DOWN = { 0, VK_DOWN };
  /** Scroll up. */
  public static final int[] SCROLLUP = { MAC ? ALT : SC, VK_UP };
  /** Scroll down. */
  public static final int[] SCROLLDOWN = { MAC ? ALT : SC, VK_DOWN };
  /** Beginning of line. */
  public static final int[] BOL = { MAC ? SC : 0, MAC ? VK_LEFT : VK_HOME };
  /** End of line. */
  public static final int[] EOL = { MAC ? SC : 0, MAC ? VK_RIGHT : VK_END };
  /** Beginning of text. */
  public static final int[] BOT = { SC, MAC ? VK_UP : VK_HOME };
  /** End of text. */
  public static final int[] EOT = { SC, MAC ? VK_DOWN : VK_END };
  /** Page up. */
  public static final int[] PAGEUP = { 0, VK_PAGE_UP };
  /** Page down. */
  public static final int[] PAGEDOWN = { 0, VK_PAGE_DOWN };
  /** Next panel. */
  public static final int[] NEXTTAB = {
    MAC ? ALT | SHF : SC, MAC ? VK_RIGHT : VK_TAB };
  /** Previous panel. */
  public static final int[] PREVTAB = {
    MAC ? ALT | SHF : SC | SHF, MAC ? VK_LEFT : VK_TAB };
  /** Delete word backwards. */
  public static final int[] DELWORDBACK = { SC, MAC ? 0 : VK_BACK_SPACE };
  /** Delete word. */
  public static final int[] DELWORD = { SC, MAC ? 0 : VK_DELETE };
  /** Delete backwards. */
  public static final int[] DELBACK = { 0, VK_BACK_SPACE };
  /** Delete. */
  public static final int[] DEL = { 0, VK_DELETE };
  /** Enter. */
  public static final int[] ENTER = { 0, VK_ENTER };
  /** Escape. */
  public static final int[] ESCAPE = { 0, VK_ESCAPE };
  /** Increment size. */
  public static final int[] INCFONT1 = { SC, VK_PLUS };
  /** Increment size (2nd variant). */
  public static final int[] INCFONT2 = { SC, VK_EQUALS };
  /** Decrease size. */
  public static final int[] DECFONT = { SC, VK_MINUS };
  /** Standard size. */
  public static final int[] NORMFONT = { SC, VK_0 };
  /** Context menu. */
  public static final int[] CONTEXT = { 0, VK_CONTEXT_MENU };
  /** Space key. */
  public static final int[] SPACE = { 0, VK_SPACE };
  /** Tab key. */
  public static final int[] TAB = { 0, VK_TAB };
  
  /** Private constructor. */
  private BaseXKeys() { }

  /**
   * Returns true if the specified key combination was pressed.
   * @param op operation
   * @param e key event
   * @return result of check
   */
  public static boolean pressed(final int[] op, final KeyEvent e) {
    final int c = e.getKeyCode() == 0 ? e.getKeyChar() : e.getKeyCode();
    return (op[0] & e.getModifiers()) == op[0] && c == op[1];
  }

  /**
   * Returns true if the system's shortcut key was pressed.
   * @param e input event
   * @return result of check
   */
  public static boolean sc(final InputEvent e) {
    return (SC & e.getModifiers()) == SC;
  }

  /**
   * Returns true if a typed key should be ignored.
   * @param e key event
   * @return result of check
   */
  public static boolean ignoreTyped(final KeyEvent e) {
    // Mac offers special characters via ALT, Windows/Linux don't..
    return (!MAC && e.isAltDown()) || (SC & e.getModifiers()) == SC;
  }

  /**
   * Returns true if the pressed key is a modifier.
   * @param e key event
   * @return result of check
   */
  public static boolean modifier(final KeyEvent e) {
    final int c = e.getKeyCode();
    return c == VK_ALT || c == VK_SHIFT || c == VK_META || c == VK_CONTROL ||
      c == VK_ESCAPE;
  }
}
