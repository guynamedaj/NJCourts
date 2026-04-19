# PCSAM-style parking citation evidence review (web)

React app in **`web/`**: case search, case summary, photo evidence.

**Citation numbers** are `Court code` + `Prefix` + `Sequence` (e.g. `1214 T90 260001`). Demo uses court **1214**, prefixes **T01** and **T90** (handheld device issues **1214** / **T90**). Sequences use **26** + serial for 2026 (e.g. `260001`). Defendant fields are typically unknown at issuance for parking.

## Run

From the **repo root** (or from `web/`):

```bash
npm install
npm run dev
```

`package.json` for the React app is under `web/`; the root `package.json` uses **npm workspaces** so `npm install` at the repo root installs dependencies for `web`.

Build: `npm run build` (root) or `cd web && npm run build`

See `web/requirements.txt` for full requirements.
