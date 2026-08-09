(:~
 : Evaluate queries of the editor via WebSockets.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/editor';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~
 : Handles a message of the editor.
 : @param  $message  message
 :)
declare
  %ws:message('/dba', '{$message}')
function dba:ws-message(
  $message  as xs:string
) as empty-sequence() {
  let $json := parse-json($message)
  return switch ($json?type) {
    case 'run'  return dba:ws-run($json?query, xs:integer($json?run), $json?indent = true())
    case 'stop' return (utils:ws-stop(), utils:ws-send({ 'type': 'stopped' }))
    default     return error((), 'Unknown message type: ' || $json?type)
  }
};

(:~
 : Stops a running query if the connection is closed.
 :)
declare
  %ws:close('/dba')
function dba:ws-close() as empty-sequence() {
  utils:ws-stop()
};

(:~
 : Reports an error to the client.
 : @param  $message  error message
 :)
declare
  %ws:error('/dba', '{$message}')
function dba:ws-error(
  $message  as xs:string
) as empty-sequence() {
  utils:ws-error('Editor', $message)
};

(:~
 : Evaluates a query and pushes its outcome to the client.
 : @param  $query   query string
 : @param  $run     number of the run
 : @param  $indent  indent result
 :)
declare %private function dba:ws-run(
  $query   as xs:string,
  $run     as xs:integer,
  $indent  as xs:boolean
) as empty-sequence() {
  utils:ws-stop(),
  let $file := config:edited-file()[.] ! file:name(.)
  (: two log entries per run, one before and one after the evaluation :)
  let $options := map:put(utils:job-options($file otherwise 'query'), 'log', 'DBA editor')
  return utils:ws-start(job:eval($query, (), $options), $run, utils:serialize-options($indent))
};
