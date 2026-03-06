'use client'

import { useState } from 'react'
import { AnimatePresence } from 'framer-motion'
import { ChatList } from './ChatList'
import { ChatWindow } from './ChatWindow'
import { CreateGroupModal } from './CreateGroupModal'
import { Chat, ChatCategory} from './types'
import {cn} from '@/app/lib/utils'
import {Users, Search} from "lucide-react"

const INITIAL_CHATS: Chat[] = [
    {
        id: 1,
        user: 'Sarah Chen',
        avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150',
        lastMsg: 'The project looks amazing!',
        time: '12:45',
        online: true,
        unread: 2,
        category: 'friends'
    },
    {
        id: 2,
        user: 'Mike Ross',
        avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150',
        lastMsg: 'Are we still meeting at 5?',
        time: '10:20',
        online: true,
        unread: 0,
        category: 'friends'
    },
    {
        id: 3,
        user: 'Emma Watson',
        avatar: 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=150',
        lastMsg: 'Sent you the files.',
        time: 'Yesterday',
        online: false,
        unread: 0,
        category: 'strangers'
    },
]

export default function Messages() {
    const [chats, setChats] = useState<Chat[]>(INITIAL_CHATS)
    const [selectedChat, setSelectedChat] = useState<Chat | null>(null)
    const [isGroupModalOpen, setIsGroupModalOpen] = useState(false)

    ъ
    return (
        <div
            className="flex h-[calc(100vh-4rem)] bg-[#121215]/60 backdrop-blur-3xl border border-white/5 rounded-[2.5rem] overflow-hidden shadow-2xl">
            <div className={cn(
                "w-full lg:w-80 h-full transition-all duration-500",
                selectedChat ? "hidden lg:block" : "block"
            )}>
                <ChatList
                    chats={chats}
                    selectedId={selectedChat?.id ?? null}
                    onSelect={setSelectedChat}
                    onNewGroup={() => setIsGroupModalOpen(true)} activeCategory={'friends'}
                    setActiveCategory={function (cat: ChatCategory): void {
                        throw new Error('Function not implemented.')
                    }}                />
            </div>

            <div className={cn(
                "flex-1 h-full transition-all duration-500",
                !selectedChat ? "hidden lg:flex" : "block"
            )}>
                {selectedChat ? (
                    <ChatWindow
                        chat={selectedChat}
                        onBack={() => setSelectedChat(null)}
                    />
                ) : (
                    <div className="flex-1 flex flex-col items-center justify-center text-gray-500 gap-4">
                        <div className="w-20 h-20 rounded-full bg-white/5 flex items-center justify-center">
                            <Users className="w-8 h-8 text-gray-600" />
                        </div>
                        <p className="font-medium">Выберите чат, чтобы начать общение</p>
                    </div>
                )}
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
        </div>
    )
}