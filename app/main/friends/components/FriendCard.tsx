'use client'
import { motion } from 'framer-motion'
import { MessageCircle, MoreVertical, Shield } from 'lucide-react'
import { cn } from '@/app/lib/utils'

interface FriendProps {
    friend: {
        id: number
        name: string
        avatar: string
        online: boolean
        username: string
        hasStory: boolean
    }
    index: number
}

export const FriendCardSkeleton = () => (
    <div className="flex items-center gap-5 p-5 rounded-[2rem] border border-transparent">
        <div className="relative shrink-0">
            <div className="w-14 h-14 rounded-[1.2rem] bg-white/5 animate-pulse" />
        </div>
        <div className="flex-1 space-y-2">
            <div className="h-4 w-24 bg-white/10 rounded-full animate-pulse" />
            <div className="h-3 w-16 bg-white/5 rounded-full animate-pulse" />
        </div>
        <div className="flex gap-3">
            <div className="w-11 h-11 rounded-2xl bg-white/5 animate-pulse" />
            <div className="w-11 h-11 rounded-2xl bg-white/5 animate-pulse" />
        </div>
    </div>
)

export const FriendCard = ({ friend, index }: FriendProps) => (
    <motion.div
        initial={{ opacity: 0, x: -10 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ delay: index * 0.05 }}
        className="flex items-center gap-5 p-5 rounded-[2rem] hover:bg-white/[0.02] border border-transparent hover:border-white/5 transition-all group"
    >
        <div className="relative shrink-0">
            <div className={cn(
                "relative w-14 h-14 rounded-[1.2rem] p-[2px] transition-all duration-500",
                friend.hasStory
                    ? "bg-gradient-to-tr from-purple-500 to-pink-500 shadow-[0_0_15px_rgba(168,85,247,0.2)]"
                    : "bg-white/10"
            )}>
                <img
                    src={friend.avatar}
                    className="w-full h-full rounded-[1.1rem] object-cover border-2 border-[#0a0a0c]"
                    alt={friend.name}
                />
            </div>
            {friend.online && (
                <div className="absolute -bottom-0.5 -right-0.5 w-4 h-4 bg-green-500 border-4 border-[#0a0a0c] rounded-full z-10" />
            )}
        </div>

        <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2">
                <h3 className="font-bold text-base text-white">{friend.name}</h3>
                <Shield className="w-3.5 h-3.5 text-purple-500/50" />
            </div>
            <p className="text-xs text-gray-500 font-medium mt-0.5">{friend.username}</p>
        </div>

        <div className="flex items-center gap-3">
            <button className="p-3 bg-[#1a1a1e] hover:bg-purple-600 text-gray-400 hover:text-white rounded-2xl transition-all transform hover:scale-110">
                <MessageCircle className="w-5 h-5" />
            </button>
            <button className="p-3 bg-[#1a1a1e] hover:bg-white/10 text-gray-400 rounded-2xl transition-all">
                <MoreVertical className="w-5 h-5" />
            </button>
        </div>
    </motion.div>
)