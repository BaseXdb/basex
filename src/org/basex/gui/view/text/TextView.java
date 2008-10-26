package org.basex.gui.view.text;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import org.basex.BaseX;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.gui.GUI;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIToolBar;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXSyntax;
import org.basex.gui.layout.BaseXText;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.view.View;
import org.basex.io.CachedOutput;
import org.basex.util.Token;

/**
 * This class offers a fast text view, using the {@link BaseXText} class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class TextView extends View {
  /** Maximum text size to be displayed. */
  public static final int MAX = 1 << 20;
  /** Text Area. */
  private final BaseXText area;
  /** Header string. */
  private final BaseXLabel header;
  /** Open button. */
  private BaseXButton export;
  
  /**
   * Default constructor.
   * @param mode panel design
   * @param head text header
   * @param help help text
   */
  public TextView(final FILL mode, final String head, final byte[] help) {
    super(help);
    setMode(mode);
    setBorder(4, 8, 8, 8);
    setLayout(new BorderLayout());
    area = new BaseXText(help, false);
    add(area, BorderLayout.CENTER);
    
    header = new BaseXLabel(head, true);
    export = GUIToolBar.newButton(GUICommands.EXPORT);

    final Box box = new Box(BoxLayout.X_AXIS);
    box.add(header);
    if(head.equals(TEXTTIT)) {
      box.add(Box.createHorizontalGlue());
      box.add(export);
    }
    add(box, BorderLayout.NORTH);

    refreshLayout();
  }
  
  @Override
  public void refreshInit() {
    area.setCaret(0);
    refreshDoc(GUI.context.current());
  }

  @Override
  public void refreshFocus() {
    repaint();
  }

  @Override
  public void refreshMark() {
    refreshDoc(GUI.context.marked());
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    refreshDoc(GUI.context.current());
  }

  /**
   * Refreshes the doc display.
   * @param nodes nodes to display
   */
  private void refreshDoc(final Nodes nodes) {
    if(!GUIProp.showtext || !header.getText().equals(TEXTTIT)) return;

    if(!GUI.context.db() || nodes.size == 0) {
      setText(Token.EMPTY, 0, true);
      return;
    }
    
    try {
      final CachedOutput out = new CachedOutput(MAX);
      final boolean chop = GUI.context.data().meta.chop;
      nodes.serialize(new XMLSerializer(out, false, chop));
      out.addInfo();
      setText(out.buffer(), out.size(), false);
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
   * @param txt text
   * @param s size
   * @param inf info flag
   */
  public void setText(final byte[] txt, final int s, final boolean inf) {
    area.setText(txt, s);
    area.setSyntax(inf ? BaseXSyntax.SIMPLE : new XMLSyntax());
  }

  /**
   * Returns the text.
   * @return XQuery
   */
  public byte[] getText() {
    return area.getText();
  }
}
