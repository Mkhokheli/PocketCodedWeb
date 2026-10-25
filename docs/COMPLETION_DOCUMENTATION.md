# Suggestion documentation

The details popup uses 12sp monospace body text with 16sp line height and a
15sp title. Reference data is bundled offline, loaded on the IO dispatcher,
and cached. Only opening a reference link requires a browser/network.

`app/src/main/assets/completion-docs/reference.json` is generated with
`python tools/build-completion-docs.py INPUT_DIR` from:

- Microsoft vscode-custom-data `web-data/data/browsers.html-data.json` and
  `browsers.css-data.json` (main, retrieved 2026-09-12), MIT license.
  https://github.com/microsoft/vscode-custom-data
- TypeScript v5.9.3 `src/lib/es5.d.ts`, `es2015.promise.d.ts`,
  `es2018.promise.d.ts`, and `es2020.promise.d.ts`, Apache 2.0.
  https://github.com/microsoft/TypeScript/tree/v5.9.3/src/lib
- TypeScript-DOM-lib-generator `baselines/dom.generated.d.ts` (main,
  retrieved 2026-09-12), Apache 2.0. Browser descriptions retain their MDN
  reference URLs. https://github.com/microsoft/TypeScript-DOM-lib-generator

Upstream license texts are bundled in `assets/licenses`. Descriptions are
converted from Markdown to plain text; examples are authored locally.
MDN references are explicitly labeled MDN, not official language standards.
Handwritten DOCTYPE, CSS var(), and package.json explanations summarize:

- https://html.spec.whatwg.org/multipage/syntax.html#the-doctype
- https://www.w3.org/TR/css-variables-1/#using-variables
- https://docs.npmjs.com/cli/v11/configuring-npm/package-json/

JavaScript receiver metadata disambiguates methods shared by different APIs.
External language-server documentation is preserved when supplied. Missing
entries explicitly say no bundled reference is available and show insertion
text instead of inventing an explanation or calling a snippet an example.
