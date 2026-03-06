'use client'

import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Play, SkipBack, SkipForward, Volume2 } from 'lucide-react'

export const MusicOverlay = () => {
    const [volume, setVolume] = useState(80)
    const [isMuted, setIsMuted] = useState(false)

    return (
        <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="mb-4 mx-4 p-3 bg-gradient-to-br from-purple-600/20 to-blue-600/10 backdrop-blur-md border border-white/10 rounded-2xl group transition-all hidden lg:block"
        >
            <div className="flex flex-col gap-3">
                <div className="flex items-center gap-3">
                    <div className="relative shrink-0 overflow-hidden rounded-lg w-10 h-10 shadow-lg shadow-purple-500/10">
                        <img
                            src="https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=100"
                            alt="Cover"
                            className="w-full h-full object-cover opacity-80 group-hover:scale-110 transition-transform duration-700"
                        />
                        <div className="absolute inset-0 flex items-center justify-center bg-black/20 group-hover:bg-black/40 transition-colors">
                            <Play className="w-3 h-3 text-white fill-white" />
                        </div>
                    </div>

                    <div className="flex-1 overflow-hidden">
                        <div className="flex justify-between items-start">
                            <p className="text-[11px] font-bold text-white truncate leading-tight">Starboy</p>
                            <div className="flex gap-[2px] items-end h-3 ml-2">
                                {[0.4, 0.7, 0.3, 0.9].map((h, i) => (
                                    <motion.div
                                        key={i}
                                        animate={{ height: ["20%", "100%", "20%"] }}
                                        transition={{ repeat: Infinity, duration: 0.8, delay: i * 0.1 }}
                                        className="w-[2px] bg-purple-500/80"
                                    />
                                ))}
                            </div>
                        </div>
                        <p className="text-[9px] text-purple-400/80 font-medium truncate uppercase tracking-widest mt-0.5">The Weeknd</p>
                    </div>
                </div>

                <div className="flex items-center justify-between gap-2 pt-1">
                    <div className="flex items-center gap-1">
                        <button className="p-1.5 text-gray-400 hover:text-white hover:bg-white/5 rounded-lg transition-colors">
                            <SkipBack className="w-3.5 h-3.5 fill-current" />
                        </button>
                        <button className="p-1.5 text-gray-400 hover:text-white hover:bg-white/5 rounded-lg transition-colors">
                            <SkipForward className="w-3.5 h-3.5 fill-current" />
                        </button>
                    </div>

                    <div className="flex items-center gap-2 group/vol flex-1 justify-end">
                        <Volume2 className="w-3.5 h-3.5 text-gray-400 group-hover/vol:text-purple-400 transition-colors shrink-0" />
                        <div className="relative w-16 h-1 bg-white/10 rounded-full overflow-hidden">
                            <motion.div
                                className="absolute left-0 top-0 h-full bg-purple-500 shadow-[0_0_8px_rgba(168,85,247,0.5)]"
                                style={{ width: `${volume}%` }}
                            />
                            <input
                                type="range"
                                min="0"
                                max="100"
                                value={volume}
                                onChange={(e) => setVolume(Number(e.target.value))}
                                className="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10"
                            />
                        </div>
                    </div>
                </div>
            </div>
        </motion.div>
    )
}