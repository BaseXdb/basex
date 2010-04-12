/*
 * -----------------------------------------------------------------------------
 * 
 * This Boo module provides methods to connect to and communicate with the
 * BaseX Server.
 *
 * The Constructor of the class expects a hostname, port, username and password
 * for the connection. The socket connection will then be established via the
 * hostname and the port.
 *
 * For the execution of commands you need to call the Execute() method with the
 * database command as argument. The method returns a boolean, indicating if
 * the command was successful. The result is stored in the Result property,
 * and the Info property returns additional processing information or error
 * output.
 *
 * An even faster approach is to call Execute() with the database command and
 * an output stream. The result will directly be printed and does not have to
 * be cached.
 * 
 * -----------------------------------------------------------------------------
 * Example:
 * namespace BaseX
 * import System

 * public class Example:

 * public static def Main(args as (string)):
 *		try:
 *			session = Session('localhost', 1984, 'admin', 'admin')
 *			// Version 1: perform command and show result or error output
 *			if session.Execute('xquery 1 to 10'):
 * 				Console.WriteLine(session.Result)
 *			else:
 *				Console.WriteLine(session.Info)
 *			stream as Stream = Console.OpenStandardOutput()
 *			// Version 2 (faster): send result to the specified output stream
 *			if not session.Execute('xquery 1 to 10', stream):
 *				Console.WriteLine(session.Info)
 *			session.Close()
 *		except e as Exception:
 *			Console.WriteLine(e.Message)
 * 
 * -----------------------------------------------------------------------------
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * -----------------------------------------------------------------------------
 */


namespace BaseX

import System
import System.Net.Sockets
import System.Security.Cryptography
import System.Text
import System.Collections.Generic
import System.IO

internal class BaseX:

	private result = MemoryStream()

	private cache as (byte) = array(byte, 4096)

	private stream as NetworkStream

	private socket as TcpClient

	private info = ''

	private bpos as int

	private bsize as int

	
	/** Constructor, creating a new socket connection. */
	
	public def constructor(host as string, port as int, username as string, pw as string):
		socket = TcpClient(host, port)
		stream = socket.GetStream()
		ts as string = ReadString()
		Send(username)
		Send(MD5((MD5(pw) + ts)))
		if stream.ReadByte() != 0:
			raise IOException('Access denied.')

	
	/** Executes the specified command. */
	
	public def Execute(com as string, ms as Stream) as bool:
		Send(com)
		Init()
		ReadString(ms)
		info = ReadString()
		return (Read() == 0)

	
	/** Executes the specified command. */
	
	public def Execute(com as string) as bool:
		result = MemoryStream()
		return Execute(com, result)

	
	/** Returns the result. */
	
	public Result as string:
		get:
			return System.Text.Encoding.UTF8.GetString(result.ToArray())

	
	/** Returns the processing information. */
	
	public Info as string:
		get:
			return info

	
	/** Closes the connection. */
	
	public def Close():
		Send('exit')
		socket.Close()

	
	/** Initializes the byte transfer. */
	
	private def Init():
		bpos = 0
		bsize = 0

	
	/** Returns a single byte from the socket. */
	
	private def Read() as byte:
		if bpos == bsize:
			bsize = stream.Read(cache, 0, 4096)
			bpos = 0
		return cache[(bpos++)]

	
	/** Receives a string from the socket. */
	
	private def ReadString(ms as Stream):
		while true:
			b as byte = Read()
			if b == 0:
				break 
			ms.WriteByte(b)

	
	/** Receives a string from the socket. */
	
	private def ReadString() as string:
		ms = MemoryStream()
		ReadString(ms)
		return System.Text.Encoding.UTF8.GetString(ms.ToArray())

	
	/** Sends strings to server. */
	
	private def Send(message as string):
		msg as (byte) = System.Text.Encoding.UTF8.GetBytes(message)
		stream.Write(msg, 0, msg.Length)
		stream.WriteByte(0)

	
	/** Returns the md5 hash of a string. */
	
	private def MD5(input as string) as string:
		MD5 = MD5CryptoServiceProvider()
		hash as (byte) = MD5.ComputeHash(Encoding.UTF8.GetBytes(input))
		
		sb = StringBuilder()
		for h as byte in hash:
			sb.Append(h.ToString('x2'))
		return sb.ToString()

