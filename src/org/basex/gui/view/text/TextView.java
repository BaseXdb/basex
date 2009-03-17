package org.basex.gui.view.text;

import static org.basex.Text.*;

import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import org.basex.BaseX;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIToolBar;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXSyntax;
import org.basex.gui.layout.BaseXText;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewNotifier;
import org.basex.io.CachedOutput;
import org.basex.util.Token;

/**
 * This class offers a fast text view, using the {@link BaseXText} class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
  /** Painted flag. */
  private boolean refreshed;
  
  /**
   * Default constructor.
   * @param man view manager
   */
  public TextView(final ViewNotifier man) {
    super(man, HELPTEXT);

    setLayout(new BorderLayout(0, 4));
    setBorder(4, 8, 8, 8);
    setMode(Fill.DOWN);
    
    area = new BaseXText(gui, HELPTEXT, false);
    add(area, BorderLayout.CENTER);
    
    header = new BaseXLabel(TEXTTIT, true);
    export = GUIToolBar.newButton(GUICommands.SAVE, gui);

    final Box box = new Box(BoxLayout.X_AXIS);
    box.add(header);
    box.add(Box.createHorizontalGlue());
    box.add(export);
    add(box, BorderLayout.NORTH);

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
    // skip refresh if text display has already been refreshed
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
      final CachedOutput out = new CachedOutput(MAX);
      nodes.serialize(new XMLSerializer(out, false, nodes.data.meta.chop));
      out.addInfo();
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
    area.setText(out.buffer(), out.size());
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
   * Returns the text.
   * @return XQuery
   */
  public byte[] getText() {
    return area.getText();
  }
}
