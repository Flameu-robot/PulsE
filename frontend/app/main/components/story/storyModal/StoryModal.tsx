'use client'

import React from 'react'
import { motion } from 'framer-motion'
import { X, Heart, Send, Share2, ChevronLeft, ChevronRight, Eye } from 'lucide-react'
import { StoryModalProps } from './types'
import { useStoryModal } from './useStoryModal'

export default function StoryModal({ stories, initialIndex, onClose, onStoryChange }: StoryModalProps) {
    const {
        currentIndex,
        progress,
        setIsPaused,
        liked,
        setLiked,
        currentStory,
        handleNext,
        handlePrev,
        handleBackdropClick
    } = useStoryModal(stories, initialIndex, onClose, onStoryChange)

    return (
        <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={handleBackdropClick}
            className="fixed inset-0 z-[999] bg-black/90 backdrop-blur-2xl flex items-center justify-center cursor-pointer"
        >
            <button
                onClick={onClose}
                className="absolute top-8 right-8 z-50 p-3 rounded-2xl bg-white/5 border border-white/10 text-white/50 hover:text-white hover:bg-white/10 hover:border-white/20 hover:rotate-90 transition-all duration-300 group"
            >
                <X className="w-6 h-6 group-hover:scale-110" />
            </button>

            <div className="relative w-full max-w-[420px] aspect-[9/16] bg-gray-900 rounded-[3rem] overflow-hidden shadow-[0_0_50px_rgba(0,0,0,0.5)] border border-white/5 cursor-default">
                <div className="absolute top-0 inset-x-0 z-40 p-6 flex gap-1.5">
                    {stories.map((_, i) => (
                        <div key={i} className="h-1 flex-1 bg-white/10 rounded-full overflow-hidden">
                            <div className="h-full bg-white transition-all duration-100 ease-linear" style={{ width: i === currentIndex ? `${progress}%` : i < currentIndex ? '100%' : '0%' }} />
                        </div>
                    ))}
                </div>

                <div className="absolute top-10 inset-x-0 z-40 px-6 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <img src={currentStory.avatar} className="w-10 h-10 rounded-full border-2 border-purple-500 shadow-lg" />
                        <div className="flex flex-col">
                            <span className="text-white font-bold text-sm shadow-md">{currentStory.username}</span>
                            {currentStory.isOwn && <span className="text-[10px] text-purple-400 font-bold uppercase tracking-tighter">Your Story</span>}
                        </div>
                    </div>
                    {currentStory.isOwn && (
                        <div className="flex items-center gap-3 bg-black/40 backdrop-blur-xl px-4 py-2 rounded-2xl border border-white/5">
                            <div className="flex items-center gap-1.5 text-white/70 text-xs font-bold"><Eye className="w-3.5 h-3.5" /> {currentStory.views}</div>
                            <div className="flex items-center gap-1.5 text-white/70 text-xs font-bold"><Heart className="w-3.5 h-3.5 fill-white/70" /> {currentStory.likes}</div>
                            <button className="ml-1 text-white/50 hover:text-white transition-colors"><Share2 className="w-3.5 h-3.5" /></button>
                        </div>
                    )}
                </div>

                <div className="w-full h-full relative bg-gray-950" onMouseDown={() => setIsPaused(true)} onMouseUp={() => setIsPaused(false)}>
                    {currentStory.type === 'video' ? <video src={currentStory.content} autoPlay muted className="w-full h-full object-cover" /> : <img src={currentStory.content} className="w-full h-full object-cover" />}
                    <div className="absolute inset-0 flex">
                        <div className="flex-1 cursor-west-resize" onClick={handlePrev} />
                        <div className="flex-1 cursor-east-resize" onClick={handleNext} />
                    </div>
                </div>

                {!currentStory.isOwn ? (
                    <div className="absolute bottom-0 inset-x-0 z-40 p-8 bg-gradient-to-t from-black/90 via-black/40 to-transparent">
                        <div className="flex items-center gap-3">
                            <div className="flex-1 relative">
                                <input type="text" placeholder="Send a message..." className="w-full bg-white/10 border border-white/10 rounded-2xl py-3.5 px-6 text-sm text-white focus:outline-none focus:bg-white/20 transition-all" />
                                <button className="absolute right-4 top-1/2 -translate-y-1/2 text-purple-400"><Send className="w-5 h-5" /></button>
                            </div>
                            <button onClick={() => setLiked(!liked)} className={`p-3.5 rounded-2xl transition-all ${liked ? 'bg-red-500/20 text-red-500' : 'bg-white/10 text-white hover:bg-white/20'}`}><Heart className={`w-6 h-6 ${liked ? 'fill-red-500' : ''}`} /></button>
                            <button className="p-3.5 bg-white/10 rounded-2xl text-white hover:bg-white/20 transition-all"><Share2 className="w-6 h-6" /></button>
                        </div>
                    </div>
                ) : (
                    <div className="absolute bottom-8 inset-x-0 z-40 px-8 flex justify-center">
                        <button className="flex items-center gap-2 bg-white/10 hover:bg-white/20 backdrop-blur-xl px-6 py-3 rounded-2xl border border-white/10 text-white text-sm font-bold transition-all">
                            <Share2 className="w-4 h-4" /> Share to Feed
                        </button>
                    </div>
                )}
            </div>

            <button onClick={handlePrev} className={`hidden md:block absolute left-10 p-4 text-white/20 hover:text-white transition-all ${currentIndex === 0 && 'opacity-0 cursor-default'}`}><ChevronLeft className="w-12 h-12" /></button>
            <button onClick={handleNext} className={`hidden md:block absolute right-10 p-4 text-white/20 hover:text-white transition-all ${currentIndex === stories.length - 1 && 'opacity-0 cursor-default'}`}><ChevronRight className="w-12 h-12" /></button>
        </motion.div>
    )
}