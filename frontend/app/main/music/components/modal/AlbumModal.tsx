import React, { memo } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { X, Play, Shuffle, Clock, Share2, Plus, Heart } from 'lucide-react'
import { TrackRow } from '../TrackRow'

const DEFAULT_IMAGE = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=800&q=80"

const MemoizedTrackRow = memo(TrackRow);

interface AlbumModalProps {
    id: number
    onClose: () => void
}

export const AlbumModal = ({ id, onClose }: AlbumModalProps) => {
    const albumImage = DEFAULT_IMAGE

    return (
        <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-[100] flex items-center justify-center p-0 md:p-6"
        >
            <div className="absolute inset-0 bg-black/90 backdrop-blur-sm" onClick={onClose} />

            <motion.div
                initial={{ scale: 0.98, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                exit={{ scale: 0.98, opacity: 0 }}
                transition={{ duration: 0.2, ease: "easeOut" }}
                className="bg-[#0c0c0e] w-full max-w-6xl h-full md:h-[85vh] md:rounded-[2.5rem] overflow-hidden border border-white/5 flex flex-col md:flex-row relative shadow-2xl will-change-transform"
            >
                <button
                    onClick={onClose}
                    className="absolute top-8 right-8 z-50 p-2.5 rounded-full bg-white/5 hover:bg-white/10 text-white/70 transition-colors backdrop-blur-md border border-white/5"
                >
                    <X size={20} />
                </button>

                <div className="w-full md:w-[50%] relative h-[40vh] md:h-auto overflow-hidden shrink-0">
                    <img
                        src={albumImage}
                        onError={(e) => { (e.target as HTMLImageElement).src = DEFAULT_IMAGE }}
                        className="w-full h-full object-cover"
                        alt="Album Cover"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-[#0c0c0e] via-transparent to-transparent" />
                </div>

                <div className="flex-1 overflow-y-auto overflow-x-hidden no-scrollbar bg-[#0c0c0e]">
                    <div className="p-8 md:p-14">
                        <header className="mb-10">
                            <div className="flex items-center gap-2 mb-4">
                                <span className="text-[11px] font-bold text-purple-500 uppercase tracking-[0.3em]">Electronic</span>
                                <span className="w-1 h-1 rounded-full bg-white/20" />
                                <span className="text-[11px] font-bold text-white/40 uppercase tracking-[0.3em]">2026</span>
                            </div>
                            <h2 className="text-4xl md:text-5xl font-black tracking-tighter mb-4 leading-none">
                                Cyberpunk Anthology
                            </h2>
                            <p className="text-lg text-gray-400 font-medium mb-8">Night City Collective</p>

                            <div className="flex items-center gap-3">
                                <button className="flex items-center gap-3 px-8 py-4 rounded-full bg-white text-black hover:bg-purple-600 hover:text-white transition-all duration-300">
                                    <Play size={18} className="fill-current" />
                                    <span className="text-[11px] font-black uppercase tracking-widest">Listen</span>
                                </button>
                                <button className="p-4 rounded-full bg-white/5 border border-white/5 text-white/70 hover:text-white hover:bg-white/10 transition-colors">
                                    <Shuffle size={18} />
                                </button>
                                <button className="p-4 rounded-full bg-white/5 border border-white/5 text-white/70 hover:text-white hover:bg-white/10 transition-colors">
                                    <Heart size={18} />
                                </button>
                            </div>
                        </header>

                        <div className="space-y-1 relative">
                            <div className="grid grid-cols-[40px_1fr_100px] px-6 py-4 text-[10px] font-black text-white/20 uppercase tracking-[0.2em] border-b border-white/5 sticky top-0 bg-[#0c0c0e] z-10">
                                <span>#</span>
                                <span>Title</span>
                                <div className="flex justify-end"><Clock size={14} /></div>
                            </div>

                            <div className="mt-4">
                                {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12].map((i) => (
                                    <MemoizedTrackRow key={i} index={i} />
                                ))}
                            </div>
                        </div>
                    </div>
                </div>
            </motion.div>
        </motion.div>
    )
}