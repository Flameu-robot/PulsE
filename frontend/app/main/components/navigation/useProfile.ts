import { useState, useEffect } from 'react';
import { api } from '@/app/lib/api';
import { UserProfile } from './types';

export const useProfile = () => {
    const [user, setUser] = useState<UserProfile | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                setIsLoading(true);
                const data = await api.get<UserProfile>('/api/users/me');
                setUser(data);
            } catch (err) {
                setError(err instanceof Error ? err : new Error('Failed to fetch profile'));
            } finally {
                setIsLoading(false);
            }
        };

        fetchProfile();
    }, []);

    const mutate = (updated: UserProfile | null) => {
        setUser(updated);
    };

    return { user, isLoading, error, mutate };
};