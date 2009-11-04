package org.basex.gui.view.text;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.io.IOException;
import javax.swing.Box;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLayout;
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

    final BaseXButton export = BaseXButton.command(GUICommands.SAVE, gui);
    final BaseXButton root = BaseXButton.command(GUICommands.HOME, gui);
    find = new BaseXTextField(gui);
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

    area = new BaseXText(false, gui);
    area.setSyntax(new XMLSyntax());
    area.addSearch(find);
    add(area, BorderLayout.CENTER);

    refreshLayout();
  }

  @Override
  public void refreshInit() {
    setText(gui.context.current());
  }

  @Override
  public void refreshFocus() {
  }

  @Override
  public void refreshMark() {
    setText(gui.context.marked());
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    setText(gui.context.current());
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
  public boolean visible() {
    return gui.prop.is(gui.context.data() != null ?
        GUIProp.SHOWTEXT : GUIProp.SHOWSTARTTEXT);
  }

  /**
   * Serializes the specified nodes.
   * @param nodes nodes to display
   */
  private void setText(final Nodes nodes) {
    if(!visible()) return;
    try {
      final CachedOutput out = new CachedOutput(
          gui.context.prop.num(Prop.MAXTEXT));
      if(nodes != null) {
        nodes.serialize(new XMLSerializer(out, false, nodes.data.meta.chop));
      }
      setText(out);
    } catch(final IOException ex) {
      Main.debug(ex);
    }
  }

  /**
   * Sets the output text.
   * @param out output cache
   */
  public void setText(final CachedOutput out) {
    area.setText(out.buffer(), out.size());
    header.setText(TEXTTIT + (out.finished() ? RESULTCHOP : ""));
  }

  /**
   * Sets the output as simple sting.
   * @param txt text
   */
  public void setText(final byte[] txt) {
    area.setText(txt);
  }

  /**
   * Returns the text.
   * @return XQuery
   */
  public byte[] getText() {
    return area.getText();
  }
}
