(:~
 : Simple WebSocket chat. WebSocket functions.
 : @author BaseX Team 2005-20, BSD License
 :)
module namespace chat-ws = 'chat-ws';

import module namespace chat-util = 'chat/util' at 'chat-util.xqm';
import module namespace request = "http://exquery.org/ns/request";

(:~ 
 : Creates a WebSocket connection. Registers the user and notifies all clients.
 :)
declare
  %ws:connect('/chat')
function chat-ws:connect() as empty-sequence() {
  ws:set(ws:id(), $chat-util:id, session:get($chat-util:id)),
  chat-util:users()
};

(:~ 
 : Processes a WebSocket message.
 : @param  $message  message
 :)
declare
  %ws:message('/chat', '{$message}')
function chat-ws:message(
  $message  as xs:string
) as empty-sequence() {
  let $json := parse-json($message)
  let $type := $json?type
  return if($type = 'message') then (
    chat-util:message($json?text, $json?to)
  ) else if($type = 'ping') then(
    (: do nothing :)
  ) else error()
};

(:~ 
 : Closes a WebSocket connection. Unregisters the user and notifies all clients.
 :)
declare
  %ws:close('/chat')
function chat-ws:close() as empty-sequence() {
  ws:delete(ws:id(), $chat-util:id),
  chat-util:users()
};
