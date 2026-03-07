import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { RegisterFormData, AuthResponse } from './types'
import {api} from "@/app/lib/api";

export const useRegister = () => {
    const [isLoading, setIsLoading] = useState(false)
    const [serverError, setServerError] = useState<string | null>(null)
    const router = useRouter()

    const registerUser = async (data: RegisterFormData) => {
        setIsLoading(true)
        setServerError(null)

        try {
            const authData = await api.post('/api/auth/register', {
                username: data.username,
                email: data.email,
                password: data.password
            }) as AuthResponse

            localStorage.setItem('accessToken', authData.accessToken)
            localStorage.setItem('refreshToken', authData.refreshToken)

            router.push('/dashboard')
        } catch (err: any) {
            setServerError(err.message)
        } finally {
            setIsLoading(false)
        }
    }

    return { registerUser, isLoading, serverError }
}