import { useState } from 'react';
import { api } from '@/app/lib/api';
import { UpdateProfileDto, UserProfile } from './types';

export const useUpdateProfile = () => {
    const [isUpdating, setIsUpdating] = useState(false);
    const [updateError, setUpdateError] = useState<Error | null>(null);

    const updateProfile = async (data: UpdateProfileDto): Promise<UserProfile | null> => {
        try {
            setIsUpdating(true);
            setUpdateError(null);
            const response = await api.patch<UserProfile>('/api/users/me', data);
            return response;
        } catch (err) {
            const error = err instanceof Error ? err : new Error('Failed to update profile');
            setUpdateError(error);
            throw error;
        } finally {
            setIsUpdating(false);
        }
    };

    return { updateProfile, isUpdating, updateError };
};