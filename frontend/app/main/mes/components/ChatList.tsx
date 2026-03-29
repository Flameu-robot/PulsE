'use client'


import { Search, Plus, Hash, Lock, Megaphone } from 'lucide-react'
import { motion } from 'framer-motion'
import { cn } from '@/app/lib/utils'
import { Chat, ChatCategory } from './types'

interface ChatListProps {
    chats: Chat[];
    selectedId: number | null;
    onSelect: (chat: Chat) => void;
    onNewGroup: () => void;
    activeCategory: ChatCategory;
    setActiveCategory: (cat: ChatCategory) => void;
    isLoading?: boolean;
}

const ChatSkeleton = () => (
    <div className="flex items-center gap-4 w-full p-4 rounded-[1.8rem] border border-transparent animate-pulse">
        <div className="w-12 h-12 rounded-2xl bg-white/5 shrink-0" />
        <div className="flex-1">
            <div className="flex justify-between mb-2">
                <div className="h-3 w-24 bg-white/10 rounded-full" />
                <div className="h-2 w-8 bg-white/5 rounded-full" />
            </div>
            <div className="h-2 w-full bg-white/5 rounded-full" />
        </div>
    </div>
)

export function ChatList({ chats, selectedId, onSelect, onNewGroup, activeCategory, setActiveCategory, isLoading }: ChatListProps) {
    return (
        <div className="w-full h-full flex flex-col bg-[#121215]/40">
            <div className="p-6 pb-2">
                <div className="flex items-center justify-between mb-6">
                    <h1 className="text-2xl font-bold tracking-tight text-white">Messages</h1>
                    <button
                        onClick={onNewGroup}
                        className="p-2.5 bg-purple-600 hover:bg-purple-500 text-white rounded-xl transition-all shadow-lg shadow-purple-600/20"
                    >
                        <Plus className="w-5 h-5" />
                    </button>
                </div>

                <div className="relative mb-6">
                    <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
                    <input
                        type="text"
                        placeholder="Search chats..."
                        className="w-full bg-white/5 border border-white/5 rounded-2xl py-3 pl-11 pr-4 text-sm focus:outline-none focus:border-purple-500/50 transition-colors placeholder:text-gray-600 text-white"
                    />
                </div>

                <div className="flex overflow-x-auto gap-2 no-scrollbar pb-4 -mx-2 px-2">
                    {['all', 'unread', 'friends', 'strangers'].map((cat) => (
                        <button
                            key={cat}
                            onClick={() => setActiveCategory(cat as ChatCategory)}
                            className={cn(
                                "px-4 py-2 rounded-xl text-xs font-bold transition-all whitespace-nowrap border capitalize",
                                activeCategory === cat
                                    ? "bg-purple-600 border-purple-500 text-white shadow-lg shadow-purple-600/20"
                                    : "bg-white/5 border-white/5 text-gray-400 hover:bg-white/10"
                            )}
                        >
                            {cat}
                        </button>
                    ))}
                </div>
            </div>

            <div className="flex-1 overflow-y-auto px-3 space-y-2 pb-6 custom-scrollbar">
                {isLoading ? (
                    Array(6).fill(0).map((_, i) => <ChatSkeleton key={i} />)
                ) : chats.length > 0 ? (
                    chats.map((chat, index) => (
                        <motion.button
                            initial={{ opacity: 0, x: -10 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ delay: index * 0.05 }}
                            key={chat.id}
                            onClick={() => onSelect(chat)}
                            className={cn(
                                "flex items-center gap-4 w-full p-4 rounded-[1.8rem] transition-all duration-300 group relative",
                                selectedId === chat.id ? "bg-purple-600/10 border border-purple-500/20" : "hover:bg-white/5 border border-transparent"
                            )}
                        >
                            <div className="relative shrink-0">
                                <img src={chat.avatar} className="w-12 h-12 rounded-2xl object-cover border border-white/10" alt="" />
                                {chat.online && <div className="absolute -bottom-1 -right-1 w-3.5 h-3.5 bg-green-500 border-2 border-[#121215] rounded-full" />}

                                {/* Добавляем значок рупора, если это группа */}
                                {chat.isGroup && (
                                    <div className="absolute -top-1 -right-1 w-5 h-5 bg-purple-600 border-2 border-[#121215] rounded-lg flex items-center justify-center shadow-lg shadow-purple-600/20">
                                        <Megaphone className="w-2.5 h-2.5 text-white" />
                                    </div>
                                )}
                            </div>
                            <div className="flex-1 text-left min-w-0">
                                <div className="flex justify-between items-center mb-1">
                                    <span className="font-bold text-sm truncate text-white">{chat.user}</span>
                                    <span className="text-[10px] text-gray-500">{chat.time}</span>
                                </div>
                                <div className="flex justify-between items-center">
                                    <p className="text-xs text-gray-400 truncate pr-4">{chat.lastMsg}</p>
                                    {chat.unread > 0 && (
                                        <span className="bg-purple-600 text-[10px] font-bold px-1.5 py-0.5 rounded-lg shrink-0 text-white">
                                            {chat.unread}
                                        </span>
                                    )}
                                </div>
                            </div>
                        </motion.button>
                    ))
                ) : (
                    <div className="text-center py-10 text-gray-500 text-sm">No chats found</div>
                )}
            </div>
        </div>
    )
}