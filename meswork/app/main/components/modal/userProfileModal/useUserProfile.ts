import { useState, useRef, ChangeEvent } from 'react'
import { useFormik } from 'formik'
import * as Yup from 'yup'

export const useUserProfile = () => {
    const [isEditing, setIsEditing] = useState(false)
    const fileInputRef = useRef<HTMLInputElement>(null)

    const formik = useFormik({
        initialValues: {
            fullName: 'Alex Johnson',
            username: 'alex_johnson',
            bio: 'Если расписывать много, то уйдут дни. Если двумя словами, то: я долбаёб.',
            avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400'
        },
        validationSchema: Yup.object({
            fullName: Yup.string()
                .min(2, 'Минимум 2 символа')
                .max(30, 'Максимум 30 символов')
                .required('Имя обязательно'),
            username: Yup.string()
                .min(3, 'Минимум 3 символа')
                .matches(/^[a-zA-Z0-9_]+$/, 'Только латиница, цифры и _')
                .required('Никнейм обязателен'),
            bio: Yup.string()
                .min(10, 'Расскажите о себе подробнее (мин. 10 симв.)')
                .max(150, 'Описание слишком длинное')
                .required('Описание обязательно'),
            avatar: Yup.string().required()
        }),
        onSubmit: (values) => {
            setIsEditing(false)
        },
    })

    const handleAvatarChange = (e: ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0]
        if (file) {
            const reader = new FileReader()
            reader.onloadend = () => {
                formik.setFieldValue('avatar', reader.result)
            }
            reader.readAsDataURL(file)
        }
    }

    const handleCancel = () => {
        formik.resetForm()
        setIsEditing(false)
    }

    const inputClasses = (name: keyof typeof formik.values) => `
        w-full bg-white/[0.03] border rounded-2xl p-3 text-white outline-none transition-all duration-200
        ${formik.touched[name] && formik.errors[name]
        ? 'border-red-500/50 bg-red-500/5 focus:border-red-500'
        : 'border-white/10 focus:border-purple-500/50 focus:bg-white/[0.05]'}
    `

    return {
        isEditing,
        setIsEditing,
        fileInputRef,
        formik,
        handleAvatarChange,
        handleCancel,
        inputClasses
    }
}