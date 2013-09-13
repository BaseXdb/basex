/**
 * Language Binding for BaseX.
 * Works with BaseX 7.0 and later
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * (C) Manfred Knobloch, BSD License
 * Institut für Wissensmedien IWM - Knowledge Media Research Center (KMRC)
 **/
package basexapi
{
	import com.adobe.crypto.MD5;
	
	import flash.errors.*;
	import flash.events.*;
	import flash.net.Socket;
	import flash.system.Security;
	import flash.utils.ByteArray;
	
	public class Session extends Object
	{
		public static const DISCONNECTED:int =  0;
		public static const CONNECTING:int = 1;
		public static const CONNECTED:int = 2;
		public static const AUTHENTICATING:int = 3;
		public static const AUTHENTICATED:int = 4;
		public static const OPERATING:int = 5;
		public static const RESULT:int = 6;
		
		[Bindable]
		public var response:String= "";
		[Bindable]
		protected var _socket:Socket;			
		[Bindable]
		public var state:int = DISCONNECTED;
				
		
		protected var _result:ByteArray;
		
		protected var user:String;
		protected var password:String;
		protected var _query:Query;
		public var socketBuffer:ByteArray;
		
		public function Session(h:String=null, p:uint=0, u:String=null,
								pass:String=null)
		{
			user = u;
			password = pass;
			_socket = new Socket();
			_socket.addEventListener(Event.CLOSE, closeHandler);
			_socket.addEventListener(Event.CONNECT, connectHandler);
			_socket.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
			_socket.addEventListener(SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);
			_socket.addEventListener(ProgressEvent.SOCKET_DATA, socketDataHandler);
			state = CONNECTING;
			_socket.connect(h,p);		
			socketBuffer = new ByteArray();
		}
		
		public function execute(arg:String):void{
			socketBuffer = new ByteArray();
			socketBuffer.writeUTFBytes(arg);
			socketBuffer.writeByte(0);
			sendBuffer(socketBuffer);
		}
		
		public function close():void{
			_socket.close();
		}
		
		public function query(q:String):Query{
			_query = new Query(this, q);
			return _query;	
		}
		
		public function sendCommand(code:int, arg:String, input:String):void{
			socketBuffer = new ByteArray();
			socketBuffer.writeByte(code);
			socketBuffer.writeUTFBytes(arg);
			socketBuffer.writeByte(0);
			socketBuffer.writeUTFBytes(input);
			socketBuffer.writeByte(0);
			sendBuffer(socketBuffer);
		}
		
		public function sendBuffer(b:ByteArray):void{
			response = "";
			_socket.writeBytes(b);
			_socket.flush();
		}
		
		protected function sendInput(code:int, arg:String):void{
			var breakat:int = arg.indexOf(" ");
			if (breakat == -1){
				return;
			}
			var arg1:String = arg.substr(0,breakat);
			var arg2:String = arg.substring(breakat);
			sendCommand(code,arg1,arg2);
		}
		
		public function create(input:String):void{			
			sendInput(8,input);	
		}
		public function add(input:String):void{
			sendInput(9, input);	
		}
		public function replace(input:String):void{
			sendInput(12, input);	
		}
		public function store(input:String):void{
			sendInput(13,input);	
		}
		
		public function authenticate():void{
			var md5pw:String = MD5.hash(password);
			var md5resp:String = MD5.hash(md5pw + response);
			socketBuffer = new ByteArray();
			socketBuffer.writeUTFBytes(user);
			socketBuffer.writeByte(0);
			socketBuffer.writeUTFBytes(md5resp);
			socketBuffer.writeByte(0);
			response = "";
			_socket.writeBytes(socketBuffer);
			_socket.flush();
			state = AUTHENTICATING;
		}
		
		public function readIntoByteBuffer(buf:ByteArray):void{
			_socket.readBytes(buf,0,_socket.bytesAvailable);
		}

		public function readstrings():String{
			_result = new ByteArray();
			var last:int = 0;
			var current:int = 0;
			var foundStrings:Vector.<String> = new Vector.<String>;
			_socket.readBytes(_result,0,_socket.bytesAvailable);
			
			//read to letter 0 or end of bytes
			while (_result.bytesAvailable > 0 ) {
				if(_result.readByte() == 0){
					current = _result.position;
					if (current > last){
						_result.position = last;
						foundStrings.push(_result.readUTFBytes(current-last));
						last = current;
					}
				}
			}				
			return foundStrings.join("");
		}
		
		// wrapper for public
		public function readUTFBytes():String{
			return _socket.readUTFBytes(_socket.bytesAvailable);
		}
		
		private function socketDataHandler(event:ProgressEvent):void {
			if (state == AUTHENTICATING){
				if( _socket.readByte() == 0){
					state = AUTHENTICATED;
					return;
				}
			}
			
			response = readstrings();
			
			if(_query && (_query.state == _query.PENDING_ID_REQUEST)){ 
				_query.id = response;
				_query.state = _query.ID_RECEIVED;
				if (!_query.needBind()){
					_query.execute();
					return;
				}
				
				_query.bind();
				return;				
			}
			
			if(_query && (_query.state == _query.PENDING_BIND_REQUEST)){
				_query.state = _query.BIND_RESPONSE
				_query.execute();
				return;
			}
			
			if (state < AUTHENTICATING){
				authenticate();
				state = AUTHENTICATING
			}
				
		}
		
		// some string representations
		public function get stateinfo():String{
			var info:String = "";
			switch (state) {
				case DISCONNECTED:
					info =  "DISCONNECTED";
					break;
				case CONNECTING:
					info = "CONNECTING";
					break;
				case CONNECTED:
					info = "CONNECTED";
					break;
				case AUTHENTICATING:
					info = "AUTHENTICATING";
					break;
				case AUTHENTICATED:
					info = "AUTHENTICATED";
					break;
				case OPERATING:
					info = "OPERATING";
					break;
			}
			return info;
		}
		
		// handler functions
		private function closeHandler(event:Event):void {
			trace("closeHandler: " + event);
		}
		
		private function connectHandler(event:Event):void {
			state = CONNECTED;
		}
		
		private function ioErrorHandler(event:IOErrorEvent):void {
			trace("ioErrorHandler: " + event);
		}
		
		private function securityErrorHandler(event:SecurityErrorEvent):void {
			trace("securityErrorHandler: " + event);
		}
	}
}