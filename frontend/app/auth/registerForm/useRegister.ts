import { useState } from 'react'
import { RegisterFormData, AuthResponse } from './types'
import { api } from "@/app/lib/api"

export const useRegister = () => {
    const [isLoading, setIsLoading] = useState(false)
    const [serverError, setServerError] = useState<string | null>(null)

    const registerUser = async (data: RegisterFormData): Promise<boolean> => {
        setIsLoading(true)
        setServerError(null)

        try {
            const authData = await api.post('/api/auth/register', {
                username: data.username,
                email: data.email,
                password: data.password
            }) as AuthResponse

            if (authData.accessToken && authData.refreshToken) {
                localStorage.setItem('accessToken', authData.accessToken)
                localStorage.setItem('refreshToken', authData.refreshToken)
            }

            return true
        } catch (err: any) {
            setServerError(err.response?.data?.message || err.message)
            return false
        } finally {
            setIsLoading(false)
        }
    }

    return { registerUser, isLoading, serverError }
}