<script lang="ts">
    let username = $state('');
    let fullName = $state('');
    let email = $state('');
    let phoneNumber = $state('');
    let password = $state('');
    let confirmPassword = $state('');

    let errorMessages = $state<string[]>([]);
    let successMessage = $state('');
    let isSubmitting = $state(false);

    async function handleRegistration(event: SubmitEvent) {
        event.preventDefault();

        errorMessages = [];
        successMessage = '';

        if (password !== confirmPassword) {
            errorMessages = ['Passwords do not match.'];
            return;
        }

        isSubmitting = true;

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

            if (response.ok && data.success) {
                successMessage = 'Account created successfully! You can now log in.';

                username = '';
                fullName = '';
                email = '';
                phoneNumber = '';
                password = '';
                confirmPassword = '';
            } else {
                errorMessages = data.errors ?? [
                    data.message ?? 'Registration failed. Please try again.'
                ];
            }
        } catch (error) {
            errorMessages = [
                'Unable to connect to the server. Please try again later.'
            ];
        } finally {
            isSubmitting = false;
        }
    }
</script>

<svelte:head>
    <title>Register | Viral Vault</title>
</svelte:head>

<main>
    <h1>Join Viral Vault!</h1>
    <p>Create an account to start collecting.</p>

    {#if successMessage}
        <p role="status" style="color: green;">
            {successMessage}
        </p>
    {/if}

    {#if errorMessages.length > 0}
        <div role="alert" style="color: red;">
            {#each errorMessages as message}
                <p>{message}</p>
            {/each}
        </div>
    {/if}

    <form onsubmit={handleRegistration}>
        <label for="username">Username</label>
        <input
            type="text"
            id="username"
            name="username"
            placeholder="Choose a username"
            bind:value={username}
            required
        />

        <label for="fullName">Full Name (Optional)</label>
        <input
            type="text"
            id="fullName"
            name="fullName"
            placeholder="Enter your full name"
            bind:value={fullName}
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
            minlength="8"
            required
        />

        <label for="confirm-password">Confirm Password</label>
        <input
            type="password"
            id="confirm-password"
            name="confirmPassword"
            placeholder="Confirm your password"
            bind:value={confirmPassword}
            required
        />

        <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Creating Account...' : 'Create Account'}
        </button>
    </form>

    <p>
        Already have an account?
        <a href="/login">Log in here</a>
    </p>
</main>