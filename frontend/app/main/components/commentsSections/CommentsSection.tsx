'use client'

import React from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Send, Smile, Heart, Reply, X, Crown } from 'lucide-react'
import { cn } from '@/app/lib/utils'
import { CommentsSectionProps, CommentItemProps } from './types'
import { useComments } from './useComments'

const CommentItem = ({ item, isReply = false, isTop = false, canReply, onLike, onReplyClick }: CommentItemProps) => (
    <motion.div
        layout
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, ease: [0.25, 0.1, 0.25, 1.0] }}
        className={cn("flex gap-3", isReply && "ml-12 mt-4")}
    >
        <img src={item.user.avatar} className={cn("rounded-xl object-cover shrink-0 shadow-sm", isReply ? "w-7 h-7" : "w-10 h-10")} alt={item.user.name} />
        <div className="flex-1 space-y-2">
            <div className={cn(
                "relative group transition-all duration-300 rounded-[1.25rem] rounded-tl-none p-4 border",
                isTop ? "bg-purple-500/5 border-purple-500/20 shadow-[0_4px_20px_-8px_rgba(168,85,247,0.3)]" : "bg-white/[0.03] border-white/5 hover:bg-white/[0.05]"
            )}>
                {isTop && (
                    <div className="absolute -top-2.5 left-4 bg-gradient-to-r from-purple-600 to-indigo-600 text-[9px] font-black uppercase tracking-tighter px-2.5 py-1 rounded-full flex items-center gap-1 shadow-sm text-white">
                        <Crown className="w-3 h-3" /> Top
                    </div>
                )}

                <div className="flex items-center justify-between mb-2">
                    <span className="text-[13px] font-bold text-white tracking-tight">{item.user.name}</span>
                    <span className="text-[10px] font-medium text-gray-500">{item.timestamp}</span>
                </div>

                <p className="text-[14px] text-gray-200 leading-[1.6] font-normal">
                    {item.content}
                </p>

                <div className="flex items-center gap-5 mt-3 pt-2">
                    <button
                        onClick={() => onLike(item.id)}
                        className={cn(
                            "flex items-center gap-2 text-xs font-bold transition-all active:scale-90",
                            item.isLiked ? "text-red-400" : "text-gray-500 hover:text-red-400"
                        )}
                    >
                        <Heart className={cn("w-4 h-4 transition-transform", item.isLiked && "fill-red-400 scale-110")} />
                        <span>{item.likes}</span>
                    </button>

                    {canReply && (
                        <button
                            onClick={() => onReplyClick(item.id, item.user.name)}
                            className="flex items-center gap-2 text-xs font-bold text-gray-500 hover:text-purple-400 transition-all active:scale-90 group/reply"
                        >
                            <Reply className="w-4 h-4 group-hover/reply:-rotate-12 transition-transform" />
                            <span>Reply</span>
                        </button>
                    )}
                </div>
            </div>
            {item.replies?.map(reply => (
                <CommentItem
                    key={reply.id}
                    item={reply}
                    isReply
                    canReply={canReply}
                    onLike={onLike}
                    onReplyClick={onReplyClick}
                />
            ))}
        </div>
    </motion.div>
)

export default function CommentsSection({ postId, isExpanded }: CommentsSectionProps) {
    const {
        comments,
        newComment,
        setNewComment,
        replyTo,
        setReplyTo,
        topComment,
        handleLike,
        handleReplyClick,
        handleSubmit
    } = useComments()

    return (
        <div className="mt-7 pt-6 border-t border-white/5 relative">
            <AnimatePresence mode="wait">
                {!isExpanded && topComment && (
                    <motion.div
                        key="preview"
                        initial={{ opacity: 0, y: -10 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -10, transition: { duration: 0.2 } }}
                        className="space-y-2"
                    >
                        <CommentItem
                            item={topComment}
                            isTop
                            canReply={false}
                            onLike={handleLike}
                            onReplyClick={handleReplyClick}
                        />
                    </motion.div>
                )}
            </AnimatePresence>

            <AnimatePresence>
                {isExpanded && (
                    <motion.div
                        key="full-list"
                        initial={{ opacity: 0, height: 0 }}
                        animate={{ opacity: 1, height: 'auto' }}
                        exit={{ opacity: 0, height: 0 }}
                        transition={{ type: "tween", ease: [0.4, 0.0, 0.2, 1], duration: 0.5 }}
                        className="space-y-8 overflow-hidden"
                    >
                        <div className="space-y-4 pt-2">
                            <AnimatePresence>
                                {replyTo && (
                                    <motion.div
                                        initial={{ opacity: 0, height: 0 }}
                                        animate={{ opacity: 1, height: 'auto' }}
                                        exit={{ opacity: 0, height: 0 }}
                                        className="flex items-center justify-between bg-purple-500/10 border border-purple-500/20 rounded-xl px-4 py-2.5 overflow-hidden"
                                    >
                                        <span className="text-[11px] text-purple-300 font-bold uppercase tracking-wider flex items-center gap-2">
                                            <Reply className="w-3 h-3" /> Replying to <span className="text-white">{replyTo.name}</span>
                                        </span>
                                        <button onClick={() => setReplyTo(null)} className="text-purple-300 hover:text-white transition-colors p-1 bg-purple-500/20 rounded-full">
                                            <X className="w-3 h-3" />
                                        </button>
                                    </motion.div>
                                )}
                            </AnimatePresence>

                            <form onSubmit={handleSubmit} className="relative flex items-start gap-3 z-10">
                                <img src="https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100" className="w-10 h-10 rounded-xl object-cover shadow-sm mt-1" alt="Your avatar" />
                                <div className="relative flex-1 group">
                                    <textarea
                                        value={newComment}
                                        onChange={(e) => setNewComment(e.target.value)}
                                        placeholder={replyTo ? "Write your reply..." : "Add a comment..."}
                                        className="w-full bg-white/[0.03] border border-white/5 focus:border-purple-500/50 rounded-2xl py-4 px-5 text-sm text-white focus:outline-none focus:ring-4 focus:ring-purple-500/5 transition-all placeholder:text-gray-600 resize-none min-h-[60px] leading-relaxed shadow-sm group-hover:bg-white/[0.05]"
                                        style={{ fieldSizing: 'content' } as any}
                                    />
                                    <div className="absolute right-3 bottom-3 flex gap-2">
                                        <button type="button" className="text-gray-600 hover:text-yellow-400 transition-colors p-2 hover:bg-yellow-400/10 rounded-xl active:scale-90">
                                            <Smile className="w-5 h-5" />
                                        </button>
                                        <button
                                            className={cn(
                                                "p-2 rounded-xl transition-all duration-300 shadow-lg active:scale-95 flex items-center justify-center",
                                                newComment.trim() ? "bg-purple-600 text-white shadow-purple-600/20 hover:bg-purple-500" : "bg-white/5 text-gray-700 cursor-not-allowed"
                                            )}
                                            disabled={!newComment.trim()}
                                        >
                                            <Send className="w-5 h-5" />
                                        </button>
                                    </div>
                                </div>
                            </form>
                        </div>

                        <div className="space-y-6 pb-4">
                            {comments.map(comment => (
                                <CommentItem
                                    key={comment.id}
                                    item={comment}
                                    canReply={true}
                                    onLike={handleLike}
                                    onReplyClick={handleReplyClick}
                                />
                            ))}
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    )
}