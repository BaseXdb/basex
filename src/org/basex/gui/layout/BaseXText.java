package org.basex.gui.layout;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import org.basex.BaseX;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.dialog.Dialog;
import org.basex.util.Action;
import org.basex.util.Array;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.Undo;

/**
 * This class offers a fast text input, using the {@link BaseXTextRenderer}
 * class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXText extends BaseXPanel {
  /** Text array to be written. */
  protected BaseXTextTokens text = new BaseXTextTokens(Token.EMPTY);
  /** Renderer reference. */
  protected final BaseXTextRenderer rend;
  /** Undo history. */
  protected Undo undo;

  /** Scrollbar reference. */
  protected final BaseXBar scroll;
  /** Editable flag. */
  protected final boolean editable;

  /**
   * Default constructor.
   * @param help help text
   */
  public BaseXText(final byte[] help) {
    this(help, true, null);
  }

  /**
   * Default constructor.
   * @param edit editable flag
   * @param help help text
   */
  public BaseXText(final byte[] help, final boolean edit) {
    this(help, edit, null);
  }
  
  /**
   * Default constructor.
   * @param help help text
   * @param edit editable flag
   * @param list reference to the dialog listener
   */
  public BaseXText(final byte[] help, final boolean edit, final Dialog list) {
    super(help);
    setFocusable(true);

    BaseXLayout.addDefaultKeys(this, list);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
    addComponentListener(this);
    addMouseListener(this);
    addKeyListener(this);
    editable = edit;

    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        cursor();
      }
      @Override
      public void focusLost(final FocusEvent e) {
        cursor.stop();
        rend.cursor(false);
        rend.repaint();
      }
    });

    setLayout(new BorderLayout(4, 0));
    scroll = new BaseXBar(this);
    rend = new BaseXTextRenderer(text, scroll);
    rend.setFont(GUIConstants.mfont);
    add(rend, BorderLayout.CENTER);
    add(scroll, BorderLayout.EAST);

    if(edit) {
      setBackground(UIManager.getColor("TextPane.background"));
      //setBorder(UIManager.getBorder("TextField.border"));
      setBorder(new EtchedBorder());
      setMode(FILL.PLAIN);
      undo = new Undo();
    } else {
      setMode(FILL.NONE);
    }
  }

  /**
   * Sets the output text.
   * @param t output text
   */
  public void setText(final byte[] t) {
    setText(t, t.length);
  }
    
  /**
   * Sets the output text.
   * @param t output text
   * @param s text size
   */
  public void setText(final byte[] t, final int s) {
    // remove 0x0Ds (carriage return) and compare old with new string
    int ns = 0;
    int ts = text.size;
    byte[] tt = text.text;
    boolean eq = true;
    for(int r = 0; r < s; r++) {
      if(t[r] != 0x0D) t[ns++] = t[r];
      eq &= ns < ts && t[ns] == tt[ns];
    }
    eq &= ns == ts;

    // new text is different...
    if(!eq) {
      text = new BaseXTextTokens(t, ns);
      rend.setText(text);
      scroll.pos(0);
    }

    if(undo != null) undo.store(t.length != ns ? Array.finish(t, ns) : t, 0);
    SwingUtilities.invokeLater(calc.single());
  }

  /**
   * Sets a syntax highlighter.
   * @param s syntax reference
   */
  public void setSyntax(final BaseXSyntax s) {
    rend.setSyntax(s);
  }

  /**
   * Sets a new cursor position.
   * @param p cursor position
   */
  public void setCursor(final int p) {
    text.pos(p);
    text.setCursor();
  }

  /**
   * Returns the output text.
   * @return output text
   */
  public byte[] getText() {
    return text.finish();
  }

  @Override
  public void setFont(final Font f) {
    if(rend != null) rend.setFont(f);
  }

  /**
   * Refreshes the syntax highlighting.
   * @param s start of optional error mark
   */
  public void error(final int s) {
    text.error(s);
    rend.repaint();
  }

  @Override
  public void setEnabled(final boolean enabled) {
    super.setEnabled(enabled);
    rend.setEnabled(enabled);
    scroll.setEnabled(enabled);
  }

  /**
   * Selects the whole text.
   */
  public void selectAll() {
    text.pos(0);
    text.startMark();
    text.pos(text.size);
    text.endMark();
    rend.repaint();
  }
    
  // OVERRIDDEN METHODS =======================================================
    
  @Override
  public void mousePressed(final MouseEvent e) {
    super.mousePressed(e);
    requestFocusInWindow();
    cursor();

    if(!SwingUtilities.isLeftMouseButton(e)) return;
    // <CG> double click...
    int c = e.getClickCount();
    if(c == 1) {
      // selection mode
      rend.select(scroll.pos(), e.getPoint(), false);
    } else if(c == 2) {
      selectWord();
    } else {
      selectLine();
    }
  }

  /**
   * Selects the word at the cursor position.
   */
  void selectWord() {
    text.pos(text.cursor());
    final boolean ch = Character.isLetterOrDigit(text.prev(true));
    while(text.pos() > 0) {
      int c = text.prev(true);
      if(c == '\n' || ch != Character.isLetterOrDigit(c)) break;
    }
    if(text.pos() != 0) text.next(true);
    text.startMark();
    while(text.pos() < text.size) {
      int c = text.curr();
      if(c == '\n' || ch != Character.isLetterOrDigit(c)) break;
      text.next(true);
    }
    text.endMark();
  }

  /**
   * Selects the word at the cursor position.
   */
  void selectLine() {
    text.pos(text.cursor());
    text.home(true);
    text.startMark();
    text.end(Integer.MAX_VALUE, true);
    text.endMark();
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if(!SwingUtilities.isLeftMouseButton(e)) return;
    copy();
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(!SwingUtilities.isLeftMouseButton(e)) return;

    // selection mode
    rend.select(scroll.pos(), e.getPoint(), true);

    if(e.getY() > getHeight() - 20) {
      scroll.pos(scroll.pos() + e.getY() - getHeight() + 20);
    } else if(e.getY() < 20) {
      scroll.pos(scroll.pos() + e.getY() - 20);
    }
    rend.repaint();
  }
  
  /** Last horizontal position. */
  private int col = -1;

  @Override
  public void keyPressed(final KeyEvent e) {
    final int c = e.getKeyCode();
    if(e.isAltDown() || e.isMetaDown() || c == KeyEvent.VK_SHIFT ||
        c == KeyEvent.VK_CONTROL || c == KeyEvent.VK_ESCAPE) return;
    
    final byte[] txt = text.text;
    text.pos(text.cursor());
    cursor();

    final int fh = rend.fontH();
    final int h = getHeight();

    final boolean ctrl = e.isControlDown();
    final boolean shf = e.isShiftDown();
    boolean down = true;
    boolean key = !ctrl && !e.isActionKey();
    if(c != KeyEvent.VK_DOWN && c != KeyEvent.VK_UP && !key) col = -1;

    if(ctrl && c == 'A') {
      selectAll();
      e.consume();
      return;
    }
    if(ctrl && c == 'C') {
      copy();
      return;
    }
    if(editable && c == KeyEvent.VK_BACK_SPACE) {
      if(text.pos() == 0) return;
      if(text.start() == -1) text.prev(shf);
      text.delete();
      down = false;
    } else if(editable && c == KeyEvent.VK_DELETE) {
      text.delete();
    } else if(editable && key) {
      if(text.start() != -1) text.delete();
      text.add(new char[] { e.getKeyChar() });
    } else if(editable && ctrl && c == 'X') {
      if(copy()) text.delete();
    } else if(editable && ctrl && c == 'V') {
      if(text.start() != -1) text.delete();
      text.add(paste().toCharArray());
    } else if(editable && ctrl && c == KeyEvent.VK_Z) {
      text = new BaseXTextTokens(undo.prev());
      rend.setText(text);
      text.pos(undo.cursor());
    } else if(editable && ctrl && c == KeyEvent.VK_Y) {
      text = new BaseXTextTokens(undo.next());
      rend.setText(text);
      text.pos(undo.cursor());
    } else {
      if(shf && text.start() == -1) text.startMark();
  
      if(c == KeyEvent.VK_RIGHT) {
        if(ctrl) {
          final boolean ch = Character.isLetterOrDigit(text.next(shf));
          while(text.pos() < text.size && ch ==
            Character.isLetterOrDigit(text.curr())) text.next(shf);
        } else {
          text.next(shf);
        }
      } else if(c == KeyEvent.VK_LEFT) {
        if(ctrl) {
          final boolean ch = Character.isLetterOrDigit(text.prev(shf));
          while(text.pos() > 0 && ch ==
            Character.isLetterOrDigit(text.prev(shf)));
          if(text.pos() != 0) text.next(shf);
        } else {
          text.prev(shf);
        }
        down = false;
      } else if(c == KeyEvent.VK_DOWN) {
        if(ctrl) {
          scroll.pos(scroll.pos() + fh);
          return;
        }
        down(1, shf);
      } else if(c == KeyEvent.VK_UP) {
        if(ctrl) {
          scroll.pos(scroll.pos() - fh);
          return;
        }
        up(1, shf);
        down = false;
      } else {
        if(!shf) text.noMark();
  
        if(c == KeyEvent.VK_HOME) {
          if(ctrl) text.pos(0);
          else text.home(shf);
          down = false;
        } else if(c == KeyEvent.VK_END) {
          if(ctrl) text.pos(text.size);
          else text.end(Integer.MAX_VALUE, shf);
        } else if(c == KeyEvent.VK_PAGE_DOWN) {
          down(h / fh, shf);
        } else if(c == KeyEvent.VK_PAGE_UP) {
          up(h / fh, shf);
          down = false;
        }
      }

      // refresh scroll position
      if(shf) text.endMark();
    }
    setText(txt != text.text, down);
  }

  /**
   * Sets a new text.
   * @param c calculate new height of scroll bar
   * @param down flag for cursor movement (up/down)
   */
  private void setText(final boolean c, final boolean down) {
    text.setCursor();
    if(c) rend.calc();

    // updates the visible area
    final int p = scroll.pos();
    final int y = rend.cursorY();
    final int m = y + rend.fontH() + 5 - getHeight();
    if(y < p) scroll.pos(down ? m : y);
    else if(m > p) scroll.pos(down ? m : y);
    rend.repaint();
  }
  
  /**
   * Moves the cursor up.
   * @param l number of lines to move cursor
   * @param shf shift flag
   */
  private void down(final int l, final boolean shf) {
    if(!shf) text.noMark();
    int x = text.home(shf);
    if(col == -1) col = x;
    text.next(shf);
    text.end(Integer.MAX_VALUE, shf);
    for(int i = 0; i < l; i++) {
      text.next(shf);
      text.end(col, shf);
    }
  }
  
  /**
   * Moves the cursor up.
   * @param l number of lines to move cursor
   * @param shf shift flag
   */
  private void up(final int l, final boolean shf) {
    if(!shf) text.noMark();
    int x = text.home(shf);
    if(text.pos() == 0) return;
    if(col == -1) col = x;
    for(int i = 0; i < l; i++) {
      text.prev(shf);
      text.home(shf);
    }
    text.end(col, shf);
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    if(editable) e.consume();
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    final int key = e.getKeyCode();
    if(e.isControlDown() && (key == KeyEvent.VK_Z || key == KeyEvent.VK_Y))
      return;

    if(undo != null) undo.store(text.finish(), text.cursor());
    if(editable) e.consume();
  }

  /**
   * Copies the selected text to the clipboard.
   * @return true if text was copied
   */
  private boolean copy() {
    final String txt = text.copy();
    if(txt.length() == 0) {
      text.noMark();
      return false;
    }

    // copy selection to clipboard
    final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    clip.setContents(new StringSelection(txt), null);
    return true;
  }

  /**
   * Returns the clipboard text.
   * @return text
   */
  private String paste() {
    // copy selection to clipboard
    final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    final Transferable t = clip.getContents(null);
    try {
      if(t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        return (String) t.getTransferData(DataFlavor.stringFlavor);
      }
    } catch(final Exception ex) {
      BaseX.debug(ex);
    }
    return "";
  }


  /** Calculation counter. */
  protected Action cursor = new Action() {
    @Override
    public void action() {
      rend.cursor(!rend.cursor());
      rend.repaint();
      Performance.sleep(500);
    }
  };

  /**
   * Handles the cursor thread; interrupts the old thread as soon as
   * new one has been started.
   */
  protected void cursor() {
    cursor.stop();
    if(!isFocusable() || !isEnabled()) return;
    rend.cursor(false);
    cursor.repeat().start();
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    scroll.pos(scroll.pos() + e.getUnitsToScroll() * 20);
    rend.repaint();
  }

  @Override
  public void componentResized(final ComponentEvent e) {
    scroll.pos(0);
    SwingUtilities.invokeLater(calc.single());
  }

  /** Calculation counter. */
  protected Action calc = new Action() {
    @Override
    public void action() {
      rend.calc();
      rend.repaint();
    }
  };
}
