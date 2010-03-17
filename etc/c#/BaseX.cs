/*
 * ----------------------------------------------------------------------------- 
 * 
 * This C# module provides methods to connect to and communicate with the
 * BaseX Server.
 *
 * The Constructor of the class expects a hostname, port, username and password
 * for the connection. The socket connection will then be established via the
 * hostname and the port.
 *
 * For the execution of commands you need to call the execute() method with the
 * database command as argument. The method returns a boolean, indicating if
 * the command was successful. The result can be requested with the result()
 * method, and the info() method returns additional processing information
 * or error output.
 *
 * -----------------------------------------------------------------------------
 * Example:
 * 
 * -----------------------------------------------------------------------------
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * -----------------------------------------------------------------------------
 */
using System;
using System.Net.Sockets;

namespace BaseX
{
	class Session
	{
		public Session(String host, int port, String username, String pw) 
		{
			TcpClient socket = new TcpClient(host, port);
		}
	}
}