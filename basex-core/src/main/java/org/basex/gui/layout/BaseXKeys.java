package org.basex.gui.layout;

import static java.awt.event.KeyEvent.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Prop.*;

import java.awt.event.*;

/**
 * This class offers system-dependent key mappings.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public enum BaseXKeys {

  // Cursor

  /** Left.                  */ PREVCHAR(0, VK_LEFT),
  /** Right.                 */ NEXTCHAR(0, VK_RIGHT),
  /** Word left.             */ PREVWORD(MAC ? ALT : META, VK_LEFT),
  /** Word right.            */ NEXTWORD(MAC ? ALT : META, VK_RIGHT),
  /** Beginning of line.     */ LINESTART(MAC ? META : 0, MAC ? VK_LEFT : VK_HOME, 1),
  /** End of line.           */ LINEEND(MAC ? META : 0, MAC ? VK_RIGHT : VK_END, 1),

  /** Up.                    */ PREVLINE(0, VK_UP),
  /** Down.                  */ NEXTLINE(0, VK_DOWN),
  /** Page up.               */ PREVPAGE(0, VK_PAGE_UP),
  /** Page down.             */ NEXTPAGE(0, VK_PAGE_DOWN),
  /** Beginning of text.     */ TEXTSTART(META, MAC ? VK_UP : VK_HOME, 1),
  /** End of text.           */ TEXTEND(META, MAC ? VK_DOWN : VK_END, 1),
  /** Scroll up.             */ SCROLLUP(MAC ? ALT : META, VK_UP, 0),
  /** Scroll down.           */ SCROLLDOWN(MAC ? ALT : META, VK_DOWN, 0),

  /** Tab key.               */ TAB(VK_TAB),

  /** Page up (read-only).   */ PREVPAGE_RO(SHIFT, VK_SPACE, 0),
  /** Page down (read-only). */ NEXTPAGE_RO(0, VK_SPACE, 0),

  // Editing

  /** Delete backwards.      */ DELPREV(0, VK_BACK_SPACE),
  /** Delete.                */ DELNEXT(0, VK_DELETE),

  /** Undo.                  */ UNDOSTEP(META, VK_Z, 0),
  /** Redo.                  */ REDOSTEP(MAC ? META | SHIFT : META, MAC ? VK_Z : VK_Y, 0),

  /** Cut.                   */ CUT1(META, VK_X, 0),
  /** Cut.                   */ CUT2(SHIFT, VK_DELETE, 0),
  /** Copy.                  */ COPY1(META, VK_C, 0),
  /** Copy.                  */ COPY2(META, VK_INSERT, 0),
  /** Paste.                 */ PASTE1(META, VK_V, 0),
  /** Paste.                 */ PASTE2(SHIFT, VK_INSERT, 0),
  /** Select all.            */ SELECTALL(META, VK_A, 0),

  /** Move line(s) down.     */ MOVEDOWN(MAC ? ALT | SHIFT : ALT, VK_DOWN, 0),
  /** Move line(s) up.       */ MOVEUP(MAC ? ALT | SHIFT : ALT, VK_UP, 0),

  /** Code completion.       */ COMPLETE(CTRL, VK_SPACE, 0),

  /** Delete word backwards. */ DELPREVWORD(MAC ? ALT : META, VK_BACK_SPACE, 0),
  /** Delete word.           */ DELNEXTWORD(MAC ? ALT : META, VK_DELETE, 0),
  /** Delete line to begin.  */ DELLINESTART(META | (MAC ? 0 : SHIFT), VK_BACK_SPACE, 0),
  /** Delete line to end.    */ DELLINEEND(META | (MAC ? 0 : SHIFT), VK_DELETE, 0),
  /** Delete complete line.  */ DELLINE(META | SHIFT, VK_D, 0),

  // Navigation

  /** Jump to input bar.     */ INPUTBAR(MAC ? META : 0, VK_F6, 0),
  /** Next tab.              */ NEXTTAB(CTRL, VK_TAB, 0),
  /** Previous tab.          */ PREVTAB(CTRL | SHIFT, VK_TAB, 0),
  /** Close tab.             */ CLOSETAB(META, VK_F4, 0),

  /** Browse back.           */ GOBACK(MAC ? META : ALT, VK_LEFT, 0),
  /** Browse back.           */ GOBACK2(VK_BACK_SPACE, 0),
  /** Browse forward.        */ GOFORWARD(MAC ? META : ALT, VK_RIGHT, 0),
  /** Browse up.             */ GOUP(MAC ? META : ALT, VK_UP, 0),
  /** Browse home.           */ GOHOME(MAC ? META : ALT, VK_HOME, 0),

  /** Go to line.            */ GOTOLINE(META, VK_L, 0),

  // Find

  /** Find search term.      */ FIND(META, VK_F, 0),
  /** Find next hit.         */ FINDNEXT1(MAC ? META : 0, VK_F3, 0),
  /** Find next hit.         */ FINDNEXT2(META, VK_G, 0),
  /** Find previous hit.     */ FINDPREV1(MAC ? META | SHIFT : SHIFT, VK_F3, 0),
  /** Find previous hit.     */ FINDPREV2(META | SHIFT, VK_G, 0),

  // Font

  /** Increment size.        */ INCFONT1(META, VK_PLUS, 0),
  /** Increment size.        */ INCFONT2(META, VK_EQUALS, 0),
  /** Decrease size.         */ DECFONT(META, VK_MINUS, 0),
  /** Standard size.         */ NORMFONT(META, VK_0, 0),

  // General

  /** Execute.               */ EXEC1(META, VK_ENTER, 0),
  /** Execute.               */ EXEC2(META, VK_F11, 0),
  /** Test.                  */ UNIT(META | SHIFT, VK_ENTER, 0),

  /** Escape.                */ ESCAPE(0, VK_ESCAPE, 0),
  /** Context menu.          */ CONTEXT(0, VK_CONTEXT_MENU, 0),
  /** Copy path.             */ COPYPATH(META | SHIFT, VK_C, 0),

  /** Refresh.               */ REFRESH(0, VK_F5, 0),
  /** Rename.                */ RENAME(0, VK_F2, 0),
  /** New directory.         */ NEWDIR(META | SHIFT, VK_N, 0),

  /** Space key.             */ SPACE(0, VK_SPACE, 0),
  /** Enter.                 */ ENTER(0, VK_ENTER, 0),
  /** Shift Enter.           */ OPEN(SHIFT, VK_ENTER, 0);

  /** Modifiers. */
  private final int mod;
  /** Key. */
  private final int key;
  /** Exclusive modifiers flag. */
  private final int excl;

  /**
   * Constructor.
   * @param m modifiers
   * @param k key code
   * @param ex modifiers exclusive:
   *  0 = ALL
   *  1 = SHIFT
   *  2 = ALT
   *  4 = CTRL
   *  9 = NONE
   *  any combiantion, 5 = SHIFT and ALT are both excluded 
   */
  BaseXKeys(final int m, final int k, final int ex) {
    mod = m;
    key = k;
    excl = ex;
  }

  /**
   * Constructor for non-exclusive modifiers.
   * @param m modifiers
   * @param k key code
   */
  BaseXKeys(final int m, final int k) {
    this(m, k, 9);
  }

  /**
   * Constructor for ignoring modifiers.
   * @param k key code
   * @param ig ignore modifiers
   */
