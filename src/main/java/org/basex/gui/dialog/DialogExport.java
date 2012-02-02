package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.SortedMap;
import org.basex.core.Prop;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IOFile;
import org.basex.io.serial.SerializerProp;

/**
 * Dialog window for changing some project's preferences.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogExport extends Dialog {
  /** Available encodings. */
  private static final String[] ENCODINGS;
  /** Directory path. */
  private final BaseXTextField path;
  /** Database info. */
  private final BaseXLabel info;
  /** XML formatting. */
  private final BaseXCheckBox format;
  /** Encoding. */
  private final BaseXCombo encoding;
  /** Buttons. */
  private final BaseXBack buttons;

  // initialize encodings
  static {
    final SortedMap<String, Charset> cs = Charset.availableCharsets();
    ENCODINGS = cs.keySet().toArray(new String[cs.size()]);
  }

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogExport(final GUI main) {
    super(main, EXPORT_XML);

    // create checkboxes
    final BaseXBack pp = new BaseXBack(new TableLayout(3, 1, 0, 4));

    BaseXBack p = new BaseXBack(new TableLayout(2, 2, 8, 0));
    /* Output label. */
    final BaseXLabel out = new BaseXLabel(
        OUTPUT_DIR + COL, true, true).border(0, 0, 4, 0);
    p.add(out);
    p.add(new BaseXLabel());

    final String dir = new IOFile(gui.context.data().meta.original).dir();
    path = new BaseXTextField(dir, this);
    path.addKeyListener(keys);
    p.add(path);

    final BaseXButton browse = new BaseXButton(BROWSE_D, this);
    browse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { choose(); }
    });
    p.add(browse);
    pp.add(p);

    p = new BaseXBack(new TableLayout(2, 1));
    p.add(new BaseXLabel(ENCODING + COL, true, true).border(0, 0, 4, 0));

    final Prop prop = gui.context.prop;
    SerializerProp sp;
    try {
      sp = new SerializerProp(prop.get(Prop.EXPORTER));
    } catch(final IOException ex) {
      // ignore invalid serialization parameters
      sp = new SerializerProp();
    }

    encoding = new BaseXCombo(this, ENCODINGS);
    String enc = gui.context.data().meta.encoding;
    boolean f = false;
    for(final String s : ENCODINGS) f |= s.equals(enc);
    if(!f) {
      enc = enc.toUpperCase(Locale.ENGLISH);
      for(final String s : ENCODINGS) f |= s.equals(enc);
    }
    encoding.setSelectedItem(f ? enc : sp.get(SerializerProp.S_ENCODING));
    encoding.addKeyListener(keys);
    BaseXLayout.setWidth(encoding, BaseXTextField.DWIDTH);
    p.add(encoding);
    pp.add(p);

    format = new BaseXCheckBox(INDENT_WITH_WS,
        sp.get(SerializerProp.S_INDENT).equals(YES), 0, this);
    pp.add(format);
    set(pp, BorderLayout.CENTER);

    // create buttons
    p = new BaseXBack(new BorderLayout());
    info = new BaseXLabel(" ").border(18, 0, 0, 0);
    p.add(info, BorderLayout.WEST);
    buttons = okCancel();
    p.add(buttons, BorderLayout.EAST);
    set(p, BorderLayout.SOUTH);

    action(null);
    finish(null);
  }

  /**
   * Creates an encoding combo box.
   * @param dialog dialog reference
   * @param encoding original encoding
   * @return encoding combo box
   */
  static BaseXCombo encoding(final Dialog dialog, final String encoding) {
    final BaseXCombo cb = new BaseXCombo(dialog, DialogExport.ENCODINGS);
    boolean f = false;
    String enc = encoding;
    for(final String s : DialogExport.ENCODINGS) f |= s.equals(enc);
    if(!f) {
      enc = enc.toUpperCase(Locale.ENGLISH);
      for(final String s : DialogExport.ENCODINGS) f |= s.equals(enc);
    }
    cb.setSelectedItem(enc);
    cb.addKeyListener(dialog.keys);
    return cb;
  }

  /**
   * Opens a file dialog to choose an XML document or directory.
   */
  void choose() {
    final IOFile io = new BaseXFileChooser(CHOOSE_DIR, path.getText(), gui).
      select(BaseXFileChooser.Mode.DOPEN);
    if(io != null) path.setText(io.path());
  }

  /**
   * Returns the chosen XML file or directory path.
   * @return file or directory
   */
  public String path() {
    return path.getText().trim();
  }

  @Override
  public void action(final Object cmp) {
    final IOFile io = new IOFile(path());
    ok = !path().isEmpty();
    info.setText(io.children().length > 0 ? DIR_NOT_EMPTY
        : null, ok ? Msg.WARN : Msg.ERROR);
    enableOK(buttons, B_OK, ok);
  }

  @Override
  public void close() {
    if(!ok) return;
    super.close();
    final boolean indent = format.isSelected();
    gui.set(Prop.EXPORTER,
        SerializerProp.S_INDENT[0] + "=" + (indent ? YES : NO) + "," +
        SerializerProp.S_ENCODING[0] + "=" + encoding.getSelectedItem() + "," +
        SerializerProp.S_OMIT_XML_DECLARATION[0] + "=" + NO);
  }
}
