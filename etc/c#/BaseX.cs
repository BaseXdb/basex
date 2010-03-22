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
using System.Collections.Generic;
using System.IO;

namespace BaseX
{
	class Session
	{
		public NetworkStream stream = null;
		public TcpClient socket = null;
		public MemoryStream result = null;
		public string info = "";
		public int bpos = 0;
		public int bsize = 0;
		public byte[] inStream = new byte[4096];
		
		/** Constructor, creating a new socket connection. */
		public Session(string host, int port, string username, string pw) 
		{
			socket = new TcpClient(host, port);
			stream = socket.GetStream();
			init();
			string ts = readString();
			string h = md5(pw);
			string hts = h + ts;
			string end = md5(hts);
			send(username + "\0");
			send(end + "\0");
			if (stream.ReadByte() != 0) {
			 	throw new Exception("Access denied.");
			}
		}
		
		/** Executes the specified command. */
		public bool execute(string com, MemoryStream ms) {
			send(com + "\0");
			init();
			readString(ms);
			info = readString();
			return read() == 0;
		}
		
		/** Executes the specified command. */
		public bool execute(string com) {
			result = new MemoryStream();
			return execute(com, result);
		}
		
		/** Returns the result. */
		public string res() {
			return System.Text.Encoding.UTF8.GetString(result.GetBuffer());
		}
		
		/** Returns the processing information. */
		public string inf() {
			return info;
		}
		
		/** Closes the connection. */
		public void close() {
			send("exit \0");
			socket.Close();
		}
		
		/** Initializes the byte transfer. */
		private void init() {
			bpos = 0;
			bsize = 0;
		}
		
		/** Returns a single byte from the socket. */
		private byte read() {
			if (bpos == bsize) {
				bsize = stream.Read(inStream, 0, 4096);
				bpos = 0;
			}
			byte b = inStream[bpos];
			bpos += 1;
			return b;
		}
		
		/** Receives a string from the socket. */
		private void readString(MemoryStream ms) {
			while (true) {
				byte b = read();
				if (b != 0) {
					ms.WriteByte(b);
				} else {
					break;
				}
			}
		}
		
		/** Receives a string from the socket. */
		private string readString() {
			List<byte> list = new List<byte>();
			while (true) {
				byte b = read();
				if (b != 0) {
					list.Add(b);
				} else {
					return System.Text.Encoding.UTF8.GetString(list.ToArray());
				}
			}
		}
		
		/** Sends strings to server. */
		private void send(string message) {
			byte[] outStream = System.Text.Encoding.UTF8.GetBytes(message);
			stream.Write(outStream, 0, outStream.Length);
            stream.Flush();
		}
		
		/** Returns the md5 hash of a string. */
		private string md5(string input) {
			MD5CryptoServiceProvider MD5 = new MD5CryptoServiceProvider();
			byte[] hashValue = new byte[1];
        	byte[] bytes = Encoding.UTF8.GetBytes(input);
        	hashValue = MD5.ComputeHash(bytes);
        	StringBuilder sb = new StringBuilder();

			for (var i = 0; i <= hashValue.Length - 1; i++) {
                sb.Append(hashValue[i].ToString("x2"));
            }
            return sb.ToString();
		}
	}
}