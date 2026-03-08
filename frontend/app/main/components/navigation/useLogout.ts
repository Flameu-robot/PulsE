import { useRouter } from 'next/navigation'
import Cookies from 'js-cookie'
import { api } from '@/app/lib/api'

export const useLogout = () => {
    const router = useRouter()

    const logout = async () => {
        try {
            const refreshToken = localStorage.getItem('refreshToken')
            const response = await api.post('/api/auth/logout', { refreshToken })

            const status = response?.status

            if (status === 200 || status === 204) {
                console.log('Server logout successful:', status)
            }
        } catch (error) {
            console.error('Logout request failed:', error)
        } finally {
            localStorage.removeItem('accessToken')
            localStorage.removeItem('refreshToken')
            Cookies.remove('accessToken')

            console.log('Local storage and cookies cleared')

            router.push('/')
            router.refresh()
        }
    }

    return { logout }
}