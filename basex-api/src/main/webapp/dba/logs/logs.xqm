(:~
 : Logging page.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/logs';

import module namespace html = 'dba/html' at '../modules/html.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'logs';

(:~
 : Logging page.
 : @param  $input  search input
 : @param  $name   name (date) of log file
 : @param  $sort   table sort key
 : @param  $error  error string
 : @param  $info   info string
 : @param  $page   current page
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
  %output:method("html")
function dba:logs(
  $input  as xs:string?,
  $name   as xs:string?,
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?,
  $page   as xs:string
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
          <div id='list'>{
            let $headers := (
              map { 'key': 'name', 'label': 'Name' },
              map { 'key': 'size', 'label': 'Size', 'type': 'bytes' }
            )
            let $entries := reverse(sort($files)) ! map {
              'name': .,
              'size': @size
            }
            let $buttons := html:button('log-delete', 'Delete', true())
            let $params := map { 'sort': $sort }
            let $options := map { 'link': function($value) { $dba:CAT } }
            return html:table($headers, $entries, $buttons, $params, $options) update {
              (: enrich link targets with current search string :)
              .//a ! (insert node attribute onclick { 'addInput(this);' } into .)
            }
          }</div>
        </form>
      </td>
      <td class='vertical'/>
      <td>{
        if($name) then (
          <form action='log-download' method='post' id='resources' autocomplete='off'>
            <h3>{
              $name, ':&#xa0;',
              <input type='hidden' name='name' value='{ $name }'/>,
              <input size='40' id='input' name='input' value='{ $input }'
                title='Enter regular expression'
                onkeydown='if(event.keyCode == 13) {{ logEntries(true); event.preventDefault(); }}'
                onkeyup='logEntries(false);'/>,
              ' ',
              html:button('download', 'Download')
            }</h3>
          </form>,
          <div id='output'/>,
          html:js('logEntries(true);')
        ) else (),
        html:focus('input')
      }</td>
    </tr>
  )
};

(:~
 : Returns entries of a specific log file.
 : @param  $input  search input
 : @param  $name   name of selected log files
 : @param  $sort   table sort key
 : @param  $page   current page
 : @return html elements
 :)
declare
  %rest:POST("{$input}")
  %rest:path("/dba/log")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("sort",    "{$sort}", "")
  %rest:query-param("page",    "{$page}", "1")
  %output:method("html")
  %output:indent("no")
  %rest:single
function dba:log(
  $input  as xs:string?,
  $name   as xs:string,
  $sort   as xs:string,
  $page   as xs:string
) as element()+ {
  let $headers := (
    map { 'key': 'time', 'label': 'Time', 'type': 'time', 'order': 'desc' },
    map { 'key': 'address', 'label': 'Address' },
    map { 'key': 'user', 'label': 'User', 'type': 'xml' },
    map { 'key': 'type', 'label': 'Type' },
    map { 'key': 'ms', 'label': 'ms', 'type': 'decimal', 'order': 'desc' },
    map { 'key': 'message', 'label': 'Message', 'type': 'xml' }
  )
  let $entries :=
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
    for $log in admin:logs($name, true())
    let $user := data($log/@user)
    let $message := data($log/text())
    let $user-found := $input-exists and contains($user, $input)
    let $message-found := $input-exists and matches($message, $input, 'i')
    where not($input-exists) or $user-found or $message-found
    return map {
      'time': $log/@time,
      'address': $log/@address,
      'user': function() { $highlight($user, $user-found) },
      'type': $log/@type,
      'ms': $log/@ms,
      'message': function() { $highlight($message, $message-found) }
    }
  let $params := map { 'name': $name, 'input': $input }
  let $options := map { 'sort': head(($sort[.], 'time')), 'page': xs:integer($page[.]) }
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
