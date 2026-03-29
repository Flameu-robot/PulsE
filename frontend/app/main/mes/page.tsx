'use client'

import { useState, useEffect } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { ChatList } from './components/ChatList'
import { ChatWindow } from './components/ChatWindow'
import { CreateGroupModal } from './components/CreateGroupModal'
import { Chat, ChatCategory } from './components/types'
import { cn } from '@/app/lib/utils'
import { Users } from "lucide-react"

const INITIAL_CHATS: Chat[] = [
    { id: 1, user: 'Sarah Chen', avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150', lastMsg: 'The project looks amazing!', time: '12:45', online: true, unread: 2, category: 'friends' },
    { id: 2, user: 'Mike Ross', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150', lastMsg: 'Are we still meeting at 5?', time: '10:20', online: true, unread: 0, category: 'friends' },
    { id: 3, user: 'Emma Watson', avatar: 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=150', lastMsg: 'Sent you the files.', time: 'Yesterday', online: false, unread: 0, category: 'strangers' },
]

export default function Messages() {
    const [isLoading, setIsLoading] = useState(true)
    const [chats, setChats] = useState<Chat[]>(INITIAL_CHATS)
    const [selectedChat, setSelectedChat] = useState<Chat | null>(null)
    const [isGroupModalOpen, setIsGroupModalOpen] = useState(false)
    const [activeCategory, setActiveCategory] = useState<ChatCategory>('all')

    useEffect(() => {
        const timer = setTimeout(() => setIsLoading(false), 1500)
        return () => clearTimeout(timer)
    }, [])

    const handleCreateGroup = (data: { name: string; description: string; isPublic: boolean; avatar: string }) => {
        const newGroup: Chat = {
            id: Date.now(),
            user: data.name,
            avatar: data.avatar,
            description: data.description,
            isPublic: data.isPublic,
            lastMsg: 'Group created',
            time: '12:00',
            online: true,
            unread: 0,
            isGroup: true,
            category: 'friends',
        }
        setChats([newGroup, ...chats])
        setSelectedChat(newGroup)
        setIsGroupModalOpen(false)
    }

    const filteredChats = chats.filter(chat => {
        if (activeCategory === 'all') return true
        if (activeCategory === 'unread') return chat.unread > 0
        return chat.category === activeCategory
    })

    return (
        <motion.div
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, ease: "easeOut" }}
            className="flex h-[calc(100vh-2rem)] bg-[#121215]/60 backdrop-blur-3xl border border-white/5 rounded-[2.5rem] overflow-hidden shadow-2xl m-4"
        >
            <div className={cn(
                "w-full lg:w-96 h-full transition-all duration-500 border-r border-white/5",
                selectedChat ? "hidden lg:block" : "block"
            )}>
                <ChatList
                    chats={filteredChats}
                    selectedId={selectedChat?.id ?? null}
                    onSelect={setSelectedChat}
                    onNewGroup={() => setIsGroupModalOpen(true)}
                    activeCategory={activeCategory}
                    setActiveCategory={setActiveCategory}
                    isLoading={isLoading}
                />
            </div>

            <div className={cn(
                "flex-1 h-full transition-all duration-500",
                !selectedChat ? "hidden lg:flex" : "block"
            )}>
                <AnimatePresence mode="wait">
                    {selectedChat ? (
                        <motion.div
                            key={selectedChat.id}
                            initial={{ opacity: 0, x: 20 }}
                            animate={{ opacity: 1, x: 0 }}
                            exit={{ opacity: 0, x: -20 }}
                            className="w-full h-full"
                        >
                            <ChatWindow
                                chat={selectedChat}
                                onBack={() => setSelectedChat(null)}
                            />
                        </motion.div>
                    ) : (
                        <motion.div
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            className="flex-1 flex flex-col items-center justify-center text-gray-500 gap-4"
                        >
                            <div className="w-20 h-20 rounded-full bg-white/5 flex items-center justify-center animate-pulse">
                                <Users className="w-8 h-8 text-gray-600" />
                            </div>
                            <p className="font-medium tracking-wide">Выберите чат, чтобы начать общение</p>
                        </motion.div>
                    )}
                </AnimatePresence>
            </div>

            <AnimatePresence>
                {isGroupModalOpen && (
                    <CreateGroupModal
                        isOpen={isGroupModalOpen}
                        onClose={() => setIsGroupModalOpen(false)}
                        onCreate={handleCreateGroup}
                    />
                )}
            </AnimatePresence>
        </motion.div>
    )
}