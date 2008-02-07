package org.basex.gui.layout;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultHighlighter;
import org.basex.gui.GUIConstants;
import org.basex.gui.dialog.Dialog;
import org.basex.util.History;
import org.basex.util.Performance;
import org.basex.util.Undo;

/**
 * Project specific Textarea implementation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXTextArea extends JTextArea {
  /** Text area highlighter. */
  static final DefaultHighlighter.DefaultHighlightPainter HIGH =
    new DefaultHighlighter.DefaultHighlightPainter(GUIConstants.colormark4);
  /** Command history. */
  protected History hist;
  /** Undo history. */
  protected Undo undo;
  /** Current highlighter. */
  protected Object high;
  /** Thread counter. */
  protected int counter;

  /**
   * Default Constructor.
   * @param hlp help text
   */
  public BaseXTextArea(final byte[] hlp) {
    this(null, hlp, null);
  }

  /**
   * Default Constructor.
   * @param txt input text
   * @param hlp help text
   * @param list reference to the dialog listener
   */
  public BaseXTextArea(final String txt, final byte[] hlp, final Dialog list) {
    setTabSize(2);
    setLineWrap(true);
    setWrapStyleWord(true);
    // restore default focus traversal with TAB key
    setFocusTraversalKeys(0, null);
    setFocusTraversalKeys(1, null);
    BaseXLayout.addDefaultKeys(this, list);
    undo = new Undo();

    if(txt != null) {
      setText(txt);
      selectAll();
    }

    BaseXLayout.addHelp(this, hlp);
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e))
          new BaseXTextPopup(BaseXTextArea.this, e.getPoint());
      }
    });
    
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        removeMark();

        final int key = e.getKeyCode();
        if(key == KeyEvent.VK_CONTEXT_MENU) {
          new BaseXTextPopup(BaseXTextArea.this, new Point(10, 10));
        }
        if(!e.isControlDown() || hist == null) return;
        
        if(key == KeyEvent.VK_UP) {
          restore(hist.prev());
        } else if(key == KeyEvent.VK_DOWN) {
          restore(hist.next());
        } else if(key == KeyEvent.VK_ENTER) {
          store();
        } else if(key == KeyEvent.VK_Z) {
          setText(undo.prev());
          setCaretPosition(undo.cursor());
        } else if(key == KeyEvent.VK_Y) {
          setText(undo.next());
          setCaretPosition(undo.cursor());
        }
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        final int key = e.getKeyCode();
        if(e.isControlDown() && (key == KeyEvent.VK_Z || key == KeyEvent.VK_Y))
          return;

        undo.store(getText(), getCaretPosition());
      }
    });

    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        repaint();
      }
      @Override
      public void focusLost(final FocusEvent e) {
        repaint();
      }
    });
  }
  
  @Override
  public void setText(final String t) {
    super.setText(t);
    undo.store(t, getCaretPosition());
  }

  /**
   * Initializes the text feature, setting an initial history string array.
   * @param history string history
   */
  public void init(final String[] history) {
    hist = new History(history);
  }

  /**
   * Returns the string history.
   * @return strings
   */
  public String[] strings() {
    if(hist == null) return null;
    store();
    return hist.strings();
  }

  /**
   * Copies the specified text to the text area.
   * @param str string to be decoded
   */
  void restore(final String str) {
    if(str != null) setText(str.replaceAll("~%~", "\n"));
  }

  /**
   * Stores the text in the history.
   */
  void store() {
    hist.store(getText().trim().replaceAll("\n", "~%~"));
  }

  /**
   * Removes a mark.
   */
  public void removeMark() {
    ++counter;
    if(high != null) {
      getHighlighter().removeHighlight(high);
      high = null;
    }
  }

  /**
   * Error marker thread.
   * @param s start position
   * @param e end position
   */
  public void mark(final int s, final int e) {
    final int count = counter;
    new Thread() {
      @Override
      public void run() {
        Performance.sleep(500);
        if(count != counter) return;
        try {
          if(high != null) getHighlighter().removeHighlight(high);
          high = getHighlighter().addHighlight(s, e, HIGH);
        } catch(final Exception ex) {
          ex.printStackTrace();
        }
      }
    }.start();
  }

  /** Text popup. */
  class BaseXTextPopup extends JPopupMenu implements ActionListener {
    /** Maximum number of displayed history entries. */
    static final int MAX = 12;
    /** Store entry. */
    static final String STORE = "Store Entry";
    /** More.
    static final String MORE = "[0]  More...";
    */

    /**
     * Constructor.
     * @param comp component reference
     * @param p mouse position
     */
    BaseXTextPopup(final BaseXTextArea comp, final Point p) {
      JMenuItem item = item(STORE);
      if(getText().length() == 0) item.setEnabled(false);
      add(item);
      addSeparator();
      final String[] str = comp.hist.strings();
      
      for(int i = 1; i <= Math.min(str.length, MAX); i++) {
        final String entry = str[str.length - i];
        String ent = "[" + i + "]  " + entry.replaceAll("(~%~| |\t)+", " ");
        if(ent.length() > 50) ent = ent.substring(0, 50) + "...";
        item = item(ent);
        item.setActionCommand(entry);
        add(item);
      }
      show(comp, p.x, p.y);
    }
    
    /**
     * Creates a new menu item.
     * @param str string
     * @return item
     */
    private JMenuItem item(final String str) {
      final JMenuItem item = new JMenuItem(str);
      item.addActionListener(this);
      return item;
    }

    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent e) {
      final String cmd = e.getActionCommand();
      if(cmd.equals(STORE)) {
        store();
      } else {
        restore(cmd);
      }
    }
  }
}
