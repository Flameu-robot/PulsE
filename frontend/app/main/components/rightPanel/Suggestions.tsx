'use client'

import { motion } from 'framer-motion'
import {
    TrendingUp,
    Zap,
    MessageSquare,
    Users,
    ChevronRight,
    ArrowUp
} from 'lucide-react'
import { cn } from '@/app/lib/utils'

export default function Suggestions() {
    return (
        <div className="space-y-8 sticky top-10">
            <section>
                <h3 className="text-[10px] font-black text-gray-500 uppercase tracking-[0.2em] mb-4 px-2">
                    Your Performance
                </h3>
                <div className="grid grid-cols-2 gap-3">
                    <div className="bg-white/[0.03] border border-white/[0.05] p-4 rounded-[1.5rem] hover:border-purple-500/30 transition-colors cursor-default group">
                        <Zap className="w-4 h-4 text-yellow-500 mb-2 group-hover:scale-110 transition-transform" />
                        <div className="text-xl font-bold">1.2k</div>
                        <div className="text-[10px] text-gray-500 font-medium">Reach</div>
                    </div>

                    <div className="bg-white/[0.03] border border-white/[0.05] p-4 rounded-[1.5rem] hover:border-blue-500/30 transition-colors cursor-default group">
                        <Users className="w-4 h-4 text-blue-500 mb-2 group-hover:scale-110 transition-transform" />
                        <div className="flex items-end gap-1.5">
                            <div className="text-xl font-bold">842</div>
                            <div className="text-[10px] text-green-500 font-bold mb-1 flex items-center">
                                <ArrowUp className="w-2 h-2" />
                                12
                            </div>
                        </div>
                        <div className="text-[10px] text-gray-500 font-medium">Followers</div>
                    </div>
                </div>
            </section>

            <section>
                <div className="flex items-center justify-between mb-4 px-2">
                    <h3 className="text-[10px] font-black text-gray-500 uppercase tracking-[0.2em]">
                        Live Trends
                    </h3>
                    <TrendingUp className="w-3 h-3 text-purple-500" />
                </div>
                <div className="space-y-1">
                    {[
                        { tag: 'nextjs15', posts: '2.4k', growth: '+12%' },
                        { tag: 'puls_e', posts: '1.8k', growth: '+54%' },
                        { tag: 'ui_ux', posts: '842', growth: '+3%' },
                    ].map((trend) => (
                        <button
                            key={trend.tag}
                            className="w-full flex items-center justify-between p-3 rounded-2xl hover:bg-white/[0.03] transition-all group cursor-pointer border border-transparent hover:border-white/5"
                        >
                            <div className="text-left">
                                <div className="text-sm font-bold text-gray-300 group-hover:text-purple-400 transition-colors">#{trend.tag}</div>
                                <div className="text-[10px] text-gray-500">{trend.posts} posts</div>
                            </div>
                            <span className="text-[10px] font-bold text-green-500 bg-green-500/10 px-2 py-1 rounded-lg border border-green-500/20">
                                {trend.growth}
                            </span>
                        </button>
                    ))}
                </div>
            </section>

            <section>
                <h3 className="text-[10px] font-black text-gray-500 uppercase tracking-[0.2em] mb-4 px-2">
                    Global Pulse
                </h3>
                <motion.div
                    whileHover={{ scale: 1.02, y: -2 }}
                    className="relative p-5 rounded-[2.2rem] bg-gradient-to-br from-indigo-600/10 via-purple-600/10 to-transparent border border-purple-500/20 group cursor-pointer overflow-hidden shadow-lg shadow-purple-500/5"
                >
                    <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 group-hover:rotate-12 transition-all duration-500">
                        <MessageSquare className="w-14 h-14 text-purple-400" />
                    </div>

                    <div className="relative z-10">
                        <div className="flex items-center gap-2 mb-3">
                            <div className="flex -space-x-2">
                                {[1, 2, 3].map((i) => (
                                    <div key={i} className="w-7 h-7 rounded-full border-2 border-[#0a0a0c] bg-gray-800 overflow-hidden shadow-md">
                                        <img src={`https://i.pravatar.cc/100?u=${i+40}`} alt="user" />
                                    </div>
                                ))}
                            </div>
                            <span className="text-[10px] font-bold text-purple-300 bg-purple-500/10 px-2 py-0.5 rounded-full border border-purple-500/20">
                                +142 online
                            </span>
                        </div>

                        <div className="flex items-center justify-between">
                            <span className="text-xs font-bold text-white tracking-wide">Enter Global Chat</span>
                            <div className="w-8 h-8 rounded-full bg-purple-600 flex items-center justify-center text-white shadow-lg shadow-purple-600/40 group-hover:translate-x-1 transition-transform">
                                <ChevronRight className="w-4 h-4" />
                            </div>
                        </div>
                    </div>
                </motion.div>
            </section>
        </div>
    )
}