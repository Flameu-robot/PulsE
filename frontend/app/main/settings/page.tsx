'use client'

import React, { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import {
    User,
    Bell,
    Lock,
    Palette,
    Camera,
    Moon,
    Zap,
    Check,
    Shield,
    Globe
} from 'lucide-react'
import { cn } from '@/app/lib/utils'

export default function SettingsPage() {
    const [activeTab, setActiveTab] = useState('profile')
    const [isSaving, setIsSaving] = useState(false)

    const MENU_ITEMS = [
        { id: 'profile', label: 'Profile', icon: User },
        { id: 'notifications', label: 'Notifications', icon: Bell },
        { id: 'security', label: 'Security', icon: Lock },
        { id: 'appearance', label: 'Appearance', icon: Palette },
    ]

    const handleSave = () => {
        setIsSaving(true)
        setTimeout(() => setIsSaving(false), 2000)
    }

    return (
        <div className="h-full bg-[#09090b] text-white overflow-y-auto no-scrollbar">
            {/* Убрали mx-auto.
                Добавили ml-0 и небольшой padding, так как основной отступ
                уже контролируется в layout.tsx (pl-64)
            */}
            <div className="max-w-5xl py-10 px-8 lg:px-12">
                <motion.div
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    className="mb-10"
                >
                    <h1 className="text-[10px] font-black text-gray-500 uppercase tracking-[0.2em] mb-2 px-1">
                        Account Settings
                    </h1>
                    <div className="text-3xl font-bold tracking-tight px-1 text-white">Control Center</div>
                </motion.div>

                <div className="grid grid-cols-1 lg:grid-cols-[260px_1fr] gap-10">
                    {/* Навигация прижата влево */}
                    <aside className="space-y-1.5">
                        {MENU_ITEMS.map((item) => (
                            <button
                                key={item.id}
                                onClick={() => setActiveTab(item.id)}
                                className={cn(
                                    "w-full flex items-center gap-4 p-3.5 rounded-[1.2rem] transition-all duration-300 group relative",
                                    activeTab === item.id
                                        ? "bg-white/[0.05] border border-white/[0.08]"
                                        : "hover:bg-white/[0.02] border border-transparent text-gray-500 hover:text-gray-300"
                                )}
                            >
                                <div className={cn(
                                    "p-2 rounded-xl transition-colors",
                                    activeTab === item.id ? "bg-purple-600 text-white shadow-lg shadow-purple-600/20" : "bg-white/[0.03]"
                                )}>
                                    <item.icon size={16} />
                                </div>
                                <span className="text-sm font-bold tracking-tight">{item.label}</span>
                                {activeTab === item.id && (
                                    <motion.div
                                        layoutId="active-indicator"
                                        className="absolute right-4 w-1 h-4 bg-purple-500 rounded-full"
                                    />
                                )}
                            </button>
                        ))}
                    </aside>

                    {/* Основная панель */}
                    <main className="max-w-2xl">
                        <AnimatePresence mode="wait">
                            {activeTab === 'profile' ? (
                                <motion.div
                                    key="profile"
                                    initial={{ opacity: 0, y: 10 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    exit={{ opacity: 0, y: -10 }}
                                    className="space-y-6"
                                >
                                    {/* Секция профиля в стиле PostCard */}
                                    <section className="bg-white/[0.02] border border-white/[0.05] rounded-[2rem] p-8">
                                        <div className="flex items-center gap-6 mb-8">
                                            <div className="relative group">
                                                <div className="w-20 h-20 rounded-[1.5rem] overflow-hidden bg-gray-800 border border-white/[0.1]">
                                                    <img
                                                        src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200"
                                                        alt="Avatar"
                                                        className="w-full h-full object-cover"
                                                    />
                                                </div>
                                                <button className="absolute -bottom-1 -right-1 p-2 bg-purple-600 rounded-lg border-2 border-[#09090b] shadow-xl hover:scale-110 transition-transform">
                                                    <Camera size={12} />
                                                </button>
                                            </div>
                                            <div>
                                                <h3 className="text-xl font-bold">Alex Rivera</h3>
                                                <p className="text-xs text-purple-400 font-bold uppercase tracking-wider mt-0.5">Verified Pro</p>
                                            </div>
                                        </div>

                                        <div className="grid grid-cols-1 gap-5">
                                            <div className="space-y-2">
                                                <label className="text-[10px] font-black text-gray-500 uppercase tracking-widest ml-1">Display Name</label>
                                                <input type="text" defaultValue="Alex Rivera" className="w-full bg-white/[0.03] border border-white/[0.05] rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-purple-500/50 transition-colors" />
                                            </div>
                                            <div className="space-y-2">
                                                <label className="text-[10px] font-black text-gray-500 uppercase tracking-widest ml-1">Email</label>
                                                <input type="email" defaultValue="alex@pulse.io" className="w-full bg-white/[0.03] border border-white/[0.05] rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-purple-500/50 transition-colors" />
                                            </div>
                                        </div>
                                    </section>

                                    {/* Секция настроек */}
                                    <section className="bg-white/[0.02] border border-white/[0.05] rounded-[2rem] p-6 space-y-3">
                                        <ToggleRow icon={Moon} title="Dark Mode" active />
                                        <ToggleRow icon={Zap} title="Fast Animations" active />
                                        <ToggleRow icon={Globe} title="Public Profile" />
                                    </section>

                                    <div className="flex justify-start gap-4 pt-2">
                                        <button
                                            onClick={handleSave}
                                            className="px-8 py-3.5 bg-white text-black rounded-xl text-xs font-black uppercase tracking-widest hover:bg-purple-600 hover:text-white transition-all active:scale-95 flex items-center gap-2"
                                        >
                                            {isSaving ? <Check size={14} /> : 'Save Changes'}
                                        </button>
                                        <button className="px-6 py-3.5 text-xs font-bold text-gray-500 hover:text-white transition-colors">
                                            Cancel
                                        </button>
                                    </div>
                                </motion.div>
                            ) : (
                                <motion.div
                                    key="soon"
                                    initial={{ opacity: 0 }}
                                    animate={{ opacity: 1 }}
                                    className="h-64 flex flex-col items-center justify-center bg-white/[0.01] border border-dashed border-white/[0.05] rounded-[2rem] text-gray-600"
                                >
                                    <Shield size={32} className="mb-2 opacity-20" />
                                    <span className="text-[10px] font-black uppercase tracking-widest">Section Restricted</span>
                                </motion.div>
                            )}
                        </AnimatePresence>
                    </main>
                </div>
            </div>
        </div>
    )
}

function ToggleRow({ icon: Icon, title, active = false }: { icon: any, title: string, active?: boolean }) {
    const [enabled, setEnabled] = useState(active)
    return (
        <div
            onClick={() => setEnabled(!enabled)}
            className="flex items-center justify-between p-3.5 hover:bg-white/[0.02] rounded-xl transition-all cursor-pointer group"
        >
            <div className="flex items-center gap-3">
                <Icon size={16} className={enabled ? "text-purple-400" : "text-gray-500"} />
                <span className="text-sm font-medium">{title}</span>
            </div>
            <div className={cn(
                "w-9 h-5 rounded-full relative transition-all duration-300",
                enabled ? "bg-purple-600" : "bg-white/10"
            )}>
                <motion.div
                    animate={{ x: enabled ? 18 : 4 }}
                    className="absolute top-1 w-3 h-3 rounded-full bg-white shadow-sm"
                />
            </div>
        </div>
    )
}