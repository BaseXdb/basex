package org.basex.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.basex.server.ServerProcess;
import org.basex.server.Sessions;
import org.basex.util.Token;

/**
 * Management of Notification Triggers.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public final class TriggerPool {
  /** Active triggers - trigger name, registered sessions. */
  private final HashMap<String, Sessions> triggers =
    new HashMap<String, Sessions>();

  /**
   * Creates a trigger with the given name.
   * @param name The trigger name.
   * @return Returns true if trigger was created successfully. 
   */
  public boolean create(final String name) {
    if (triggers.containsKey(name))
      return false;
    
    triggers.put(name, new Sessions());
    return true;
  }

  /**
   * Drops the named trigger from the pool.
   * @param name The trigger name.
   * @return Returns true if trigger was deleted successfully. 
   */
  public boolean drop(final String name) {
    if (!triggers.containsKey(name))
      return false;
    
    triggers.remove(name);
    return true;
  }

  /**
   * Attaches the server process to the trigger.
   * @param name trigger name
   * @param sp server process
   * @return success of operation
   */
  public boolean attach(final String name, final ServerProcess sp) {
    final Sessions s = triggers.get(name);
    if(s == null) return false;
    s.add(sp);
    return true;
  }

  /**
   * Detaches the server process from the trigger.
   * @param name trigger name
   * @param sp server process
   * @return success of operation
   */
  public boolean detach(final String name, final ServerProcess sp) {
    final Sessions s = triggers.get(name);
    if(s == null) return false;
    s.delete(sp);
    return true;
  }
  
  /**
   * Returns information on all triggers.
   * @return Information on all triggers.
   * @throws IOException io exception
   */
  public byte[] info() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int i = triggers.size();
    for (String name : triggers.keySet()) {
      --i;
      out.write(name.getBytes());
      
      if (i != 0)
        out.write('\n');
    }
    return out.toByteArray();
  }

  /**
   * Notifies the attached sessions about a triggered event.
   * @param sp server process
   * @param name name
   * @param r item
   */
  public void notify(final ServerProcess sp, final byte[] name,
      final byte[] r) {
    Sessions sessions = triggers.get(Token.string(name));
    if (sessions == null)
      return;
    
    for (ServerProcess srv : sessions) {
      if(!srv.equals(sp)) {
        try {
          srv.out.write(name);
          srv.out.write(' ');
          srv.out.write(r);
          srv.out.write(0);
//          srv.out.writeString("result");
//          srv.out.writeString("INFO");
          srv.out.write(0);
          srv.out.write(0);
          srv.out.flush();
        } catch(IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }
}