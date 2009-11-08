package org.basex.gui.view.text;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.Box;
import org.basex.core.Main;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.dialog.Dialog;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXText;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewNotifier;
import org.basex.io.CachedOutput;
import org.basex.io.IO;
import org.basex.io.PrintOutput;

/**
 * This class offers a fast text view, using the {@link BaseXText} class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class TextView extends View implements ActionListener {
  /** Find text field. */
  private final BaseXTextField find;
  /** Header string. */
  private final BaseXLabel header;
  /** Text Area. */
  private final BaseXText area;
  /** Result process. */
  private Process proc;
  /** Result nodes. */
  private Nodes ns;

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

    final BaseXButton save = new BaseXButton(gui, "save", HELPSAVE);
    save.addActionListener(this);
    find = new BaseXTextField(gui);
    BaseXLayout.setHeight(find, (int) save.getPreferredSize().getHeight());

    final BaseXBack sp = new BaseXBack(Fill.NONE);
    sp.setLayout(new TableLayout(1, 5));
    sp.add(find);
    sp.add(Box.createHorizontalStrut(5));
    sp.add(save);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(BaseXButton.command(GUICommands.HOME, gui));
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
   * @param n nodes to display
   */
  private void setText(final Nodes n) {
    ns = n;
    if(!visible()) return;
    try {
      final CachedOutput out = new CachedOutput(
          gui.context.prop.num(Prop.MAXTEXT));
      if(n != null) {
        final XMLSerializer xml = new XMLSerializer(out);
        n.serialize(xml);
        xml.close();
      }
      setText(out, null);
    } catch(final IOException ex) {
      Main.debug(ex);
    }
  }

  /**
   * Sets the output text.
   * @param out output cache
   * @param p process
   */
  public void setText(final CachedOutput out, final Process p) {
    area.setText(out.buffer(), out.size());
    header.setText(TEXTTIT + (out.finished() ? RESULTCHOP : ""));
    if(!out.finished()) {
      proc = null;
      ns = null;
    } else {
      proc = p;
    }
  }

  public void actionPerformed(final ActionEvent e) {
    final BaseXFileChooser fc = new BaseXFileChooser(GUISAVEAS,
        gui.prop.get(GUIProp.SAVEPATH), gui);
    final IO file = fc.select(BaseXFileChooser.Mode.FSAVE);
    if(file == null) return;
    gui.prop.set(GUIProp.SAVEPATH, file.path());

    try {
      final PrintOutput out = new PrintOutput(file.toString());
      if(proc != null) proc.execute(gui.context, out);
      else if(ns != null) ns.serialize(new XMLSerializer(out));
      else out.write(area.getText());
      out.close();
    } catch(final IOException ex) {
      Dialog.error(gui, NOTSAVED);
    }
  }
}
