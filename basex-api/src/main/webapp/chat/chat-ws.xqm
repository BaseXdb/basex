(:~
 : Simple WebSocket chat. WebSocket functions.
 :
 : Like the web pages in chat.xqm, the functions here are tied to events by
 : annotations: %ws:connect runs when a client connects, %ws:message when a
 : message arrives, and %ws:close when a connection ends. WebSocket URLs
 : start with /ws, so the browser connects to ws://HOST/ws/chat (see chat.js).
 :
 : @author BaseX Team, BSD License
 :)
module namespace chat-ws = 'chat-ws';

import module namespace chat-util = 'chat/util' at 'chat-util.xqm';

(:~
 : Runs when a new client connects: registers the user, tells all clients,
 : and makes sure the heartbeat job is running.
 :)
declare
  %ws:connect('/chat')
  (: the connection is only accepted if the browser asks for the
   : same protocol name (see 'new WebSocket' in chat.js) :)
  %ws:subprotocol('chat.v1')
function chat-ws:connect() as empty-sequence() {
  (: the session still knows who logged in; store that name
   : with the new connection (identified by ws:id()) :)
  ws:set(ws:id(), $chat-util:id, session:get($chat-util:id)),
  (: send the updated users list to everyone :)
  chat-util:users(),
  chat-ws:heartbeat()
};

(:~
 : Handles an incoming message. The '{$message}' part of the annotation
 : puts the received text into the $message parameter.
 : @param  $message  message
 :)
declare
  %ws:message('/chat', '{$message}')
function chat-ws:message(
  $message  as xs:string
) as empty-sequence() {
  (: the browser sends JSON text (see the send function in chat.js) :)
  let $json := parse-json($message)
  return if($json?type = 'message') then (
    chat-util:message($json?text, $json?to)
  ) else error()  (: anything else is unexpected: stop with an error :)
};

(:~
 : Runs when a connection ends – no matter if the browser, the server, or
 : a network problem closed it. Unregisters the user, tells all clients,
 : and logs the close status and reason.
 : @param  $status  close status code
 : @param  $reason  close reason (empty string if none was supplied)
 :)
declare
  %ws:close('/chat', '{$status}', '{$reason}')
function chat-ws:close(
  $status  as xs:integer,
  $reason  as xs:string
) as empty-sequence() {
  (: forget the connection and send the updated users list to everyone :)
  ws:delete(ws:id(), $chat-util:id),
  chat-util:users(),
  admin:write-log('Chat connection closed: ' || $status || (': ' || $reason)[$reason], 'CHAT')
};

(:~
 : Starts a job that sends a short ping to all clients every 15 seconds.
 : Without it, connections that stay quiet for a while could be dropped.
 :)
declare %private function chat-ws:heartbeat() as empty-sequence() {
  (: do nothing if the job was already started by an earlier connection :)
  if(job:list() = 'chat-heartbeat') then () else void(
    (: run the given query every 15 seconds ('PT15S'); it
     : pings all clients that are connected at that moment :)
    job:eval('ws:ids() ! ws:ping(.)', (), { 'id': 'chat-heartbeat', 'interval': 'PT15S' })
  )
};
