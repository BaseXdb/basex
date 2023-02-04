/*
 * Language Binding for BaseX.
 * Works with BaseX 7.0 and later
 *
 * Documentation: https://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-23, BSD License
 */
using System;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.IO;
using System.Threading;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Threading.Tasks;

namespace BaseXClient
{
  public class Session
  {
    private readonly byte[] cache = new byte[4096];
    internal NetworkStream Stream { get; private set; }
    private readonly TcpClient socket;
    private int bpos;
    private int bsize;

    private Session(TcpClient socket)
    {
      this.socket = socket;
      Stream = socket.GetStream();
    }
    public Session(string host, int port, string username, string pw) : this(new TcpClient(host, port))
    {
      Login(username, pw);
    }
    public static Session Create(string host, int port, string username, string pw)
    {
      return new Session(host, port, username, pw);
    }
    public static async Task<Session> CreateAsync(string host, int port, string username, string pw, CancellationToken cancellationToken = default)
    {
      TcpClient socket = new TcpClient(host, port);
      Session session = new Session(socket);
      await session.LoginAsync(username, pw, cancellationToken);
      return session;
    }

    private void Login(string username, string pw)
    {
      string[] response = Receive().Split(':');
      string nonce;
      string code;
      if (response.Length > 1)
      {
        code = username + ":" + response[0] + ":" + pw;
        nonce = response[1];
      }
      else
      {
        code = pw;
        nonce = response[0];
      }

      Send(username);
      Send(Md5(Md5(code) + nonce));
      if (Stream.ReadByte() != 0)
      {
        throw new IOException("Access denied.");
      }
    }
    
    private async Task LoginAsync(string username, string pw, CancellationToken cancellationToken = default)
    {
      string[] response = (await this.ReceiveAsync(cancellationToken)).Split(':');
      string nonce;
      string code;
      if (response.Length > 1)
      {
        code = username + ":" + response[0] + ":" + pw;
        nonce = response[1];
      }
      else
      {
        code = pw;
        nonce = response[0];
      }

      await this.SendAsync(username, cancellationToken);
      await this.SendAsync(Md5(Md5(code) + nonce), cancellationToken);
      if (Stream.ReadByte() != 0)
      {
        throw new IOException("Access denied.");
      }
    }



    public void Execute(string com, Stream ms)
    {
      Send(com);
      Init();
      Receive(ms);
      this.Info = Receive();
      if(!Ok())
      {
        throw new IOException(this.Info);
      }
    }

    public async Task ExecuteAsync(string com, Stream ms, CancellationToken cancellationToken = default)
    {
      await SendAsync(com, cancellationToken);
      Init();
      await ReceiveAsync(ms, cancellationToken);
      this.Info = await ReceiveAsync(cancellationToken);
      if(!await OkAsync(cancellationToken))
      {
        throw new IOException(this.Info);
      }
    }
    
    public String Execute(string com)
    {
      MemoryStream ms = new MemoryStream();
      Execute(com, ms);
      return Encoding.UTF8.GetString(ms.ToArray());
    }
    public async Task<String> ExecuteAsync(string com, CancellationToken cancellationToken = default)
    {
      MemoryStream ms = new MemoryStream();
      await ExecuteAsync(com, ms, cancellationToken);
      return Encoding.UTF8.GetString(ms.ToArray());
    }

    public Query Query(string q)
    {
      return new Query(this, q);
    }

    public void Create(string name, Stream s)
    {
      Stream.WriteByte(8);
      Send(name);
      Send(s);
    }
    public async Task CreateAsync(string name, Stream s, CancellationToken cancellationToken = default)
    {
      Stream.WriteByte(8);
      await SendAsync(name, cancellationToken);
      await SendAsync(s, cancellationToken);
    }
    
    public void Add(string path, Stream s)
    {
      Stream.WriteByte(9);
      Send(path);
      Send(s);
    }
    public async Task AddAsync(string path, Stream s, CancellationToken cancellationToken = default)
    {
      Stream.WriteByte(9);
      await SendAsync(path, cancellationToken);
      await SendAsync(s, cancellationToken);
    }
    
    public void Replace(string path, Stream s)
    {
      Stream.WriteByte(12);
      Send(path);
      Send(s);
    }
    
    public async Task ReplaceAsync(string path, Stream s, CancellationToken cancellationToken = default)
    {
      Stream.WriteByte(12);
      await SendAsync(path, cancellationToken);
      await SendAsync(s, cancellationToken);
    }
    
    public void Store(string path, Stream s)
    {
      Stream.WriteByte(13);
      Send(path);
      Send(s);
    }
    
    public async Task StoreAsync(string path, Stream s, CancellationToken cancellationToken = default)
    {
      Stream.WriteByte(13);
      await SendAsync(path, cancellationToken);
      await SendAsync(s, cancellationToken);
    }

    public string Info { get; private set; } = "";

    public void Close()
    {
      Send("exit");
      socket.Close();
    }
    
    public async Task CloseAsync(CancellationToken cancellationToken = default)
    {
      await SendAsync("exit", cancellationToken);
      socket.Close();
    }

