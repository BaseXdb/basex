package org.basex.gui.view.text;

import static org.basex.gui.GUIConstants.*;
import java.awt.BorderLayout;
import org.basex.BaseX;
import org.basex.data.Nodes;
import org.basex.data.PrintSerializer;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.layout.BaseXBack;
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
  final BaseXText area;
  /** Header string. */
  final BaseXLabel header;
  /** Background for header.NORTH. **/
  BaseXBack north;
  /** Background for header.CENTER. **/
  BaseXBack center;
  

  /**
   * Default constructor.
   * @param mode panel design
   * @param head text header
   * @param help help text
   */
  public TextView(final FILL mode, final String head, final byte[] help) {
    super(help);
    setMode(mode);
    setBorder(8, 8, 8, 8);
    setLayout(new BorderLayout());
    area = new BaseXText(help, false);
    add(area, BorderLayout.CENTER);
    north = new BaseXBack(FILL.NONE);
    north.setLayout(new BorderLayout());
    header = new BaseXLabel(head, 10);
    initHeader();
    refreshLayout();
  }

  /**
   * Init header displaying ftsearch strings.
   */
  public void initHeader() {
    north.add(header, BorderLayout.WEST);
    //add(header, BorderLayout.NORTH);
    add(north, BorderLayout.NORTH);
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
    if(!GUIProp.showtext ||
        !header.getText().equals(GUIConstants.TEXTVIEW)) {
      return;
    }
    if(!GUI.context.db() || nodes.size == 0) {
      setText(Token.EMPTY, 0, true);
      return;
    }
    
    try {
      final CachedOutput out = new CachedOutput(MAX);
      final boolean chop = GUI.context.data().meta.chop;
      nodes.serialize(new PrintSerializer(out, false, chop));
      out.addInfo();
      setText(out.buffer(), out.size(), false);
    } catch(final Exception ex) {
      BaseX.debug(ex);
    }
  }

  @Override
  public void refreshLayout() {
    header.setFont(GUIConstants.lfont);
    header.setForeground(COLORS[16]);
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
}
