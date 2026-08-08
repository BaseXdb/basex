(:~
 : Utility functions.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace utils = 'dba/utils';

import module namespace config = 'dba/config' at 'config.xqm';

(:~ WebSocket attribute: id of the job that runs for the current connection. :)
declare %private variable $utils:JOB := 'dba-job';

(:~ Regular expression for backups. :)
declare variable $utils:BACKUP-REGEX := '^(.*)-(\d{4}-\d\d-\d\d)-(\d\d)-(\d\d)-(\d\d)$';
(:~ Regular expression for backups. :)
declare variable $utils:BACKUP-ZIP-REGEX := '^(.*)-(\d{4}-\d\d-\d\d)-(\d\d)-(\d\d)-(\d\d)\.zip$';

(:~
 : Parses a query.
 : @param  $query  query string
 : @param  $uri    base URI (optional)
 : @return parse result
 :)
declare function utils:query-parse(
  $query  as xs:string,
  $uri    as xs:string?
) as element() {
  xquery:parse($query, {
    'base-uri': $uri otherwise config:edited-file() otherwise config:editor-dir(),
    'plan'    : false(),
    'pass'    : true()
  })
};

(:~
 : Serializes a value, considering the specified system limits.
 : @param  $value   value
 : @param  $indent  indent output
 : @return string
 :)
declare function utils:serialize(
  $value  as item()*
) as xs:string {
  utils:chop(serialize($value, utils:serialize-options(false())), config:get($config:MAXCHARS))
};

(:~
 : Returns the parameters for serializing a query result.
 : @param  $indent  indent output
 : @return serialization parameters
 :)
declare function utils:serialize-options(
  $indent  as xs:boolean
) as map(*) {
  (: serialize more characters than requested, because limit represents number of bytes :)
  {
    'limit' : config:get($config:MAXCHARS) * 2 + 1,
    'indent': $indent,
    'method': 'basex'
  }
};

(:~
 : Sends a message to the client of the current WebSocket connection.
 : @param  $message  message
 :)
declare function utils:ws-send(
  $message  as map(*)
) as empty-sequence() {
  ws:send(serialize($message, { 'method': 'json' }), ws:id())
};

(:~
 : Logs an error of a WebSocket connection and reports it to the client.
 : @param  $category  category of the connection
 : @param  $message   error message
 :)
declare function utils:ws-error(
  $category  as xs:string,
  $message   as xs:string
) as empty-sequence() {
  admin:write-log($category || ': ' || $message, 'DBA'),
  utils:ws-send({ 'type': 'error', 'message': $message })
};

(:~
 : Registers a job for the current connection and starts a job that pushes its outcome.
 : @param  $id       id of the job to wait for
 : @param  $run      number of the run (echoed to the client, which drops outdated results)
 : @param  $options  serialization parameters
 :)
declare function utils:ws-start(
  $id       as xs:string,
  $run      as xs:integer,
  $options  as map(*)
) as empty-sequence() {
  ws:set(ws:id(), $utils:JOB, $id),
  void(ws:eval(xs:anyURI('ws-eval.xq'), {
    'id'     : $id,
    'run'    : $run,
    'options': $options
  }, { 'serializer': { 'method': 'json' } }))
};

(:~
 : Stops the job that runs for the current connection.
 :)
declare function utils:ws-stop() as empty-sequence() {
  for $id in ws:get(ws:id(), $utils:JOB)
  return (
    ws:delete(ws:id(), $utils:JOB),
    job:remove($id)
  )
};

(:~
 : Returns the options for running a query as job.
 : @return options
 :)
declare function utils:job-options() as map(*) {
  {
    'timeout'   : config:get($config:TIMEOUT),
    'memory'    : config:get($config:MEMORY),
    'permission': config:get($config:PERMISSION),
    'base-uri'  : config:edited-file() otherwise config:editor-dir()
  }
};

(:~
 : Returns the entries to be shown on the current page. While a table is being sorted, all entries
 : are returned, as sorting and paging are then performed in html:table.
 : @param  $entries  all entries
 : @param  $page     current page
 : @param  $sort     sort key
 : @return entries to display
 :)
declare function utils:slice(
  $entries  as item()*,
  $page     as xs:integer,
  $sort     as xs:string
) as item()* {
  if ($page and not($sort)) {
    let $max := config:get($config:MAXROWS)
    return subsequence($entries, ($page - 1) * $max + 1, $max)
  } else {
    $entries
  }
};

(:~
 : Chops a string result to the maximum number of allowed characters.
 : @param  $string  string
 : @param  $max     maximum number of characters
 : @return string
 :)
declare function utils:chop(
  $string  as xs:string,
  $max     as xs:integer
) as xs:string {
  if (string-length($string) > $max) {
    substring($string, 1, $max) || '...'
  } else {
    $string
  }
};

(:~
 : Resolves a relative path against a base directory. Guards file access against path traversal,
 : independent of the servlet container; raises a bad-request error if the path escapes the base.
 : @param  $dir   base directory
 : @param  $name  relative path
 : @return resolved native path, located within the base directory
 :)
declare function utils:safe-path(
  $dir   as xs:string,
  $name  as xs:string
) as xs:string {
  let $base := file:resolve-path($dir)
  let $path := file:resolve-path($name, $base)
  return if (starts-with($path, $base)) {
    $path
  } else {
    web:error(400, 'Invalid path: ' || $name)
  }
};

(:~
 : Returns a count info for the specified items.
 : @param  $items   items
 : @param  $name    name of item (singular form)
 : @param  $action  action label (past tense)
 : @return result
 :)
declare function utils:info(
  $items   as item()*,
  $name    as xs:string,
  $action  as xs:string
) as xs:string {
  let $count := count($items)
  return `{ $count } { $name || (if ($count != 1) then 's were ' else ' was ') || $action }.`
};

(:~
 : Capitalizes a string.
 : @param  $string  string
 : @return capitalized string
 :)
declare function utils:capitalize(
  $string  as xs:string
) as xs:string {
  upper-case(substring($string, 1, 1)) || substring($string, 2)
};

(:~
 : Convenience function for redirecting to another page from update operations.
 : @param  $url     URL
 : @param  $params  query parameters
 :)
declare %updating function utils:redirect(
  $url     as xs:string,
  $params  as map(*)
) {
  update:output(web:redirect($url, $params))
};

(:~
 : Runs the requested action and redirects to its target page: an info message is shown if the
 : action succeeds, the error description if it fails.
 : The actions of a category are supplied as a map. Each entry assigns an action name to a
 : function that takes the request parameters and returns the following keys:
 : * 'page': target page (mandatory)
 : * 'run': function performing the action (mandatory)
 : * 'params': query parameters of the target page
 : * 'info': info message
 :
 : @param  $action   name of action
 : @param  $actions  actions of the category
 : @return redirection
 :)
declare %updating function utils:dispatch(
  $action   as xs:string,
  $actions  as map(*)
) {
  let $entry := $actions?($action) otherwise web:error(404, 'Unknown action: ' || $action)
  let $target := $entry(request:parameter-map())
  let $page := '/dba/' || $target?page
  let $params := $target?params otherwise {}
  let $run := $target?run
  return try {
    updating $run(),
    utils:redirect($page, map:merge((
      $params, { 'info': $target?info }[$target?info]
    )))
  } catch * {
    utils:redirect($page, map:put($params, 'error', $err:description))
  }
};
