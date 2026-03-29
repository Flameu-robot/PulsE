'use client'

import { useForm } from 'react-hook-form'
import { motion } from 'framer-motion'
import { User, Lock, ArrowRight, Loader2 } from 'lucide-react'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import { LoginFormData } from './types'
import { useLogin } from './useLogin'

export default function LoginForm() {
    const { register, handleSubmit, formState: { errors } } = useForm<LoginFormData>()
    const { loginUser, isLoading, serverError } = useLogin()

    const onSubmit = (data: LoginFormData) => {
        loginUser(data)
    }

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            {serverError && (
                <div className="p-3 text-sm text-red-400 bg-red-500/10 border border-red-500/20 rounded-lg">
                    {serverError}
                </div>
            )}

            <Input
                label="Login"
                type="text"
                icon={<User size={18} className="text-purple-400" />}
                placeholder="Enter your login"
                error={errors.login?.message}
                {...register('login', {
                    required: 'Login is required',
                })}
            />

            <div className="space-y-1">
                <Input
                    label="Password"
                    type="password"
                    icon={<Lock size={18} className="text-purple-400" />}
                    placeholder="••••••••"
                    error={errors.password?.message}
                    {...register('password', {
                        required: 'Password is required'
                    })}
                />
                <div className="flex justify-end">
                    <button type="button" className="text-xs text-purple-400 hover:text-purple-300 transition-colors">
                        Forgot password?
                    </button>
                </div>
            </div>

            <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                <Button type="submit" className="w-full group" disabled={isLoading}>
                    {isLoading ? (
                        <Loader2 className="w-5 h-5 animate-spin" />
                    ) : (
                        <>
                            Sign In
                            <ArrowRight className="w-4 h-4 ml-2 group-hover:translate-x-1 transition-transform" />
                        </>
                    )}
                </Button>
            </motion.div>
        </form>
    )
}