
<script lang="ts">
    import { goto } from '$app/navigation';
    import {
        UserRound,
        UserPlus,
        Mail,
        Phone,
        LockKeyhole,
        ArrowRight
    } from '@lucide/svelte';

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
            const response = await fetch(
                'http://localhost:8080/api/users/register',
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        username: username.trim(),
                        fullName: fullName.trim(),
                        email: email.trim(),
                        phoneNumber: phoneNumber.trim(),
                        password,
                        confirmPassword
                    })
                }
            );

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

<main class="register-page">
    <section class="register-card" aria-labelledby="register-title">

        <div class="icon-circle">
            <UserPlus size={34} strokeWidth={2} />
        </div>

        <h1 id="register-title">Join Viral Vault!</h1>

        <p class="subtitle">
            Create an account to start collecting.
        </p>

        <form onsubmit={handleRegister}>

            <div class="form-grid">

                <!-- Username -->
                <div class="form-group">
                    <label for="username">Username</label>

                    <div class="input-wrapper">
                        <UserRound size={20} />

                        <input
                            type="text"
                            id="username"
                            name="username"
                            placeholder="Choose a username"
                            autocomplete="username"
                            bind:value={username}
                            required
                        />
                    </div>
                </div>

                <!-- Full Name -->
                <div class="form-group">
                    <label for="fullName">Full Name</label>

                    <div class="input-wrapper">
                        <UserRound size={20} />

                        <input
                            type="text"
                            id="fullName"
                            name="fullName"
                            placeholder="Enter your full name"
                            autocomplete="name"
                            bind:value={fullName}
                            required
                        />
                    </div>
                </div>

                <!-- Email -->
                <div class="form-group">
                    <label for="email">Email</label>

                    <div class="input-wrapper">
                        <Mail size={20} />

                        <input
                            type="email"
                            id="email"
                            name="email"
                            placeholder="Enter your email"
                            autocomplete="email"
                            bind:value={email}
                            required
                        />
                    </div>
                </div>

                <!-- Phone Number -->
                <div class="form-group">
                    <label for="phoneNumber">Phone Number</label>

                    <div class="input-wrapper">
                        <Phone size={20} />

                        <input
                            type="tel"
                            id="phoneNumber"
                            name="phoneNumber"
                            placeholder="Enter your phone number"
                            autocomplete="tel"
                            bind:value={phoneNumber}
                            required
                        />
                    </div>
                </div>

                <!-- Password -->
                <div class="form-group">
                    <label for="password">Password</label>

                    <div class="input-wrapper">
                        <LockKeyhole size={20} />

                        <input
                            type="password"
                            id="password"
                            name="password"
                            placeholder="Create a password"
                            autocomplete="new-password"
                            bind:value={password}
                            required
                        />
                    </div>
                </div>

                <!-- Confirm Password -->
                <div class="form-group">
                    <label for="confirm-password">Confirm Password</label>

                    <div class="input-wrapper">
                        <LockKeyhole size={20} />

                        <input
                            type="password"
                            id="confirm-password"
                            name="confirm-password"
                            placeholder="Confirm your password"
                            autocomplete="new-password"
                            bind:value={confirmPassword}
                            required
                        />
                    </div>
                </div>

            </div>

            {#if errorMessage}
                <div class="error-message" role="alert">
                    {errorMessage}
                </div>
            {/if}

            <button
                type="submit"
                class="register-button"
                disabled={loading}
            >
                {loading ? 'Creating Account...' : 'Create Account'}

                {#if !loading}
                    <ArrowRight size={20} />
                {/if}
            </button>

        </form>

        <div class="card-footer">
            <p>
                Already have an account?
                <a href="/login">Log in here</a>
            </p>
        </div>

    </section>
</main>

<style>
    .register-page {
        min-height: 75vh;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 3.5rem 1rem;
        background: #ffedad;
        color: #40342e;
        font-family: var(--font-body, 'Nunito', sans-serif);
    }

    .register-card {
        width: 100%;
        max-width: 760px;
        padding: 2.5rem;
        background: #fffdf7;
        border: 3px solid #40342e;
        border-radius: 18px;
        box-shadow: 7px 7px 0 #40342e;
        text-align: center;
    }

    .icon-circle {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 76px;
        height: 76px;
        margin: 0 auto 1.25rem;
        background: #f9dc7a;
        border: 2px solid #40342e;
        border-radius: 50%;
    }

    h1 {
        margin: 0.5rem 0;
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 2.3rem;
        font-weight: 700;
        line-height: 1.2;
    }

    .subtitle {
        margin: 0.75rem 0 2rem;
        color: #73645a;
        font-size: 1rem;
    }

    form {
        display: flex;
        flex-direction: column;
        gap: 1.5rem;
        text-align: left;
    }

    .form-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        column-gap: 1.25rem;
        row-gap: 1.4rem;
    }

    .form-group {
        display: flex;
        flex-direction: column;
        min-width: 0;
        gap: 0.5rem;
    }

    label {
        font-weight: 800;
        font-size: 1rem;
    }

    .input-wrapper {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        min-width: 0;
        padding: 0.9rem 1rem;
        background: white;
        border: 2px solid #40342e;
        border-radius: 10px;
        transition: outline 0.2s ease;
    }

    .input-wrapper :global(svg) {
        flex-shrink: 0;
        color: #73645a;
    }

    input {
        width: 100%;
        min-width: 0;
        padding: 0;
        border: none;
        outline: none;
        background: transparent;
        color: #40342e;
        font: inherit;
        font-size: 1rem;
    }

    input::placeholder {
        color: #94867c;
    }

    .input-wrapper:focus-within {
        outline: 2px solid #e8a598;
        outline-offset: 2px;
    }

    .register-button {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 0.6rem;
        width: 100%;
        margin-top: 0.5rem;
        padding: 0.95rem 1rem;
        background: #e8a598;
        color: #40342e;
        border: 2px solid #40342e;
        border-radius: 10px;
        box-shadow: 3px 3px 0 #40342e;
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 1.2rem;
        font-weight: 700;
        cursor: pointer;
        transition: background 0.2s ease, transform 0.2s ease;
    }

    .register-button:hover:not(:disabled) {
        background: #f2b8ad;
        transform: translateY(-2px);
    }

    .register-button:active:not(:disabled) {
        transform: translateY(1px);
        box-shadow: 1px 1px 0 #40342e;
    }

    .register-button:disabled {
        opacity: 0.65;
        cursor: wait;
    }

    .error-message {
        padding: 0.8rem 1rem;
        background: #fff0ee;
        color: #b42318;
        border: 1px solid #e8a598;
        border-radius: 8px;
        font-size: 0.95rem;
        font-weight: 700;
    }

    .card-footer {
        margin-top: 2rem;
        padding-top: 1.5rem;
        border-top: 1px solid #e9ded2;
    }

    .card-footer p {
        margin: 0;
        color: #73645a;
        font-size: 0.95rem;
    }

    .card-footer a {
        color: #40342e;
        font-weight: 800;
        text-decoration: underline;
        text-decoration-color: #e8a598;
        text-decoration-thickness: 2px;
        text-underline-offset: 3px;
    }

    .card-footer a:hover {
        color: #b36f63;
    }

    @media (max-width: 650px) {
        .register-page {
            padding: 2rem 1rem;
        }

        .register-card {
            padding: 2rem 1.4rem;
            box-shadow: 5px 5px 0 #40342e;
        }

        .form-grid {
            grid-template-columns: 1fr;
            row-gap: 1.25rem;
        }

        h1 {
            font-size: 1.9rem;
        }
    }
</style>
