(:~
 : Settings.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/settings';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace html = 'dba/html' at '../lib/html.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'settings';

(:~
 : Settings.
 : @param  $info  info string
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/settings')
  %rest:query-param('info',  '{$info}')
  %output:method('html')
  %output:html-version('5')
function dba:settings(
  $info  as xs:string?
) as element(html) {
  let $system := html:properties(db:system())
  let $table-row := fn($label, $items) {
    <tr><td>{ $label, <br/>, $items }</td></tr>
  }
  let $option := fn($key, $values, $label) {
    $table-row($label,
      <select name='{ $key }'>{
        let $selected := config:get($key)
        for $value in $values
        return element option { attribute selected { }[$value = $selected], $value }
      }</select>
    )
  }
  let $number := fn($key, $label) {
    $table-row($label, <input type='number' name='{ $key }' value='{ config:get($key) }'/>)
  }
  let $string := fn($key, $label) {
    $table-row($label, <input type='text' name='{ $key }' value='{ config:get($key) }'/>)
  }
  return (
    <tr>
      <td width='33%'>
        <form method='post' autocomplete='off'>
          <h2>Settings » { html:button('settings-save', 'Save') }</h2>
          <h3>Queries</h3>
          <table>{
            $number($config:TIMEOUT, 'Timeout, in seconds (0 = disabled)'),
            $number($config:MEMORY, 'Memory limit, in MB (0 = disabled)'),
            $number($config:MAXCHARS, 'Maximum output size'),
            $option($config:PERMISSION, $config:PERMISSIONS, 'Permission'),
            $option($config:INDENT, $config:INDENTS, 'Indent results')
          }</table>
          <h3>Tables</h3>
          <table>{
            $number($config:MAXROWS,  'Displayed table rows')
          }</table>
          <h3>Logs</h3>
          <table>{
            $string($config:IGNORE-LOGS, <span>Ignore entries (e.g. <code>/dba</code>):</span>)
          }</table>
        </form>
      </td>
      <td class='vertical'/>
      <td width='33%'>
        <form method='post' autocomplete='off'>
          <h2>Global Options » { html:button('settings-gc', 'GC') }</h2>
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
  ) => html:wrap({ 'header': $dba:CAT, 'info': $info })
};

(:~
 : Saves the settings.
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/settings-save')
function dba:settings-save(
) as element(rest:response) {
  config:save(html:parameters()),
  web:redirect($dba:CAT, { 'info': 'Settings were saved.' })
};
