(:~
 : Logs.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/logs';

import module namespace form = 'dba/lib/form' at '../lib/form.xqm';
import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace table = 'dba/lib/table' at '../lib/table.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'logs';

(:~ Maximum number of entries that a search over several files collects. :)
declare variable $dba:MAX-ENTRIES := 10000;

(:~ Table columns. :)
declare variable $dba:COLUMNS := (
  (: the values of a known format get the width they need; the text takes the rest :)
  { 'key': 'time', 'label': 'Time', 'order': 'desc', 'width': 'calc(12ch + 0.5rem)' },
  { 'key': 'address', 'label': 'Address', 'width': '11rem' },
  { 'key': 'user', 'label': 'User', 'width': '11rem' },
  { 'key': 'type', 'label': 'Type', 'width': 'calc(8ch + 0.5rem)' },
  { 'key': 'ms', 'label': 'ms', 'type': 'decimal', 'order': 'desc', 'width': 'calc(8ch + 0.5rem)' },
  { 'key': 'text', 'label': 'Text' }
);

(:~
 : Logs.
 : @param  $input  search input
 : @param  $name   name (date) of log file
 : @param  $sort   table sort key
 : @param  $page   number of shown pages
 : @param  $entry  entry to highlight
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/logs')
  %rest:query-param('input', '{$input}')
  %rest:query-param('name',  '{$name}')
  %rest:query-param('sort',  '{$sort}', '')
  %rest:query-param('page',  '{$page}', '1')
  %rest:query-param('entry', '{$entry}')
  %output:method('html')
function dba:logs(
  $input  as xs:string?,
  $name   as xs:string?,
  $sort   as xs:string,
  $page   as xs:string,
  $entry  as xs:string?
) as element(html) {
  let $files := admin:logs()
  let $date := $name otherwise string(head($files))
  (: the entries come first: the files are a choice that is rarely made, as the latest log is
     what is looked at :)
  return (
    html:panel(
      if ($date) {
        <div class='sticky searchbar'>{
          <h2>Log: { $date }</h2>,
          <input type='text' id='input' name='input' value='{ $input }' autocomplete='off'
                 placeholder='Search, e.g. admin'
                 title='{ 'Words that the user, type or text of an entry must contain, ' ||
                   'or a regular expression' }'
                 autofocus='' data-filter='logEntries'/>,
          <label title='Reload the entries at the interval chosen in the settings'>{
            <input type='checkbox' id='live' data-live='logs' onchange='liveChanged()'/>, ' Live'
          }</label>,
          (: shown while the entries do not start with the first page; see showLogEntries :)
          <button type='button' id='top' hidden='' onclick='showTop();'
                  title='Show the first page of entries'>Top</button>,
          <span class='ignore'>{
            <input type='text' id='ignore' class='smallinput' autocomplete='off'
                   placeholder='Ignore, e.g. /dba'
                   title='Regular expression: entries whose text matches it are hidden'
                   data-filter='logEntries'/>
          }</span>
        }</div>,
        <div id='output'/>
      },
      (: the panel keeps its track even without a log file to show: it is the whole page
         beside the list :)
      { 'hidden': false(), 'label': $date }
    ),
    html:panel(
      <form method='post' id='dates' autocomplete='off'>
        <input type='hidden' id='date' value='{ $date }'/>
        <input type='hidden' id='sort' value='{ $sort }'/>
        <input type='hidden' id='page' value='{ $page }'/>
        <input type='hidden' id='entry' value='{ $entry }'/>
        <div id='list'>{ dba:files($date, '', $sort) }</div>
      </form>,
      { 'label': 'Logs' }
    )
  ) => html:wrap({
    'header': $dba:CAT, 'divided': true(), 'columns': ('1fr', '200px'),
    (: the panels fill the viewport and scroll on their own, so their heads can be pinned :)
    'rows': '1fr',
    'scripts': 'logs', 'init': 'initLogs();'[$date]
  })
};

(:~
 : Creates the list of log files.
 : @param  $date     name (date) of the opened log file
 : @param  $sort     table sort key; empty string for the newest files first
 : @param  $entries  sort key of the log entries, included in the links to the files
 : @return list
 :)
declare function dba:files(
  $date     as xs:string,
  $sort     as xs:string,
  $entries  as xs:string
) as element(div) {
  let $sort := $sort[.] otherwise 'name'
  (: the filter is a control of the list, and shares the row of its buttons :)
  let $buttons := (
    form:button('logs-download', 'Download', 'CHECK',
      title := 'Download the selected log files'),
    form:button('logs/delete', 'Delete', ('CHECK', 'CONFIRM'),
      title := 'Delete the selected log files'),
    <input type='text' id='log-filter' maxlength='10'
           data-filter='logFilter' class='smallinput' placeholder='Date, e.g. 2026-09'
           title='Show the log files whose date starts with the input, e.g. 2026-09'/>
  )
  let $headers := (
    { 'key': 'name', 'label': 'Name', 'order': 'desc' },
    { 'key': 'size', 'label': 'Size', 'type': 'bytes', 'order': 'desc' }
  )
  let $files :=
    for $file in admin:logs()
    return {
      (: the reference is a deep link naming the file; following it in place asks for
         its entries over the connection the view already opened :)
      'name': html:select($file, $dba:CAT, { 'sort': $entries, 'name': $file },
        $date = $file, 'name', 'selectLog'),
      'size': $file/@size
    }
  (: the head is pinned, so the actions stay in reach while the files scroll.
     The files of a server are few, and the filter looks through all of them :)
  return <div data-sort='{ $sort }'>{
    table:create($headers, $files, $buttons, options := {
      'sort': $sort, 'presort': 'name', 'sticky': <h2>Log Files</h2>, 'all': true(), 'noun': 'file',
      'empty': 'There are no log files yet.'
    })
  }</div>
};

(:~
 : Sends a log entry table or the list of log files to the client.
 : @param  $message  message
 :)
declare
  %ws:message('/dba/logs', '{$message}')
function dba:ws-message(
  $message  as xs:string
) as empty-sequence() {
  let $json := parse-json($message)
  return switch ($json?type) {
    case 'files' return utils:ws-panel('list',
      dba:files(string($json?date), string($json?sort), string($json?entries)))
    default return dba:ws-entries($json)
  }
};

(:~
 : Searches log entries and sends them to the client.
 : @param  $json  message
 :)
declare %private function dba:ws-entries(
  $json  as map(*)
) as empty-sequence() {
  let $run := xs:integer($json?run)
  let $filters := $json?filters otherwise {}
  (: an expression is incomplete while it is being typed: report it, do not raise it :)
  let $error := head(
    ($json?input[.], $filters?*) ! (try { void(analyze-string('', .)) } catch * { $err:description })
  )
  return if ($error) then (
    utils:ws-send({ 'type': 'error', 'run': $run, 'message': $error })
  ) else (
    (: searching a large log file takes time: stop a search that is superseded by this one :)
    utils:ws-stop(),
    let $id := job:eval(dba:entries#7, [
      $json?input,
      (: the checked files, or the one that is opened :)
      $json?dates?*,
      $json?sort[.] otherwise 'time',
      xs:integer($json?page),
      $json?entry,
      $json?ignore,
      $filters
    ], { 'cache': true(), 'id': utils:job-id('logs'), 'serializer': { 'method': 'html' } })
    return utils:ws-start($id, $run, false())
  )
};

