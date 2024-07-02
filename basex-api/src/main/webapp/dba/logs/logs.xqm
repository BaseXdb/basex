(:~
 : Logging page.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/logs';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace html = 'dba/html' at '../lib/html.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'logs';

(:~
 : Redirects to the URL that creates a log entry table for the specified timestamp.
 : @param  $date  name of log file
 : @param  $time  timestamp to be found
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path('/dba/logs-jump')
  %rest:query-param('date', '{$date}')
  %rest:query-param('time', '{$time}')
function dba:logs-jump(
  $date  as xs:string,
  $time  as xs:string
) as element(rest:response) {
  let $page := head((
    let $ignore-logs := config:get($config:IGNORE-LOGS)
    let $max := config:get($config:MAXROWS)
    for $log at $pos in reverse(
      admin:logs($date, true())[not($ignore-logs and matches(., $ignore-logs, 'i'))]
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
 : Logging page.
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
  %output:html-version('5')
function dba:logs(
  $input  as xs:string?,
  $name   as xs:string?,
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?,
  $page   as xs:string,
  $time   as xs:string?
) as element(html) {
  let $files := reverse(sort(admin:logs()))
  let $date := $name otherwise string(head($files))
  return html:wrap({ 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='190'>
        <h2>{
          'Logs', '&#xa0;',
          <input type='text' id='log-filter' name='log-filter' max-length='10'
                 onkeyup='logFilter();' class='smallinput'/>
        }</h2>

        <form method='post' id='dates'>
          <input type='hidden' name='date' id='date' value='{ $date }'/>
          <input type='hidden' name='sort' id='sort' value='{ $sort }'/>
          <input type='hidden' name='page' id='page' value='{ $page }'/>
          <input type='hidden' name='time' id='time' value='{ $time }'/>
          <div id='list'>{
            let $buttons := (
              html:button('logs-download', 'Download', 'CHECK'),
              html:button('logs-delete', 'Delete', ('CHECK', 'CONFIRM'))
            )
            let $headers := (
              { 'key': 'name', 'label': 'Name', 'type': 'dynamic' },
              { 'key': 'size', 'label': 'Size', 'type': 'bytes' }
            )
            let $entries :=
              for $entry in $files
              return {
                'name': fn() {
                  let $link := html:link(
                    $entry, $dba:CAT, ({ 'sort': $sort }, { 'name': $entry })
                  ) update {
                    (: enrich link targets with current search string :)
                    insert node attribute onclick { 'addInput(this);' } into .
                  }
                  return if($date = $entry) then element b { $link } else $link
                },
                'size': $entry/@size
              }
            return html:table($headers, $entries, $buttons)
          }</div>
        </form>
      </td>
      <td class='vertical'/>
      <td>{
        if($date) then (
          <h3>{
            $date, '&#xa0;',
            <input type='hidden' name='name' value='{ $date }'/>,
            <input type='text' id='input' name='input' value='{ $input }' autocomplete='off'
                   title='Enter regular expression' autofocus='autofocus'
                   onkeyup='logEntries(event.key);'/>
          }</h3>,
          <div id='output'/>,
          html:js('logEntries();')
        )
      }</td>
    </tr>
  )
};

(:~
 : Returns a log entry table.
 : @param  $input  search input
 : @param  $date   name of selected log files
 : @param  $sort   table sort key
 : @param  $page   current page
 : @param  $time   timestamp to highlight
 : @return html elements
 :)
declare
  %rest:POST('{$input}')
  %rest:path('/dba/log')
  %rest:query-param('date', '{$date}')
  %rest:query-param('sort', '{$sort}', 'time')
  %rest:query-param('page', '{$page}', '1')
  %rest:query-param('time', '{$time}')
  %output:method('html')
  %output:html-version('5')
  %output:indent('no')
  %rest:single
function dba:log(
  $input  as xs:string?,
  $date   as xs:string,
  $sort   as xs:string,
  $page   as xs:string,
  $time   as xs:string?
) as element()+ {
  (: check if input is a valid regular expression :)
  $input[.] ! void(analyze-string('', .)),

  let $headers := (
    { 'key': 'time', 'label': 'Time', 'type': 'dynamic', 'order': 'desc' },
    { 'key': 'address', 'label': 'Address' },
    { 'key': 'user', 'label': 'User', 'type': 'dynamic' },
    { 'key': 'type', 'label': 'Type', 'type': 'dynamic' },
    { 'key': 'ms', 'label': 'ms', 'type': 'decimal', 'order': 'desc' },
    { 'key': 'text', 'label': 'Text', 'type': 'dynamic' }
  )
  let $entries := (
    let $ignore-logs := config:get($config:IGNORE-LOGS)
    let $regex-string := matches($input, '[+*?^$(){}|\[\]\\]')
    let $terms := if($regex-string) then $input else tokenize($input)
    let $joined-terms := if($regex-string) then $input else string-join($terms, '|')

    for $log in reverse(admin:logs($date, true()))
    let $text := string($log)
    where not($ignore-logs and matches($text, $ignore-logs, 'i'))

    for $map-results in (
      let $map := {
        'user': string($log/@user),
        'type': string($log/@type),
        'text': $text
      }
      return if($input) then (
        if(every $term in $terms satisfies (
          some $v in $map?* satisfies matches($v, $term, 'i')
        )) then (
          map:merge(
            map:for-each($map, fn($k, $v) {
              map:entry($k, (
                if(matches($v, $joined-terms, 'i')) then fn() {
                  for $match in analyze-string($v, $joined-terms, 'i')/*
                  let $value := string($match)
                  return if($match/self::fn:match) then element b { $value } else $value
                } else (
                  $v
                )
              ))
            })
          )
        )
      ) else (
        $map
      )
    )

    let $id := string($log/@time)
    return map:merge((
      $map-results,
      {
        'id': $id,
        'address': string($log/@address),
        'ms': xs:decimal($log/@ms),
        'time': fn() {
          let $link := html:link($id, $dba:CAT || '-jump', { 'date': $date, 'time': $id })
          return if(not($input) and $id = $time) then element b { $link } else $link
        }
      }
    ))
  )
  let $params := { 'name': $date, 'input': $input }
  let $options := { 'sort': $sort, 'presort': 'time', 'page': xs:integer($page) }
  return html:table($headers, $entries, (), $params, $options)
};
