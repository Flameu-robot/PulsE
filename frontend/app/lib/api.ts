import Cookies from 'js-cookie';

const BASE_URL = 'http://localhost:8000';

async function fetcher(endpoint: string, options: RequestInit = {}): Promise<any> {
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

    if (response.status === 401 && endpoint !== '/api/auth/login' && endpoint !== '/api/auth/refresh') {
        const refreshToken = localStorage.getItem('refreshToken');

        if (refreshToken) {
            try {
                const refreshResponse = await fetch(`${BASE_URL}/api/auth/refresh`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ refreshToken }),
                });

                if (refreshResponse.ok) {
                    const data = await refreshResponse.json();

                    localStorage.setItem('accessToken', data.accessToken);
                    localStorage.setItem('refreshToken', data.refreshToken);
                    Cookies.set('accessToken', data.accessToken, { expires: 7, path: '/', sameSite: 'strict' });

                    const newHeaders = {
                        ...headers,
                        'Authorization': `Bearer ${data.accessToken}`
                    };

                    return fetcher(endpoint, { ...options, headers: newHeaders });
                }
            } catch (e) {
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                Cookies.remove('accessToken');
                window.location.href = '/';
                return;
            }
        }

        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        Cookies.remove('accessToken');
        window.location.href = '/';
    }

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