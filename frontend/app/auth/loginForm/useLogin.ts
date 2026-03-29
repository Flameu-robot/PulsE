import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Cookies from 'js-cookie'
import { LoginFormData, AuthResponse } from './types'
import { api } from "@/app/lib/api"

export const useLogin = () => {
    const [isLoading, setIsLoading] = useState(false)
    const [serverError, setServerError] = useState<string | null>(null)
    const router = useRouter()

    const loginUser = async (data: LoginFormData) => {
        setIsLoading(true)
        setServerError(null)

        try {
            const authData = await api.post('/api/auth/login', {
                login: data.login,
                password: data.password
            }) as AuthResponse

            if (authData.accessToken && authData.refreshToken) {
                localStorage.setItem('accessToken', authData.accessToken)
                localStorage.setItem('refreshToken', authData.refreshToken)

                Cookies.set('accessToken', authData.accessToken, { expires: 7, sameSite: 'strict' })

                router.push('/main')
            }
        } catch (err: any) {
            setServerError(err.response?.data?.message || err.message || 'Login failed')
        } finally {
            setIsLoading(false)
        }
    }

    return { loginUser, isLoading, serverError }
}