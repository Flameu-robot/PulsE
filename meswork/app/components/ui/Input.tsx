'use client'

import { InputHTMLAttributes, forwardRef, ReactNode, useState } from 'react'
import { cn } from '@/app/lib/utils'
import { Eye, EyeOff } from 'lucide-react'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
    label?: string
    icon?: ReactNode
    error?: string
}

const Input = forwardRef<HTMLInputElement, InputProps>(
    ({ className, label, icon, error, type, ...props }, ref) => {
        const [showPassword, setShowPassword] = useState(false)
        const isPassword = type === 'password'
        const inputType = isPassword ? (showPassword ? 'text' : 'password') : type

        return (
            <div className="space-y-1.5 w-full text-left">
                {label && (
                    <label className="block text-sm font-medium text-gray-300 ml-1">
                        {label}
                    </label>
                )}
                <div className="relative group">
                    {icon && (
                        <div className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500 group-focus-within:text-purple-400 transition-colors pointer-events-none">
                            {icon}
                        </div>
                    )}
                    <input
                        type={inputType}
                        className={cn(
                            'w-full rounded-xl border border-purple-500/20 bg-gray-800/50 px-4 py-2.5 text-white transition-all duration-200 focus:border-purple-500/50 focus:ring-4 focus:ring-purple-500/10 placeholder:text-gray-600',
                            icon && 'pl-11',
                            isPassword && 'pr-11',
                            error && 'border-red-500/50 focus:border-red-500/50 focus:ring-red-500/10',
                            className
                        )}
                        ref={ref}
                        {...props}
                    />
                    {isPassword && (
                        <button
                            type="button"
                            onClick={() => setShowPassword(!showPassword)}
                            className="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-300 transition-colors"
                        >
                            {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                        </button>
                    )}
                </div>
                {error && (
                    <p className="text-xs text-red-400 mt-1 ml-1 animate-in fade-in slide-in-from-top-1">
                        {error}
                    </p>
                )}
            </div>
        )
    }
)

Input.displayName = 'Input'
export default Input