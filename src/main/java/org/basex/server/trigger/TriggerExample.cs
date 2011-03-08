/*
 * This example shows how a client can be attached to a trigger.
 * Documentation: http://basex.org/api
 *
 * (C) BaseX Team 2005-11, ISC License
 */
using System;
using System.Diagnostics;
using System.IO;

namespace BaseXClient
{
  public class Example
  {
    public static void Main(string[] args)
    {
      try
      {

        // create session
        Session session = new Session("localhost", 1984, "admin", "admin");
        
		// attach at trigger
		session.AttachTrigger("trigger", new Notification());
				
        // close session
        // session.Close();
      }
      catch (IOException e)
      {
        // print exception
        Console.WriteLine(e.Message);
      }
    }
  }
  
  class Notification : TriggerNotification
    {
      public void Update (string data)
		{
			Console.WriteLine("TRIGGERED: " + data);
		}
    }
}