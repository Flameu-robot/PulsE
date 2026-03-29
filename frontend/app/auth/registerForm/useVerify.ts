import { useState } from 'react'
import { api } from "@/app/lib/api"

export const useVerify = () => {
    const [isSending, setIsSending] = useState(false)
    const [isVerifying, setIsVerifying] = useState(false)
    const [serverError, setServerError] = useState<string | null>(null)
    const [successMessage, setSuccessMessage] = useState<string | null>(null)

    const sendCode = async () => {
        setIsSending(true)
        setServerError(null)
        setSuccessMessage(null)
        try {
            await api.post('/api/auth/verify/send')
            setSuccessMessage('Код подтверждения отправлен на почту')
            return true
        } catch (err: any) {
            setServerError(err.response?.data?.message || err.message)
            return false
        } finally {
            setIsSending(false)
        }
    }

    const confirmCode = async (code: string) => {
        setIsVerifying(true)
        setServerError(null)
        try {
            await api.post('/api/auth/verify/confirm', { code })
            return true
        } catch (err: any) {
            setServerError(err.response?.data?.message || err.message)
            return false
        } finally {
            setIsVerifying(false)
        }
    }

    return { sendCode, confirmCode, isSending, isVerifying, serverError, successMessage }
}