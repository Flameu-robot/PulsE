'use client'

import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import LoginForm from './loginForm/LoginForm'
import RegisterForm from './registerForm/RegisterForm'
import Button from '../components/ui/Button'
import styles from './AuthCard.module.css'
import { cn } from '@/app/lib/utils'

export default function AuthCard() {
    const [isLogin, setIsLogin] = useState(true)
    const [successMessage, setSuccessMessage] = useState<string | null>(null)

    const handleRegisterSuccess = () => {
        setIsLogin(true)
        setSuccessMessage('Registration successful! Please log in.')
        setTimeout(() => setSuccessMessage(null), 5000)
    }

    return (
        <motion.div
            initial={{ scale: 0.96, opacity: 0, y: 20 }}
            animate={{ scale: 1, opacity: 1, y: 0 }}
            transition={{ duration: 0.5, ease: 'easeOut' }}
            className={styles.authCard}
        >
            <div className="text-center mb-8">
                <motion.h2
                    key={isLogin ? 'login-title' : 'reg-title'}
                    initial={{ y: -10, opacity: 0 }}
                    animate={{ y: 0, opacity: 1 }}
                    className={styles.title}
                >
                    {isLogin ? 'Welcome back' : 'Create account'}
                </motion.h2>
                <motion.p
                    key={isLogin ? 'login-sub' : 'reg-sub'}
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ delay: 0.1 }}
                    className={styles.subtitle}
                >
                    {isLogin
                        ? 'Glad to see you again! Please enter your details.'
                        : 'Join our community and start your journey today.'
                    }
                </motion.p>
            </div>

            <AnimatePresence>
                {successMessage && (
                    <motion.div
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: 'auto', opacity: 1 }}
                        exit={{ height: 0, opacity: 0 }}
                        className="mb-6 p-3 text-sm text-emerald-400 bg-emerald-500/10 border border-emerald-500/20 rounded-lg text-center"
                    >
                        {successMessage}
                    </motion.div>
                )}
            </AnimatePresence>

            <div className={styles.tabContainer}>
                <button
                    onClick={() => setIsLogin(true)}
                    className={cn(
                        styles.tabButton,
                        isLogin ? styles.tabActive : styles.tabInactive
                    )}
                >
                    Sign In
                </button>
                <button
                    onClick={() => setIsLogin(false)}
                    className={cn(
                        styles.tabButton,
                        !isLogin ? styles.tabActive : styles.tabInactive
                    )}
                >
                    Sign Up
                </button>
            </div>

            <div className="relative overflow-hidden min-h-[300px]">
                <AnimatePresence mode="wait" initial={false}>
                    <motion.div
                        key={isLogin ? 'login' : 'register'}
                        initial={{ x: 20, opacity: 0 }}
                        animate={{ x: 0, opacity: 1 }}
                        exit={{ x: -20, opacity: 0 }}
                        transition={{ duration: 0.2, ease: 'easeInOut' }}
                        className={styles.formContainer}
                    >
                        {isLogin ? <LoginForm /> : <RegisterForm onSuccess={handleRegisterSuccess} />}
                    </motion.div>
                </AnimatePresence>
            </div>

            <div className="relative my-8">
                <div className="absolute inset-0 flex items-center">
                    <span className="w-full border-t border-purple-500/20"></span>
                </div>
                <div className="relative flex justify-center text-xs uppercase">
                    <span className="bg-[#111827] px-4 text-gray-500 font-medium tracking-wider">
                        or continue with
                    </span>
                </div>
            </div>

            <div className={styles.socialGrid}>
                <Button variant="outline" className="border-purple-500/20 bg-gray-800/40 hover:bg-purple-500/10 text-gray-300">
                    <img
                        src="https://www.svgrepo.com/show/475656/google-color.svg"
                        alt="Google"
                        className="w-4 h-4 mr-2"
                    />
                    Google
                </Button>
                <Button variant="outline" className="border-purple-500/20 bg-gray-800/40 hover:bg-purple-500/10 text-gray-300">
                    <img
                        src="https://www.svgrepo.com/show/512317/github-142.svg"
                        alt="GitHub"
                        className="w-4 h-4 mr-2 invert"
                    />
                    GitHub
                </Button>
            </div>

            <p className="mt-8 text-center text-[11px] text-gray-500 leading-relaxed uppercase tracking-widest opacity-60">
                Secured & encrypted connection
            </p>
        </motion.div>
    )
}