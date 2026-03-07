const BASE_URL = 'http://localhost:8000';

async function fetcher(endpoint: string, options: RequestInit = {}) {
    const url = `${BASE_URL}${endpoint}`;

    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
    };


    const token = localStorage.getItem('accessToken');
    if (token) {
        (headers as any)['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
        ...options,
        headers,
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
        throw new Error(data.message || 'Something went wrong');
    }

    return data;
}

export const api = {
    get: (endpoint: string, options?: RequestInit) =>
        fetcher(endpoint, { ...options, method: 'GET' }),

    post: (endpoint: string, body: any, options?: RequestInit) =>
        fetcher(endpoint, { ...options, method: 'POST', body: JSON.stringify(body) }),

    put: (endpoint: string, body: any, options?: RequestInit) =>
        fetcher(endpoint, { ...options, method: 'PUT', body: JSON.stringify(body) }),

    delete: (endpoint: string, options?: RequestInit) =>
        fetcher(endpoint, { ...options, method: 'DELETE' }),
};