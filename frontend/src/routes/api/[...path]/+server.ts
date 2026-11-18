import type { RequestHandler } from './$types';

const proxy: RequestHandler = async ({ request, url }) => {
    const backend = process.env.BACKEND_URL ?? 'http://localhost:8080';

    const target = new URL(url.pathname + url.search, backend);

    const headers = new Headers(request.headers);

    headers.delete('host');
    headers.delete('origin');
    headers.delete('referer');

    try {
        const response = await fetch(target, {
            method: request.method,
            headers,
            body: request.method === 'GET' || request.method === 'HEAD'
                ? undefined
                : await request.arrayBuffer(),
            redirect: 'manual'
        });

        return new Response(response.body, {
            status: response.status,
            headers: response.headers
        });
    } catch (err) {
        console.error('Backend proxy failed:', err);
        return new Response('Backend unavailable', { status: 502 });
    }
};

export const GET = proxy;
export const POST = proxy;
export const PUT = proxy;
export const PATCH = proxy;
export const DELETE = proxy;
