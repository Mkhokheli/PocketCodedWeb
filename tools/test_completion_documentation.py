import json
import unittest
from pathlib import Path


class CompletionReferenceTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        path = Path(__file__).resolve().parents[1] / 'app/src/main/assets/completion-docs/reference.json'
        cls.entries = json.loads(path.read_text(encoding='utf-8'))

    def test_bundled_entries_have_descriptions_and_https_sources(self):
        for key, entry in self.entries.items():
            with self.subTest(key=key):
                self.assertTrue(entry['description'].strip())
                self.assertTrue(entry['source'].startswith('https://'))

    def test_html_and_css_context_is_preserved(self):
        for key in ['html:button', 'attribute:input:type', 'attribute:class',
                    'css:display', 'css-value:display:flex', 'css::hover']:
            with self.subTest(key=key):
                self.assertTrue(key in self.entries, key)
        self.assertIn('display:', self.entries['css:display']['syntax'])

    def test_javascript_receivers_have_distinct_documentation(self):
        for key in ['js:Array:map', 'js:String:replace', 'js:Location:replace',
                    'js:Headers:get', 'js:URLSearchParams:get', 'js:ParentNode:querySelector']:
            with self.subTest(key=key):
                self.assertTrue(key in self.entries, key)
        self.assertNotEqual(self.entries['js:Headers:get']['description'],
                            self.entries['js:URLSearchParams:get']['description'])


if __name__ == '__main__':
    unittest.main()
