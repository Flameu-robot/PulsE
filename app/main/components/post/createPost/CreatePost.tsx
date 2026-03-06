'use client'

import React from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Smile, Send, Paperclip, X, FileText } from 'lucide-react'
import Button from '../../../../components/ui/Button'
import { cn } from '@/app/lib/utils'
import { CreatePostProps } from './types'
import { useCreatePost, EMOJIS } from './useCreatePost'

export default function CreatePost({ onPublish }: CreatePostProps) {
    const {
        isExpanded,
        setIsExpanded,
        text,
        setText,
        attachments,
        showEmojis,
        setShowEmojis,
        fileInputRef,
        handleFileChange,
        removeAttachment,
        addEmoji,
        handlePublish
    } = useCreatePost(onPublish)

    return (
        <motion.div
            layout
            className="relative bg-[#121215]/80 backdrop-blur-2xl border border-white/5 rounded-[2.5rem] p-5 shadow-2xl overflow-visible"
        >
            <div className="absolute inset-x-0 top-0 h-[2px] bg-gradient-to-r from-transparent via-purple-500/40 to-transparent" />

            <div className="flex gap-4">
                <div className="relative shrink-0 w-12 h-12">
                    <div className="w-12 h-12 rounded-2xl overflow-hidden border border-white/10 relative z-10">
                        <img
                            src="https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100"
                            className="w-full h-full object-cover"
                            alt="Avatar"
                        />
                    </div>
                </div>

                <div className="flex-1 flex flex-col gap-3">
                    <textarea
                        value={text}
                        onChange={(e) => setText(e.target.value)}
                        onFocus={() => setIsExpanded(true)}
                        placeholder="What's on your mind?"
                        className="w-full bg-transparent border-none outline-none text-white placeholder:text-gray-500 resize-none py-3 min-h-[50px] text-lg leading-relaxed"
                    />

                    <AnimatePresence>
                        {attachments.length > 0 && (
                            <motion.div
                                initial={{ opacity: 0, y: 10 }}
                                animate={{ opacity: 1, y: 0 }}
                                exit={{ opacity: 0, y: 10 }}
                                className="flex flex-wrap gap-3 mb-2"
                            >
                                {attachments.map((file, i) => (
                                    <div key={i} className="relative group w-24 h-24 rounded-2xl overflow-hidden border border-white/10 bg-white/5">
                                        {file.type.startsWith('image/') ? (
                                            <img src={file.url} className="w-full h-full object-cover" />
                                        ) : (
                                            <div className="w-full h-full flex flex-col items-center justify-center gap-1">
                                                <FileText className="w-8 h-8 text-purple-400" />
                                                <span className="text-[10px] text-gray-400 px-2 truncate w-full text-center">
                                                    {file.file.name}
                                                </span>
                                            </div>
                                        )}
                                        <button
                                            onClick={() => removeAttachment(i)}
                                            className="absolute top-1 right-1 p-1 bg-black/60 rounded-lg text-white opacity-0 group-hover:opacity-100 transition-opacity"
                                        >
                                            <X className="w-3 h-3" />
                                        </button>
                                    </div>
                                ))}
                            </motion.div>
                        )}
                    </AnimatePresence>
                </div>
            </div>

            <div className="flex items-center justify-between mt-4 pt-4 border-t border-white/5">
                <div className="flex items-center gap-2 relative">
                    <input
                        type="file"
                        ref={fileInputRef}
                        onChange={handleFileChange}
                        multiple
                        className="hidden"
                    />
                    <button
                        onClick={() => fileInputRef.current?.click()}
                        className="p-3 rounded-2xl text-gray-400 hover:text-purple-400 hover:bg-purple-500/10 transition-all"
                    >
                        <Paperclip className="w-5 h-5" />
                    </button>

                    <div className="relative">
                        <button
                            onClick={() => setShowEmojis(!showEmojis)}
                            className={cn(
                                "p-3 rounded-2xl transition-all",
                                showEmojis ? "text-purple-400 bg-purple-500/10" : "text-gray-400 hover:text-yellow-400 hover:bg-yellow-400/10"
                            )}
                        >
                            <Smile className="w-5 h-5" />
                        </button>

                        <AnimatePresence>
                            {showEmojis && (
                                <motion.div
                                    initial={{ opacity: 0, scale: 0.9, y: 10 }}
                                    animate={{ opacity: 1, scale: 1, y: 0 }}
                                    exit={{ opacity: 0, scale: 0.9, y: 10 }}
                                    className="absolute bottom-full left-0 mb-4 p-2 bg-[#1a1a1e] border border-white/10 rounded-2xl shadow-2xl flex gap-1 z-[100]"
                                >
                                    {EMOJIS.map(emoji => (
                                        <button
                                            key={emoji}
                                            onClick={() => addEmoji(emoji)}
                                            className="p-2 hover:bg-white/5 rounded-xl transition-colors text-xl"
                                        >
                                            {emoji}
                                        </button>
                                    ))}
                                </motion.div>
                            )}
                        </AnimatePresence>
                    </div>
                </div>

                <Button
                    onClick={handlePublish}
                    className={cn(
                        "relative overflow-hidden rounded-2xl px-8 h-12 transition-all duration-500 border-none outline-none shadow-none cursor-pointer",
                        text.trim() || attachments.length > 0
                            ? "bg-gradient-to-r from-purple-600 via-pink-600 to-purple-600 bg-[length:200%_auto] hover:bg-right shadow-[0_10px_25px_-8px_rgba(168,85,247,0.6)] text-white"
                            : "bg-gray-800/50 text-gray-500 cursor-not-allowed opacity-50"
                    )}
                >
                    <div className="relative z-10 flex items-center gap-2">
                        <span className="font-bold tracking-wide">Publish</span>
                        <Send className="w-4 h-4" />
                    </div>
                </Button>
            </div>
        </motion.div>
    )
}