
<svelte:head>
    <title>Register | Viral Vault</title>
</svelte:head>

<main>
    <h1>Join Viral Vault!</h1>
    <p>Create an account to start collecting.</p>

    <form>
        <label for="username">Username</label>
        <input
            type="text"
            id="username"
            name="username"
            placeholder="Choose a username"
            required
        />

        <label for="email">Email</label>
        <input
            type="email"
            id="email"
            name="email"
            placeholder="Enter your email"
            required
        />

        <label for="password">Password</label>
        <input
            type="password"
            id="password"
            name="password"
            placeholder="Create a password"
            required
        />

        <label for="confirm-password">Confirm Password</label>
        <input
            type="password"
            id="confirm-password"
            name="confirm-password"
            placeholder="Confirm your password"
            required
        />

        <button type="button">Create Account</button>
    </form>

    <p>
        Already have an account?
        <a href="/login">Log in here</a>
    </p>
</main>
