(:~
 : Logging page.
 :
 : @author Christian Grün, BaseX Team 2005-19, BSD License
 :)
module namespace dba = 'dba/logs';

import module namespace options = 'dba/options' at '../modules/options.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';

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
  %rest:path("/dba/logs-jump")
  %rest:query-param("name", "{$name}")
  %rest:query-param("time", "{$time}")
function dba:jump-page(
  $name  as xs:string,
  $time  as xs:string
) as element(rest:response) {
  let $page := head((
    let $max := options:get($options:MAXROWS)
    for $log at $pos in reverse(admin:logs($name, true()))
    where $log/@time = $time
    return ($pos - 1) idiv $max + 1,
    1
  ))
  return web:redirect('/dba/logs', map { 'name': $name, 'page': $page, 'time': $time })
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
  %rest:path("/dba/logs")
  %rest:query-param("input", "{$input}")
  %rest:query-param("name",  "{$name}")
  %rest:query-param("sort",  "{$sort}", "")
  %rest:query-param("error", "{$error}")
  %rest:query-param("info",  "{$info}")
  %rest:query-param("page",  "{$page}", "1")
  %rest:query-param("time",  "{$time}")
  %output:method("html")
function dba:logs(
  $input  as xs:string?,
  $name   as xs:string?,
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?,
  $page   as xs:string,
  $time   as xs:string?
) as element(html) {
  let $files := (
    for $file in admin:logs()
    order by $file descending
    return $file
  )
  let $name := if($name) then $name else string(head($files))
  return html:wrap(map { 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='205'>
        <h2>{ html:link('Logs', $dba:CAT) }</h2>
        <form action="{ $dba:CAT }" method="post" class="update">
          <input type='hidden' name='name' id='name' value='{ $name }'/>
          <input type='hidden' name='sort' id='sort' value='{ $sort }'/>
          <input type='hidden' name='page' id='page' value='{ $page }'/>
          <input type='hidden' name='time' id='time' value='{ $time }'/>
          <div id='list'>{
            let $buttons := html:button('log-delete', 'Delete', true())
            let $params := map { 'sort': $sort }
            let $headers := (
              map { 'key': 'name', 'label': 'Name', 'type': 'xml' },
              map { 'key': 'size', 'label': 'Size', 'type': 'bytes' }
            )
            let $entries := 
              for $entry in reverse(sort($files))
              return map {
                'name': function() {
                  let $link := html:link(
                    $entry, $dba:CAT, ($params, map { 'name': $entry })
                  ) update {
                    (: enrich link targets with current search string :)
                    insert node attribute onclick { 'addInput(this);' } into .
                  }
                  return if ($name = $entry) then element b { $link } else $link
                },
                'size': $entry/@size
              }
            return html:table($headers, $entries, $buttons, $params, map { })
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
              <input size='40' id='input' name='input' value='{ $input }'
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
  %rest:POST("{$input}")
  %rest:path("/dba/log")
  %rest:query-param("name", "{$name}")
  %rest:query-param("sort", "{$sort}", "")
  %rest:query-param("page", "{$page}", "1")
  %rest:query-param("time", "{$time}")
  %output:method("html")
  %output:indent("no")
  %rest:single
function dba:log(
  $input  as xs:string?,
  $name   as xs:string,
  $sort   as xs:string,
  $page   as xs:string,
  $time   as xs:string?
) as element()+ {
  let $sort := if($sort = 'time') then '' else $sort
  let $headers := (
    map { 'key': 'time', 'label': 'Time', 'type': 'xml', 'order': 'desc' },
    map { 'key': 'address', 'label': 'Address' },
    map { 'key': 'user', 'label': 'User', 'type': 'xml' },
    map { 'key': 'type', 'label': 'Type' },
    map { 'key': 'ms', 'label': 'ms', 'type': 'decimal', 'order': 'desc' },
    map { 'key': 'message', 'label': 'Message', 'type': 'xml' }
  )
  let $entries :=
    let $ignore-logs := options:get($options:IGNORE-LOGS)
    let $input-exists := boolean($input)
    let $highlight := function($string, $found) {
      if($found) then (
        for $match in analyze-string($string, $input, 'i')/*
        let $text := string($match)
        return if(local-name($match) = 'match') then element b { $text } else $text
      ) else (
        $string
      )
    }
    for $log in reverse(admin:logs($name, true()))
    let $user := data($log/@user)
    let $message := data($log/text())
    let $user-found := $input-exists and contains($user, $input)
    let $message-found := $input-exists and not($user-found) and matches($message, $input, 'i')
    where (not($input-exists) or $user-found or $message-found) and (
      not($ignore-logs and matches($message, $ignore-logs, 'i'))
    )
    return map {
      'time': function() {
        let $value := string($log/@time)
        return if($input or $sort) then (
          html:link($value, $dba:CAT || '-jump', map { 'name': $name, 'time': $value })
        ) else if($value = $time) then (
          <b>{ $value }</b>
        ) else (
          $value
        )
      },
      'address': $log/@address,
      'user': function() { $highlight($user, $user-found) },
      'type': $log/@type,
      'ms': $log/@ms,
      'message': function() { $highlight($message, $message-found) }
    }
  let $params := map { 'name': $name, 'input': $input }
  let $options := map { 'sort': $sort, 'page': xs:integer($page) }
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
  %rest:path("/dba/logs")
  %rest:query-param("action", "{$action}")
  %rest:query-param("name",   "{$names}")
function dba:logs-redirect(
  $action  as xs:string,
  $names   as xs:string*
) as element(rest:response) {
  web:redirect($action, map { 'name': $names[.], 'redirect': $dba:CAT })
};
