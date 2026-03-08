'use client'

import { useState, useEffect } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Mail, ArrowRight, Loader2, X, CheckCircle2 } from 'lucide-react'
import Button from '../../components/ui/Button'
import { useVerify } from './useVerify'

interface EmailVerificationModalProps {
    isOpen: boolean
    onClose: () => void
    onVerified: () => void
}

export default function EmailVerificationModal({ isOpen, onClose, onVerified }: EmailVerificationModalProps) {
    const [code, setCode] = useState('')
    const { sendCode, confirmCode, isSending, isVerifying, serverError, successMessage } = useVerify()

    useEffect(() => {
        if (isOpen) {
            sendCode()
        }
    }, [isOpen])

    const handleConfirm = async (e: React.FormEvent) => {
        e.preventDefault()
        const success = await confirmCode(code)
        if (success) {
            onVerified()
        }
    }

    return (
        <AnimatePresence>
            {isOpen && (
                <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
                    <motion.div
                        initial={{ opacity: 0, scale: 0.9, y: 20 }}
                        animate={{ opacity: 1, scale: 1, y: 0 }}
                        exit={{ opacity: 0, scale: 0.9, y: 20 }}
                        className="relative w-full max-w-md bg-[#0d0d0f] border border-purple-500/20 rounded-3xl p-8 shadow-2xl shadow-purple-500/10"
                    >
                        <button
                            onClick={onClose}
                            className="absolute top-4 right-4 p-2 text-gray-500 hover:text-white transition-colors"
                        >
                            <X className="w-5 h-5" />
                        </button>

                        <div className="flex flex-col items-center text-center space-y-4">
                            <div className="w-16 h-16 bg-purple-600/10 rounded-2xl flex items-center justify-center border border-purple-500/20">
                                <Mail className="w-8 h-8 text-purple-400" />
                            </div>

                            <div className="space-y-2">
                                <h2 className="text-2xl font-black text-white tracking-tight">Verify Email</h2>
                                <p className="text-gray-400 text-sm">
                                    We've sent a 6-digit code to your email. Please enter it below to verify your account.
                                </p>
                            </div>

                            <form onSubmit={handleConfirm} className="w-full space-y-6 pt-4">
                                <div className="space-y-2">
                                    <input
                                        type="text"
                                        maxLength={6}
                                        value={code}
                                        onChange={(e) => setCode(e.target.value)}
                                        placeholder="000000"
                                        className="w-full bg-white/5 border border-white/10 focus:border-purple-500/50 rounded-2xl py-4 text-center text-2xl font-bold tracking-[1em] text-white outline-none transition-all"
                                        required
                                    />
                                    {serverError && <p className="text-xs text-red-400">{serverError}</p>}
                                    {successMessage && <p className="text-xs text-green-400">{successMessage}</p>}
                                </div>

                                <div className="space-y-3">
                                    <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                                        <Button type="submit" className="w-full py-4 rounded-2xl" disabled={isVerifying || code.length < 6}>
                                            {isVerifying ? (
                                                <Loader2 className="w-5 h-5 animate-spin" />
                                            ) : (
                                                <>
                                                    Verify Account
                                                    <CheckCircle2 className="w-4 h-4 ml-2" />
                                                </>
                                            )}
                                        </Button>
                                    </motion.div>

                                    <button
                                        type="button"
                                        onClick={() => sendCode()}
                                        disabled={isSending}
                                        className="text-sm text-purple-400 hover:text-purple-300 disabled:opacity-50 transition-colors"
                                    >
                                        {isSending ? 'Sending...' : "Didn't receive a code? Resend"}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </motion.div>
                </div>
            )}
        </AnimatePresence>
    )
}