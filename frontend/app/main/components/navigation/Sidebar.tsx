'use client'

import { useState } from 'react'
import { motion } from 'framer-motion'
import Link from 'next/link'
import { usePathname } from 'next/navigation'
import {
    LayoutDashboard,
    MessageSquare,
    Search,
    Settings,
    Users,
    Rss,
    Bell,
    LogOut,
    Zap,
    Music,
    Play,
    SkipBack,
    SkipForward,
    Volume2
} from 'lucide-react'
import { cn } from '@/app/lib/utils'
import { UserProfileModal } from '../modal/userProfileModal/UserProfileModal'
import { NotificationsModal } from '../modal/notificationsModal/NotificationsModal'
import { NavItem as NavItemType } from './types'
import { useLogout } from './useLogout'
import { useProfile } from './useProfile'

const PRIMARY_NAV: NavItemType[] = [
    { id: 'feed', icon: LayoutDashboard, label: 'Feed', href: '/main' },
    { id: 'messages', icon: MessageSquare, label: 'Messages', href: '/main/mes' },
    { id: 'music', icon: Music, label: 'Music', href: '/main/music' },
    { id: 'friends', icon: Users, label: 'Friends', href: '/main/friends' },
    { id: 'stories', icon: Rss, label: 'Stories', href: '/main/stories' },
]

