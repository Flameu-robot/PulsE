import { useRouter } from 'next/navigation'
import Cookies from 'js-cookie'
import { api } from '@/app/lib/api'

export const useLogout = () => {
    const router = useRouter()

    const logout = async () => {
        try {
            const refreshToken = localStorage.getItem('refreshToken')
            const response = await api.post('/api/auth/logout', { refreshToken })

            if (response.status === 200) {
                console.log('Successfully logged out from server')
            }
        } catch (error) {
            console.error('Logout failed', error)
        } finally {
            localStorage.removeItem('accessToken')
            localStorage.removeItem('refreshToken')
            Cookies.remove('accessToken')
            router.push('/')
            router.refresh()
        }
    }

    return { logout }
}