package org.basex.gui.view.text;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.io.IOException;
import javax.swing.Box;
import org.basex.BaseX;
import org.basex.core.Prop;
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

/**
 * This class offers a fast text view, using the {@link BaseXText} class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class TextView extends View {
  /** Text Area. */
  private final BaseXText area;
  /** Header string. */
  private final BaseXLabel header;
  /** Find text field. */
  private final BaseXTextField find;
  /** Painted flag. */
  private boolean refreshed;

  /**
   * Default constructor.
   * @param man view manager
   */
  public TextView(final ViewNotifier man) {
    super(HELPTEXT, man);

    setLayout(new BorderLayout(0, 4));
    setBorder(6, 8, 8, 8);
    setFocusable(false);

    header = new BaseXLabel(TEXTTIT, true, false);

    final BaseXBack back = new BaseXBack(Fill.NONE);
    back.setLayout(new BorderLayout());
    back.add(header, BorderLayout.CENTER);

    final BaseXButton export = GUIToolBar.newButton(GUICommands.SAVE, gui);
    final BaseXButton root = GUIToolBar.newButton(GUICommands.HOME, gui);
    find = new BaseXTextField(null, gui);
    BaseXLayout.setHeight(find, (int) root.getPreferredSize().getHeight());

    final BaseXBack sp = new BaseXBack(Fill.NONE);
    sp.setLayout(new TableLayout(1, 5));
    sp.add(find);
    sp.add(Box.createHorizontalStrut(5));
    sp.add(root);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(export);
    back.add(sp, BorderLayout.EAST);
    add(back, BorderLayout.NORTH);

    area = new BaseXText(HELPTEXT, false, gui);
    area.addSearch(find);
    add(area, BorderLayout.CENTER);

    refreshLayout();
  }

  @Override
  public void refreshInit() {
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
    if(!GUIProp.showtext) return;

    try {
      final CachedOutput out = new CachedOutput(Prop.maxtext);
      if(nodes != null) {
        nodes.serialize(new XMLSerializer(out, false, nodes.data.meta.chop));
      }
      setText(out);
      refreshed = false;
    } catch(final IOException ex) {
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

  @Override
  protected boolean visible() {
    return GUIProp.showtext;
  }

  /**
   * Sets the output text.
   * @param out output cache
   */
  public void setText(final CachedOutput out) {
    area.setSyntax(new XMLSyntax());

    final byte[] buf = out.buffer();
    String head = TEXTTIT;
    if(out.finished()) head += RESULTCHOP;
    area.setText(buf, out.size());
    header.setText(head);
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