const MusicOverlay = () => {
    const [volume, setVolume] = useState(80)

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

                    <div className="flex-1 min-w-0">
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
                        <motion.button whileHover={{ scale: 1.1 }} whileTap={{ scale: 0.9 }} className="p-1.5 text-gray-400 hover:text-white hover:bg-white/5 rounded-lg transition-colors">
                            <SkipBack className="w-3.5 h-3.5 fill-current" />
                        </motion.button>
                        <motion.button whileHover={{ scale: 1.1 }} whileTap={{ scale: 0.9 }} className="p-1.5 text-gray-400 hover:text-white hover:bg-white/5 rounded-lg transition-colors">
                            <SkipForward className="w-3.5 h-3.5 fill-current" />
                        </motion.button>
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

const NavItem = ({ item, pathname }: { item: NavItemType, pathname: string | null }) => {
    const isActive = pathname === item.href || (item.href !== '/main' && pathname?.startsWith(item.href))

    return (
        <Link href={item.href} className="block w-full">
            <motion.button
                whileHover={{ x: 4 }}
                whileTap={{ scale: 0.98 }}
                className={cn(
                    "w-full flex items-center gap-4 px-4 py-3 rounded-2xl transition-all duration-300 group relative",
                    isActive
                        ? "bg-purple-600/10 text-purple-400"
                        : "text-gray-500 hover:text-white hover:bg-white/5"
                )}
            >
                <item.icon className={cn(
                    "w-5 h-5 transition-transform duration-300 group-hover:scale-110 z-10",
                    isActive && "text-purple-500"
                )} />
                <span className="font-bold text-sm hidden lg:block tracking-wide z-10">{item.label}</span>

                {isActive && (
                    <motion.div
                        layoutId="activeNavIndicator"
                        className="absolute left-0 w-1 h-5 bg-purple-500 rounded-r-full"
                        transition={{ type: "spring", stiffness: 350, damping: 30 }}
                    />
                )}
            </motion.button>
        </Link>
    )
}

export default function Sidebar() {
    const pathname = usePathname()
    const { logout } = useLogout()
    const { user } = useProfile()
    const [isProfileOpen, setIsProfileOpen] = useState(false)
    const [isNotifOpen, setIsNotifOpen] = useState(false)

    return (
        <>
            <aside className="fixed left-0 top-0 h-full w-20 lg:w-64 bg-[#0d0d0f]/80 backdrop-blur-2xl border-r border-purple-500/10 z-50 flex flex-col transition-all duration-500">
                <div className="p-6 flex items-center gap-3">
                    <motion.div
                        animate={{ rotate: [0, 10, 0] }}
                        transition={{ repeat: Infinity, duration: 5 }}
                        className="w-9 h-9 bg-gradient-to-tr from-purple-600 to-blue-500 rounded-xl flex items-center justify-center shadow-lg shadow-purple-500/20 shrink-0"
                    >
                        <Zap className="w-5 h-5 text-white fill-white" />
                    </motion.div>
                    <span className="text-lg font-black bg-clip-text text-transparent bg-gradient-to-r from-white to-gray-500 hidden lg:block tracking-tighter">
                        PULSE
                    </span>
                </div>

                <div className="px-4 mb-4">
                    <div className="relative group">
                        <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500 group-focus-within:text-purple-400 transition-colors" />
                        <input
                            type="text"
                            placeholder="Search..."
                            className="w-full bg-white/5 border border-white/5 focus:border-purple-500/30 focus:bg-white/10 rounded-xl py-2.5 pl-11 pr-4 text-sm text-white outline-none transition-all hidden lg:block"
                        />
                        <div className="lg:hidden flex justify-center">
                            <motion.button whileTap={{ scale: 0.9 }} className="p-3 text-gray-500 hover:text-white bg-white/5 rounded-xl transition-all">
                                <Search className="w-5 h-5" />
                            </motion.button>
                        </div>
                    </div>
                </div>

                <nav className="flex-1 px-4 py-2 space-y-1">
                    {PRIMARY_NAV.map((item) => (
                        <NavItem key={item.id} item={item} pathname={pathname} />
                    ))}
                </nav>

                <div className="mt-auto py-4">
                    <MusicOverlay />

                    <div className="px-4 space-y-4">
                        <motion.button
                            whileHover={{ scale: 1.02 }}
                            whileTap={{ scale: 0.98 }}
                            onClick={() => setIsProfileOpen(true)}
                            className="w-full flex items-center gap-3 p-2 bg-white/5 rounded-2xl border border-white/5 hidden lg:flex hover:bg-white/10 transition-colors cursor-pointer group"
                        >
                            <div className="w-10 h-10 rounded-full border-2 border-purple-500/20 p-0.5 shrink-0 group-hover:border-purple-500/50 transition-all">
                                {user?.avatarUrl ? (
                                    <img
                                        src={user.avatarUrl}
                                        className="w-full h-full rounded-full object-cover"
                                        alt="Profile"
                                    />
                                ) : (
                                    <div className="w-full h-full rounded-full bg-purple-600/20 flex items-center justify-center text-xs text-purple-400 font-bold">
                                        {user?.username?.charAt(0).toUpperCase()}
                                    </div>
                                )}
                            </div>
                            <div className="flex flex-col overflow-hidden text-left">
                                <span className="text-sm font-bold text-white truncate">
                                    {user?.displayName || user?.username || 'Loading...'}
                                </span>
                            </div>
                        </motion.button>

                        <div className="flex lg:flex-row flex-col items-center justify-around bg-gray-800/20 rounded-2xl p-1 border border-white/5">
                            <div className="relative">
                                <motion.button
                                    whileHover={{ scale: 1.1 }}
                                    whileTap={{ scale: 0.9 }}
                                    onClick={() => setIsNotifOpen(!isNotifOpen)}
                                    className={cn(
                                        "p-3 transition-colors relative",
                                        isNotifOpen ? "text-purple-400" : "text-gray-500 hover:text-purple-400"
                                    )}
                                >
                                    <Bell className="w-5 h-5" />
                                    <span className="absolute top-2.5 right-2.5 w-1.5 h-1.5 bg-purple-500 rounded-full border border-[#0d0d0f]" />
                                </motion.button>

                                <NotificationsModal isOpen={isNotifOpen} onClose={() => setIsNotifOpen(false)} />
                            </div>

                            <Link href="/main/settings">
                                <motion.button whileHover={{ scale: 1.1 }} whileTap={{ scale: 0.9 }} className="p-3 text-gray-500 hover:text-purple-400 transition-colors">
                                    <Settings className="w-5 h-5" />
                                </motion.button>
                            </Link>

                            <motion.button
                                whileHover={{ scale: 1.1 }}
                                whileTap={{ scale: 0.9 }}
                                onClick={logout}
                                className="p-3 text-gray-500 hover:text-red-400 transition-colors"
                            >
                                <LogOut className="w-5 h-5" />
                            </motion.button>
                        </div>
                    </div>
                </div>
            </aside>

            <UserProfileModal isOpen={isProfileOpen} onClose={() => setIsProfileOpen(false)} />
        </>
    )
}