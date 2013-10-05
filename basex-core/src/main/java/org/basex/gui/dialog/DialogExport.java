package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.util.*;

/**
 * Dialog window for changing some project's preferences.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogExport extends BaseXDialog {
  /** Available encodings. */
  private static final String[] ENCODINGS;
  /** Directory path. */
  private final BaseXTextField path;
  /** Database info. */
  private final BaseXLabel info;
  /** Serialization method. */
  private final BaseXCombo method;
  /** Encoding. */
  private final BaseXCombo encoding;
  /** Buttons. */
  private final BaseXBack buttons;
  /** Parameters. */
  private final BaseXTextField params;

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
    super(main, EXPORT);

    // create checkboxes
    final BaseXBack p = new BaseXBack(new TableLayout(4, 1, 0, 0));
    p.add(new BaseXLabel(OUTPUT_DIR + COL, true, true).border(0, 0, 6, 0));

    // output label
    BaseXBack pp = new BaseXBack(new TableLayout(1, 2, 8, 0));

    path = new BaseXTextField(main.gopts.get(GUIOptions.INPUTPATH), this);
    path.history(gui, GUIOptions.INPUTS);
    pp.add(path);

    final BaseXButton browse = new BaseXButton(BROWSE_D, this);
    browse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { choose(); }
    });
    pp.add(browse);
    p.add(pp);

    // encoding
    final Options opts = gui.context.options;
    final String exporter = opts.get(Options.EXPORTER);
    final SerializerOptions sopts = new SerializerOptions(exporter);

    // encoding and method
    final String[] methods = new String[DataText.METHODS.length - 1];
    for(int m = 0; m < methods.length; m++) {
      methods[m] = DataText.METHODS[m].toUpperCase(Locale.ENGLISH);
    }
    method = new BaseXCombo(this, methods);
    method.setSelectedItem(sopts.get(SerializerOptions.S_METHOD).toUpperCase(
        Locale.ENGLISH));

    encoding = new BaseXCombo(this, ENCODINGS);
    String enc = sopts.get(SerializerOptions.S_ENCODING);
    boolean f = false;
    for(final String s : ENCODINGS) f |= s.equals(enc);
    if(!f) {
      enc = enc.toUpperCase(Locale.ENGLISH);
      for(final String s : ENCODINGS) f |= s.equals(enc);
    }
    encoding.setSelectedItem(f ? enc : sopts.get(SerializerOptions.S_ENCODING));

    params = new BaseXTextField(parameters(sopts, true), this);

    pp = new BaseXBack(new TableLayout(3, 2, 16, 6)).border(8, 0, 8, 0);
    pp.add(new BaseXLabel(METHOD + COL, true, true));
    pp.add(method);
    pp.add(new BaseXLabel(ENCODING + COL, true, true));
    pp.add(encoding);
    pp.add(new BaseXLabel(PARAMETERS + COL, true, true));
    pp.add(params);
    p.add(pp);
    info = new BaseXLabel(" ").border(8, 0, 0, 0);
    p.add(info);

    // indentation
    set(p, BorderLayout.CENTER);

    // buttons
    pp = new BaseXBack(new BorderLayout());
    buttons = okCancel();
    pp.add(buttons, BorderLayout.EAST);
    set(pp, BorderLayout.SOUTH);

    action(method);
    finish(null);
  }

  /**
   * Creates an encoding combo box and selects the specified encoding.
   * @param dialog dialog reference
   * @param encoding original encoding
   * @return combo box
   */
  static BaseXCombo encoding(final BaseXDialog dialog, final String encoding) {
    final BaseXCombo cb = new BaseXCombo(dialog, ENCODINGS);
    boolean f = false;
    String enc = encoding;
    for(final String s : ENCODINGS) f |= s.equals(enc);
    if(!f) {
      enc = enc.toUpperCase(Locale.ENGLISH);
      for(final String s : ENCODINGS) f |= s.equals(enc);
    }
    cb.setSelectedItem(enc);
    return cb;
  }

  /**
   * Opens a file dialog to choose an XML document or directory.
   */
  void choose() {
    final IOFile io = new BaseXFileChooser(CHOOSE_DIR, path.getText(), gui).
      select(Mode.DOPEN);
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
  public void action(final Object comp) {
    final String pth = path();
    final IOFile io = new IOFile(pth);
    String inf = io.isDir() && io.children().length > 0 ? DIR_NOT_EMPTY : null;
    ok = !pth.isEmpty();

    if(ok) {
      gui.gopts.set(GUIOptions.INPUTPATH, pth);
      // validate serialization parameters
      try {
        final String par = params.getText();
        Serializer.get(new ArrayOutput(), new SerializerOptions(par));
        gui.set(Options.EXPORTER, par);
      } catch(final IOException ex) {
        ok = false;
        inf = ex.getLocalizedMessage();
      }
    }

    info.setText(inf, ok ? Msg.WARN : Msg.ERROR);
    enableOK(buttons, B_OK, ok);
  }

  @Override
  public void close() {
    if(!ok) return;

    // remembers serialization parameters
    final String mth = method.getSelectedItem().toString().toLowerCase(Locale.ENGLISH);
    final String enc = encoding.getSelectedItem().toString();
    final SerializerOptions sp = new SerializerOptions(params.getText());
    sp.set(SerializerOptions.S_METHOD, mth);
    sp.set(SerializerOptions.S_ENCODING, enc);
    gui.set(Options.EXPORTER, parameters(sp, false));

    super.close();
    path.store();
  }

  /**
   * Create a serialization parameter string.
   * @param sopts serialization parameters
   * @param excl exclude method and encoding
   * @return result string
   */
  private String parameters(final SerializerOptions sopts, final boolean excl) {
    final String[] ex = !excl ? new String[0] : new String[] {
        SerializerOptions.S_METHOD.key, SerializerOptions.S_ENCODING.key };
    final StringBuilder sb = new StringBuilder();
    for(final String key : sopts) {
      if(Token.eq(key, ex)) continue;
      final Object val = sopts.get(key);
      if(!Serializer.OPTIONS.get(key).equals(val)) {
        if(sb.length() != 0) sb.append(',');
        sb.append(key).append('=').append(val);
      }
    }
    return sb.toString();
  }
}
