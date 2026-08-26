(:~
 : Refresh the panels of the databases view and query its resources via WebSockets.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/databases-ws';

import module namespace panels = 'dba/lib/db-panels' at 'panels.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~
 : Handles a message of the databases view.
 : @param  $message  message
 :)
declare
  %ws:message('/dba/databases', '{$message}')
function dba:ws-message(
  $message  as xs:string
) as empty-sequence() {
  let $json := parse-json($message)
  let $sort := string($json?sort)
  let $page := xs:integer($json?page otherwise 1)
  return switch ($json?type) {
    case 'databases'   return utils:ws-panel('databases-panel',
      panels:databases($sort, $page, $json?name))
    case 'database'    return utils:ws-panel('database-panel',
      panels:database($json?name, $sort, $page, $json?resource, string($json?dir),
        string($json?filter)))
    case 'backups'     return utils:ws-panel('backups-panel', panels:backups($json?name))
    case 'information' return utils:ws-panel('information-panel', panels:information($json?name))
    case 'index'       return utils:ws-panel('index-panel',
      panels:index($json?name, string($json?index), string($json?prefix), $sort, $page))
    case 'resource'    return dba:ws-resource($json?name, $json?resource)
    case 'query'       return dba:ws-query($json?name, $json?resource, $json?query,
                                           xs:integer($json?run), $json?indent = true())
    case 'stop'        return (utils:ws-stop(), utils:ws-send({ 'type': 'stopped' }))
    default            return error((), 'Unknown message type: ' || $json?type)
  }
};

(:~
 : Stops a running query if the connection is closed.
 :)
declare
  %ws:close('/dba/databases')
function dba:ws-close() as empty-sequence() {
  utils:ws-stop()
};

(:~
 : Reports an error to the client.
 : @param  $message  error message
 :)
declare
  %ws:error('/dba/databases', '{$message}')
function dba:ws-error(
  $message  as xs:string
) as empty-sequence() {
  utils:ws-error('Databases', $message)
};

(:~
 : Renders the resource panel and pushes it, together with the document it refers to.
 : @param  $name      selected database
 : @param  $resource  selected resource
 :)
declare %private function dba:ws-resource(
  $name      as xs:string?,
  $resource  as xs:string?
) as empty-sequence() {
  let $document := panels:document($name, $resource)
  return utils:ws-editor('resource-panel', panels:resource($name, $resource, $document), $document)
};

(:~
 : Evaluates a query on a resource and pushes its outcome to the client.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $query     query string
 : @param  $run       number of the run
 : @param  $indent    indent result
 :)
declare %private function dba:ws-query(
  $name      as xs:string,
  $resource  as xs:string,
  $query     as xs:string,
  $run       as xs:integer,
  $indent    as xs:boolean
) as empty-sequence() {
  (: the resource is the context of the query; requesting it unchanged is the query '.' :)
  (: a query on a large resource takes time: stop one that is superseded by this one :)
  utils:ws-stop(),
  let $id := job:eval(fn(
    $db      as xs:string,
    $path    as xs:string,
    $string  as xs:string
  ) {
    let $context := head(panels:resource-value($db, $path))
    return xquery:eval($string, { '': $context }, { 'pass': true() })
  }, [ $name, $resource, $query ], utils:job-options($name || '/' || $resource, ()))
  return utils:ws-start($id, $run, utils:serialize-options($indent))
};
