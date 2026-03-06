'use client'

import React from 'react'
import { createPortal } from 'react-dom'
import {
    Heart,
    MessageCircle,
    Share2,
    MoreHorizontal,
    Maximize2,
    X,
    Bookmark,
    CheckCircle2,
    Trash2,
    MessageSquareOff,
    Copy,
    Send
} from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import { cn } from '@/app/lib/utils'
import CommentsSection from '../../commentsSections/CommentsSection'
import { PostProps } from './types'
import { usePostCard, MOCK_FRIENDS } from './usePostCard'

export default function PostCard({ post, isOwnPost, onDelete }: PostProps) {
    const {
        isLiked,
        likesCount,
        isSaved,
        setIsSaved,
        isImageOpen,
        setIsImageOpen,
        showMoreMenu,
        setShowMoreMenu,
        showShareModal,
        setShowShareModal,
        commentsDisabled,
        showComments,
        setShowComments,
        mounted,
        menuRef,
        handleCopyLink,
        handleLikeToggle,
        handleToggleCommentsDisabled,
        handleDelete
    } = usePostCard(post, onDelete)

    const modalContent = (
        <AnimatePresence>
            {isImageOpen && (
                <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="fixed inset-0 z-[100] bg-black/95 backdrop-blur-xl flex items-center justify-center p-4"
                    onClick={() => setIsImageOpen(false)}
                >
                    <button className="absolute top-6 right-6 p-3 bg-white/10 rounded-2xl text-white hover:bg-white/20 transition-colors">
                        <X />
                    </button>
                    <motion.img
                        initial={{ scale: 0.9 }}
                        animate={{ scale: 1 }}
                        src={post.image}
                        className="max-w-full max-h-full rounded-3xl object-contain shadow-2xl"
                    />
                </motion.div>
            )}

            {showShareModal && (
                <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="fixed inset-0 z-[100] bg-black/60 backdrop-blur-md flex items-center justify-center p-4"
                >
                    <motion.div
                        initial={{ scale: 0.9, y: 20 }}
                        animate={{ scale: 1, y: 0 }}
                        className="bg-[#121215] border border-white/10 w-full max-w-md rounded-[2.5rem] overflow-hidden shadow-2xl"
                    >
                        <div className="p-6 border-b border-white/5 flex justify-between items-center bg-white/[0.02]">
                            <h3 className="text-xl font-bold text-white tracking-tight">Send to friends</h3>
                            <button onClick={() => setShowShareModal(false)} className="p-2 hover:bg-white/5 rounded-xl text-white transition-colors">
                                <X className="w-5 h-5" />
                            </button>
                        </div>
                        <div className="p-4">
                            <div className="relative mb-4">
                                <input
                                    type="text"
                                    placeholder="Search friends..."
                                    className="w-full bg-white/5 border border-white/5 rounded-2xl py-3 pl-11 pr-4 text-white focus:outline-none focus:border-purple-500/50 transition-all"
                                />
                            </div>
                            <div className="space-y-2 max-h-[300px] overflow-y-auto pr-2 custom-scrollbar">
                                {MOCK_FRIENDS.map(friend => (
                                    <div key={friend.id} className="flex items-center justify-between p-3 hover:bg-white/5 rounded-2xl transition-colors group">
                                        <div className="flex items-center gap-3">
                                            <img src={friend.avatar} className="w-11 h-11 rounded-xl object-cover shadow-md" />
                                            <span className="font-bold text-white tracking-tight">{friend.name}</span>
                                        </div>
                                        <button className="bg-purple-600 p-2.5 rounded-xl opacity-0 group-hover:opacity-100 transition-all text-white shadow-lg shadow-purple-600/20 active:scale-95">
                                            <Send className="w-4 h-4" />
                                        </button>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    )

    return (
        <motion.div layout className="bg-[#121215]/60 backdrop-blur-xl border border-white/5 rounded-[2.5rem] p-6 shadow-xl relative overflow-hidden group/card">
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-4">
                    <div className="relative">
                        <div className="w-14 h-14 rounded-2xl overflow-hidden border-2 border-purple-500/20 p-0.5 group-hover:border-purple-500/40 transition-colors">
                            <img src={post.user.avatar} className="w-full h-full rounded-[0.9rem] object-cover" />
                        </div>
                        {post.user.online && (
                            <div className="absolute -bottom-1 -right-1 w-4 h-4 bg-green-500 border-[3px] border-[#121215] rounded-full shadow-lg" />
                        )}
                    </div>
                    <div>
                        <div className="flex items-center gap-1.5">
                            <h4 className="font-black text-white tracking-tight text-lg">{post.user.name}</h4>
                            {post.user.isVerified && <CheckCircle2 className="w-4 h-4 text-purple-400 fill-purple-400/10" />}
                        </div>
                        <p className="text-xs font-bold text-gray-500 uppercase tracking-widest">{post.user.username} · {post.time}</p>
                    </div>
                </div>

                <div className="relative" ref={menuRef}>
                    <button
                        onClick={() => setShowMoreMenu(!showMoreMenu)}
                        className="p-3 rounded-2xl text-gray-500 hover:bg-white/5 hover:text-white transition-all active:scale-90"
                    >
                        <MoreHorizontal className="w-6 h-6" />
                    </button>

                    <AnimatePresence>
                        {showMoreMenu && (
                            <motion.div
                                initial={{ opacity: 0, scale: 0.95, y: 10 }}
                                animate={{ opacity: 1, scale: 1, y: 0 }}
                                exit={{ opacity: 0, scale: 0.95, y: 10 }}
                                className="absolute right-0 mt-2 w-64 bg-[#1a1a1e]/95 backdrop-blur-xl border border-white/10 rounded-2xl shadow-2xl z-50 py-2 overflow-hidden"
                            >
                                <button
                                    onClick={handleCopyLink}
                                    className="w-full flex items-center gap-3 px-5 py-3.5 text-sm text-gray-300 hover:bg-white/5 transition-all text-left"
                                >
                                    <Copy className="w-4 h-4 text-gray-500" /> Copy link
                                </button>

                                {isOwnPost && (
                                    <>
                                        <button
                                            onClick={handleToggleCommentsDisabled}
                                            className="w-full flex items-center gap-3 px-5 py-3.5 text-sm text-gray-300 hover:bg-white/5 transition-all text-left"
                                        >
                                            <MessageSquareOff className="w-4 h-4 text-gray-500" />
                                            {commentsDisabled ? 'Enable comments' : 'Disable comments'}
                                        </button>

                                        <div className="my-1 mx-2 h-px bg-white/5" />

                                        <button
                                            onClick={handleDelete}
                                            className="w-full flex items-center gap-3 px-5 py-3.5 text-sm text-red-400 hover:bg-red-500/10 transition-all text-left font-bold"
                                        >
                                            <Trash2 className="w-4 h-4" /> Delete post
                                        </button>
                                    </>
                                )}
                            </motion.div>
                        )}
                    </AnimatePresence>
                </div>
            </div>

            <div className="space-y-5">
                <p className="text-gray-100 text-[17px] leading-relaxed font-medium">{post.content}</p>

                {post.image && (
                    <div
                        className="relative rounded-[2rem] overflow-hidden border border-white/5 cursor-pointer group/img shadow-2xl"
                        onClick={() => setIsImageOpen(true)}
                    >
                        <img
                            src={post.image}
                            className="w-full max-h-[520px] object-cover transition-transform duration-1000 group-hover/img:scale-[1.05]"
                        />
                        <div className="absolute inset-0 bg-black/20 opacity-0 group-hover/img:opacity-100 transition-opacity flex items-center justify-center backdrop-blur-[2px]">
                            <div className="bg-white/10 p-4 rounded-full backdrop-blur-md border border-white/20">
                                <Maximize2 className="w-8 h-8 text-white" />
                            </div>
                        </div>
                    </div>
                )}
            </div>

            <div className="flex items-center justify-between mt-8 pt-6 border-t border-white/5">
                <div className="flex items-center gap-2">
                    <button
                        onClick={handleLikeToggle}
                        className={cn(
                            "flex items-center gap-2.5 px-6 py-3 rounded-2xl transition-all active:scale-90 shadow-sm",
                            isLiked
                                ? "bg-red-500/10 text-red-400 border border-red-500/20"
                                : "text-gray-400 hover:bg-white/5 border border-transparent"
                        )}
                    >
                        <Heart className={cn("w-5 h-5 transition-transform", isLiked && "fill-red-500 scale-110")} />
                        <span className="font-bold text-sm tracking-tight">{likesCount}</span>
                    </button>

                    {!commentsDisabled && (
                        <button
                            onClick={() => setShowComments(!showComments)}
                            className={cn(
                                "flex items-center gap-2.5 px-6 py-3 rounded-2xl transition-all active:scale-90 border shadow-sm",
                                showComments
                                    ? "bg-purple-500/10 text-purple-400 border-purple-500/20"
                                    : "text-gray-400 hover:bg-white/5 border-transparent hover:text-white"
                            )}
                        >
                            <MessageCircle className={cn("w-5 h-5", showComments && "fill-purple-400/10")} />
                            <span className="font-bold text-sm tracking-tight">{post.comments}</span>
                        </button>
                    )}

                    <button
                        onClick={() => setShowShareModal(true)}
                        className="p-3 rounded-2xl text-gray-400 hover:bg-white/5 hover:text-blue-400 transition-all active:scale-90"
                    >
                        <Share2 className="w-5 h-5" />
                    </button>
                </div>

                <button
                    onClick={() => setIsSaved(!isSaved)}
                    className={cn(
                        "p-3 rounded-2xl transition-all active:scale-90",
                        isSaved ? "text-yellow-400 bg-yellow-400/10 border border-yellow-400/20" : "text-gray-500 hover:text-gray-200"
                    )}
                >
                    <Bookmark className={cn("w-5 h-5", isSaved && "fill-yellow-400")} />
                </button>
            </div>

            <CommentsSection
                postId={post.id}
                isExpanded={showComments}
            />

            {mounted && createPortal(modalContent, document.body)}
        </motion.div>
    )
}