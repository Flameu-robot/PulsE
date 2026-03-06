'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { motion } from 'framer-motion'
import { Mail, Lock, ArrowRight, Loader2 } from 'lucide-react'
import Input from '../components/ui/Input'
import Button from '../components/ui/Button'

export default function LoginForm() {
    const [isLoading, setIsLoading] = useState(false)
    const { register, handleSubmit, formState: { errors } } = useForm()

    const onSubmit = async (data: any) => {
        setIsLoading(true)
        // Имитация запроса к API
        await new Promise(resolve => setTimeout(resolve, 1500))
        console.log('Login success:', data)
        setIsLoading(false)
    }

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <Input
                label="Email"
                type="email"
                icon={<Mail size={18} />}
                placeholder="name@example.com"
                error={errors.email?.message as string}
                {...register('email', {
                    required: 'Email is required',
                    pattern: { value: /^\S+@\S+$/i, message: 'Invalid email' }
                })}
            />

            <div className="space-y-1">
                <Input
                    label="Password"
                    type="password"
                    icon={<Lock size={18} />}
                    placeholder="••••••••"
                    error={errors.password?.message as string}
                    {...register('password', { required: 'Password is required' })}
                />
                <div className="flex justify-end">
                    <button type="button" className="text-xs text-purple-400 hover:text-purple-300 transition-colors">
                        Forgot password?
                    </button>
                </div>
            </div>

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
        </form>
    )
}