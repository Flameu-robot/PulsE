'use client'

import { ButtonHTMLAttributes, forwardRef } from 'react'
import { motion } from 'framer-motion'
import { cn } from '@/app/lib/utils'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: 'primary' | 'secondary' | 'outline' | 'ghost'
    size?: 'sm' | 'md' | 'lg'
}

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
    ({ className, variant = 'primary', size = 'md', children, ...props }, ref) => {
        const base = 'inline-flex items-center justify-center font-medium transition-all duration-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-60 disabled:pointer-events-none'

        const variants = {
            primary:
                'bg-gradient-to-r from-purple-600 to-pink-600 text-white hover:from-purple-700 hover:to-pink-700 focus:ring-purple-500/50 shadow-sm hover:shadow-md active:scale-[0.98]',
            secondary:
                'bg-gray-200 text-gray-900 hover:bg-gray-300 focus:ring-gray-500/50',
            outline:
                'border border-gray-300 text-gray-700 hover:bg-gray-50 hover:border-gray-400 focus:ring-gray-500/50',
            ghost:
                'text-gray-700 hover:bg-gray-100/80 focus:ring-gray-500/50',
        }

        const sizes = {
            sm: 'px-4 py-2 text-sm',
            md: 'px-5 py-2.5 text-base',
            lg: 'px-6 py-3 text-lg',
        }

        return (
            <motion.button
                ref={ref}
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                className={cn(base, variants[variant], sizes[size], className)}
                {...props}
            >
                {children}
            </motion.button>
        )
    }
)

Button.displayName = 'Button'

export default Button