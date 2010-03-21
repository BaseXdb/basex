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
 * using System;
 * 
 * namespace BaseX
 *	{
 *	public class Example
 *	{	
 *		public static void Main(string[] args)
 *		{	
 *			try {
 *				Session session = new Session("localhost", 1984, "admin", "admin");
 *				if ((session.execute("xquery 1 to 10")) == "\0") {
 *					Console.WriteLine(s.res());
 * 				} else {
 *					Console.WriteLine(s.inf());
 *				}
 *				session.close();
 *			} catch (Exception e) {
 *				Console.WriteLine(e.Message);
 *		  }
 *		}
 *	  }
 *	} 
 * 
 * -----------------------------------------------------------------------------
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * -----------------------------------------------------------------------------
 */
using System;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;

namespace BaseX
{
	class Session
	{
		public NetworkStream stream = null;
		public TcpClient socket = null;
		public byte[] outStream = null;
		public byte[] inStream = null;
		public string result = "";
		public string info = "";
		
		/** Constructor, creating a new socket connection. */
		public Session(string host, int port, string username, string pw) 
		{
			socket = new TcpClient(host, port);
			stream = socket.GetStream();
			string ts = readString();
			string h = md5(pw);
			string hts = h + ts;
			string end = md5(hts);
			send(username + "\0");
			send(end + "\0");
			if (read() != "\0") {
			 	throw new Exception("Access denied.");
			}
		}
		
		/** Executes the specified command. */
		public string execute(string com) {
			send(com + "\0");
			result = readString();
			info = readString();
			return read();
		}
		
		/** Returns the result. */
		public string res() {
			return result;
		}
		
		/** Returns the processing information. */
		public string inf() {
			return info;
		}
		
		/** Closes the connection. */
		public void close() {
			send("exit");
			socket.Close();
		}
		
		/** Receives a single byte from the socket. */
		private string read() {
			inStream = new byte[1];
			stream.Read(inStream, 0, 1);
			string test = System.Text.Encoding.ASCII.GetString(inStream);
            return test;
		}
		
		/** Returns the received string. */
		private string readString() {
			string complete = "";
			string t = "";
			while ((t = read()) != "\0") {
				complete += t;
			}
			return complete;
		}
		
		/** Sends strings to server. */
		private void send(string message) {
			outStream = System.Text.Encoding.ASCII.GetBytes(message);
			stream.Write(outStream, 0, outStream.Length);
            stream.Flush();
		}
		
		/** Returns the md5 hash of a string. */
		private string md5(string input) {
		var MD5 = new MD5CryptoServiceProvider();
		var hashValue = new Byte[1];
        byte[] bytes = Encoding.ASCII.GetBytes(input);
        hashValue = MD5.ComputeHash(bytes);
        var sb = new StringBuilder();

		for (var i = 0; i <= hashValue.Length - 1; i++)
            {
                sb.Append(hashValue[i].ToString("x2"));
            }
            return sb.ToString();
		}
	}
}