/*
 * -----------------------------------------------------------------------------
 *
 * This example shows how BaseX commands can be performed via the C# API.
 * The execution time will be printed along with the result of the command.
 *
 * -----------------------------------------------------------------------------
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * -----------------------------------------------------------------------------
 */
using System;
using System.Diagnostics;

namespace BaseX
{
	public class Example
	{	
		public static void Main(string[] args)
		{	
			// initialize timer
			Stopwatch watch = new Stopwatch();
			watch.Start();
			
			// command to be performed
			string com = "xquery 1 to 10";
			
			try {
				// create session
				Session session = new Session("localhost", 1984, "admin", "admin");
				
				// perform command and show result or error output
				if (session.execute(com)) {
					Console.WriteLine(session.res());
				} else {
					Console.WriteLine(session.inf());
				}
				
				// close session
				session.close();
				
				// print time needed
				watch.Stop();
				Console.WriteLine(watch.ElapsedMilliseconds + " ms.");
				Console.ReadLine();
			} catch (Exception e) {
				Console.WriteLine(e.Message);
			}
		}
	}
}