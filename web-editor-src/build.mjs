import esbuild from "esbuild";

await esbuild.build({
  entryPoints: ["web-editor-src/editor.js"],
  bundle: true,
  format: "iife",
  target: "es2020",
  minify: true,
  sourcemap: false,
  outfile: "app/src/main/assets/codemirror-editor/editor.js",
});