    private void Init()
    {
      bpos = 0;
      bsize = 0;
    }

    public byte Read()
    {
      if (bpos == bsize)
      {
        bsize = Stream.Read(cache, 0, 4096);
        bpos = 0;
      }
      return cache[bpos++];
    }
    
    public async Task<byte> ReadAsync(CancellationToken cancellationToken = default)
    {
      if (bpos == bsize)
      {
        bsize = await Stream.ReadAsync(cache, 0, 4096, cancellationToken);
        bpos = 0;
      }
      return cache[bpos++];
    }

    private void Receive(Stream ms)
    {
      while (true)
      {
        byte b = Read();
        if (b == 0) break;
        // read next byte if 0xFF is received
        ms.WriteByte(b == 0xFF ? Read() : b);
      }
    }
    
    private async Task ReceiveAsync(Stream ms, CancellationToken cancellationToken = default)
    {
      while (true)
      {
        byte b = await ReadAsync(cancellationToken);
        if (b == 0) break;
        // read next byte if 0xFF is received
        ms.WriteByte(b == 0xFF ? await ReadAsync(cancellationToken) : b);
      }
    }

    public string Receive()
    {
      MemoryStream ms = new MemoryStream();
      Receive(ms);
      return Encoding.UTF8.GetString(ms.ToArray());
    }
    
    public async Task<string> ReceiveAsync(CancellationToken cancellationToken = default)
    {
      MemoryStream ms = new MemoryStream();
      await ReceiveAsync(ms, cancellationToken);
      return Encoding.UTF8.GetString(ms.ToArray());
    }

    public void Send(string message)
    {
      byte[] msg = Encoding.UTF8.GetBytes(message);
      Stream.Write(msg, 0, msg.Length);
      Stream.WriteByte(0);
    }
    
    public async Task SendAsync(string message, CancellationToken cancellationToken = default)
    {
      byte[] msg = Encoding.UTF8.GetBytes(message);
      await Stream.WriteAsync(msg, 0, msg.Length, cancellationToken);
      Stream.WriteByte(0);
    }

    private void Send(Stream s)
    {
      while (true)
      {
          int t = s.ReadByte();
          if (t == -1) break;
          if (t == 0x00 || t == 0xFF) Stream.WriteByte(Convert.ToByte(0xFF));
          Stream.WriteByte(Convert.ToByte(t));
      }
      Stream.WriteByte(0);
      this.Info = Receive();
      if(!Ok())
      {
        throw new IOException(this.Info);
      }
    }
    
    private async Task SendAsync(Stream s, CancellationToken cancellationToken = default)
    {
      while (true)
      {
        int t = s.ReadByte();
        if (t == -1) break;
        if (t == 0x00 || t == 0xFF) Stream.WriteByte(Convert.ToByte(0xFF));
        Stream.WriteByte(Convert.ToByte(t));
      }
      Stream.WriteByte(0);
      this.Info = await ReceiveAsync(cancellationToken);
      if(!await this.OkAsync(cancellationToken))
      {
        throw new IOException(this.Info);
      }
    }
    

    public bool Ok()
    {
      return Read() == 0;
    }
    public async Task<bool> OkAsync(CancellationToken cancellationToken = default)
    {
      return await ReadAsync(cancellationToken) == 0;
    }

    private static string Md5(string input)
    {
      MD5CryptoServiceProvider md5 = new MD5CryptoServiceProvider();
      byte[] hash = md5.ComputeHash(Encoding.UTF8.GetBytes(input));

      StringBuilder sb = new StringBuilder();
      foreach (byte h in hash)
      {
        sb.Append(h.ToString("x2"));
      }
      return sb.ToString();
    }
  }

  public class Query
  {
    private readonly Session session;
    private readonly string id;
    private ArrayList? cache;
    private int pos;

    private Query(string id, Session s)
    {
      session = s;
      this.id = id;
    }
    public Query(Session s, string query)
    {
      session = s;
      id = Exec(0, query);
    }
    public static Query Create(Session s, string query)
    {
      return new Query(s: s, query: query);
    }
    
    public static async Task<Query> CreateAsync(Session s, string query, CancellationToken cancellationToken = default)
    {
      string id = await ExecAsync(s, 0, query, cancellationToken);
      return new Query(id: id, s: s);
    }

    public void Bind(string name, string value)
    {
      Bind(name, value, "");
    }
    
    public async Task BindAsync(string name, string value, CancellationToken cancellationToken = default)
    {
      await this.BindAsync(name, value, "", cancellationToken);
    }

    public void Bind(string name, string value, string type)
    {
      cache = null;
      Exec(3, $"{id}\0{name}\0{value}\0{type}");
    }

    public async Task BindAsync(string name, string value, string type, CancellationToken cancellationToken = default)
    {
      cache = null;
      await this.ExecAsync(3, $"{this.id}\0{name}\0{value}\0{type}", cancellationToken);
    }

    public void Context(string value)
    {
      Context(value, "");
    }
    public Task ContextAsync(string value, CancellationToken cancellationToken = default)
    {
      return ContextAsync(value, "", cancellationToken);
    }

