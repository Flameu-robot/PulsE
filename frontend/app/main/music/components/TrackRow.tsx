import React from 'react'
import { Heart, MoreHorizontal, Play } from 'lucide-react'

const DEFAULT_IMAGE = "https://i.pinimg.com/736x/43/03/c1/4303c1ea521d7a46b4321be917b227ee.jpg"

export const TrackRow = ({ index }: { index: number }) => (
    <div className="group flex items-center gap-6 p-4 rounded-2xl hover:bg-white/[0.04] transition-all duration-300 border border-transparent hover:border-white/[0.08] relative overflow-hidden">
        <div className="w-8 flex justify-center">
            <span className="text-sm font-mono text-gray-600 group-hover:hidden">0{index}</span>
            <Play size={14} className="hidden group-hover:block text-purple-400 fill-current" />
        </div>

        <div className="w-12 h-12 rounded-xl bg-black overflow-hidden border border-white/10 shrink-0 shadow-xl">
            <img
                src={`https://images.unsplash.com/photo-${1500000000000 + (index * 100000)}?w=100&h=100&fit=crop&q=80`}
                onError={(e) => {
                    const target = e.target as HTMLImageElement;
                    target.src = DEFAULT_IMAGE;
                }}
                alt="Track"
                className="w-full h-full object-cover opacity-80 group-hover:opacity-100 group-hover:scale-110 transition-all duration-500"
            />
        </div>

        <div className="flex-1 min-w-0">
            <h4 className="text-[15px] font-bold text-gray-100 truncate group-hover:text-purple-400 transition-colors tracking-tight">
                Track Name {index}
            </h4>
            <p className="text-[10px] text-gray-500 font-bold uppercase tracking-[0.1em] mt-0.5 opacity-70">
                Artist Name • Album Name
            </p>
        </div>

        <div className="flex items-center gap-8">
            <div className="hidden md:flex flex-col items-end opacity-40 group-hover:opacity-100 transition-opacity">
                <span className="text-[9px] text-gray-500 font-black uppercase">Streams</span>
                <span className="text-xs text-gray-300 font-mono font-bold">{(index * 1.2).toFixed(1)}M</span>
            </div>
            <div className="flex items-center gap-6 opacity-0 group-hover:opacity-100 transition-all translate-x-4 group-hover:translate-x-0">
                <Heart size={16} className="text-purple-500 fill-purple-500 cursor-pointer" />
                <span className="text-sm font-mono text-gray-400 font-bold">3:42</span>
                <MoreHorizontal size={18} className="text-gray-500 hover:text-white cursor-pointer" />
            </div>
        </div>
    </div>
)