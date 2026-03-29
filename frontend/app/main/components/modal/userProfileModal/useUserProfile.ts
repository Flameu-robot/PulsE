import { useState, useRef, ChangeEvent, useMemo } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { useProfile } from '../../navigation/useProfile';
import { useUpdateProfile } from './useUpdateProfile';

export const useUserProfile = () => {
    const { user, isLoading, mutate } = useProfile();
    const { updateProfile, isUpdating } = useUpdateProfile();
    const [isEditing, setIsEditing] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const formik = useFormik({
        enableReinitialize: true,
        initialValues: {
            username: user?.username || '',
            avatarUrl: user?.avatarUrl || '',
            bio: user?.bio || '',
        },
        validationSchema: Yup.object({
            username: Yup.string()
                .min(3, 'Минимум 3 символа')
                .matches(/^[a-zA-Z0-9_]+$/, 'Только латиница, цифры и _')
                .required('Никнейм обязателен'),
            avatarUrl: Yup.string().nullable(),
            bio: Yup.string().max(150, 'Максимум 150 символов').nullable(),
        }),
        onSubmit: async (values) => {
            try {
                const updated = await updateProfile({
                    username: values.username,
                    avatarUrl: values.avatarUrl || null,
                    bio: values.bio || null,
                });
                if (updated) {
                    mutate(updated);
                }
                setIsEditing(false);
            } catch (e) {
                console.error(e);
            }
        },
    });

    const handleAvatarChange = (e: ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            const reader = new FileReader();
            reader.onloadend = () => {
                formik.setFieldValue('avatarUrl', reader.result as string);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleCancel = () => {
        formik.resetForm();
        setIsEditing(false);
    };

    const inputClasses = (name: keyof typeof formik.values) =>
        `w-full bg-white/[0.03] border rounded-2xl p-3 text-white outline-none transition-all duration-200
    ${formik.touched[name] && formik.errors[name]
            ? 'border-red-500/50 bg-red-500/5 focus:border-red-500'
            : 'border-white/10 focus:border-purple-500/50 focus:bg-white/[0.05]'}`;

    const userInitial = useMemo(() => {
        return formik.values.username ? formik.values.username.charAt(0).toUpperCase() : '?';
    }, [formik.values.username]);

    return {
        isEditing,
        setIsEditing,
        fileInputRef,
        formik,
        handleAvatarChange,
        handleCancel,
        inputClasses,
        isLoading: isLoading || isUpdating,
        userInitial,
        user,
    };
};