package org.basex.gui.view.query;

import java.awt.Color;
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
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXLayout;
import org.basex.util.History;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.Undo;

/**
 * Project specific TextPane implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class QueryText extends JTextPane {
  /** Query area reference. */
  protected final QueryArea area;
  /** Undo history. */
  protected final Undo undo;
  /** Command history. */
  protected History hist;
  /** Thread counter. */
  protected int counter;

  /** Document. */
  protected StyledDoc doc;
  /** Quote Style. */
  protected Style quote;
  /** Quote Style. */
  protected Style deflt;
  /** Quote Style. */
  protected Style error;
  /** Quote Style. */
  protected Style code;

  /**
   * Default Constructor.
   * @param a query area
   * @param hlp help text
   */
  public QueryText(final QueryArea a, final byte[] hlp) {
    // restore default focus traversal with TAB key
    setFocusTraversalKeys(0, null);
    setFocusTraversalKeys(1, null);
    setBackground(Color.white);
    undo = new Undo();
    area = a;
    
    doc = new StyledDoc();
    setDocument(doc);

    deflt = doc.addStyle(null, null);
    quote = doc.addStyle(null, null);
    StyleConstants.setForeground(quote, GUIConstants.COLORERROR);
    error = doc.addStyle(null, null);
    StyleConstants.setBackground(error, GUIConstants.COLORMARK);
    code = doc.addStyle(null, null);
    StyleConstants.setForeground(code, GUIConstants.COLORQUOTE);

    BaseXLayout.addHelp(this, hlp);
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e))
          new BaseXTextPopup(QueryText.this, e.getPoint());
      }
    });

    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        ++counter;

        final int key = e.getKeyCode();
        if(key == KeyEvent.VK_CONTEXT_MENU) {
          new BaseXTextPopup(QueryText.this, new Point(10, 10));
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
        area.query(false);
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
    area.query(false);
  }

  /**
   * Stores the text in the history.
   */
  void store() {
    hist.store(getText().trim().replaceAll("\n", "~%~"));
  }

  /**
   * Refreshes the syntax highlighting.
   * @param s start of optional error mark
   * @param e end of optional error mark
   */
  public void refresh(final int s, final int e) {
    final int count = counter;
    new Thread() {
      @Override
      public void run() {
        Performance.sleep(300);

        //Performance pp = new Performance();
        doc.lock();
        
        final String txt = getText();
        final int tl = txt.length();
        int t = 0;
        while(t < tl) {
          if(count != counter) break;

          byte ch = (byte) txt.charAt(t);
          final boolean qu = ch == '"' || ch == '\'';

          if(!qu && !Token.letterOrDigit(ch) && !Token.ws(ch)) {
            doc.setCharacterAttributes(t, 1, code, true);
            t++;
            continue;
          }

          int i = t;
          while(++t < tl) {
            ch = (byte) txt.charAt(t);
            if(ch == '"' || ch == '\'') break;
            if(!qu && !Token.letterOrDigit(ch) && !Token.ws(ch)) break;
          }
          if(qu) ++t;
          doc.setCharacterAttributes(i, t - i, qu ? quote : deflt, true);
        }
        if(count == counter && s != -1)
          doc.setCharacterAttributes(s, e - s, error, false);

        doc.unlock();
        //System.out.println(pp.getTimer());
      }
    }.start();
  }

  /** Text popup. */
  class BaseXTextPopup extends JPopupMenu implements ActionListener {
    /** Maximum number of displayed history entries. */
    static final int MAX = 12;
    /** Store entry. */
    static final String STORE = "Store Entry";

    /**
     * Constructor.
     * @param comp component reference
     * @param p mouse position
     */
    BaseXTextPopup(final QueryText comp, final Point p) {
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
  
  /** Styled Document. */
  static class StyledDoc extends DefaultStyledDocument {
    /** Locks the attribute change. */
    public void lock() {
      writeLock();
    }

    /** Unlocks the attribute change. */
    public void unlock() {
      writeUnlock();
    }

    @Override
    public void setCharacterAttributes(final int off, final int len, 
        final AttributeSet s, final boolean rep) {

      final DefaultDocumentEvent changes = new DefaultDocumentEvent(off, len,
          DocumentEvent.EventType.CHANGE);

      buffer.change(off, len, changes);

      final AttributeSet sCopy = s.copyAttributes();
      int lastEnd = Integer.MAX_VALUE;
      for(int pos = off; pos < (off + len); pos = lastEnd) {
        final Element run = getCharacterElement(pos);
        lastEnd = run.getEndOffset();
        if(pos == lastEnd) break;

        MutableAttributeSet attr = (MutableAttributeSet) run.getAttributes();
        changes.addEdit(new AttributeUndoableEdit(run, sCopy, rep));
        if(rep) attr.removeAttributes(attr);
        attr.addAttributes(s);
      }
      changes.end();
      fireChangedUpdate(changes);
      fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
    }
  }
}
