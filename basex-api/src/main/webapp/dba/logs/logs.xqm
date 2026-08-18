(:~
 : Logs.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/logs';

import module namespace config = 'dba/lib/config' at '../lib/config.xqm';
import module namespace form = 'dba/lib/form' at '../lib/form.xqm';
import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace table = 'dba/lib/table' at '../lib/table.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'logs';

(:~ Table columns :)
declare variable $dba:COLUMNS := (
  { 'key': 'time', 'label': 'Time', 'type': 'dynamic', 'order': 'desc', 'width': '10%' },
  { 'key': 'address', 'label': 'Address', 'width': '18%' },
  { 'key': 'user', 'label': 'User', 'type': 'dynamic', 'width': '10%' },
  { 'key': 'type', 'label': 'Type', 'type': 'dynamic', 'width': '10%' },
  { 'key': 'ms', 'label': 'ms', 'type': 'decimal', 'order': 'desc', 'width': '7%' },
  { 'key': 'text', 'label': 'Text', 'type': 'dynamic', 'width': '45%' }
);

(:~
 : Logs.
 : @param  $input  search input
 : @param  $name   name (date) of log file
 : @param  $sort   table sort key
 : @param  $error  error string
 : @param  $info   info string
 : @param  $page   current page
 : @param  $time   timestamp to highlight
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/logs')
  %rest:query-param('input', '{$input}')
  %rest:query-param('name',  '{$name}')
  %rest:query-param('sort',  '{$sort}', '')
  %rest:query-param('error', '{$error}')
  %rest:query-param('info',  '{$info}')
  %rest:query-param('page',  '{$page}', '1')
  %rest:query-param('time',  '{$time}')
  %output:method('html')
function dba:logs(
  $input  as xs:string?,
  $name   as xs:string?,
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?,
  $page   as xs:string,
  $time   as xs:string?
) as element(html) {
  let $files := admin:logs()
  let $date := $name otherwise string(head($files))
  return (
    <div class='panel'>
      <div class='pane'>
      <form method='post' id='dates' autocomplete='off'>
        <input type='hidden' name='date' id='date' value='{ $date }'/>
        <input type='hidden' name='sort' id='sort' value='{ $sort }'/>
        <input type='hidden' name='page' id='page' value='{ $page }'/>
        <input type='hidden' name='time' id='time' value='{ $time }'/>
        <div id='list'>{
          (: the filter is a control of the list, and shares the row of its buttons :)
          let $buttons := (
            form:button('logs-download', 'Download', 'CHECK'),
            form:button('logs/delete', 'Delete', ('CHECK', 'CONFIRM')),
            <input type='text' id='log-filter' name='log-filter' maxlength='10'
                   onkeyup='logFilter();' class='smallinput'/>
          )
          let $headers := (
            { 'key': 'name', 'label': 'Name', 'type': 'dynamic' },
            { 'key': 'size', 'label': 'Size', 'type': 'bytes' }
          )
          let $entries :=
            for $entry in $files
            return {
              'name': fn() {
                (: the reference is a deep link naming the file; following it in place asks
                   for its entries over the connection the view already opened :)
                html:link($entry, $dba:CAT, ({ 'sort': $sort }, { 'name': $entry })) update {
                  insert node (
                    attribute class { 'selected' }[$date = $entry],
                    attribute data-select { $entry },
                    attribute onclick { 'selectLog(this.dataset.select); return false;' }
                  ) into .
                }
              },
              'size': $entry/@size
            }
          (: the head is pinned, so the actions stay in reach while the files scroll.
             The files of a server are few, and the filter looks through all of them :)
          return table:create($headers, $entries, $buttons, {},
            { 'sticky': <h2>Logs</h2>, 'all': true() })
        }</div>
      </form>
      </div>
    </div>,
    <div class='panel stack-first'>{
      if ($date) {
        <div class='pane'>{
        <div class='sticky logbar'>{
          <h3>{ $date }</h3>,
          <input type='hidden' name='name' value='{ $date }'/>,
          <input type='text' id='input' name='input' value='{ $input }' autocomplete='off'
                 title='Enter regular expression' autofocus='' onkeyup='logEntries(event.key);'/>,
          <label title='Reload the entries every second'>{
            <input type='checkbox' id='live' data-live='logs' onchange='liveChanged()'/>, ' Live'
          }</label>,
          <span class='ignore'>{
            <input type='text' id='ignore' class='smallinput' autocomplete='off'
                   placeholder='Ignore, e.g. /dba' title='Regular expression of entries to hide'
                   onkeyup='ignoreLogs(event.key);'/>
          }</span>
        }</div>,
        <div id='output'/>
        }</div>
      }
    }</div>
  ) => html:wrap({
    'header': $dba:CAT, 'info': $info, 'error': $error, 'columns': ('200px', '1fr'),
    (: the panels fill the viewport and scroll on their own, so their heads can be pinned :)
    'rows': '1fr',
    'scripts': 'logs', 'init': 'initLogs();'[$date]
  })
};

(:~
 : Sends a log entry table to the client.
 : @param  $message  message
 :)
declare
  %ws:message('/dba/logs', '{$message}')
function dba:ws-message(
  $message  as xs:string
) as empty-sequence() {
  let $json := parse-json($message)
  let $run := xs:integer($json?run)
  let $filters := $json?filters otherwise {}
  (: an expression is incomplete while it is being typed: report it, do not raise it :)
  let $error := head(
    ($json?input[.], $filters?*) ! (try { void(analyze-string('', .)) } catch * { $err:description })
  )
  return if ($error) {
    utils:ws-send({ 'type': 'error', 'run': $run, 'message': $error })
  } else {
    (: searching a large log file takes time: stop a search that is superseded by this one :)
    utils:ws-stop(),
    let $id := job:eval(dba:entries#7, [
      $json?input,
      $json?date,
      $json?sort[.] otherwise 'time',
      xs:integer($json?page),
      $json?time,
      $json?ignore,
      $filters
    ], { 'cache': true(), 'id': utils:job-id('logs') })
    return utils:ws-start($id, $run, { 'method': 'html' })
  }
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
 : Returns a log entry table.
 : @param  $input    search input
 : @param  $date     name of selected log files
 : @param  $sort     table sort key
 : @param  $page     current page
 : @param  $time     timestamp to highlight
 : @param  $ignore   regular expression of entries to hide
 : @param  $filters  column filters
 : @return html elements
 :)
declare function dba:entries(
  $input    as xs:string?,
  $date     as xs:string,
  $sort     as xs:string,
  $page     as xs:integer,
  $time     as xs:string?,
  $ignore   as xs:string?,
  $filters  as map(*)
) as element()+ {
  let $entries := (
    let $regex-string := matches($input, '[+*?^$(){}|\[\]\\]')
    let $terms := if ($regex-string) then $input else tokenize($input)
    let $joined-terms := if ($regex-string) then $input else string-join($terms, '|')

    for $log in reverse(admin:logs($date, true()))
    let $text := string($log)
    where not($ignore and matches($text, $ignore, 'i'))
    (: AND-combine column filters :)
    where every $key in map:keys($filters) satisfies matches(
      if ($key = 'text') then $text else string($log/@*[name() = $key]), $filters($key), 'i'
    )

    for $map-results in (
      let $map := {
        'user': string($log/@user),
        'type': string($log/@type),
        'text': $text
      }
      return if ($input) {
        if (every $term in $terms satisfies (
          some $v in $map?* satisfies matches($v, $term, 'i')
        )) {
          map:merge(
            map:for-each($map, fn($k, $v) {
              map:entry($k, (
                if (matches($v, $joined-terms, 'i')) {
                  fn() {
                    for $match in analyze-string($v, $joined-terms, 'i')/*
                    let $value := string($match)
                    return if ($match/self::fn:match) then element b { $value } else $value
                  }
                } else {
                  $v
                }
              ))
            })
          )
        }
      } else {
        $map
      }
    )

    let $id := string($log/@time)
    return map:merge((
      $map-results,
      {
        'id': $id,
        'address': string($log/@address),
        'ms': xs:decimal($log/@ms),
        'time': fn() {
          let $link := html:link($id, $dba:CAT || '-jump',
            ({ 'date': $date, 'time': $id }, { 'ignore': $ignore }[$ignore]))
          return if (not($input) and $id = $time) then element b { $link } else $link
        }
      }
    ))
  )
  let $params := map:merge((
    { 'name': $date, 'input': $input },
    map:for-each($filters, fn($key, $value) { map:entry('f-' || $key, $value) })
  ))
  (: filter fields, displayed below the table header :)
  let $filter-row := element tr {
    for $column in $dba:COLUMNS
    let $name := 'f-' || $column?key
    return element td {
      attribute class { 'num' }[$column?type = $table:NUMBER],
      <input type='text' class='filter' name='{ $name }' value='{ $filters($column?key) }'
             placeholder='{ $column?label }' autocomplete='off'
             title='Filter: { $column?label }' onkeyup='logEntries(event.key);'/>
    }
  }
  let $options := {
    'sort': $sort, 'presort': 'time', 'page': $page, 'filters': $filter-row
  }
  return table:create($dba:COLUMNS, $entries, (), $params, $options)
};

(:~
 : Redirects to the URL that returns logs for the specified timestamp.
 : @param  $date  date
 : @param  $time  time
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path('/dba/logs-jump')
  %rest:query-param('date',   '{$date}')
  %rest:query-param('time',   '{$time}')
  %rest:query-param('ignore', '{$ignore}')
function dba:logs-jump(
  $date    as xs:string,
  $time    as xs:string,
  $ignore  as xs:string?
) as element(rest:response) {
  let $page := head((
    let $max := config:get($config:MAXROWS)
    for $log at $pos in reverse(
      admin:logs($date, true())[not($ignore and matches(., $ignore, 'i'))]
    )
    where $log/@time = $time
    return ($pos - 1) idiv $max + 1,
    1
  ))
  return web:redirect('/dba/logs', { 'name': $date, 'page': $page, 'time': $time }) update {
    .//*:header/@value ! (replace value of node . with . || '#' || $time)
  }
};

(:~
 : Runs a log action.
 : @param  $action  name of action
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/logs/{$action}')
function dba:action(
  $action  as xs:string
) {
  utils:dispatch($dba:CAT, $action, {
    'delete': fn($args) { {
      'info': utils:info($args?name, 'log', 'deleted'),
      'run' : %updating fn() { $args?name ! admin:delete-logs(.) }
    } }
  })
};
