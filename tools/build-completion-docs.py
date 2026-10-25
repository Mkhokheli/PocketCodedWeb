"""Build offline reference data from the downloaded upstream files in INPUT_DIR.

See docs/COMPLETION_DOCUMENTATION.md for sources and licenses.
"""
import json
import re
import sys
from pathlib import Path

root = Path(__file__).resolve().parents[1]
src = Path(sys.argv[1])
entries = {}

def plain(value):
    if isinstance(value, dict):
        value = value.get('value', '')
    value = re.sub(r'\[([^\]]+)\]\([^\n]*?\)', r'\1', value or '')
    return value.replace('**', '').replace('`', '').strip()

def add(key, entry, syntax='', reference=''):
    description = plain(entry.get('description', ''))
    if not description:
        return
    refs = entry.get('references', [])
    entries[key] = dict(description=description, syntax=syntax,
                        source=refs[0]['url'] if refs else reference)

html = json.loads((src / 'html.json').read_text(encoding='utf-8-sig'))
value_sets = {entry['name']: entry.get('values', []) for entry in html.get('valueSets', [])}
for tag in html['tags']:
    name = tag['name']
    void = name in 'area base br col embed hr img input link meta param source track wbr'.split()
    add('html:' + name, tag, f'<{name}>' if void else f'<{name}>…</{name}>',
        'https://html.spec.whatwg.org/multipage/semantics.html')
    for attr in tag.get('attributes', []):
        if not plain(attr.get('description', '')) and attr.get('valueSet') in value_sets:
            values = ', '.join(value['name'] for value in value_sets[attr['valueSet']])
            attr = dict(attr, description=f"The {attr['name']} attribute on <{name}> accepts these reference values: {values}.")
        add('attribute:' + name + ':' + attr['name'], attr, attr['name'] + '="value"',
            'https://html.spec.whatwg.org/multipage/indices.html#attributes-3')
for attr in html['globalAttributes']:
    add('attribute:' + attr['name'], attr, attr['name'] + '="value"',
        'https://html.spec.whatwg.org/multipage/dom.html#global-attributes')

css = json.loads((src / 'css.json').read_text(encoding='utf-8-sig'))
for group in ['properties', 'atDirectives', 'pseudoClasses', 'pseudoElements']:
    for entry in css.get(group, []):
        name = entry['name']
        syntax = (name + ': ' + entry.get('syntax', 'value') + ';') if group == 'properties' else name
        add('css:' + name, entry, syntax, 'https://www.w3.org/Style/CSS/')
        for value in entry.get('values', []):
            add('css-value:' + name + ':' + value['name'], value,
                name + ': ' + value['name'] + ';',
                entry.get('references', [{}])[0].get('url', 'https://www.w3.org/Style/CSS/'))

# Extract documented members, keeping their receiver type to disambiguate names.
for filename in ['es5.d.ts', 'es2015.promise.d.ts', 'es2018.promise.d.ts', 'es2020.promise.d.ts', 'dom.d.ts']:
    text = (src / filename).read_text(encoding='utf-8-sig')
    interfaces = list(re.finditer(r'^interface (\w+)[^\n]*\{', text, re.M))
    for index, interface in enumerate(interfaces):
        body = text[interface.end():interfaces[index + 1].start() if index + 1 < len(interfaces) else len(text)]
        body = body.split('\n}', 1)[0]
        for match in re.finditer(r'/\*\*([\s\S]*?)\*/\s*(?:readonly\s+)?(\w+)([^\n]*)', body):
            comment, member, rest = match.groups()
            comment = re.sub(r'^\s*\* ?', '', comment, flags=re.M).strip()
            links = re.findall(r'\[MDN Reference\]\((https://[^)]+)\)', comment)
            description = plain(re.sub(r'\[MDN Reference\]\([^)]+\)', '', comment))
            if not description:
                continue
            owner = interface.group(1)
            key = 'js:' + owner + ':' + member
            entries.setdefault(key, dict(description=description, syntax=member + rest.rstrip(';'),
                source=links[0] if links else 'https://www.typescriptlang.org/docs/handbook/declaration-files/by-example.html'))

dest = root / 'app/src/main/assets/completion-docs'
dest.mkdir(parents=True, exist_ok=True)
(dest / 'reference.json').write_text(json.dumps(entries, ensure_ascii=False, separators=(',', ':')), encoding='utf-8')
for name, target in [('LICENSE', 'vscode-custom-data-LICENSE.txt'), ('typescript-license.txt', 'typescript-LICENSE.txt'), ('dom-license.txt', 'typescript-dom-LICENSE.txt')]:
    (root / 'app/src/main/assets/licenses' / target).write_bytes((src / name).read_bytes())
print(f'Wrote {len(entries)} documentation entries.')
