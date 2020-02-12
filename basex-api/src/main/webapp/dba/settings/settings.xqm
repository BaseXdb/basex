(:~
 : Settings page.
 :
 : @author Christian Grün, BaseX Team 2005-20, BSD License
 :)
module namespace dba = 'dba/settings';

import module namespace options = 'dba/options' at '../lib/options.xqm';
import module namespace html = 'dba/html' at '../lib/html.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'settings';

(:~
 : Settings page.
 : @param  $error  error string
 : @param  $info   info string
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/settings')
  %rest:query-param('error', '{$error}')
  %rest:query-param('info',  '{$info}')
  %output:method('html')
function dba:settings(
  $error  as xs:string?,
  $info   as xs:string?
) as element(html) {
  let $system := html:properties(db:system())
  let $table-row := function($items) {
    <tr><td>{ $items }</td></tr>
  }
  let $number := function($key, $label) {
    $table-row((
      $label,
      <br/>,
      <input name='{ $key }' type='number' value='{ options:get($key) }'/>
    ))
  }
  let $string := function($key, $label) {
    $table-row((
      $label,
      <br/>,
      <input name='{ $key }' type='text' value='{ options:get($key) }'/>
    ))
  }
  return html:wrap(map { 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='33%'>
        <form action='settings' method='post'>
          <h2>Settings » { html:button('save', 'Save') }</h2>
          <h3>Queries</h3>
          <table>
            {
              $number($options:TIMEOUT, 'Timeout, in seconds (0 = disabled)'),
              $number($options:MEMORY, 'Memory limit, in MB (0 = disabled)'),
              $number($options:MAXCHARS, 'Maximum output size')
            }
            <tr>
              <td colspan='2'>Permission:</td>
            </tr>
            <tr>
              <td>
                <select name='permission'>{
                  let $pm := options:get($options:PERMISSION)
                  for $p in $options:PERMISSIONS
                  return element option { attribute selected { }[$p = $pm], $p }
                }</select>
              </td>
            </tr>
          </table>
          <h3>Tables</h3>
          <table>{
            $number($options:MAXROWS,  'Displayed table rows')
          }</table>
          <h3>Logs</h3>
          <table>{
            $string($options:IGNORE-LOGS, <span>Ignore entries (e.g. <code>/dba</code>):</span>)
          }</table>
        </form>
      </td>
      <td class='vertical'/>
      <td width='33%'>
        <form action='settings-gc' method='post'>
          <h2>Global Options » { html:button('gc', 'GC') }</h2>
          <table>{
            $system/tr[th][3]/preceding-sibling::tr[not(th)]
          }</table>
        </form>
      </td>
      <td class='vertical'/>
      <td width='33%'>
        <h2>Local Options</h2>
        <table>{
          $system/tr[th][3]/following-sibling::tr
        }</table>
      </td>
    </tr>
  )
};

(:~
 : Saves the settings.
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/settings')
function dba:settings-save(
) as element(rest:response) {
  options:save(html:parameters()),
  web:redirect($dba:CAT, map { 'info': 'Settings were saved.' })
};
