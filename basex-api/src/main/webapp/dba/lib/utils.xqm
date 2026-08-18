(:~
 : Utility functions.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace utils = 'dba/lib/utils';

import module namespace config = 'dba/lib/config' at 'config.xqm';

(:~ WebSocket attribute: id of the job that runs for the current connection. :)
declare %private variable $utils:JOB := 'dba-job';

(:~ Regular expression for XQuery files; matches the suffixes of IO.XQSUFFIXES. :)
declare variable $utils:XQUERY-REGEX := '\.(xq|xqm|xqy|xql|xqu|xquery|xpath)$';

(:~ Regular expression for backup names. :)
declare variable $utils:BACKUP-REGEX := '^(.*)-(\d{4}-\d\d-\d\d)-(\d\d)-(\d\d)-(\d\d)$';
(:~ Regular expression for the file names of backups. :)
declare variable $utils:BACKUP-ZIP-REGEX := '^(.*)-(\d{4}-\d\d-\d\d)-(\d\d)-(\d\d)-(\d\d)\.zip$';

(:~
 : Parses a query.
 : @param  $query  query string
 : @param  $uri    base URI
 : @return parse result
 :)
declare function utils:query-parse(
  $query  as xs:string,
  $uri    as xs:string
) as element() {
  xquery:parse($query, {
    'base-uri': $uri,
    'plan'    : false(),
    'pass'    : true()
  })
};

(:~
 : Serializes a value, considering the specified system limits.
 : @param  $value  value
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
 : Renders a panel and pushes it to the client. Empty contents hide the panel, which is why the
 : message is sent even then.
 : @param  $type      type of the panel
 : @param  $contents  panel contents
 :)
declare function utils:ws-panel(
  $type      as xs:string,
  $contents  as element()*
) as empty-sequence() {
  utils:ws-send({ 'type': $type, 'html': utils:html($contents) })
};

(:~
 : Serializes nodes as the HTML that the client inserts into a panel.
 : @param  $nodes  nodes
 : @return HTML
 :)
declare function utils:html(
  $nodes  as node()*
) as xs:string {
  serialize($nodes, { 'method': 'html' })
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
  (: the log keeps the full text, the client is sent the code and its description :)
  admin:write-log($category || ': ' || $message, 'DBA'),
  let $text := replace($message, '\s*Stack Trace:.*', '', 's')
  let $line := tokenize($text, '\n')[starts-with(., '[')]
  return utils:ws-send({ 'type': 'error', 'message': head($line) otherwise $text })
};

(:~
 : Starts a job that pushes the outcome of a query to the client, and registers both jobs for the
 : current connection.
 : @param  $id       id of the job to wait for
 : @param  $run      number of the run (echoed to the client, which drops outdated results)
 : @param  $options  serialization parameters
 :)
declare function utils:ws-start(
  $id       as xs:string,
  $run      as xs:integer,
  $options  as map(*)
) as empty-sequence() {
  let $maxchars := config:get($config:MAXCHARS)
  (: the reader is registered as well: it must be gone before the ID is assigned again :)
  return ws:set(ws:id(), $utils:JOB, {
    'reader': ws:eval(fn() {
      job:wait($id),
      try {
        let $string := serialize(job:result($id), $options)
        return {
          'type'  : 'result',
          'run'   : $run,
          'result': if ($options?limit) { utils:chop($string, $maxchars) } else { $string }
        }
      } catch * {
        {
          'type'   : 'error',
          'run'    : $run,
          'message': $err:description,
          'line'   : $err:line-number,
          'column' : $err:column-number
        }
      }
    }, (), { 'serializer': { 'method': 'json' } }),
    'query': $id
  })
};

(:~
 : Stops the jobs that run for the current connection. The reader is always stopped: once the
 : connection is gone, it has nothing left to push its outcome to.
 : @param  $query  also stop the query. A query that is left running keeps its result cached,
 :                 and can be watched and read in the activity view
 :)
declare function utils:ws-stop(
  $query  as xs:boolean := true()
) as empty-sequence() {
  let $jobs := ws:get(ws:id(), $utils:JOB)
  return (
    ws:delete(ws:id(), $utils:JOB),
    (: wait for the jobs to be unregistered before new ones are started :)
    for $id in ($jobs?reader, $jobs?query[$query])
    return (job:remove($id), job:wait($id))
  )
};

(:~
 : Returns an ID for a job with the specified label. The connection the job is started for
 : completes the name: a name is only reserved once the job starts, so two connections that
 : choose the same one at the same time would collide. A connection starts one job at a time,
 : and gives up the name before it asks for the next one.
 : @param  $label  label of the job
 : @return job ID
 :)
declare function utils:job-id(
  $label  as xs:string
) as xs:string {
  (: the prefix of a connection id is a constant; only its number identifies the connection :)
  'dba:' || $label || '-' || replace(ws:id(), '^websocket', '')
};

(:~
 : Returns the options for running a query as job.
 : @param  $label     label of the job
 : @param  $base-uri  base URI against which the query resolves relative paths
 : @return options
 :)
declare function utils:job-options(
  $label     as xs:string,
  $base-uri  as xs:string?
) as map(*) {
  map:merge((
    {
      'timeout'   : config:get($config:TIMEOUT),
      'memory'    : config:get($config:MEMORY),
      'permission': config:get($config:PERMISSION),
      'cache'     : true(),
      'id'        : utils:job-id($label)
    },
    { 'base-uri': $base-uri }[$base-uri]
  ))
};

(:~
 : Returns the entries to be shown on the current page. While a table is being sorted, all entries
 : are returned, as sorting and paging are then performed by the table itself.
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
 : Returns files as a download. A single file is sent as it is; several files are packed
 : into an archive, with the supplied name.
 : @param  $paths    paths of the files
 : @param  $archive  name of the archive, without suffix
 : @return rest response and binary data
 :)
declare function utils:download(
  $paths    as xs:string*,
  $archive  as xs:string
) as item()+ {
  if (count($paths) = 1) {
    utils:attachment(file:name($paths), file:read-binary($paths))
  } else {
    utils:attachment($archive || '.zip',
      archive:create($paths ! file:name(.), $paths ! file:read-binary(.)))
  }
};

(:~
 : Returns binary data as a downloadable attachment.
 : @param  $name  name of the file
 : @param  $data  binary data
 : @return rest response and binary data
 :)
declare %private function utils:attachment(
  $name  as xs:string,
  $data  as xs:base64Binary
) as item()+ {
  web:response-header({ 'media-type': web:content-type($name) }, utils:disposition($name)),
  $data
};

(:~
 : Returns the header that offers a response as a download. The name is encoded: a resource path
 : may contain spaces, commas and characters outside ASCII, all of which a bare name would
 : truncate or misrepresent.
 : @param  $name  name of the file
 : @return response header
 :)
declare function utils:disposition(
  $name  as xs:string
) as map(*) {
  { 'Content-Disposition': "attachment; filename*=UTF-8''" || encode-for-uri($name) }
};

(:~
 : Returns the URL of a DBA page. The context path is not included: web:redirect resolves
 : absolute locations against the request URI, and thus adds it already.
 : @param  $page  name of the page
 : @return URL
 :)
declare function utils:page(
  $page  as xs:string
) as xs:string {
  '/dba/' || $page
};

(:~
 : Returns an info message for the specified items: a single item is named, others are counted.
 : @param  $items   items
 : @param  $name    name of item (singular form)
 : @param  $action  action label (past tense)
 : @return message
 :)
declare function utils:info(
  $items   as item()*,
  $name    as xs:string,
  $action  as xs:string
) as xs:string {
  let $count := count($items)
  return if ($count = 1) {
    `{ utils:capitalize($name) } "{ $items }" was { $action }.`
  } else {
    (: a noun that ends with a consonant and y is pluralized with -ies :)
    `{ $count } { replace($name, 'y$', 'ie') }s were { $action }.`
  }
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
 : Runs the requested action and redirects to the page it belongs to: an info message is shown
 : if the action succeeds, the error description if it fails.
 : The actions of a category are supplied as a map. Each entry assigns an action name to a
 : function that takes the request parameters and returns the following keys:
 : * 'run': function performing the action (mandatory)
 : * 'params': query parameters of the target page
 : * 'info': info message
 :
 : @param  $page     page the actions belong to
 : @param  $action   name of action
 : @param  $actions  actions of the category
 : @return redirection
 :)
declare %updating function utils:dispatch(
  $page     as xs:string,
  $action   as xs:string,
  $actions  as map(*)
) {
  let $entry := $actions?($action) otherwise web:error(404, 'Unknown action: ' || $action)
  let $url := utils:page($page)
  (: an action can fail before it runs: a parameter that is evaluated is reported like the
     update it was meant for, not as a server error :)
  let $target := try {
    $entry(request:parameter-map())
  } catch * {
    { 'error': $err:description }
  }
  let $params := $target?params otherwise {}
  let $run := $target?run
  return if ($target?error) {
    utils:redirect($url, map:put($params, 'error', $target?error))
  } else {
    try {
      updating $run(),
      utils:redirect($url, map:merge((
        $params, { 'info': $target?info }[$target?info]
      )))
    } catch * {
      utils:redirect($url, map:put($params, 'error', $err:description))
    }
  }
};
