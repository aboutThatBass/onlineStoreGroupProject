# Viral Vault — Running the UI

The frontend is already set up with SvelteKit. **Do not create a new Svelte project.**

## 1. Install Node.js (one time)

Download and install the **LTS** version from https://nodejs.org/ if you don't already have it. Restart your terminal, then check:

node -v
npm -v

Both commands should display version numbers.

## 2. Get the latest code

Clone the team's GitHub repository if you haven't already. Otherwise, pull the latest changes:

git pull

## 3. Start the UI

From the repository's root folder (in the terminal either on your machine or in your IDE):

cd frontend
npm install
npm run dev


Open the local URL shown in the terminal, usually **http://localhost:5173/**.

`npm install` is needed on first setup and when dependencies change; on later runs, you can usually just run `npm run dev` from `frontend`.

**Windows PowerShell note:** If PowerShell blocks npm scripts, use `npm.cmd install` and `npm.cmd run dev` instead.

## 4. Stop the UI

Press **Ctrl+C** in the terminal running the development server.

The UI currently uses mock product data, so you do **not** need the Spring Boot backend running to view it.
