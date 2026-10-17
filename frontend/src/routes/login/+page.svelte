
<svelte:head>
    <title>Login | Viral Vault</title>
</svelte:head>

<main>
    <h1>Welcome Back!</h1>
    <p>Log in to your Viral Vault account.</p>

    <form>
        <label for="email">Email</label>
        <input
            type="email"
            id="email"
            name="email"
            placeholder="Enter your email"
        />

        <label for="password">Password</label>
        <input
            type="password"
            id="password"
            name="password"
            placeholder="Enter your password"
        />

        <button type="button">Log In</button>
    </form>

    <p>
        Don't have an account?
        <a href="/register">Register here</a>
    </p>
</main>
