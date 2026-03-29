'use client'

import React from 'react'
import { motion } from 'framer-motion'
import Image from 'next/image'
import { Users, MessageCircle, MoreHorizontal } from 'lucide-react'
import { useRightPanel } from './useRightPanel'

export default function RightPanel() {
    const { friends, topics, onlineCount } = useRightPanel()

    return (
        <motion.aside
            initial={{ x: 100, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ duration: 0.5 }}
            className="w-80 h-full bg-gray-900/80 backdrop-blur-md border-l border-purple-500/20 p-6 hidden xl:block overflow-y-auto scrollbar-thin scrollbar-thumb-purple-500/20 scrollbar-track-transparent"
        >
            <div className="mb-8">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-lg font-semibold text-white flex items-center gap-2">
                        <Users className="w-5 h-5 text-purple-400" />
                        Active Friends
                    </h2>
                    <span className="text-xs text-purple-400">{onlineCount} online</span>
                </div>

                <div className="space-y-3">
                    {friends.map((friend, index) => (
                        <motion.div
                            key={friend.id}
                            initial={{ opacity: 0, x: 20 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ delay: index * 0.1 }}
                            className="flex items-center justify-between group cursor-pointer"
                        >
                            <div className="flex items-center gap-3">
                                <div className="relative">
                                    <div className="relative w-10 h-10 rounded-full overflow-hidden">
                                        <Image
                                            src={friend.avatar}
                                            alt={friend.name}
                                            fill
                                            className="object-cover"
                                        />
                                    </div>
                                    {friend.online && (
                                        <div className="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-green-500 rounded-full border-2 border-gray-900" />
                                    )}
                                </div>
                                <div>
                                    <p className="text-sm font-medium text-white">{friend.name}</p>
                                    <p className="text-xs text-gray-500">
                                        {friend.online ? 'Online' : friend.lastActive}
                                    </p>
                                </div>
                            </div>

                            <button className="opacity-0 group-hover:opacity-100 transition-opacity p-2 hover:bg-purple-500/10 rounded-lg">
                                <MessageCircle className="w-4 h-4 text-purple-400" />
                            </button>
                        </motion.div>
                    ))}
                </div>
            </div>

            <div>
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-lg font-semibold text-white">Trending Topics</h2>
                    <MoreHorizontal className="w-5 h-5 text-gray-500" />
                </div>

                <div className="space-y-4">
                    {topics.map((topic, index) => (
                        <motion.div
                            key={topic}
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            transition={{ delay: 0.3 + index * 0.1 }}
                            className="group cursor-pointer"
                        >
                            <p className="text-sm text-purple-400 group-hover:text-purple-300 transition-colors">
                                {topic}
                            </p>
                            <p className="text-xs text-gray-600">
                                {Math.floor(Math.random() * 50 + 10)}k posts
                            </p>
                        </motion.div>
                    ))}
                </div>
            </div>
        </motion.aside>
    )
}