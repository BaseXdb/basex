package org.basex.gui.layout;

import static java.awt.event.KeyEvent.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Prop.*;

import java.awt.event.*;

/**
 * This class offers system-dependent key mappings.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public enum BaseXKeys {

  // Cursor

  /** Left.                  */ PREVCHAR(NO_MOD, VK_LEFT, SHIFT),
  /** Right.                 */ NEXTCHAR(NO_MOD, VK_RIGHT, SHIFT),
  /** Word left.             */ PREVWORD(MAC ? ALT : META, VK_LEFT, SHIFT),
  /** Word right.            */ NEXTWORD(MAC ? ALT : META, VK_RIGHT, SHIFT),
  /** Beginning of line.     */ LINESTART(MAC ? META : NO_MOD, MAC ? VK_LEFT : VK_HOME, SHIFT),
  /** End of line.           */ LINEEND(MAC ? META : NO_MOD, MAC ? VK_RIGHT : VK_END, SHIFT),

  /** Up.                    */ PREVLINE(NO_MOD, VK_UP, SHIFT),
  /** Down.                  */ NEXTLINE(NO_MOD, VK_DOWN, SHIFT),
  /** Page up.               */ PREVPAGE(NO_MOD, VK_PAGE_UP, SHIFT),
  /** Page down.             */ NEXTPAGE(NO_MOD, VK_PAGE_DOWN, SHIFT),
  /** Beginning of text.     */ TEXTSTART(META, MAC ? VK_UP : VK_HOME, SHIFT),
  /** End of text.           */ TEXTEND(META, MAC ? VK_DOWN : VK_END, SHIFT),
  /** Scroll up.             */ SCROLLUP(MAC ? ALT : META, VK_UP),
  /** Scroll down.           */ SCROLLDOWN(MAC ? ALT : META, VK_DOWN),

  /** Tab key.               */ TAB(NO_MOD, VK_TAB, SHIFT),

  /** Page up (read-only).   */ PREVPAGE_RO(SHIFT, VK_SPACE),
  /** Page down (read-only). */ NEXTPAGE_RO(NO_MOD, VK_SPACE),

  // Editing

  /** Delete backwards.      */ DELPREV(NO_MOD, VK_BACK_SPACE, SHIFT),
  /** Delete.                */ DELNEXT(NO_MOD, VK_DELETE),

  /** Undo.                  */ UNDOSTEP(META, VK_Z),
  /** Redo.                  */ REDOSTEP(MAC ? META | SHIFT : META, MAC ? VK_Z : VK_Y),

  /** Cut.                   */ CUT1(META, VK_X),
  /** Cut.                   */ CUT2(SHIFT, VK_DELETE),
  /** Copy.                  */ COPY1(META, VK_C),
  /** Copy.                  */ COPY2(META, VK_INSERT),
  /** Paste.                 */ PASTE1(META, VK_V),
  /** Paste.                 */ PASTE2(SHIFT, VK_INSERT),
  /** Select all.            */ SELECTALL(META, VK_A),

  /** Move line(s) down.     */ MOVEDOWN(MAC ? ALT | SHIFT : ALT, VK_DOWN),
  /** Move line(s) up.       */ MOVEUP(MAC ? ALT | SHIFT : ALT, VK_UP),

  /** Code completion.       */ COMPLETE(CTRL, VK_SPACE),

  /** Delete word backwards. */ DELPREVWORD(MAC ? ALT : META, VK_BACK_SPACE),
  /** Delete word.           */ DELNEXTWORD(MAC ? ALT : META, VK_DELETE),
  /** Delete line to begin.  */ DELLINESTART(META | (MAC ? NO_MOD : SHIFT), VK_BACK_SPACE),
  /** Delete line to end.    */ DELLINEEND(META | (MAC ? NO_MOD : SHIFT), VK_DELETE),
  /** Delete complete line.  */ DELLINE(META | SHIFT, VK_D),

  // Navigation

  /** Jump to input bar.     */ INPUTBAR(MAC ? META : NO_MOD, VK_F6),
  /** Next tab.              */ NEXTTAB(CTRL, VK_TAB),
  /** Previous tab.          */ PREVTAB(CTRL | SHIFT, VK_TAB),
  /** Close tab.             */ CLOSETAB(META, VK_F4),

  /** Browse back.           */ GOBACK(MAC ? META : ALT, VK_LEFT),
  /** Browse back.           */ GOBACK2(VK_BACK_SPACE, NO_MOD),
  /** Browse forward.        */ GOFORWARD(MAC ? META : ALT, VK_RIGHT),
  /** Browse up.             */ GOUP(MAC ? META : ALT, VK_UP),
  /** Browse home.           */ GOHOME(MAC ? META : ALT, VK_HOME),

  /** Go to line.            */ GOTOLINE(META, VK_L),

  // Find

  /** Find search term.      */ FIND(META, VK_F),
  /** Find next hit.         */ FINDNEXT1(MAC ? META : NO_MOD, VK_F3),
  /** Find next hit.         */ FINDNEXT2(META, VK_G),
  /** Find previous hit.     */ FINDPREV1(MAC ? META | SHIFT : SHIFT, VK_F3),
  /** Find previous hit.     */ FINDPREV2(META | SHIFT, VK_G),

  // Font

  /** Increment size.        */ INCFONT1(META, VK_PLUS),
  /** Increment size.        */ INCFONT2(META, VK_EQUALS),
  /** Decrease size.         */ DECFONT(META, VK_MINUS),
  /** Standard size.         */ NORMFONT(META, VK_0),

  // General

  /** Execute.               */ EXEC1(META, VK_ENTER),
  /** Execute.               */ EXEC2(META, VK_F11),
  /** Test.                  */ UNIT(META | SHIFT, VK_ENTER),

  /** Escape.                */ ESCAPE(NO_MOD, VK_ESCAPE),
  /** Context menu.          */ CONTEXT(NO_MOD, VK_CONTEXT_MENU),
  /** Copy path.             */ COPYPATH(META | SHIFT, VK_C),
  /** Add as import.         */ ADDIMPORT(META | SHIFT, VK_I),

  /** Refresh.               */ REFRESH(NO_MOD, VK_F5),
  /** Rename.                */ RENAME(NO_MOD, VK_F2),
  /** New directory.         */ NEWDIR(META | SHIFT, VK_N),

  /** Space key.             */ SPACE(NO_MOD, VK_SPACE),
  /** Enter.                 */ ENTER(NO_MOD, VK_ENTER),
  /** Shift Enter.           */ SHIFT_ENTER(SHIFT, VK_ENTER);

  /** Modifiers. */
  private final int modifiers;
  /** Key. */
  private final int key;
  /** Exclusive modifiers flag. */
  private final int allowed;

  /**
   * Constructor.
   * @param modifiers modifiers (shift, control, meta, alt key)
   * @param key key code
   * @param allowed additionally allowed modifiers
   */
  BaseXKeys(final int modifiers, final int key, final int allowed) {
    this.modifiers = modifiers;
    this.key = key;
    this.allowed = allowed;
  }

  /**
   * Constructor for non-exclusive modifiers.
   * @param m modifiers
   * @param k key code
   */
  BaseXKeys(final int m, final int k) {
    this(m, k, NO_MOD);
  }

  /**
   * Returns true if the specified key combination was pressed.
   * @param e key event
   * @return result of check
   */
  public boolean is(final KeyEvent e) {
    final int c = e.getKeyCode();
    final int m = e.getModifiers() | allowed;
    return m == (modifiers | allowed) && (c == 0 ? e.getKeyChar() : c) == key;
  }

  /**
   * Returns true if the system's shortcut key was pressed.
   * @param e input event
   * @return result of check
   */
  public static boolean sc(final InputEvent e) {
    return (META & e.getModifiers()) == META;
  }

  /**
   * Returns true if the pressed key includes a control key.
   * @param e key event
   * @return result of check
   */
  public static boolean control(final KeyEvent e) {
    // With Mac, special characters are available via ALT
    return e.isControlDown() || e.isMetaDown() || !MAC && e.isAltDown();
  }

  /**
   * Returns true if the pressed key is a modifier key
   * (including 'escape' and 'alt'-'tab').
   * @param e key event
   * @return result of check
   */
  public static boolean modifier(final KeyEvent e) {
    final int c = e.getKeyCode();
    return c == VK_ALT || c == VK_SHIFT || c == VK_META || c == VK_CONTROL ||
        c == VK_PAUSE || c == VK_CAPS_LOCK || c == VK_ESCAPE ||
        c == VK_TAB && e.isAltDown();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(getKeyModifiersText(modifiers));
    if(sb.length() != 0) sb.append('+');
    return sb.append(KeyEvent.getKeyText(key)).toString();
  }

  /**
   * Returns a shortcut string.
   * @return shortcut string
   */
  String shortCut() {
    final StringBuilder sb = new StringBuilder();
    if((modifiers & InputEvent.META_MASK) != 0) sb.append("meta").append(' ');
    if((modifiers & InputEvent.CTRL_MASK) != 0) sb.append("ctrl").append(' ');
    if((modifiers & InputEvent.ALT_MASK) != 0) sb.append("alt").append(' ');
    if((modifiers & InputEvent.SHIFT_MASK) != 0) sb.append("shift").append(' ');

    if(key == VK_ENTER) sb.append("ENTER");
    else if(key == VK_DELETE) sb.append("DELETE");
    else if(key == VK_PERIOD) sb.append("PERIOD");
    else sb.append(getKeyText(key));
    return sb.toString();
  }
}

