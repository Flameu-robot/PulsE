'use client'

import React, { useState, useEffect } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Search, Heart, Users, PlayCircle, Music2, ChevronDown, ChevronUp } from 'lucide-react'
import { TrackRow } from './TrackRow'

const DEFAULT_IMAGE = "https://i.pinimg.com/736x/43/03/c1/4303c1ea521d7a46b4321be917b227ee.jpg"

const itemVariants = {
    hidden: { y: 20, opacity: 0 },
    visible: {
        y: 0,
        opacity: 1,
        transition: { type: 'spring', stiffness: 100, damping: 15 }
    }
}

interface MyLibraryProps {
    onAlbumClick: (id: number) => void
}

export const MyLibrary = ({ onAlbumClick }: MyLibraryProps) => {
    const [isLoading, setIsLoading] = useState(true)
    const [isExpanded, setIsExpanded] = useState(false)
    const [following, setFollowing] = useState<Record<string, boolean>>({
        'The Weeknd': true,
        'Gesaffelstein': true,
        'Perturbator': true
    })

    useEffect(() => {
        const timer = setTimeout(() => setIsLoading(false), 800)
        return () => clearTimeout(timer)
    }, [])

    const toggleFollow = (artist: string) => {
        setFollowing(prev => ({ ...prev, [artist]: !prev[artist] }))
    }

    const tracks = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
    const visibleTracks = isExpanded ? tracks : tracks.slice(0, 4)

    const playlists = [
        { id: 1, name: 'Synthwave Essentials', img: 'https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=300&q=80', count: 42 },
        { id: 2, name: 'Midnight City', img: '', count: 18 },
        { id: 3, name: 'Cyberpunk 2077', img: '', count: 56 }
    ]

    const artists = [
        { name: 'The Weeknd', img: 'https://images.unsplash.com/photo-1557672172-298e090bd0f1?w=100&q=80' },
        { name: 'Gesaffelstein', img: '' },
        { name: 'Perturbator', img: 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=100&q=80' }
    ]

    if (isLoading) return <LibrarySkeleton />

    return (
        <motion.div
            initial="hidden"
            animate="visible"
            variants={{ visible: { transition: { staggerChildren: 0.05 } } }}
            className="space-y-12 pb-20"
        >
            <motion.div variants={itemVariants} className="relative group max-w-md">
                <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-600 group-focus-within:text-purple-500 transition-colors z-10" size={16} />
                <input
                    type="text"
                    placeholder="Search in your library..."
                    className="w-full bg-white/[0.02] border border-white/[0.05] group-focus-within:border-purple-500/50 rounded-2xl py-3.5 pl-11 pr-4 text-xs focus:outline-none transition-all focus:bg-white/[0.04]"
                />
            </motion.div>

            <motion.section variants={itemVariants}>
                <div className="flex items-center justify-between mb-6">
                    <div className="flex items-center gap-3">
                        <Heart size={18} className="text-purple-500 fill-purple-500" />
                        <h2 className="text-2xl font-black tracking-tight">Favorite Tracks</h2>
                    </div>
                    <button
                        onClick={() => setIsExpanded(!isExpanded)}
                        className="flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-gray-400 hover:text-white transition-colors"
                    >
                        {isExpanded ? <><ChevronUp size={14} /> Show Less</> : <><ChevronDown size={14} /> View All</>}
                    </button>
                </div>
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-x-12 gap-y-2">
                    <AnimatePresence initial={false} mode="popLayout">
                        {visibleTracks.map((t) => (
                            <motion.div
                                key={t}
                                initial={{ opacity: 0, scale: 0.95 }}
                                animate={{ opacity: 1, scale: 1 }}
                                exit={{ opacity: 0, scale: 0.95 }}
                                transition={{ duration: 0.2 }}
                            >
                                <TrackRow index={t} />
                            </motion.div>
                        ))}
                    </AnimatePresence>
                </div>
            </motion.section>

            <div className="grid grid-cols-1 xl:grid-cols-2 gap-12">
                <motion.section variants={itemVariants}>
                    <div className="flex items-center gap-3 mb-6">
                        <PlayCircle size={18} className="text-purple-500" />
                        <h2 className="text-xl font-black tracking-tight uppercase">Your Playlists</h2>
                    </div>
                    <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
                        {playlists.map((pl) => (
                            <div
                                key={pl.id}
                                className="group cursor-pointer"
                                onClick={() => onAlbumClick(pl.id)}
                            >
                                <div className="aspect-square rounded-2xl overflow-hidden border border-white/5 mb-3 group-hover:border-purple-500/50 transition-all relative bg-[#16161a]">
                                    <img
                                        src={pl.img || DEFAULT_IMAGE}
                                        className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                                        alt={pl.name}
                                    />
                                    <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                                        <Music2 size={24} className="text-white" />
                                    </div>
                                </div>
                                <p className="text-sm font-bold truncate">{pl.name}</p>
                                <p className="text-[10px] text-gray-500 uppercase font-black">{pl.count} Tracks</p>
                            </div>
                        ))}
                    </div>
                </motion.section>

                <motion.section variants={itemVariants}>
                    <div className="flex items-center gap-3 mb-6">
                        <Users size={18} className="text-purple-500" />
                        <h2 className="text-xl font-black tracking-tight uppercase">Followed Artists</h2>
                    </div>
                    <div className="space-y-4">
                        {artists.map((artist, i) => (
                            <div key={i} className="flex items-center justify-between p-3 rounded-2xl bg-white/[0.02] border border-transparent hover:border-white/5 hover:bg-white/[0.04] transition-all cursor-pointer">
                                <div className="flex items-center gap-4">
                                    <div className="w-10 h-10 rounded-full overflow-hidden border border-white/10 bg-[#16161a]">
                                        <img src={artist.img || DEFAULT_IMAGE} className="w-full h-full object-cover" alt={artist.name} />
                                    </div>
                                    <span className="text-sm font-bold">{artist.name}</span>
                                </div>
                                <button
                                    onClick={(e) => {
                                        e.stopPropagation()
                                        toggleFollow(artist.name)
                                    }}
                                    className={cn(
                                        "text-[10px] font-black uppercase tracking-widest px-4 py-2 rounded-full border transition-all",
                                        following[artist.name]
                                            ? "border-purple-500/50 text-purple-400 hover:bg-red-500 hover:text-white hover:border-red-500"
                                            : "border-white/10 text-white hover:bg-white hover:text-black"
                                    )}
                                >
                                    {following[artist.name] ? 'Unfollow' : 'Follow'}
                                </button>
                            </div>
                        ))}
                    </div>
                </motion.section>
            </div>
        </motion.div>
    )
}

const LibrarySkeleton = () => (
    <div className="space-y-12 animate-pulse">
        <div className="h-12 w-full max-w-md bg-white/[0.03] rounded-2xl" />
        <section>
            <div className="h-8 w-48 bg-white/[0.03] rounded-md mb-6" />
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                {[1, 2, 3, 4].map(i => (
                    <div key={i} className="h-20 bg-white/[0.02] rounded-2xl" />
                ))}
            </div>
        </section>
        <div className="grid grid-cols-1 xl:grid-cols-2 gap-12">
            <div className="grid grid-cols-3 gap-4">
                {[1, 2, 3].map(i => (
                    <div key={i} className="space-y-3">
                        <div className="aspect-square bg-white/[0.03] rounded-2xl" />
                        <div className="h-4 w-2/3 bg-white/[0.03] rounded-md" />
                    </div>
                ))}
            </div>
            <div className="space-y-4">
                {[1, 2, 3].map(i => (
                    <div key={i} className="h-16 bg-white/[0.02] rounded-2xl" />
                ))}
            </div>
        </div>
    </div>
)

function cn(...classes: string[]) {
    return classes.filter(Boolean).join(' ')
}