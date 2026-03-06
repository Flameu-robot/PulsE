'use client'
import { useState, useEffect } from 'react'
import { Search } from 'lucide-react'
import { cn } from '@/app/lib/utils'
import { FriendCard, FriendCardSkeleton } from './components/FriendCard'
import { InvitePanel } from './components/InvitePanel'

const MOCK_FRIENDS = [
    { id: 1, name: 'Sarah Chen', avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150', online: true, username: '@sarahc', hasStory: true },
    { id: 2, name: 'Mike Ross', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150', online: true, username: '@mross', hasStory: false },
    { id: 3, name: 'Emma Watson', avatar: 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=150', online: false, username: '@emmaw', hasStory: true },
    { id: 4, name: 'James Wilson', avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150', online: false, username: '@jwilson', hasStory: false },
    { id: 5, name: 'Anna Novak', avatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150', online: true, username: '@anovak', hasStory: true },
]

export default function FriendsPage() {
    const [searchQuery, setSearchQuery] = useState('')
    const [isLoading, setIsLoading] = useState(true)

    useEffect(() => {
        const timer = setTimeout(() => setIsLoading(false), 1500)
        return () => clearTimeout(timer)
    }, [])

    return (
        <div className="flex h-full bg-[#0a0a0c] text-white overflow-hidden">
            <div className="flex-1 flex flex-col border-r border-white/5 overflow-hidden">
                <div className="p-8 space-y-8">
                    <div className="flex items-center justify-between">
                        <div className="space-y-1">
                            <h1 className="text-3xl font-bold tracking-tight">Friends</h1>
                            <div className="flex items-center gap-2">
                                <span className="flex h-2 w-2 rounded-full bg-green-500" />
                                <p className="text-sm text-gray-500 font-medium">142 Friends Total</p>
                            </div>
                        </div>
                        <div className="bg-[#121215] p-1 rounded-2xl border border-white/5 flex gap-1">
                            {['All', 'Online', 'Requests'].map((tab) => (
                                <button key={tab} className={cn(
                                    "px-5 py-2 text-[11px] font-black uppercase tracking-wider rounded-xl transition-all",
                                    tab === 'All' ? "bg-purple-600 text-white shadow-lg shadow-purple-600/20" : "text-gray-500 hover:text-gray-300"
                                )}> {tab} </button>
                            ))}
                        </div>
                    </div>

                    <div className="relative group">
                        <div className="absolute -inset-1 bg-purple-500/5 rounded-[1.6rem] blur-xl opacity-0 group-focus-within:opacity-100 transition-opacity" />
                        <Search className="absolute left-5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500 group-focus-within:text-purple-500 transition-colors z-10" />
                        <input
                            type="text"
                            placeholder="Find a friend by name or tag..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="relative w-full bg-[#121215] border border-white/5 rounded-[1.5rem] py-4 pl-12 pr-4 text-sm focus:outline-none focus:border-purple-500/30 transition-all z-10"
                        />
                    </div>
                </div>

                <div className="flex-1 overflow-y-auto no-scrollbar px-8 pb-10 space-y-3">
                    {isLoading ? (
                        Array.from({ length: 5 }).map((_, i) => (
                            <FriendCardSkeleton key={i} />
                        ))
                    ) : (
                        MOCK_FRIENDS.map((friend, i) => (
                            <FriendCard key={friend.id} friend={friend} index={i} />
                        ))
                    )}
                </div>
            </div>
            <InvitePanel />
        </div>
    )
}