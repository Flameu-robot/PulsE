import React from 'react'
import { Play } from 'lucide-react'

const ALBUM_COVERS = [
    "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=400&q=80",
    "https://images.unsplash.com/photo-1493225255756-d9584f8606e9?w=400&q=80",
    "https://images.unsplash.com/photo-1459749411177-042180ceea72?w=400&q=80",
    "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=400&q=80",
    "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&q=80",
    "https://images.unsplash.com/photo-1514525253344-99a4299965cc?w=400&q=80"
]

export const AlbumCard = ({ id, onClick }: { id: number; onClick: (id: number) => void }) => (
    <div className="group relative cursor-pointer" onClick={() => onClick(id)}>
        <div className="relative aspect-square mb-5 overflow-hidden rounded-2xl bg-[#16161a] border border-white/[0.05] shadow-2xl">
            <div className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition-all duration-500 z-10 flex items-center justify-center backdrop-blur-sm">
                <div className="w-14 h-14 rounded-full bg-purple-600 text-white flex items-center justify-center scale-90 group-hover:scale-100 transition-transform duration-300 shadow-[0_0_30px_rgba(168,85,247,0.5)]">
                    <Play size={24} className="fill-current ml-1" />
                </div>
            </div>
            <img
                src={ALBUM_COVERS[(id - 1) % ALBUM_COVERS.length]}
                className="w-full h-full object-cover transition-transform duration-1000 group-hover:scale-110 group-hover:rotate-1"
                alt="Album Cover"
            />
        </div>
        <h3 className="text-[15px] font-bold text-white truncate group-hover:text-purple-400 transition-colors tracking-tight">
            Cyberpunk Anthology
        </h3>
        <p className="text-[11px] text-gray-500 font-bold uppercase tracking-widest mt-1 opacity-80">
            Night City Collective
        </p>
    </div>
)