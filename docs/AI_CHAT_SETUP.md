# PocketCodedWeb AI Chat Setup

PocketCodedWeb includes an AI Chat screen that can call Gemini directly with a personal API key
entered by the user in the app. The Firebase backend does not host a shared Gemini key.

## What the app does

- Adds an AI Chat fullscreen screen inside the IDE.
- Requires a personal Gemini API key before AI requests are enabled.
- Stores the API key locally in app-private preferences.
- Shows the selected model before each request.
- Sends compact project context: active editor text, selected small project files, project tree,
  and recent terminal output.
- Parses assistant suggestions into explicit approval actions:
  - `terminal` fenced block: show a **Run** command button.
  - `replace-active-file` fenced block: show a **Replace** active file button.
  - `PocketCodedWeb-actions` JSON block: show an **Apply** button for project file writes/deletes.
- Never runs terminal commands or overwrites code without a user tap.

## Gemini API key

1. Create a Gemini API key in Google AI Studio or the Google Cloud console.
2. Open AI Chat in PocketCodedWeb.
3. Paste the key into the app's Gemini API key field.
4. Choose a model and send a prompt.

In the current direct flow, the API key is not bundled in the APK and is not sent to a
PocketCodedWeb server. Gemini usage and quota belong to the Google project that owns the key.

## Model choices

The app currently offers:

- `gemini-3.5-flash` as the default fast model;
- `gemini-3.1-flash-lite` as the lightweight fallback;
- `gemini-2.5-pro` for deeper reasoning when available;
- `gemini-2.5-flash` as a stable Gemini 2.5 option.

If a model is unavailable for a key, choose another model or enable access for that key's Google
project.

## Copilot-style file edits

When the user asks AI Chat to create or update code, the model should return normal explanation text
plus a fenced JSON block:

```PocketCodedWeb-actions
[
  {
    "action": "write_file",
    "path": "index.html",
    "content": "<!doctype html>\n<title>Hello</title>\n"
  },
  {
    "action": "delete_file",
    "path": "old-file.js"
  }
]
```

Supported actions:

- `write_file`: create or replace a file with complete final content.
- `delete_file`: delete a file.

The Android app validates every path before applying it. It rejects absolute paths, `..`, metadata
folders like `.git` and `.ssh`, folders, and files outside the current project. The user must
approve the suggested change before the app writes anything.

## Data safety note

AI Chat can send prompts, code context, file names, and recent terminal output to Google's Gemini
API when users request AI assistance. Do not paste secrets or private credentials into prompts.
