'use client'

import React, { useState, ChangeEvent } from 'react'
import { motion } from 'framer-motion'
import {
    Play,
    Pause,
    SkipForward,
    SkipBack,
    Volume2,
    Heart,
    Shuffle,
    Repeat,
    ListMusic,
    Maximize2,
    VolumeX
} from 'lucide-react'
import { cn } from '@/app/lib/utils'

export const Player = () => {
    const [isPlaying, setIsPlaying] = useState(false)
    const [isLiked, setIsLiked] = useState(false)
    const [volume, setVolume] = useState(70)
    const [progress, setProgress] = useState(35)

    const handleProgressChange = (e: ChangeEvent<HTMLInputElement>) => {
        setProgress(Number(e.target.value))
    }

    const handleVolumeChange = (e: ChangeEvent<HTMLInputElement>) => {
        setVolume(Number(e.target.value))
    }

    return (
        <motion.div
            initial={{ y: 100 }}
            animate={{ y: 0 }}
            className="fixed bottom-0 left-0 lg:left-64 right-0 z-40"
        >
            <style jsx>{`
                input[type='range'] {
                    -webkit-appearance: none;
                    background: transparent;
                    cursor: pointer;
                }
                input[type='range']::-webkit-slider-runnable-track {
                    background: rgba(255, 255, 255, 0.05);
                    border-radius: 999px;
                    height: 4px;
                }
                input[type='range']::-webkit-slider-thumb {
                    -webkit-appearance: none;
                    height: 12px;
                    width: 12px;
                    border-radius: 50%;
                    background: white;
                    margin-top: -4px;
                    box-shadow: 0 0 15px rgba(168, 85, 247, 0.5);
                    border: 2px solid #9333ea;
                    opacity: 0;
                    transition: all 0.2s ease;
                }
                .group-slider:hover input[type='range']::-webkit-slider-thumb {
                    opacity: 1;
                    transform: scale(1.1);
                }
            `}</style>

            <div className="absolute inset-0 bg-[#09090b]/95 backdrop-blur-3xl border-t border-white/[0.03]" />

            <div className="absolute top-0 left-0 right-0 h-[1px] bg-gradient-to-r from-transparent via-purple-500/30 to-transparent" />

            <div className="relative px-8 py-5 flex items-center justify-between gap-8">
                <div className="flex items-center gap-4 flex-1 min-w-0 pl-20 lg:pl-0">
                    <div className="relative group shrink-0">
                        <img
                            src="https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=400&q=80"
                            className="relative w-14 h-14 rounded-xl object-cover border border-white/10 shadow-2xl"
                            alt="Cover"
                        />
                    </div>
                    <div className="flex flex-col min-w-0">
                        <span className="text-[15px] font-bold text-white truncate tracking-tight">
                            After Hours
                        </span>
                        <span className="text-[10px] text-purple-500 font-bold uppercase tracking-[0.2em] mt-0.5 truncate">
                            The Weeknd
                        </span>
                    </div>
                    <motion.button
                        whileTap={{ scale: 0.9 }}
                        onClick={() => setIsLiked(!isLiked)}
                        className="ml-2 hidden xl:block"
                    >
                        <Heart
                            size={18}
                            className={cn(
                                "transition-all duration-300",
                                isLiked ? "text-red-500 fill-red-500 drop-shadow-[0_0_8px_rgba(239,68,68,0.4)]" : "text-gray-500 hover:text-white"
                            )}
                        />
                    </motion.button>
                </div>

                <div className="flex flex-col items-center gap-2.5 flex-[2] max-w-[650px]">
                    <div className="flex items-center gap-7">
                        <button className="hidden sm:block text-gray-500 hover:text-purple-400 transition-colors">
                            <Shuffle size={18} />
                        </button>
                        <button className="text-gray-300 hover:text-white transition-transform active:scale-90">
                            <SkipBack size={22} className="fill-current" />
                        </button>

                        <motion.button
                            whileHover={{ scale: 1.05 }}
                            whileTap={{ scale: 0.95 }}
                            onClick={() => setIsPlaying(!isPlaying)}
                            className="w-12 h-12 rounded-full bg-white flex items-center justify-center shadow-[0_0_25px_rgba(168,85,247,0.2)] hover:shadow-purple-500/40 transition-all"
                        >
                            {isPlaying ? (
                                <Pause size={22} className="text-black fill-black" />
                            ) : (
                                <Play size={22} className="text-black fill-black ml-1" />
                            )}
                        </motion.button>

                        <button className="text-gray-300 hover:text-white transition-transform active:scale-90">
                            <SkipForward size={22} className="fill-current" />
                        </button>
                        <button className="hidden sm:block text-gray-500 hover:text-purple-400 transition-colors">
                            <Repeat size={18} />
                        </button>
                    </div>

                    <div className="w-full flex items-center gap-3 group-slider">
                        <span className="text-[10px] text-gray-500 font-mono w-10 text-right tabular-nums">1:24</span>
                        <div className="flex-1 relative flex items-center">
                            <div
                                className="absolute h-[3px] bg-purple-500 rounded-full pointer-events-none z-10 shadow-[0_0_12px_rgba(168,85,247,0.6)]"
                                style={{ width: `${progress}%` }}
                            />
                            <input
                                type="range"
                                min="0"
                                max="100"
                                value={progress}
                                onChange={handleProgressChange}
                                className="w-full h-[3px] z-20"
                            />
                        </div>
                        <span className="text-[10px] text-gray-500 font-mono w-10 tabular-nums">4:02</span>
                    </div>
                </div>

                <div className="flex items-center justify-end gap-6 flex-1">
                    <div className="hidden xl:flex items-center gap-3 group-slider">
                        <button onClick={() => setVolume(v => v === 0 ? 70 : 0)} className="text-gray-400 hover:text-white transition-colors">
                            {volume === 0 ? <VolumeX size={18} /> : <Volume2 size={18} />}
                        </button>
                        <div className="w-24 relative flex items-center">
                            <div
                                className="absolute h-[3px] bg-white/40 rounded-full pointer-events-none z-10"
                                style={{ width: `${volume}%` }}
                            />
                            <input
                                type="range"
                                min="0"
                                max="100"
                                value={volume}
                                onChange={handleVolumeChange}
                                className="w-full h-[3px] z-20"
                            />
                        </div>
                    </div>
                    <div className="flex items-center gap-4 border-l border-white/5 pl-6">
                        <button className="hidden md:block text-gray-400 hover:text-purple-400 transition-colors">
                            <ListMusic size={20} />
                        </button>
                        <button className="text-gray-400 hover:text-white transition-colors">
                            <Maximize2 size={18} />
                        </button>
                    </div>
                </div>
            </div>
        </motion.div>
    )
}