(:~
 : Stops a running search if the connection is closed.
 :)
declare
  %ws:close('/dba/logs')
function dba:ws-close() as empty-sequence() {
  utils:ws-stop()
};

(:~
 : Reports an error to the client.
 : @param  $message  error message
 :)
declare
  %ws:error('/dba/logs', '{$message}')
function dba:ws-error(
  $message  as xs:string
) as empty-sequence() {
  utils:ws-error('Logs', $message)
};

(:~
 : Returns the searched columns of a log entry: an entry is shown if every term is found in one
 : of them, and what a term matched is marked in the column that holds it.
 : @param  $columns  searched columns of the entry
 : @param  $terms    search terms
 : @param  $joined   terms as one regular expression
 : @return columns; empty sequence if the entry is not matched
 :)
declare %private function dba:searched(
  $columns  as map(*),
  $terms    as xs:string*,
  $joined   as xs:string
) as map(*)? {
  (: a column that is marked is produced when it is shown, as most entries are never rendered :)
  if (every $term in $terms satisfies (
    some $value in $columns?* satisfies matches($value, $term, 'i')
  )) {
    {
      for key $key value $value in $columns
      return { $key: if (matches($value, $joined, 'i')) then (
        fn() {
          for $match in analyze-string($value, $joined, 'i')/*
          let $string := string($match)
          return if ($match/self::fn:match) then element b { $string } else $string
        }
      ) else (
        $value
      ) }
    }
  }
};

(:~
 : Returns a log entry table.
 : @param  $input    search input
 : @param  $dates    names of the log files to be searched
 : @param  $sort     table sort key
 : @param  $page     number of shown pages
 : @param  $entry    entry to highlight
 : @param  $ignore   regular expression of entries to hide
 : @param  $filters  column filters
 : @return html element
 :)
declare function dba:entries(
  $input    as xs:string?,
  $dates    as xs:string+,
  $sort     as xs:string,
  $page     as xs:integer,
  $entry    as xs:string?,
  $ignore   as xs:string?,
  $filters  as map(*)
) as element(div) {
  (: the files are searched newest first, so that the first page is the most recent data :)
  let $files := reverse(sort(distinct-values($dates), '?lang=en'))
  (: a single file is what the view was opened on: its date is stated by the panel, and every
     entry of it is collected. A range of files can hold millions of entries, of which the ones
     that are not shown are not worth reading :)
  let $several := count($files) > 1
  (: one entry more than the limit: it is what tells that the search was cut short :)
  let $max := $dba:MAX-ENTRIES + 1
  (: a single reference: the files are only read until the limit is reached :)
  let $shown := subsequence((
    let $regex-string := matches($input, '[+*?^$(){}|\[\]\\]')
    let $terms := if ($regex-string) then $input else tokenize($input)
    let $joined-terms := string-join($terms, '|')

    for $date in $files
    let $logs := admin:logs($date, true())
    for $n in reverse(1 to count($logs))
    let $log := $logs[$n]
    let $text := string($log)
    where not($ignore and matches($text, $ignore, 'i'))
    (: AND-combine column filters; an address is found by what is shown and by what is logged :)
    where every $key in map:keys($filters) satisfies (
      let $value := if ($key = 'text') then $text else string($log/@*[name() = $key])
      let $shown := if ($key = 'address') then html:localhost($value)
      return some $string in ($value, $shown) satisfies matches($string, $filters?$key, 'i')
    )

    for $map-results in (
      let $map := {
        'user': string($log/@user),
        'type': string($log/@type),
        'text': $text
      }
      return if ($input) then dba:searched($map, $terms, $joined-terms) else $map
    )

    let $time := string($log/@time)
    (: entries can share a time: their position in the file tells them apart :)
    let $key := `{ $time }-{ $n }`
    let $address := string($log/@address)
    (: two files hold the same times of day: what names an entry, and what is shown for it,
       is prefixed by the file it belongs to :)
    let $prefix := ($date || ' ')[$several]
    (: a failed request is told apart by its type: an error, an HTTP client or server error, or
       a WebSocket close code other than 1000, 1001 (closed in order) and 1005 (no code) :)
    let $type := $map-results?type
    let $failed := matches($log/@type, '^(ERROR|[45]\d\d|10(0[2-46-9]|1[0-5]))$')
    return {
      map:remove($map-results, 'type'),
      {
        'type': if ($failed) then fn() {
          <span class='log-failed'>{ if ($type instance of fn(*)) then $type() else $type }</span>
        } else $type,
        'id': ($date || 'T')[$several] || $key,
        'address': fn() { html:address($address) },
        'ms': xs:decimal($log/@ms),
        'time': fn() {
          let $link := <a href='{
            web:create-url($dba:CAT, { 'name': $date, 'entry': $key }, $key)
          }'>{ $prefix || $time }</a>
          return if (not($input) and $key = $entry) then element b { $link } else $link
        }
      }
    }
  ), 1, $max[$several])
  let $params := { 'name': head($files), 'input': $input }
  (: filter fields, displayed below the table header :)
  let $filter-row := element tr {
    for $column in $dba:COLUMNS
    let $name := 'f-' || $column?key
    return element td {
      attribute class { 'num' }[$column?type = $table:NUMBER],
      <input type='text' class='filter' name='{ $name }' value='{ $filters?($column?key) }'
             placeholder='{ $column?label }' autocomplete='off'
             title='Regular expression of the { lower-case($column?label) } values to show'
             data-filter='logEntries'/>
    }
  }
  let $options := {
    'sort': $sort, 'presort': 'time', 'page': $page, 'focus': $entry[not($input)],
    'filters': $filter-row,
    (: the number of entries stays in view below the search line :)
    'pinned': true(),
    'empty': if ($input or $ignore or map:size($filters) > 0) then 'No entries match the search.'
      else 'The log is empty.'
  }
  (: the number of shown pages is stated for the client; see shownPages :)
  return <div data-page='{ $page }'>{
    (: a search that stopped at the limit reports as much: what it did not reach is not
       missing from the log, it was not looked at :)
    <div class='note warn'>{
      `The search was stopped after { $max - 1 } entries. Choose fewer files, or narrow the search.`
    }</div>[$several and count($shown) = $max],
    (: the time of an entry from one of several files is preceded by its date :)
    let $columns := if ($several) then (
      map:put(head($dba:COLUMNS), 'width', 'calc(23ch + 0.5rem)'), tail($dba:COLUMNS)
    ) else $dba:COLUMNS
    return table:create($columns, $shown, (), $params, $options)
  }</div>
};

(:~
 : Deletes log files.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/logs/delete')
function dba:delete() {
  utils:dispatch($dba:CAT, fn($args) { {
    'info': utils:info($args?name, 'log', 'deleted'),
    'run' : %updating fn() { $args?name ! admin:delete-logs(.) }
  } })
};
