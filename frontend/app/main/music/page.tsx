'use client'

import React, { useState, useEffect } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Search, Sparkles, ChevronRight } from 'lucide-react'
import { cn } from '@/app/lib/utils'
import { Player } from './components/Player'
import { AlbumCard } from './components/AlbumCard'
import { TrackRow } from './components/TrackRow'
import { MyLibrary } from './components/MyLibrary'
import { AlbumModal } from './components/modal/AlbumModal'

const CATEGORIES = [
    { id: 'home', label: 'Home' },
    { id: 'my-tracks', label: 'My Library' },
    { id: 'albums', label: 'Albums' },
    { id: 'trending', label: 'Trending' },
]

const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
        opacity: 1,
        transition: { staggerChildren: 0.1, delayChildren: 0.3 }
    }
}

const itemVariants = {
    hidden: { y: 20, opacity: 0 },
    visible: {
        y: 0,
        opacity: 1,
        transition: { type: 'spring', stiffness: 100, damping: 15 }
    }
}

export default function MusicPage() {
    const [isLoading, setIsLoading] = useState(true)
    const [activeTab, setActiveTab] = useState('home')
    const [selectedAlbum, setSelectedAlbum] = useState<number | null>(null)

    useEffect(() => {
        const timer = setTimeout(() => setIsLoading(false), 1200)
        return () => clearTimeout(timer)
    }, [])

    return (
        <div className="flex flex-col h-screen bg-[#09090b] text-white selection:bg-purple-500/30 overflow-hidden font-sans">
            <AnimatePresence>
                {selectedAlbum && (
                    <AlbumModal
                        id={selectedAlbum}
                        onClose={() => setSelectedAlbum(null)}
                    />
                )}
            </AnimatePresence>

            <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                className="flex-1 overflow-y-auto no-scrollbar px-6 md:px-12 pb-32"
            >
                <div className="mt-8 mb-12 flex flex-col md:flex-row md:items-center justify-between gap-6">
                    <nav className="flex items-center gap-1 p-1.5 bg-white/[0.02] rounded-[1.8rem] border border-white/[0.05] backdrop-blur-xl">
                        {CATEGORIES.map((cat) => (
                            <button
                                key={cat.id}
                                onClick={() => setActiveTab(cat.id)}
                                className={cn(
                                    "px-6 py-2 text-[10px] uppercase tracking-[0.2em] font-black transition-all duration-300 relative rounded-[1.4rem]",
                                    activeTab === cat.id ? "text-white" : "text-gray-500 hover:text-gray-400"
                                )}
                            >
                                <span className="relative z-10">{cat.label}</span>
                                {activeTab === cat.id && (
                                    <motion.div
                                        layoutId="activeTab"
                                        className="absolute inset-0 bg-purple-600 border border-purple-500/50 rounded-[1.4rem] shadow-[0_0_25px_rgba(147,51,234,0.2)]"
                                    />
                                )}
                            </button>
                        ))}
                    </nav>

                    {activeTab !== 'my-tracks' && (
                        <div className="relative group w-full md:w-80">
                            <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-600 group-focus-within:text-purple-500 z-10" size={16} />
                            <input
                                type="text"
                                placeholder="Search tracks..."
                                className="w-full bg-white/[0.02] border border-white/[0.05] rounded-2xl py-3.5 pl-11 pr-4 text-xs focus:outline-none focus:bg-white/[0.04] transition-all"
                            />
                        </div>
                    )}
                </div>

                <main>
                    <AnimatePresence mode="wait">
                        {isLoading ? (
                            <PageSkeleton key="skeleton" />
                        ) : activeTab === 'home' ? (
                            <motion.div
                                key="home"
                                variants={containerVariants}
                                initial="hidden"
                                animate="visible"
                                className="space-y-16"
                            >
                                <section>
                                    <motion.div variants={itemVariants} className="flex items-end justify-between mb-10">
                                        <div className="space-y-4">
                                            <div className="flex items-center gap-3 text-purple-500 text-[10px] font-black tracking-[0.4em] uppercase">
                                                <div className="h-px w-8 bg-purple-500/50" />
                                                <span>Featured</span>
                                            </div>
                                            <h2 className="text-5xl font-black tracking-tighter">New Releases</h2>
                                        </div>
                                        <button className="hidden md:flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-gray-500 hover:text-white transition-colors group">
                                            View All <ChevronRight size={14} className="group-hover:translate-x-1 transition-transform" />
                                        </button>
                                    </motion.div>

                                    <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-x-6 gap-y-10">
                                        {[1, 2, 3, 4, 5, 6].map((i) => (
                                            <motion.div key={i} variants={itemVariants}>
                                                <AlbumCard id={i} onClick={(id) => setSelectedAlbum(id)} />
                                            </motion.div>
                                        ))}
                                    </div>
                                </section>

                                <section>
                                    <motion.div variants={itemVariants} className="flex items-center gap-3 mb-8">
                                        <Sparkles size={18} className="text-purple-500" />
                                        <h3 className="text-xl font-black tracking-tight">Recently Played</h3>
                                    </motion.div>
                                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                                        {[1, 2, 3, 4].map((i) => (
                                            <motion.div key={i} variants={itemVariants}>
                                                <TrackRow index={i} />
                                            </motion.div>
                                        ))}
                                    </div>
                                </section>
                            </motion.div>
                        ) : activeTab === 'my-tracks' ? (
                            <MyLibrary key="library" onAlbumClick={setSelectedAlbum} />
                        ) : (
                            <motion.div
                                key="coming-soon"
                                initial={{ opacity: 0 }}
                                animate={{ opacity: 1 }}
                                className="flex items-center justify-center h-64 text-gray-500 uppercase tracking-widest text-[10px] font-black"
                            >
                                Section Coming Soon
                            </motion.div>
                        )}
                    </AnimatePresence>
                </main>
            </motion.div>
            <Player />
        </div>
    )
}

const PageSkeleton = () => (
    <div className="space-y-16 animate-pulse">
        <section>
            <div className="h-12 w-64 bg-white/[0.03] rounded-xl mb-10" />
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-6">
                {[1, 2, 3, 4, 5, 6].map(i => (
                    <div key={i} className="space-y-4">
                        <div className="aspect-square bg-white/[0.03] rounded-2xl" />
                        <div className="h-4 w-3/4 bg-white/[0.03] rounded-md" />
                    </div>
                ))}
            </div>
        </section>
        <section>
            <div className="h-8 w-48 bg-white/[0.03] rounded-md mb-8" />
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                {[1, 2, 3, 4].map(i => (
                    <div key={i} className="h-20 bg-white/[0.02] rounded-2xl" />
                ))}
            </div>
        </section>
    </div>
)