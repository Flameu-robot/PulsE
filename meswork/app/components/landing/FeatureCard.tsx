'use client'

import { motion } from 'framer-motion'
import { ReactNode } from 'react'

interface FeatureCardProps {
    icon: ReactNode
    title: string
    description: string
    index: number
}

export default function FeatureCard({ icon, title, description, index }: FeatureCardProps) {
    return (
        <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.4 + index * 0.1, duration: 0.5 }}
            whileHover={{ scale: 1.05, transition: { duration: 0.2 } }}
            className="group relative p-5 rounded-xl bg-white/5 backdrop-blur-sm border border-white/10 hover:bg-white/10 transition-all"
        >
            <div className="absolute inset-0 rounded-xl bg-gradient-to-r from-purple-600/20 to-pink-600/20 opacity-0 group-hover:opacity-100 transition-opacity" />
            <div className="relative z-10">
                <div className="text-purple-400 mb-3 group-hover:scale-110 transition-transform">
                    {icon}
                </div>
                <h3 className="font-semibold mb-1 text-white">{title}</h3>
                <p className="text-xs text-gray-400">{description}</p>
            </div>
        </motion.div>
    )
}