/*
 * This example shows how to use the event feature.
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-12, BSD License
 */
using System;
using System.Diagnostics;
using System.IO;

namespace BaseXClient
{
  public class EventExample
  {
    public static void Main(string[] args)
    {
      try
      {
        // create session
        Session session1 = new Session("localhost", 1984, "admin", "admin");
        Session session2 = new Session("localhost", 1984, "admin", "admin");

        session1.Execute("create event messenger");
        session2.Watch("messenger", new Notification());
        session2.Query("for $i in 1 to 1000000 where $i = 0 return $i").Execute();
        session1.Query("db:event('messenger', 'Hello World!')").Execute();
        session2.Unwatch("messenger");
        session1.Execute("drop event messenger");

        // close session
        session1.Close();
        session2.Close();
      }
      catch (IOException e)
      {
        // print exception
        Console.WriteLine(e.Message);
      }
    }
  }
  
  class Notification : EventNotification
  {
    public void Update (string data)
    {
      Console.WriteLine("Message received: " + data);
    }
  }
}