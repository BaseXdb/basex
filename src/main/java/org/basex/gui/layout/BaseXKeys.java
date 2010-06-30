package org.basex.gui.layout;

import static java.awt.event.KeyEvent.*;
import java.awt.Event;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.basex.core.Prop;

/**
 * This class offers system-dependent key mappings. Each key mapping is
 * represented as an integer array. The semantics is as follows:
 * <ul>
 *   <li>The first integer defines the modifier keys
 *   <li>The second integer defines the key code
 *   <li>If a third integer is set, no other modifier keys are allowed
 * </ul>
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class BaseXKeys {

  /** Mac OS flag. */
  private static final boolean MAC = Prop.MAC;
  /** Shift key. */
  private static final int SHF = Event.SHIFT_MASK;
  /** Alt key. */
  private static final int ALT = Event.ALT_MASK;
  /** Ctrl key. */
  private static final int CTRL = Event.CTRL_MASK;
  /** Shortcut key (CTRL/META). */
  public static final int SC = Prop.MAC ? Event.META_MASK : Event.CTRL_MASK;

  /** Find search term. */
  public static final int[] FIND = { SC, VK_F, 0 };
  /** Find next hit. */
  public static final int[] FINDNEXT = { SC, VK_G, 0 };
  /** Find previous hit. */
  public static final int[] FINDPREV = { SC | SHF, VK_G, 0 };
  /** Select all. */
  public static final int[] SELECTALL = { SC, VK_A, 0 };
  /** Browse back. */
  public static final int[] GOBACK = { MAC ? SC : ALT, VK_LEFT, 0 };
  /** Browse back. */
  public static final int[] GOBACK2 = { 0, VK_BACK_SPACE, 0 };
  /** Browse forward. */
  public static final int[] GOFORWARD = { MAC ? SC : ALT, VK_RIGHT, 0 };
  /** Browse up. */
  public static final int[] GOUP = { MAC ? SC : ALT, VK_UP, 0 };
  /** Browse home. */
  public static final int[] GOHOME = { MAC ? SC : ALT, VK_HOME, 0 };
  /** Copy. */
  public static final int[] COPY = { SC, VK_C, 0 };
  /** Cut. */
  public static final int[] CUT = { SC, VK_X, 0 };
  /** Paste. */
  public static final int[] PASTE = { SC, VK_V, 0 };
  /** Undo. */
  public static final int[] UNDO = { SC, VK_Z, 0 };
  /** Redo. */
  public static final int[] REDO = { MAC ? SC | SHF : SC,
      MAC ? VK_Z : VK_Y, 0 };

  /** Word right. */
  public static final int[] NEXTWORD = { MAC ? ALT : SC, VK_RIGHT };
  /** Word left. */
  public static final int[] PREVWORD = { MAC ? ALT : SC, VK_LEFT };
  /** Right. */
  public static final int[] NEXT = { 0, VK_RIGHT };
  /** Left. */
  public static final int[] PREV = { 0, VK_LEFT };
  /** Up. */
  public static final int[] PREVLINE = { 0, VK_UP };
  /** Down. */
  public static final int[] NEXTLINE = { 0, VK_DOWN };
  /** Beginning of line. */
  public static final int[] LINESTART =
    { MAC ? SC : 0, MAC ? VK_LEFT : VK_HOME };
  /** End of line. */
  public static final int[] LINEEND = { MAC ? SC : 0, MAC ? VK_RIGHT : VK_END };
  /** Beginning of text. */
  public static final int[] TEXTSTART = { SC, MAC ? VK_UP : VK_HOME };
  /** End of text. */
  public static final int[] TEXTEND = { SC, MAC ? VK_DOWN : VK_END };
  /** Page up. */
  public static final int[] PREVPAGE = { 0, VK_PAGE_UP };
  /** Page down. */
  public static final int[] NEXTPAGE = { 0, VK_PAGE_DOWN };

  /** Scroll up. */
  public static final int[] SCROLLUP = { MAC ? ALT : SC, VK_UP, 0 };
  /** Scroll down. */
  public static final int[] SCROLLDOWN = { MAC ? ALT : SC, VK_DOWN, 0 };

  /** Delete word backwards. */
  public static final int[] DELPREVWORD = { MAC ? ALT : SC, VK_BACK_SPACE, 0 };
  /** Delete word. */
  public static final int[] DELNEXTWORD = { MAC ? ALT : SC, VK_DELETE, 0 };
  /** Delete line to begin. */
  public static final int[] DELLINESTART = { SC | (MAC ? 0 : SHF),
    VK_BACK_SPACE, 0 };
  /** Delete line to end. */
  public static final int[] DELLINEEND = { SC | (MAC ? 0 : SHF), VK_DELETE, 0 };
  /** Delete backwards. */
  public static final int[] DELPREV = { 0, VK_BACK_SPACE };
  /** Delete. */
  public static final int[] DELNEXT = { 0, VK_DELETE };

  /** Escape. */
  public static final int[] ESCAPE = { 0, VK_ESCAPE };
  /** Next panel. */
  public static final int[] NEXTTAB = { CTRL, VK_TAB, 0 };
  /** Previous panel. */
  public static final int[] PREVTAB = { CTRL | SHF, VK_TAB, 0 };
  /** Context menu. */
  public static final int[] CONTEXT = { 0, VK_CONTEXT_MENU };
  /** Space key. */
  public static final int[] SPACE = { 0, VK_SPACE };
  /** Tab key. */
  public static final int[] TAB = { 0, VK_TAB };
  /** Enter. */
  public static final int[] ENTER = { 0, VK_ENTER };
  /** Execute. */
  public static final int[] EXEC = { SC, VK_ENTER, 0 };

  /** Increment size. */
  public static final int[] INCFONT1 = { SC, VK_PLUS, 0 };
  /** Increment size (2nd variant). */
  public static final int[] INCFONT2 = { SC, VK_EQUALS, 0 };
  /** Decrease size. */
  public static final int[] DECFONT = { SC, VK_MINUS, 0 };
  /** Standard size. */
  public static final int[] NORMFONT = { SC, VK_0, 0 };
  /** Jump to input bar. */
  public static final int[] INPUT1 = { SC, VK_L, 0 };
  /** Jump to input bar. */
  public static final int[] INPUT2 = { 0, VK_F6, 0 };

  /** Private constructor. */
  private BaseXKeys() { }

  /**
   * Returns true if the specified key combination was pressed.
   * @param op operation
   * @param e key event
   * @return result of check
   */
  public static boolean pressed(final int[] op, final KeyEvent e) {
    final int code = e.getKeyCode() == 0 ? e.getKeyChar() : e.getKeyCode();
    int mod = e.getModifiers();
    if(op.length != 3) mod &= op[0];
    return mod == op[0] && code == op[1];
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
    return !MAC && e.isAltDown() || (SC & e.getModifiers()) == SC;
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
