'use client'

import React from 'react'
import { motion } from 'framer-motion'
import Image from 'next/image'
import { Plus } from 'lucide-react'
import { cn } from "@/app/lib/utils"
import { useStoryBar } from './useStoryBar'

export default function StoryBar() {
    const { stories } = useStoryBar()

    return (
        <div className="mb-8">
            <div className="flex gap-4 overflow-x-auto pb-4 scrollbar-thin scrollbar-thumb-purple-500/20 scrollbar-track-transparent">
                {stories.map((story, index) => (
                    <motion.div
                        key={story.id}
                        initial={{ scale: 0, opacity: 0 }}
                        animate={{ scale: 1, opacity: 1 }}
                        transition={{ delay: index * 0.05 }}
                        className="flex flex-col items-center gap-1 min-w-[70px] cursor-pointer group"
                    >
                        <div className={cn(
                            "relative w-16 h-16 rounded-full p-[2px]",
                            story.viewed
                                ? "bg-gray-600"
                                : "bg-gradient-to-r from-purple-400 to-pink-400"
                        )}>
                            <div className="relative w-full h-full rounded-full overflow-hidden border-2 border-gray-900">
                                <Image
                                    src={story.avatar}
                                    alt={story.user}
                                    fill
                                    className="object-cover"
                                />
                            </div>
                            {story.id === '1' && (
                                <div className="absolute -bottom-1 -right-1 w-5 h-5 bg-purple-600 rounded-full border-2 border-gray-900 flex items-center justify-center">
                                    <Plus className="w-3 h-3 text-white" />
                                </div>
                            )}
                        </div>
                        <span className="text-xs text-gray-400 group-hover:text-white transition-colors">
                            {story.user}
                        </span>
                    </motion.div>
                ))}
            </div>
        </div>
    )
}