'use client'

import { useState, useRef } from 'react'
import { motion } from 'framer-motion'
import { X, Users, Camera, Globe, Lock, Copy, Check } from 'lucide-react'
import { cn } from '@/app/lib/utils'

interface CreateGroupModalProps {
    isOpen: boolean;
    onClose: () => void;
    onCreate: (data: { name: string; description: string; isPublic: boolean; avatar: string }) => void;
}

export function CreateGroupModal({ isOpen, onClose, onCreate }: CreateGroupModalProps) {
    const [name, setName] = useState('')
    const [description, setDescription] = useState('')
    const [isPublic, setIsPublic] = useState(true)
    const [avatar, setAvatar] = useState('https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=150')
    const [copied, setCopied] = useState(false)

    const inviteLink = `nexus.chat/join/${Math.random().toString(36).substring(7)}`

    const handleCopy = () => {
        navigator.clipboard.writeText(inviteLink)
        setCopied(true)
        setTimeout(() => setCopied(false), 2000)
    }

    if (!isOpen) return null

    return (
        <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md"
        >
            <motion.div
                initial={{ scale: 0.95, y: 20 }}
                animate={{ scale: 1, y: 0 }}
                className="w-full max-w-xl bg-[#16161a] border border-white/10 rounded-[2.5rem] overflow-hidden shadow-2xl flex flex-col max-h-[90vh]"
            >
                <div className="p-6 border-b border-white/5 flex justify-between items-center bg-white/5">
                    <h2 className="text-xl font-bold flex items-center gap-3">
                        <div className="p-2 bg-purple-600/20 rounded-xl">
                            <Users className="w-5 h-5 text-purple-400" />
                        </div>
                        New Group
                    </h2>
                    <button onClick={onClose} className="p-2 hover:bg-white/5 rounded-xl transition-colors">
                        <X className="w-5 h-5" />
                    </button>
                </div>

                <div className="flex-1 overflow-y-auto p-8 space-y-8 custom-scrollbar">
                    <div className="flex flex-col items-center gap-4">
                        <div className="relative group">
                            <img src={avatar} className="w-24 h-24 rounded-[2rem] object-cover border-2 border-purple-500/30" alt="" />
                            <button className="absolute inset-0 bg-black/40 rounded-[2rem] opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                                <Camera className="w-6 h-6 text-white" />
                            </button>
                        </div>
                        <p className="text-[10px] font-bold text-gray-500 uppercase tracking-widest">Set Group Avatar</p>
                    </div>

                    <div className="space-y-4">
                        <div className="space-y-2">
                            <label className="text-xs font-bold text-gray-400 ml-2 uppercase">Group Name</label>
                            <input
                                type="text"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                placeholder="Design Team"
                                className="w-full bg-white/5 border border-white/5 rounded-2xl py-4 px-6 text-sm focus:outline-none focus:border-purple-500/50 transition-colors"
                            />
                        </div>

                        <div className="space-y-2">
                            <label className="text-xs font-bold text-gray-400 ml-2 uppercase">Description</label>
                            <textarea
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                placeholder="What's this group about?"
                                rows={3}
                                className="w-full bg-white/5 border border-white/5 rounded-2xl py-4 px-6 text-sm focus:outline-none focus:border-purple-500/50 transition-colors resize-none"
                            />
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <button
                            onClick={() => setIsPublic(true)}
                            className={cn(
                                "p-4 rounded-2xl border transition-all text-left",
                                isPublic ? "bg-purple-600/10 border-purple-500/50" : "bg-white/5 border-transparent opacity-60 hover:opacity-100"
                            )}
                        >
                            <Globe className={cn("w-5 h-5 mb-2", isPublic ? "text-purple-400" : "text-gray-400")} />
                            <div className="font-bold text-sm">Public</div>
                            <div className="text-[10px] text-gray-500">Anyone can join</div>
                        </button>
                        <button
                            onClick={() => setIsPublic(false)}
                            className={cn(
                                "p-4 rounded-2xl border transition-all text-left",
                                !isPublic ? "bg-purple-600/10 border-purple-500/50" : "bg-white/5 border-transparent opacity-60 hover:opacity-100"
                            )}
                        >
                            <Lock className={cn("w-5 h-5 mb-2", !isPublic ? "text-purple-400" : "text-gray-400")} />
                            <div className="font-bold text-sm">Private</div>
                            <div className="text-[10px] text-gray-500">Only by invitation</div>
                        </button>
                    </div>

                    <div className="bg-black/20 border border-white/5 rounded-2xl p-4">
                        <label className="text-[10px] font-bold text-gray-500 uppercase block mb-3">Invitation Link</label>
                        <div className="flex gap-2">
                            <input
                                readOnly
                                value={inviteLink}
                                className="flex-1 bg-transparent border-none text-xs text-purple-400 focus:outline-none truncate"
                            />
                            <button
                                onClick={handleCopy}
                                className="shrink-0 p-2 hover:bg-white/5 rounded-lg transition-colors text-gray-400"
                            >
                                {copied ? <Check className="w-4 h-4 text-green-500" /> : <Copy className="w-4 h-4" />}
                            </button>
                        </div>
                    </div>
                </div>

                <div className="p-8 pt-0">
                    <button
                        onClick={() => {
                            if(name.trim()) {
                                onCreate({ name, description, isPublic, avatar })
                            }
                        }}
                        disabled={!name.trim()}
                        className="w-full bg-purple-600 hover:bg-purple-500 disabled:opacity-50 py-4 rounded-2xl font-bold transition-all shadow-lg shadow-purple-600/20"
                    >
                        Create Group
                    </button>
                </div>
            </motion.div>
        </motion.div>
    )
}