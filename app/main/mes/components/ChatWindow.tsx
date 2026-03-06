'use client'

import { useState } from 'react'
import { Phone, Video, MoreVertical, Paperclip, Smile, Send, CheckCheck, ChevronLeft } from 'lucide-react'
import { cn } from '@/app/lib/utils'
import { Chat } from './types'

interface ChatWindowProps {
    chat: Chat;
    onBack: () => void;
}

export function ChatWindow({ chat, onBack }: ChatWindowProps) {
    const [message, setMessage] = useState('')

    return (
        <div className="flex-1 flex flex-col relative bg-gradient-to-b from-transparent to-purple-900/5 h-full">
            <div className="p-4 lg:p-6 border-b border-white/5 flex items-center justify-between">
                <div className="flex items-center gap-3 lg:gap-4">
                    <button onClick={onBack} className="lg:hidden p-2 -ml-2 hover:bg-white/5 rounded-xl transition-colors">
                        <ChevronLeft className="w-6 h-6" />
                    </button>
                    <img src={chat.avatar} className="w-10 h-10 lg:w-11 lg:h-11 rounded-2xl object-cover" alt="" />
                    <div>
                        <h3 className="font-bold text-sm">{chat.user}</h3>
                        <span className="text-[11px] text-green-500 flex items-center gap-1">
                            <div className="w-1.5 h-1.5 bg-green-500 rounded-full animate-pulse" />
                            {chat.isGroup ? 'Active now' : 'Online'}
                        </span>
                    </div>
                </div>
                <div className="flex items-center gap-1 lg:gap-2">
                    <button className="hidden sm:block p-2.5 rounded-xl hover:bg-white/5 text-gray-400 transition-colors"><Phone className="w-5 h-5" /></button>
                    <button className="hidden sm:block p-2.5 rounded-xl hover:bg-white/5 text-gray-400 transition-colors"><Video className="w-5 h-5" /></button>
                    <button className="p-2.5 rounded-xl hover:bg-white/5 text-gray-400 transition-colors"><MoreVertical className="w-5 h-5" /></button>
                </div>
            </div>

            <div className="flex-1 overflow-y-auto p-4 lg:p-8 space-y-6">
                <div className="flex flex-col items-center mb-8">
                    <div className="bg-white/5 px-4 py-1.5 rounded-full text-[10px] font-bold text-gray-500 uppercase tracking-widest">Today</div>
                </div>

                <div className="flex gap-4 max-w-[90%] lg:max-w-[80%]">
                    <img src={chat.avatar} className="w-8 h-8 rounded-xl shrink-0 mt-auto" alt="" />
                    <div className="bg-white/5 border border-white/5 p-4 rounded-2xl rounded-bl-none">
                        <p className="text-sm leading-relaxed">{chat.lastMsg}</p>
                        <span className="text-[10px] text-gray-500 mt-2 block">{chat.time} PM</span>
                    </div>
                </div>

                <div className="flex flex-row-reverse gap-4 max-w-[90%] lg:max-w-[80%] ml-auto">
                    <div className="bg-purple-600 p-4 rounded-2xl rounded-br-none shadow-lg shadow-purple-600/20">
                        <p className="text-sm leading-relaxed text-white font-medium">Hey! I'm checking the latest updates. Everything seems to be working perfectly! 🚀</p>
                        <div className="flex items-center justify-end gap-1 mt-2">
                            <span className="text-[10px] text-purple-200">12:48 PM</span>
                            <CheckCheck className="w-3 h-3 text-purple-200" />
                        </div>
                    </div>
                </div>
            </div>

            <div className="p-4 lg:p-6">
                <div className="bg-[#1a1a1e] border border-white/5 rounded-[1.8rem] p-2 flex items-center gap-1 lg:gap-2 focus-within:border-purple-500/30 transition-all duration-300">
                    <button className="p-2 lg:p-3 text-gray-500 hover:text-purple-400 transition-colors"><Paperclip className="w-5 h-5" /></button>
                    <input
                        type="text"
                        value={message}
                        onChange={(e) => setMessage(e.target.value)}
                        placeholder={`Message ${chat.user}...`}
                        className="flex-1 bg-transparent border-none focus:outline-none text-sm px-2"
                    />
                    <button className="hidden sm:block p-3 text-gray-500 hover:text-yellow-400 transition-colors"><Smile className="w-5 h-5" /></button>
                    <button
                        disabled={!message.trim()}
                        className={cn(
                            "p-3 rounded-2xl transition-all duration-300",
                            message.trim() ? "bg-purple-600 text-white shadow-lg shadow-purple-600/30 scale-100" : "bg-gray-800 text-gray-500 scale-95 opacity-50"
                        )}
                    >
                        <Send className="w-5 h-5" />
                    </button>
                </div>
            </div>
        </div>
    )
}