/*  BaseXKeys(final int k, final int ig) {
    this(0, k, ig);
  }*/

  /**
   * Constructor without modifiers.
   * @param k key code
   */
  BaseXKeys(final int k) {
    this(0, k);
  }

  /**
   * Returns true if the specified key combination was pressed.
   * @param e key event
   * @return result of check
   */
  public boolean is(final KeyEvent e) {
    final int c = e.getKeyCode();
    int m = e.getModifiers();
    if(excl==9)  {
		  m &= mod;
	  } else {
		  if((excl & 1)==1 && e.isShiftDown()) m &= mod;
		  if((excl & 2)==2 && e.isAltDown()) m &= mod;
		  if((excl & 4)==4 && e.isControlDown()) m &= mod;
	  }
    return m == mod && (c == 0 ? e.getKeyChar() : c) == key;
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
    final StringBuilder sb = new StringBuilder(getKeyModifiersText(mod));
    if(sb.length() != 0) sb.append('+');
    return sb.append(KeyEvent.getKeyText(key)).toString();
  }

  /**
   * Returns a shortcut string.
   * @return shortcut string
   */
  public String shortCut() {
    final StringBuilder sb = new StringBuilder();
    if((mod & InputEvent.META_MASK) != 0) sb.append("meta").append(' ');
    if((mod & InputEvent.CTRL_MASK) != 0) sb.append("ctrl").append(' ');
    if((mod & InputEvent.ALT_MASK) != 0) sb.append("alt").append(' ');
    if((mod & InputEvent.SHIFT_MASK) != 0) sb.append("shift").append(' ');

    if(key == VK_ENTER) sb.append("ENTER");
    else if(key == VK_DELETE) sb.append("DELETE");
    else if(key == VK_PERIOD) sb.append("PERIOD");
    else sb.append(getKeyText(key));
    return sb.toString();
  }
}

