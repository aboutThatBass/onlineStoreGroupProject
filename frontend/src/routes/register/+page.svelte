
<script lang="ts">
    import { goto } from '$app/navigation';

    let username = $state('');
    let fullName = $state('');
    let email = $state('');
    let phoneNumber = $state('');
    let password = $state('');
    let confirmPassword = $state('');
    let errorMessage = $state('');
    let loading = $state(false);

    async function handleRegister(event: SubmitEvent) {
        event.preventDefault();
        errorMessage = '';

        if (password !== confirmPassword) {
            errorMessage = 'Passwords do not match.';
            return;
        }

        loading = true;

        try {
            const response = await fetch('http://localhost:8080/api/users/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username,
                    fullName,
                    email,
                    phoneNumber,
                    password,
                    confirmPassword
                })
            });

            const data = await response.json();

            if (!response.ok || !data.success) {
                errorMessage = Array.isArray(data.errors)
                    ? data.errors.join(' ')
                    : data.message || 'Registration failed.';
                return;
            }

            await goto('/login');
        } catch {
            errorMessage = 'Unable to connect to the server.';
        } finally {
            loading = false;
        }
    }
</script>

<svelte:head>
    <title>Register | Viral Vault</title>
</svelte:head>

<main>
    <h1>Join Viral Vault!</h1>
    <p>Create an account to start collecting.</p>

    <form onsubmit={handleRegister}>
        <label for="username">Username</label>
        <input
            type="text"
            id="username"
            name="username"
            placeholder="Choose a username"
            bind:value={username}
            required
        />

        <label for="fullName">Full Name</label>
        <input
            type="text"
            id="fullName"
            name="fullName"
            placeholder="Enter your full name"
            bind:value={fullName}
            required
        />

        <label for="email">Email</label>
        <input
            type="email"
            id="email"
            name="email"
            placeholder="Enter your email"
            bind:value={email}
            required
        />

        <label for="phoneNumber">Phone Number</label>
        <input
            type="tel"
            id="phoneNumber"
            name="phoneNumber"
            placeholder="Enter your phone number"
            bind:value={phoneNumber}
            required
        />

        <label for="password">Password</label>
        <input
            type="password"
            id="password"
            name="password"
            placeholder="Create a password"
            bind:value={password}
            required
        />

        <label for="confirm-password">Confirm Password</label>
        <input
            type="password"
            id="confirm-password"
            name="confirm-password"
            placeholder="Confirm your password"
            bind:value={confirmPassword}
            required
        />

        {#if errorMessage}
            <p role="alert">{errorMessage}</p>
        {/if}

        <button type="submit" disabled={loading}>
            {loading ? 'Creating Account...' : 'Create Account'}
        </button>
    </form>

    <p>
        Already have an account?
        <a href="/login">Log in here</a>
    </p>
</main>
