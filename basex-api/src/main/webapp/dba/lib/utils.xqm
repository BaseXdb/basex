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
 : Evaluates a query, considering the configured limits.
 : @param  $query  query string
 : @return result
 :)
declare function utils:evaluate(
  $query  as xs:string?
) as item()* {
  xquery:eval($query, (), {
    'permission': config:get($config:PERMISSION),
    'timeout'   : config:get($config:TIMEOUT),
    'memory'    : config:get($config:MEMORY),
    'pass'      : true()
  })
};

(:~
 : Serializes a value as the expression that yields it again, so that what is shown can be
 : stored back.
 : @param  $value  value
 : @return expression text, and whether it was truncated
 :)
declare function utils:expression(
  $value  as item()*
) as map(*) {
  let $max := config:get($config:MAXCHARS)
  (: what holds further values is laid out over several lines :)
  let $serialized := serialize($value, {
    'method': 'adaptive', 'expression': true(), 'indent': true(), 'limit': $max * 2 + 1
  })
  (: a single item is an expression of its own: the parentheses that the expression method puts
     around every sequence are dropped for it. A truncated text has lost its closing one, and is
     left as it is :)
  let $text := if (count($value) = 1 and starts-with($serialized, '(') and
      ends-with($serialized, ')')) {
    substring($serialized, 2, string-length($serialized) - 2)
  } else {
    $serialized
  }
  let $truncated := string-length($text) > $max
  return {
    'text': if ($truncated) { substring($text, 1, $max) } else { $text },
    'truncated': $truncated
  }
};

(:~
 : Returns what an editor shows, and what can be done with it.
 : @param  $text     text the editor shows
 : @param  $reasons  reasons why the value cannot be edited, as sentence fragments
 : @return properties: whether the value can be edited, the reason why it cannot, and its text
 :)
declare function utils:editable(
  $text     as xs:string,
  $reasons  as xs:string*
) as map(*) {
  (: everything that speaks against editing is stated in one note; a value that nothing
     speaks against is edited in place :)
  {
    'editable': empty($reasons),
    'text'    : $text,
    'note'    : ('Read-only: ' || string-join($reasons, '; ') || '.')[exists($reasons)]
  }
};

(:~
 : Serializes a value, considering the specified system limits.
 : @param  $value  value
 : @return string
 :)
declare function utils:serialize(
  $value  as item()*
) as xs:string {
  utils:preview($value, config:get($config:MAXCHARS))
};

(:~
 : Serializes the beginning of a value: what is shown of it where a whole one has no room.
 : @param  $value  value
 : @param  $max    maximum number of characters
 : @return string
 :)
declare function utils:preview(
  $value  as item()*,
  $max    as xs:integer
) as xs:string {
  (: serialize more characters than requested, because limit represents number of bytes :)
  utils:chop(serialize($value, { 'method': 'basex', 'limit': $max * 2 + 1 }), $max)
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
 : Renders a panel and pushes it to the client.
 : @param  $id        id of the panel
 : @param  $contents  panel contents
 :)
declare function utils:ws-panel(
  $id        as xs:string,
  $contents  as element()*
) as empty-sequence() {
  (: the panel is named by the block it is filled into, so that the client needs no table of
     its own to tell one from another. Empty contents hide the panel, which is why the message
     is sent even then :)
  utils:ws-send({ 'type': 'panel', 'id': $id, 'html': utils:html($contents) })
};

(:~
 : Renders a panel and pushes it, together with the value that the editor beside it holds.
 : @param  $id        id of the panel
 : @param  $contents  panel contents
 : @param  $value     properties of the value: its text, and whether it can be edited
 :)
declare function utils:ws-editor(
  $id        as xs:string,
  $contents  as element()*,
  $value     as map(*)
) as empty-sequence() {
  utils:ws-send({
    'type'    : 'editor',
    'id'      : $id,
    'html'    : utils:html($contents),
    'text'    : $value?text,
    'editable': $value?editable = true()
  })
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
      (: the information is read first: fetching the result discards the cached job :)
      let $info := job:info($id)
      return map:merge((
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
        },
        { 'info': utils:html(utils:query-info($info)) }[exists($info)]
      ))
    }, (), { 'serializer': { 'method': 'json' } }),
    'query': $id
  })
};

(:~
 : Returns the name with which a key is displayed.
 : @param  $key  key
 : @return name
 :)
declare %private function utils:title(
  $key  as xs:string
) as xs:string {
  string-join(tokenize($key, '-') ! utils:capitalize(.), ' ')
};

(:~
 : Renders the query information of a job.
 : @param  $info  query information
 : @return sections
 :)