    public void Context(string value, string type)
    {
      cache = null;
      Exec(14, $"{id}\0{value}\0{type}");
    }

    public async Task ContextAsync(string value, string type, CancellationToken cancellationToken = default)
    {
      cache = null;
      await this.ExecAsync(14, $"{this.id}\0{value}\0{type}", cancellationToken);
    }

    [MemberNotNullWhen(true, nameof(cache))]
    public bool More()
    {
      if(cache is null) 
      {
        session.Stream.WriteByte(4);
        session.Send(id);
        cache = new ArrayList();
        while (session.Read() > 0)
        {
          cache.Add(session.Receive());
        }
        if(!session.Ok())
        {
          throw new IOException(session.Receive());
        }
        pos = 0;
      }
      if(pos < cache.Count) return true;
      cache = null;
      return false;
    }
    
    [MemberNotNullWhen(true, nameof(cache))]
    public async Task<bool> MoreAsync(CancellationToken cancellationToken = default)
    {
      if(cache is null) 
      {
        session.Stream.WriteByte(4);
        await this.session.SendAsync(this.id, cancellationToken);
        cache = new ArrayList();
        while (await this.session.ReadAsync(cancellationToken) > 0)
        {
          cache.Add(await this.session.ReceiveAsync(cancellationToken));
        }
        if(!session.Ok())
        {
          throw new IOException(await this.session.ReceiveAsync(cancellationToken));
        }
        pos = 0;
      }
      if(pos < cache.Count) return true;
      cache = null;
      return false;
    }

    public string? Next()
    {
      if(More()) 
      {
        return cache[pos++] as string;
      }
      else
      {
        return null;
      }
    }
    
    public async Task<string?> NextAsync(CancellationToken cancellationToken = default)
    {
      if(await MoreAsync(cancellationToken)) 
      {
        return cache[pos++] as string;
      }
      else
      {
        return null;
      }
    }

    public string Execute()
    {
      return Exec(5, id);
    }
    
    public async Task<string> ExecuteAsync(CancellationToken cancellationToken = default)
    {
      return await this.ExecAsync(5, this.id, cancellationToken);
    }

    public string Info()
    {
      return Exec(6, id);
    }
    
    public async Task<string> InfoAsync(CancellationToken cancellationToken = default)
    {
      return await this.ExecAsync(6, this.id, cancellationToken);
    }

    public string Options()
    {
      return Exec(7, id);
    }
    
    public async Task<string> OptionsAsync(CancellationToken cancellationToken = default)
    {
      return await this.ExecAsync(7, this.id, cancellationToken);
    }

    public void Close()
    {
      Exec(2, id);
    }
    
    public async Task CloseAsync(CancellationToken cancellationToken = default)
    {
      await this.ExecAsync(2, this.id, cancellationToken);
    }

    private string Exec(byte cmd, string arg)
    {
      session.Stream.WriteByte(cmd);
      session.Send(arg);
      string s = session.Receive();
      bool ok = session.Ok();
      if(!ok)
      {
        throw new IOException(session.Receive());
      }
      return s;
    }
    
    private Task<string> ExecAsync(byte cmd, string arg, CancellationToken cancellationToken = default)
    {
      return ExecAsync(this.session, cmd, arg, cancellationToken);
    }
    
    private static async Task<string> ExecAsync(Session session, byte cmd, string arg, CancellationToken cancellationToken = default)
    {
      session.Stream.WriteByte(cmd);
      await session.SendAsync(arg, cancellationToken);
      string s = await session.ReceiveAsync(cancellationToken);
      bool ok = await session.OkAsync(cancellationToken);
      if(!ok)
      {
        throw new IOException(await session.ReceiveAsync(cancellationToken));
      }
      return s;
    }
  }
}


namespace System.Diagnostics.CodeAnalysis
{
  [AttributeUsage(AttributeTargets.Method | AttributeTargets.Property, Inherited = false, AllowMultiple = true)]
  [SuppressMessage("ReSharper", "UnusedAutoPropertyAccessor.Global")]
  internal sealed class MemberNotNullAttribute : Attribute
  {
    public string[] Members { get; }
    public MemberNotNullAttribute(string member)
    {
      Members = new[] { member };
    }
    public MemberNotNullAttribute(params string[] members)
    {
      Members = members;
    }
  }
 
  [AttributeUsage(AttributeTargets.Method | AttributeTargets.Property, Inherited = false, AllowMultiple = true)]
  [SuppressMessage("ReSharper", "UnusedAutoPropertyAccessor.Global")]
  internal sealed class MemberNotNullWhenAttribute : Attribute
  {
    public bool ReturnValue { get; }
    public string[] Members { get; }
    public MemberNotNullWhenAttribute(bool returnValue, string member)
    {
        ReturnValue = returnValue;
        Members = new[] { member };
    }
    public MemberNotNullWhenAttribute(bool returnValue, params string[] members)
    {
        ReturnValue = returnValue;
        Members = members;
    }
  }
}