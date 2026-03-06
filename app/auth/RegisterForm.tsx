'use client'

import { useForm } from 'react-hook-form'
import { motion } from 'framer-motion'
import { User, Mail, Lock, ArrowRight } from 'lucide-react'
import Input from '../components/ui/Input'
import Button from '../components/ui/Button'

interface RegisterFormData {
    username: string
    email: string
    password: string
    confirmPassword: string
}

export default function RegisterForm() {
    const { register, handleSubmit, watch, formState: { errors } } = useForm<RegisterFormData>()
    const password = watch('password')

    const onSubmit = (data: RegisterFormData) => {
        console.log('Register data:', data)
    }

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <Input
                label="Username"
                type="text"
                icon={<User className="w-5 h-5 text-purple-400" />}
                placeholder="Choose a username"
                error={errors.username?.message}
                {...register('username', {
                    required: 'Username is required',
                    minLength: {
                        value: 3,
                        message: 'Username must be at least 3 characters'
                    }
                })}
            />

            <Input
                label="Email"
                type="email"
                icon={<Mail className="w-5 h-5 text-purple-400" />}
                placeholder="Enter your email"
                error={errors.email?.message}
                {...register('email', {
                    required: 'Email is required',
                    pattern: {
                        value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                        message: 'Invalid email address'
                    }
                })}
            />

            <Input
                label="Password"
                type="password"
                icon={<Lock className="w-5 h-5 text-purple-400" />}
                placeholder="Create a password"
                error={errors.password?.message}
                {...register('password', {
                    required: 'Password is required',
                    minLength: {
                        value: 6,
                        message: 'Password must be at least 6 characters'
                    },
                    pattern: {
                        value: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,}$/,
                        message: 'Password must contain at least one letter and one number'
                    }
                })}
            />

            <Input
                label="Confirm Password"
                type="password"
                icon={<Lock className="w-5 h-5 text-purple-400" />}
                placeholder="Confirm your password"
                error={errors.confirmPassword?.message}
                {...register('confirmPassword', {
                    required: 'Please confirm your password',
                    validate: value => value === password || 'Passwords do not match'
                })}
            />

            <div className="flex items-center gap-2">
                <input
                    type="checkbox"
                    id="terms"
                    className="w-4 h-4 rounded border-purple-500/30 bg-gray-800 text-purple-600 focus:ring-purple-500 focus:ring-offset-gray-900"
                    required
                />
                <label htmlFor="terms" className="text-sm text-gray-400">
                    I agree to the{' '}
                    <button type="button" className="text-purple-400 hover:text-purple-300">
                        Terms of Service
                    </button>{' '}
                    and{' '}
                    <button type="button" className="text-purple-400 hover:text-purple-300">
                        Privacy Policy
                    </button>
                </label>
            </div>

            <motion.div
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
            >
                <Button type="submit" className="w-full group">
                    Create Account
                    <ArrowRight className="w-4 h-4 ml-2 group-hover:translate-x-1 transition-transform" />
                </Button>
            </motion.div>
        </form>
    )
}