declare function utils:query-info(
  $info  as map(*)
) as element(dl) {
  <dl class='query-info'>{
    for $key in map:keys($info)
    let $value := $info($key)
    return (
      <dt>{ utils:title($key) }:</dt>,
      <dd>{
        if ($value instance of map(*)) {
          <ul>{
            map:for-each($value, fn($name, $entry) { <li>{ utils:title($name) }: { $entry }</li> })
          }</ul>
        } else if ($value instance of array(*)) {
          <ul>{ $value?* ! <li>{ . }</li> }</ul>
        } else {
          <pre>{ $value }</pre>
        }
      }</dd>
    )
  }</dl>
};

(:~
 : Stops the jobs that run for the current connection.
 : @param  $query  also stop the query. A query that is left running keeps its result cached,
 :                 and can be watched and read in the activity view
 :)
declare function utils:ws-stop(
  $query  as xs:boolean := true()
) as empty-sequence() {
  (: the reader is always stopped: once the connection is gone, it has nothing left to push
     its outcome to :)
  let $jobs := ws:get(ws:id(), $utils:JOB)
  return (
    ws:delete(ws:id(), $utils:JOB),
    (: wait for the jobs to be unregistered before new ones are started :)
    for $id in ($jobs?reader, $jobs?query[$query])
    return (job:remove($id), job:wait($id))
  )
};

(:~
 : Returns an ID for a job with the specified label.
 : @param  $label  label of the job
 : @return job ID
 :)
declare function utils:job-id(
  $label  as xs:string
) as xs:string {
  (: the connection completes the name: a name is only reserved once the job starts, so two
     connections that choose the same one at the same time would collide. A connection starts
     one job at a time, and gives up the name before it asks for the next one.
     The prefix of a connection id is a constant; only its number identifies the connection :)
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
 : Returns the entries to be shown on the current page.
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
  (: while a table is being sorted, all entries are returned: sorting and paging are then
     performed by the table itself :)
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
 : Resolves a relative path against a base directory.
 : @param  $dir   base directory
 : @param  $name  relative path
 : @return resolved native path, located within the base directory
 :)
declare function utils:safe-path(
  $dir   as xs:string,
  $name  as xs:string
) as xs:string {
  (: guards file access against path traversal, independent of the servlet container: a path
     that escapes the base is a bad request, never a file :)
  let $base := file:resolve-path($dir)
  let $path := file:resolve-path($name, $base)
  return if (starts-with($path, $base)) {
    $path
  } else {
    web:error(400, 'Invalid path: ' || $name)
  }
};

(:~
 : Resolves a file of the file panel: the directory the client supplies, and a name below it.
 : @param  $dir   directory supplied by the client (empty: use the default)
 : @param  $name  relative path
 : @return resolved native path, located within the directory
 :)
declare function utils:file-path(
  $dir   as xs:string?,
  $name  as xs:string
) as xs:string {
  (: the two belong together, so that the traversal guard cannot be skipped by resolving apart :)
  utils:safe-path(config:files-dir($dir), $name)
};

(:~
 : Returns the files of an upload: a form parameter is a map of names and contents if files
 : were chosen, and anything else if none were.
 : @param  $value  value of the form parameter
 : @return files, keyed by their name
 :)
declare function utils:files(
  $value  as item()*
) as map(*) {
  head($value[. instance of map(*)]) otherwise {}
};

(:~
 : Returns files as a download: a single file as it is, several of them in an archive.
 : @param  $paths    paths of the files
 : @param  $archive  name of the archive, without suffix
 : @return rest response and binary data
 :)
declare function utils:download(
  $paths    as xs:string*,
  $archive  as xs:string
) as item()+ {
  utils:archive($paths ! file:name(.), $paths ! file:read-binary(.), $archive)
};

(:~
 : Returns named contents as a download: a single one as it is, several of them in an archive.
 : @param  $names     names of the entries
 : @param  $contents  contents of the entries
 : @param  $archive   name of the archive, without suffix
 : @return rest response and binary data
 :)
declare function utils:archive(
  $names     as xs:string*,
  $contents  as item()*,
  $archive   as xs:string
) as item()+ {
  if (count($names) = 1) {
    utils:attachment($names, $contents)
  } else {
    utils:attachment($archive || '.zip', archive:create($names, $contents))
  }
};

(:~
 : Returns data as a downloadable attachment.
 : @param  $name  name of the file
 : @param  $data  data of the file
 : @param  $type  media type; derived from the name if none is supplied
 : @return rest response and file content
 :)
