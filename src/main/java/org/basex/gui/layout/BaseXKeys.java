package org.basex.gui.layout;

import static java.awt.event.KeyEvent.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.core.Prop.MAC;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * This class offers system-dependent key mappings.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public enum BaseXKeys {
  /** Find search term.  */ FIND(SC, VK_F, true),
  /** Find next hit.     */ FINDNEXT(SC, VK_G, true),
  /** Find next hit.     */ FINDNEXT2(MAC ? SC : 0, VK_F3, true),
  /** Find previous hit. */ FINDPREV(SC | SHF, VK_G, true),
  /** Find next hit.     */ FINDPREV2(MAC ? SC | SHF : SHF, VK_F3, true),
  /** Select all.        */ SELECTALL(SC, VK_A, true),
  /** Browse back.       */ GOBACK(MAC ? SC : ALT, VK_LEFT, true),
  /** Browse back.       */ GOBACK2(VK_BACK_SPACE, true),
  /** Browse forward.    */ GOFORWARD(MAC ? SC : ALT, VK_RIGHT, true),
  /** Browse up.         */ GOUP(MAC ? SC : ALT, VK_UP, true),
  /** Browse home.       */ GOHOME(MAC ? SC : ALT, VK_HOME, true),
  /** Copy.              */ COPY1(SC, VK_C, true),
  /** Copy.              */ COPY2(SC, VK_INSERT, true),
  /** Cut.               */ CUT1(SC, VK_X, true),
  /** Cut.               */ CUT2(SHF, VK_DELETE, true),
  /** Paste.             */ PASTE1(SC, VK_V, true),
  /** Paste.             */ PASTE2(SHF, VK_INSERT, true),
  /** Undo.              */ UNDO(SC, VK_Z, true),
  /** Redo.              */ REDO(MAC ? SC | SHF : SC, MAC ? VK_Z : VK_Y, true),

  /** Word right.        */ NEXTWORD(MAC ? ALT : SC, VK_RIGHT),
  /** Word left.         */ PREVWORD(MAC ? ALT : SC, VK_LEFT),
  /** Right.             */ NEXT(VK_RIGHT),
  /** Left.              */ PREV(VK_LEFT),
  /** Up.                */ PREVLINE(VK_UP),
  /** Down.              */ NEXTLINE(VK_DOWN),
  /** Beginning of line. */ LINESTART (MAC ? SC : 0, MAC ? VK_LEFT : VK_HOME),
  /** End of line.       */ LINEEND(MAC ? SC : 0, MAC ? VK_RIGHT : VK_END),
  /** Beginning of text. */ TEXTSTART(SC, MAC ? VK_UP : VK_HOME),
  /** End of text.       */ TEXTEND(SC, MAC ? VK_DOWN : VK_END),
  /** Page up.           */ PREVPAGE(VK_PAGE_UP),
  /** Page down.         */ NEXTPAGE(VK_PAGE_DOWN),

  /** Scroll up.         */ SCROLLUP(MAC ? ALT : SC, VK_UP, true),
  /** Scroll down.       */ SCROLLDOWN(MAC ? ALT : SC, VK_DOWN, true),

  /** Delete word backwards.
                         */ DELPREVWORD(MAC ? ALT : SC, VK_BACK_SPACE, true),
  /** Delete word.       */ DELNEXTWORD(MAC ? ALT : SC, VK_DELETE, true),
  /** Delete line to begin.
                         */ DELLINESTART(SC | (MAC ? 0 : SHF),
                             VK_BACK_SPACE, true),
  /** Delete line to end.
                         */ DELLINEEND(SC | (MAC ? 0 : SHF), VK_DELETE, true),
  /** Delete backwards.  */ DELPREV(VK_BACK_SPACE),
  /** Delete.            */ DELNEXT(VK_DELETE),

  /** Escape.            */ ESCAPE(VK_ESCAPE),
  /** Next panel.        */ NEXTTAB(CTRL, VK_TAB, true),
  /** Previous panel.    */ PREVTAB(CTRL | SHF, VK_TAB, true),
  /** Context menu.      */ CONTEXT(VK_CONTEXT_MENU),
  /** Space key.         */ SPACE(VK_SPACE),
  /** Tab key.           */ TAB(VK_TAB),
  /** Enter.             */ ENTER(VK_ENTER),
  /** Execute.           */ EXEC(SC, VK_ENTER, true),

  /** Increment size.    */ INCFONT1(SC, VK_PLUS, true),
  /** Increment size.    */ INCFONT2(SC, VK_EQUALS, true),
  /** Decrease size.     */ DECFONT(SC, VK_MINUS, true),
  /** Standard size.     */ NORMFONT(SC, VK_0, true),
  /** Jump to input bar. */ INPUT1(SC, VK_L, true),
  /** Jump to input bar. */ INPUT2(VK_F6, true),

  /** (Un)comment.       */ COMMENT(SC, VK_SLASH, false);

  /** Modifiers. */
  private final int mod;
  /** Key. */
  private final int key;
  /** Exclusive modifiers flag. */
  private final boolean excl;

  /**
   * Constructor.
   * @param m modifiers
   * @param k key code
   * @param ex modifiers exclusive
   */
  private BaseXKeys(final int m, final int k, final boolean ex) {
    mod = m;
    key = k;
    excl = ex;
  }

  /**
   * Constructor for non-exclusive modifiers.
   * @param m modifiers
   * @param k key code
   */
  private BaseXKeys(final int m, final int k) {
    this(m, k, false);
  }

  /**
   * Constructor for ignoring modifiers.
   * @param k key code
   * @param ig ignore modifiers
   */
  private BaseXKeys(final int k, final boolean ig) {
    this(0, k, ig);
  }

  /**
   * Constructor without modifiers.
   * @param k key code
   */
  private BaseXKeys(final int k) {
    this(0, k);
  }

  /**
   * Returns true if the specified key combination was pressed.
   * @param e key event
   * @return result of check
   */
  public boolean is(final KeyEvent e) {
    final int code = e.getKeyCode() == 0 ? e.getKeyChar() : e.getKeyCode();
    int m = e.getModifiers();
    if(!excl) m &= mod;
    return m == mod && code == key;
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
   * Returns true if the pressed key includes a control key.
   * @param e key event
   * @return result of check
   */
  public static boolean control(final KeyEvent e) {
    // Mac offers special characters via ALT, Windows/Linux don't..
    return e.isControlDown() || e.isMetaDown() || !MAC && e.isAltDown();
  }

  /**
   * Returns true if the pressed key is a modifier key (including 'escape').
   * @param e key event
   * @return result of check
   */
  public static boolean modifier(final KeyEvent e) {
    final int c = e.getKeyCode();
    return c == VK_ALT || c == VK_SHIFT || c == VK_META || c == VK_CONTROL ||
      c == VK_ESCAPE || c == VK_CAPS_LOCK;
  }
}
