'use client'

import React from 'react'
import { Plus, ChevronLeft, ChevronRight } from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import StoryModal from '@/app/main/components/story/storyModal/StoryModal'
import { useStories } from './useStories'

export default function Stories() {
    const {
        scrollRef,
        fileInputRef,
        showLeftArrow,
        showRightArrow,
        viewedStories,
        activeStoryIndex,
        setActiveStoryIndex,
        allStories,
        handleFileChange,
        scrollLeft,
        scrollRight,
        markAsViewed,
        handleStoryClick
    } = useStories()

    return (
        <div className="relative group/container pt-8 -mt-4 overflow-visible max-w-full">
            <input type="file" ref={fileInputRef} className="hidden" accept="image/*,video/*" onChange={handleFileChange} />
            <AnimatePresence>
                {showLeftArrow && (
                    <motion.button
                        initial={{ opacity: 0, x: -10 }}
                        animate={{ opacity: 1, x: 0 }}
                        exit={{ opacity: 0, x: -10 }}
                        onClick={scrollLeft}
                        className="absolute left-0 top-[74px] z-[60] p-2 rounded-full bg-gray-900/95 border border-purple-500/40 text-white shadow-lg backdrop-blur-md hover:bg-purple-600 transition-all"
                    >
                        <ChevronLeft className="w-5 h-5" />
                    </motion.button>
                )}
            </AnimatePresence>

            <div ref={scrollRef} className="flex gap-5 overflow-x-auto no-scrollbar scroll-smooth px-2 py-6 items-start overflow-y-visible">
                <motion.div
                    whileHover={{ scale: 1.05, y: -6 }}
                    onClick={() => fileInputRef.current?.click()}
                    className="flex-shrink-0 flex flex-col items-center gap-3 cursor-pointer group relative z-10"
                >
                    <div className="w-[72px] h-[72px] rounded-[1.8rem] bg-gray-900 border-2 border-dashed border-purple-500/40 flex items-center justify-center group-hover:border-purple-500 transition-all duration-300">
                        <Plus className="w-7 h-7 text-purple-400" />
                    </div>
                    <span className="text-[11px] text-gray-400 font-bold uppercase tracking-wider">Create</span>
                </motion.div>

                {allStories.map((story, index) => (
                    <motion.div
                        key={story.id}
                        whileHover={{ scale: 1.05, y: -6 }}
                        onClick={() => handleStoryClick(index, story.id)}
                        className="flex-shrink-0 flex flex-col items-center gap-3 cursor-pointer group relative z-10"
                    >
                        <div className={`w-[72px] h-[72px] rounded-[1.8rem] p-[3px] transition-all duration-500 ${viewedStories.includes(story.id) ? 'bg-gray-700' : 'bg-gradient-to-tr from-purple-600 via-pink-500 to-orange-400'}`}>
                            <div className="w-full h-full rounded-[1.6rem] border-[3px] border-[#0a0a0c] overflow-hidden bg-gray-800">
                                <img src={story.avatar} className="w-full h-full object-cover" alt="" />
                            </div>
                        </div>
                        <span className={`text-[11px] font-bold uppercase tracking-wider transition-colors ${viewedStories.includes(story.id) ? 'text-gray-600' : 'text-gray-500 group-hover:text-purple-400'}`}>
                            {story.isOwn ? 'Your Story' : story.username.split(' ')[0]}
                        </span>
                    </motion.div>
                ))}
            </div>

            <AnimatePresence>
                {showRightArrow && (
                    <motion.button
                        initial={{ opacity: 0, x: 10 }}
                        animate={{ opacity: 1, x: 0 }}
                        exit={{ opacity: 0, x: 10 }}
                        onClick={scrollRight}
                        className="absolute right-0 top-[74px] z-[60] p-2 rounded-full bg-gray-900/95 border border-purple-500/40 text-white shadow-lg backdrop-blur-md hover:bg-purple-600 transition-all"
                    >
                        <ChevronRight className="w-5 h-5" />
                    </motion.button>
                )}
            </AnimatePresence>

            <AnimatePresence>
                {activeStoryIndex !== null && (
                    <StoryModal
                        stories={allStories}
                        initialIndex={activeStoryIndex}
                        onClose={() => setActiveStoryIndex(null)}
                        onStoryChange={markAsViewed}
                    />
                )}
            </AnimatePresence>
        </div>
    )
}