declare function utils:attachment(
  $name  as xs:string,
  $data  as item()*,
  $type  as xs:string? := ()
) as item()+ {
  web:response-header(
    { 'media-type': $type otherwise web:content-type($name) },
    utils:disposition($name)
  ),
  $data
};

(:~
 : Returns the header that offers a response as a download.
 : @param  $name  name of the file
 : @return response header
 :)
declare function utils:disposition(
  $name  as xs:string
) as map(*) {
  (: the name is encoded: a resource path may contain spaces, commas and characters outside
     ASCII, all of which a bare name would truncate or misrepresent :)
  { 'Content-Disposition': "attachment; filename*=UTF-8''" || encode-for-uri($name) }
};

(:~
 : Returns the URL of a DBA page.
 : @param  $page  name of the page
 : @return URL
 :)
declare function utils:page(
  $page  as xs:string
) as xs:string {
  (: the context path is not included: web:redirect resolves absolute locations against the
     request URI, and thus adds it already :)
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
    `{ utils:count($count, $name) } were { $action }.`
  }
};

(:~
 : Returns a noun in the form that a number requires.
 : @param  $count  number of items
 : @param  $noun   name of item (singular form)
 : @return noun
 :)
declare function utils:plural(
  $count  as xs:integer,
  $noun   as xs:string
) as xs:string {
  (: a noun that ends with a consonant and y is pluralized with -ies :)
  if ($count = 1) { $noun } else { replace($noun, 'y$', 'ie') || 's' }
};

(:~
 : Returns how many items there are, and what they are called.
 : @param  $count  number of items
 : @param  $noun   name of item (singular form)
 : @return count and noun
 :)
declare function utils:count(
  $count  as xs:integer,
  $noun   as xs:string
) as xs:string {
  `{ $count } { utils:plural($count, $noun) }`
};

(:~
 : Returns the message of an error that stopped a query: where it happened, and what it says.
 : @param  $module       module (can be {@code null})
 : @param  $line         line number (can be {@code null})
 : @param  $column       column number (can be {@code null})
 : @param  $description  error description
 : @return message
 :)
declare function utils:error-message(
  $module       as xs:string?,
  $line         as xs:integer?,
  $column       as xs:integer?,
  $description  as xs:string?
) as xs:string {
  `Stopped at { $module }, { $line }/{ $column }:{ char('\n') }{ $description }`
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
 : Returns the redirection that reports the outcome of an action.
 : @param  $page     page the action belongs to
 : @param  $params   query parameters of the target page
 : @param  $message  outcome: an 'info' or an 'error' entry; empty if there is nothing to report
 : @return redirection
 :)
declare function utils:outcome(
  $page     as xs:string,
  $params   as map(*),
  $message  as map(*)? := ()
) as element(rest:response) {
  (: every action ends in one of these, whether it is run by the dispatcher or by an endpoint
     of its own: the page it belongs to, with what it did or why it failed :)
  web:redirect(utils:page($page), map:merge(($params, $message)))
};

(:~
 : Reports the outcome of an update operation; see utils:outcome.
 : @param  $page     page the action belongs to
 : @param  $params   query parameters of the target page
 : @param  $message  outcome: an 'info' or an 'error' entry; empty if there is nothing to report
 :)
declare %updating function utils:redirect(
  $page     as xs:string,
  $params   as map(*),
  $message  as map(*)? := ()
) {
  update:output(utils:outcome($page, $params, $message))
};

(:~
 : Runs the requested action and redirects to the page it belongs to.
 : @param  $page     page the actions belong to
 : @param  $action   name of action
 : @param  $actions  actions of the category: each entry assigns a name to a function that takes
 :                   the request parameters and returns 'run' (the function that performs the
 :                   action, mandatory), 'params' (query parameters of the target page) and
 :                   'info' (info message)
 : @return redirection
 :)
declare %updating function utils:dispatch(
  $page     as xs:string,
  $action   as xs:string,
  $actions  as map(*)
) {
  let $entry := $actions?($action) otherwise web:error(404, 'Unknown action: ' || $action)
  (: an action can fail before it runs: a parameter that is evaluated is reported like the
     update it was meant for, not as a server error :)
  let $target := try {
    $entry(request:parameter-map())
  } catch * {
    { 'error': $err:description }
  }
  let $params := $target?params otherwise {}
  let $run := $target?run
  (: an info message is shown if the action succeeds, the error description if it fails :)
  return if ($target?error) {
    utils:redirect($page, $params, { 'error': $target?error })
  } else {
    try {
      updating $run(),
      utils:redirect($page, $params, { 'info': $target?info }[$target?info])
    } catch * {
      utils:redirect($page, $params, { 'error': $err:description })
    }
  }
};
