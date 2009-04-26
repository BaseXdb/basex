package org.basex.gui.view.text;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.Box;
import org.basex.BaseX;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIToolBar;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXSyntax;
import org.basex.gui.layout.BaseXText;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewNotifier;
import org.basex.io.CachedOutput;
import org.basex.util.Array;
import org.basex.util.Token;

/**
 * This class offers a fast text view, using the {@link BaseXText} class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class TextView extends View {
  /** Text Area. */
  final BaseXText area;
  /** Header string. */
  final BaseXLabel header;
  /** Search field. */
  BaseXTextField search;
  /** Painted flag. */
  boolean refreshed;
  
  /**
   * Default constructor.
   * @param man view manager
   */
  public TextView(final ViewNotifier man) {
    super(man, HELPTEXT);

    setLayout(new BorderLayout(0, 4));
    setBorder(4, 8, 8, 8);
    setFocusable(false);
    
    area = new BaseXText(gui, HELPTEXT, false);
    add(area, BorderLayout.CENTER);
    
    header = new BaseXLabel(TEXTTIT, true);
    final BaseXButton export = GUIToolBar.newButton(GUICommands.SAVE, gui);
    final BaseXButton root = GUIToolBar.newButton(GUICommands.HOME, gui);
    search = new BaseXTextField(null);

    final Font f = getFont();
    search.setFont(f.deriveFont((float) f.getSize() + 2));
    search.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        final int co = e.getKeyCode();
        if(co == KeyEvent.VK_ESCAPE) {
          area.requestFocusInWindow();
        } else if(co == KeyEvent.VK_ENTER || co == KeyEvent.VK_F3) {
          area.find(search.getText(), e.isShiftDown());
        }
        // ignore Control/F shortcut
        if((Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() &
            e.getModifiers()) != 0 && co == KeyEvent.VK_F) e.consume();
      }
      @Override
      public void keyReleased(final KeyEvent e) {
        final char ch = e.getKeyChar();
        if(ch != KeyEvent.VK_ENTER && Character.isDefined(ch))
          area.find(search.getText(), false);
      }
    });
    
    BaseXLayout.setWidth(search, 120);
    BaseXLayout.setHeight(search, (int) export.getPreferredSize().getHeight());

    final BaseXBack back = new BaseXBack();
    back.setMode(Fill.NONE);
    back.setLayout(new BorderLayout());
    back.add(header, BorderLayout.WEST);

    final BaseXBack sp = new BaseXBack();
    sp.setMode(Fill.NONE);
    sp.setLayout(new TableLayout(1, 5));
    sp.add(search);
    sp.add(Box.createHorizontalStrut(5));
    sp.add(root);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(export);

    back.add(sp, BorderLayout.EAST);
    add(back, BorderLayout.NORTH);

    refreshLayout();
  }
  
  @Override
  public void refreshInit() {
    area.setCaret(0);
    refreshText(gui.context.current());
  }

  @Override
  public void refreshFocus() {
  }

  @Override
  public void refreshMark() {
    // skip refresh if text has already been updated
    if(refreshed) refreshed = false;
    else refreshText(gui.context.marked());
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    refreshText(gui.context.current());
  }

  /**
   * Refreshes the doc display.
   * @param nodes nodes to display
   */
  private void refreshText(final Nodes nodes) {
    if(!GUIProp.showtext || !header.getText().equals(TEXTTIT)) return;

    if(!gui.context.db()) {
      setText(Token.EMPTY);
      return;
    }
    
    try {
      final CachedOutput out = new CachedOutput(GUIProp.maxtext);
      nodes.serialize(new XMLSerializer(out, false, nodes.data.meta.chop));
      setText(out);
      refreshed = false;
    } catch(final Exception ex) {
      BaseX.debug(ex);
    }
  }

  @Override
  public void refreshLayout() {
    header.setFont(GUIConstants.lfont);
    area.setFont(GUIConstants.mfont);
  }

  @Override
  public void refreshUpdate() {
    refreshContext(false, true);
  }
  
  /**
   * Sets the output text.
   * @param out output cache
   */
  public void setText(final CachedOutput out) {
    area.setSyntax(new XMLSyntax());
    
    final byte[] buf = out.buffer();
    String head = TEXTTIT;
    if(out.finished()) {
      final byte[] chop = Token.token(DOTS);
      Array.copy(chop, buf, out.size() - chop.length);
      head += RESULTCHOP;
    }
    area.setText(buf, out.size());
    if(!header.getText().equals(head)) header.setText(head);
    refreshed = true;
  }

  /**
   * Sets the output text.
   * @param txt text
   */
  public void setText(final byte[] txt) {
    area.setSyntax(BaseXSyntax.SIMPLE);
    area.setText(txt, txt.length);
  }

  /**
   * Activates the search field.
   */
  public void find() {
    search.requestFocusInWindow();
  }

  /**
   * Returns the text.
   * @return XQuery
   */
  public byte[] getText() {
    return area.getText();
  }
}
