module namespace chat = 'http://basex.org/modules/web-page';

import module namespace session = 'http://basex.org/modules/Session';
import module namespace sessions = 'http://basex.org/modules/Sessions';
import module namespace ws = 'http://basex.org/modules/ws';

(:~ Session chat id. :)
declare variable $chat:ID		  := 'chat';
(:~ WebSocket ID of the WebSocket instance :)
declare variable $chat:ws-ID   := 'ws-id';

(:~ 
 : Processes a WebSocket message.
 : @param  $message  message
 :)
declare
  %ws:message('/chat', '{$message}')
function chat:ws-message(
  $message  as xs:string
) as empty-sequence() {
  let $json := parse-json($message)
  let $type := $json?type
  return if($type = 'message') then (
    chat:message($json?text, $json?to)
  ) else if($type = 'users') then (
    chat:users()
  ) else error()
};

(:~ 
 : Opens a WebSocket.
 :)
declare
  %ws:connect('/chat')
function chat:ws-connect() as empty-sequence() {
  let $ws-ids := session:get($chat:ws-ID)
  return session:set($chat:ws-ID, ($ws-ids, ws:id()))
};

(:~ 
 : Closes a WebSocket.
 :)
declare
  %ws:close('/chat')
function chat:ws-close() as empty-sequence() {
  let $ws-ids := session:get($chat:ws-ID)
  return (
    session:set($chat:ws-ID, $ws-ids[. != ws:id()]),
    chat:users()
  )
};
  
(:~
 : Sends a users list (all, active) to all registered clients.
 :)
declare %private function chat:users() as empty-sequence() {
  ws:emit(json:serialize(map {
    'type': 'users',
    'users': array { sort(user:list()) },
    'active': array { distinct-values(
      sessions:ids() ! sessions:get(., $chat:ID)
    )}
  }))
};

(:~ 
 : Sends a message to all clients, or to the clients of a specific user.
 : @param  $text  text to be sent
 : @param  $to    receiver of a private message (optional)
 :)
declare %private function chat:message(
  $text  as xs:string,
  $to    as xs:string?
) as empty-sequence() {
  let $ws-ids := trace(if($to) then (
    for $id in sessions:ids()
    where sessions:get($id, $chat:ID) = $to
    return sessions:get($id, $chat:ws-ID)
  ) else (
    ws:ids()
  ))
  return ws:send(json:serialize(map {
    'type': 'message',
    'text': serialize($text),
    'from': session:get($chat:ID),
    'date': format-time(current-time(), '[H02]:[m02]:[s02]'),
    'private': boolean($to)
  }), $ws-ids)
};
