(:~
 : Logging page.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/logs';

import module namespace html = 'dba/html' at '../lib/html.xqm';
import module namespace options = 'dba/options' at '../lib/options.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'logs';

(:~
 : Redirects to the URL that creates a log entry table for the specified timestamp.
 : @param  $name  name (date) of log file
 : @param  $time  timestamp to be found
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path('/dba/logs-jump')
  %rest:query-param('name', '{$name}')
  %rest:query-param('time', '{$time}')
function dba:logs-jump(
  $name  as xs:string,
  $time  as xs:string
) as element(rest:response) {
  let $page := head((
    let $ignore-logs := options:get($options:IGNORE-LOGS)
    let $max := options:get($options:MAXROWS)
    for $log at $pos in reverse(
      admin:logs($name, true())[not($ignore-logs and matches(., $ignore-logs, 'i'))]
    )
    where $log/@time = $time
    return ($pos - 1) idiv $max + 1,
    1
  ))
  return web:redirect('/dba/logs', map { 'name': $name, 'page': $page, 'time': $time }) update {
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
  let $name := if($name) then $name else string(head($files))
  return html:wrap(map { 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='205'>
        <h2>{ html:link('Logs', $dba:CAT) }</h2>
        <form action='{ $dba:CAT }' method='post' class='update'>
          <input type='hidden' name='name' id='name' value='{ $name }'/>
          <input type='hidden' name='sort' id='sort' value='{ $sort }'/>
          <input type='hidden' name='page' id='page' value='{ $page }'/>
          <input type='hidden' name='time' id='time' value='{ $time }'/>
          <div id='list'>{
            let $buttons := html:button('log-delete', 'Delete', true())
            let $headers := (
              map { 'key': 'name', 'label': 'Name', 'type': 'dynamic' },
              map { 'key': 'size', 'label': 'Size', 'type': 'bytes' }
            )
            let $entries := 
              for $entry in $files
              return map {
                'name': function() {
                  let $link := html:link(
                    $entry, $dba:CAT, (map { 'sort': $sort }, map { 'name': $entry })
                  ) update {
                    (: enrich link targets with current search string :)
                    insert node attribute onclick { 'addInput(this);' } into .
                  }
                  return if ($name = $entry) then element b { $link } else $link
                },
                'size': $entry/@size
              }
            return html:table($headers, $entries, $buttons, map { }, map { })
          }</div>
        </form>
      </td>
      <td class='vertical'/>
      <td>{
        if($name) then (
          <form action='log-download' method='post' id='resources' autocomplete='off'>
            <h3>{
              $name, '&#xa0;',
              <input type='hidden' name='name' value='{ $name }'/>,
              <input type='text' id='input' name='input' value='{ $input }'
                title='Enter regular expression'
                onkeydown='if(event.keyCode==13) {{logEntries(true,false);event.preventDefault();}}'
                onkeyup='logEntries(false, true);'/>,
              ' ',
              html:button('download', 'Download')
            }</h3>
          </form>,
          <div id='output'/>,
          html:js('logEntries(true, false);')
        ) else (),
        html:focus('input')
      }</td>
    </tr>
  )
};

(:~
 : Returns a log entry table.
 : @param  $input  search input
 : @param  $name   name of selected log files
 : @param  $sort   table sort key
 : @param  $page   current page
 : @param  $time   timestamp to highlight
 : @return html elements
 :)
declare
  %rest:POST('{$input}')
  %rest:path('/dba/log')
  %rest:query-param('name', '{$name}')
  %rest:query-param('sort', '{$sort}', 'time')
  %rest:query-param('page', '{$page}', '1')
  %rest:query-param('time', '{$time}')
  %output:method('html')
  %output:html-version('5.0')
  %output:indent('no')
  %rest:single
function dba:log(
  $input  as xs:string?,
  $name   as xs:string,
  $sort   as xs:string,
  $page   as xs:string,
  $time   as xs:string?
) as element()+ {
  (: check if input is a valid regular expression :)
  if ($input) then prof:void(analyze-string('', $input)),

  let $headers := (
    map { 'key': 'time', 'label': 'Time', 'type': 'dynamic', 'order': 'desc' },
    map { 'key': 'address', 'label': 'Address' },
    map { 'key': 'user', 'label': 'User', 'type': 'dynamic' },
    map { 'key': 'type', 'label': 'Type', 'type': 'dynamic' },
    map { 'key': 'ms', 'label': 'ms', 'type': 'decimal', 'order': 'desc' },
    map { 'key': 'text', 'label': 'Text', 'type': 'dynamic' }
  )
  let $entries := (
    let $ignore-logs := options:get($options:IGNORE-LOGS)
    let $regex := matches($input, '[+*?^$(){}|\[\]\\]')
    let $terms := $regex ?? $input !! tokenize($input)
    let $joined-terms := $regex ?? $input !! string-join($terms, '|')

    for $log in reverse(admin:logs($name, true()))
    let $text := string($log)
    where not($ignore-logs and matches($text, $ignore-logs, 'i'))

    for $map-results in (
      let $map := map {
        'user': string($log/@user),
        'type': string($log/@type),
        'text': $text
      }
      return if($input) then (
        if(every $term in $terms satisfies (
          some $v in $map?* satisfies matches($v, $term, 'i')
        )) then (
          map:merge(
            map:for-each($map, function($k, $v) {
              map:entry($k, (
                if (matches($v, $joined-terms)) then function() {
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
      map {
        'id': $id,
        'address': string($log/@address),
        'ms': xs:decimal($log/@ms),
        'time': if($input or $sort != 'time') then (
          function() {
            html:link($id, $dba:CAT || '-jump', map { 'name': $name, 'time': $id })
          }
        ) else if($id = $time) then (
          element b { $id }
        ) else (
          $id
        )
      }
    ))
  )
  let $params := map { 'name': $name, 'input': $input }
  let $options := map { 'sort': $sort, 'presort': 'time', 'page': xs:integer($page) }
  return html:table($headers, $entries, (), $params, $options)
};

(:~
 : Redirects to the specified action.
 : @param  $action  action to perform
 : @param  $names   names of selected log files
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/logs')
  %rest:query-param('action', '{$action}')
  %rest:query-param('name',   '{$names}')
function dba:logs-redirect(
  $action  as xs:string,
  $names   as xs:string*
) as element(rest:response) {
  web:redirect($action, map { 'name': $names[.], 'redirect': $dba:CAT })
};
