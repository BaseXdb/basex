/*
 * -----------------------------------------------------------------------------
 *
 * This example shows how BaseX commands can be performed via the Ruby API.
 * The execution time will be printed along with the result of the command.
 *
 * -----------------------------------------------------------------------------
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * -----------------------------------------------------------------------------
 */
using System;

namespace BaseX
{
	public class Example
	{	
		public static void Main(string[] args)
		{
			new Session("localhost", 1984, "admin", "admin");
		}
	}
}
