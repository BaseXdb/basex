(:~
 : Run the queries of the workspace view and refresh its file panel via WebSockets.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/workspace-ws';

import module namespace config = 'dba/lib/config' at '../lib/config.xqm';
import module namespace panel = 'dba/lib/file-panel' at 'file-panel.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~
 : Handles a message of the editor.
 : @param  $message  message
 :)
declare
  %ws:message('/dba/workspace', '{$message}')
function dba:ws-message(
  $message  as xs:string
) as empty-sequence() {
  let $json := parse-json($message)
  return switch ($json?type) {
    case 'run'   return dba:ws-run($json?query, xs:integer($json?run), $json?indent = true(),
                                   $json?dir, $json?file)
    case 'stop'  return (utils:ws-stop(), utils:ws-send({ 'type': 'stopped' }))
    case 'files' return dba:ws-files($json?sort, $json?dir)
    default      return error((), 'Unknown message type: ' || $json?type)
  }
};

(:~
 : Gives up the outcome of a query if the connection is closed.
 :)
declare
  %ws:close('/dba/workspace')
function dba:ws-close() as empty-sequence() {
  (: the query keeps running: its result is cached, and the activity view is where it is then
     watched and read :)
  utils:ws-stop(false())
};

(:~
 : Reports an error to the client.
 : @param  $message  error message
 :)
declare
  %ws:error('/dba/workspace', '{$message}')
function dba:ws-error(
  $message  as xs:string
) as empty-sequence() {
  utils:ws-error('Editor', $message)
};

(:~
 : Renders the file panel and pushes it to the client.
 : @param  $sort  sort key of the file list
 : @param  $dir   directory to be shown; a relative step is appended by the client
 :)
declare %private function dba:ws-files(
  $sort  as xs:string?,
  $dir   as xs:string?
) as empty-sequence() {
  utils:ws-panel('files-panel', panel:files($sort[.] otherwise 'name', $dir))
};

(:~
 : Evaluates a query and pushes its outcome to the client.
 : @param  $query   query string
 : @param  $run     number of the run
 : @param  $indent  indent result
 : @param  $dir     directory of the file panel
 : @param  $file    name of the opened file (empty for an untitled buffer)
 :)
declare %private function dba:ws-run(
  $query   as xs:string,
  $run     as xs:integer,
  $indent  as xs:boolean,
  $dir     as xs:string?,
  $file    as xs:string?
) as empty-sequence() {
  utils:ws-stop(),
  (: relative paths resolve against the opened file, or against the shown directory :)
  let $base-uri := config:files-dir($dir) || $file
  (: two log entries per run, one before and one after the evaluation :)
  let $options := utils:job-options($file[.] otherwise 'query', $base-uri)
    => map:put('log', 'DBA editor')
    => map:put('info', true())
  let $id := job:eval($query, (), $options)
  return (
    (: a notification, not an outcome: it carries no run number :)
    utils:ws-send({ 'type': 'job', 'id': $id }),
    utils:ws-start($id, $run, utils:serialize-options($indent))
  )
};
