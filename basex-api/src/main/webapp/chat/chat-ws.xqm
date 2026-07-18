(:~
 : WebSocket chat with rooms. WebSocket functions.
 :
 : Like the web pages in chat.xqm, the functions here are tied to events by
 : annotations: %ws:connect runs when a client connects, %ws:message when a
 : message arrives, %ws:close when a connection ends, and %ws:error when the
 : connection fails. WebSocket URLs start with /ws, so the browser connects
 : to ws://HOST/ws/chat/<room> (see chat.js).
 :
 : The path is a template: '/chat/{$room=[a-z0-9-]+}' captures the room name
 : and binds it to the $room parameter. The =[a-z0-9-]+ part is a constraint;
 : a handshake for a room with any other characters is refused right away.
 :
 : @author BaseX Team, BSD License
 :)
module namespace chat-ws = 'chat-ws';

import module namespace chat-util = 'chat/util' at 'chat-util.xqm';

(:~
 : Runs before a client is allowed to connect: %perm:check functions guard
 : all URLs below the given path, including WebSocket handshakes. Without
 : this check, anyone could skip the login page and connect to a room
 : directly. The path also covers every room below /ws/chat.
 :)
declare
  %perm:check('/ws/chat')
function chat-ws:check() as empty-sequence() {
  (: no user in the session: refuse the upgrade with 403 :)
  if(empty(session:get($chat-util:id))) then web:error(403, 'Please log in.') else ()
};

(:~
 : Runs when a new client connects to a room. The '{$room}' part of the path
 : is bound to the $room parameter. The connection is only accepted if the
 : browser offers one of the listed sub-protocols; the server picks the first
 : match (see 'new WebSocket' in chat.js).
 : @param  $room  room the client connected to
 :)
declare
  %ws:connect('/chat/{$room=[a-z0-9-]+}')
  %ws:subprotocol('chat.v2', 'chat.v1')
function chat-ws:connect(
  $room  as xs:string
) as empty-sequence() {
  (: the session still knows who logged in :)
  let $name := session:get($chat-util:id)
  return (
    (: store the name with the new connection (identified by ws:id()) :)
    ws:set(ws:id(), $chat-util:id, $name),
    (: greet the new client, and tell everyone else that it joined :)
    chat-util:system('You joined the "' || chat-util:name($room) || '" room', ws:id()),
    chat-util:announce('User "' || $name || '" joined the "' || chat-util:name($room) || '" room'),
    (: send the updated users list to everyone :)
    chat-util:users(),
    (: log the connection; the request functions read details of the HTTP
     : request that opened the WebSocket (kept out of the chat window) :)
    admin:write-log('Chat connect [' || $room || ']: ' || $name || ' via ' ||
      request:scheme() || ' (' || request:header('User-Agent', 'unknown client') || ')', 'CHAT'),
    chat-ws:heartbeat()
  )
};

(:~
 : Handles an incoming message. The '{$message}' part of the annotation puts
 : the received text into the $message parameter; $room comes from the path.
 : @param  $room     room the message was sent from
 : @param  $message  message
 :)
declare
  %ws:message('/chat/{$room=[a-z0-9-]+}', '{$message}')
function chat-ws:message(
  $room     as xs:string,
  $message  as xs:string
) as empty-sequence() {
  (: the browser sends JSON text (see the send function in chat.js) :)
  let $json := parse-json($message)
  return switch($json?type)
    (: a chat message, public or private :)
    case 'message' return chat-util:message($json?text, $json?to, $room)
    (: a request for server statistics, answered asynchronously :)
    case 'info'    return chat-ws:info($room)
    (: anything else is unexpected: stop with an error :)
    default        return error()
};

(:~
 : Runs when a connection ends – no matter if the browser, the server, or a
 : network problem closed it. Unregisters the user, tells all clients, and
 : logs the close status and reason.
 : @param  $room    room the connection belonged to
 : @param  $status  close status code
 : @param  $reason  close reason (empty string if none was supplied)
 :)
declare
  %ws:close('/chat/{$room=[a-z0-9-]+}', '{$status}', '{$reason}')
function chat-ws:close(
  $room    as xs:string,
  $status  as xs:integer,
  $reason  as xs:string
) as empty-sequence() {
  (: the connection is still in the pool while this handler runs (BaseX removes
   : it afterwards, for a client- or server-side close alike). Read the name,
   : forget the connection, tell the others, and refresh everyone's users list. :)
  let $name := ws:get(ws:id(), $chat-util:id)
  return (
    ws:delete(ws:id(), $chat-util:id),
    $name ! chat-util:announce('User "' || . || '" left the "' || chat-util:name($room) || '" room'),
    chat-util:users(),
    admin:write-log('Chat close [' || $room || ']: ' || $status ||
      (': ' || $reason)[$reason], 'CHAT')
  )
};

(:~
 : Runs when a connection fails on the transport level (a broken pipe, a
 : protocol error, …). Its result is not sent to the client, so it is used
 : here for server-side logging only.
 : @param  $room     room the connection belonged to
 : @param  $message  error message
 :)
declare
  %ws:error('/chat/{$room=[a-z0-9-]+}', '{$message}')
function chat-ws:error(
  $room     as xs:string,
  $message  as xs:string
) as empty-sequence() {
  admin:write-log('Chat error [' || $room || ']: ' || $message, 'CHAT')
};

(:~
 : Answers a request for server statistics. ws:eval runs the query in a new
 : job; when it finishes, its result is pushed back to this connection only.
 : The query uses functions that do not need a bound connection (ws:ids,
 : ws:get and ws:path all work with an explicit id).
 : @param  $room  room the request came from
 :)
declare %private function chat-ws:info(
  $room  as xs:string
) as empty-sequence() {
  void(ws:eval(
    'declare variable $room external;
     declare variable $name external;
     let $ids := ws:ids()
     let $here := $ids[ws:path(.) = "/chat/" || $room]
     return {
       "type": "system",
       "text": count(distinct-values($ids ! ws:get(., "id"))) || " people online, " ||
         count($here) || " here in the " || $name || " room",
       "date": format-time(current-time(), "[H02]:[m02]:[s02]")
     }',
    { 'room': $room, 'name': chat-util:name($room) }
  ))
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
