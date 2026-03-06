'use client'

import React from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { X, Bell, Trash2, Clock, CheckCircle2 } from 'lucide-react'
import { NotificationsModalProps, Notification } from './types'
import { useNotifications } from './useNotifications'

export const NotificationsModal = ({ isOpen, onClose }: NotificationsModalProps) => {
    const { notifications, deleteAll, deleteOne, newItems, oldItems } = useNotifications()

    return (
        <AnimatePresence>
            {isOpen && (
                <>
                    <div className="fixed inset-0 z-[110]" onClick={onClose} />
                    <motion.div
                        initial={{ opacity: 0, scale: 0.9, x: -20, y: 20 }}
                        animate={{ opacity: 1, scale: 1, x: 0, y: 0 }}
                        exit={{ opacity: 0, scale: 0.9, x: -20, y: 20 }}
                        className="absolute bottom-full left-0 lg:left-full lg:bottom-0 mb-4 lg:mb-0 lg:ml-4 w-[320px] sm:w-[380px] bg-[#0d0d0f] border border-white/10 rounded-[32px] shadow-[0_20px_50px_rgba(0,0,0,0.7)] z-[111] overflow-hidden origin-bottom-left lg:origin-left"
                    >
                        <div className="p-5 border-b border-white/5 flex items-center justify-between bg-white/[0.01]">
                            <div className="flex items-center gap-3">
                                <div className="w-8 h-8 rounded-full bg-purple-500/10 flex items-center justify-center text-purple-400">
                                    <Bell className="w-4 h-4" />
                                </div>
                                <h3 className="text-white text-sm font-bold">Notifications</h3>
                            </div>
                            <div className="flex items-center gap-1">
                                {notifications.length > 0 && (
                                    <motion.button
                                        whileHover={{ scale: 1.1, color: '#ef4444' }}
                                        whileTap={{ scale: 0.9 }}
                                        onClick={deleteAll}
                                        className="p-2 text-gray-500 transition-colors"
                                    >
                                        <Trash2 className="w-4 h-4" />
                                    </motion.button>
                                )}
                                <motion.button
                                    whileHover={{ scale: 1.1 }}
                                    whileTap={{ scale: 0.9 }}
                                    onClick={onClose}
                                    className="p-2 text-gray-400 hover:text-white"
                                >
                                    <X className="w-4 h-4" />
                                </motion.button>
                            </div>
                        </div>

                        <div className="max-h-[350px] overflow-y-auto no-scrollbar p-3 space-y-4">
                            {notifications.length === 0 ? (
                                <div className="py-12 text-center space-y-3">
                                    <CheckCircle2 className="w-8 h-8 text-gray-800 mx-auto" />
                                    <p className="text-gray-600 text-xs font-medium">No new notifications</p>
                                </div>
                            ) : (
                                <>
                                    {newItems.length > 0 && (
                                        <div className="space-y-2">
                                            <p className="text-[9px] uppercase font-black text-purple-500 tracking-[0.2em] ml-2 mb-1">New</p>
                                            {newItems.map(item => (
                                                <NotificationItem key={item.id} item={item} onDelete={() => deleteOne(item.id)} />
                                            ))}
                                        </div>
                                    )}

                                    {oldItems.length > 0 && (
                                        <div className="space-y-2">
                                            <p className="text-[9px] uppercase font-black text-gray-600 tracking-[0.2em] ml-2 mb-1">Earlier</p>
                                            {oldItems.map(item => (
                                                <NotificationItem key={item.id} item={item} onDelete={() => deleteOne(item.id)} />
                                            ))}
                                        </div>
                                    )}
                                </>
                            )}
                        </div>
                    </motion.div>
                </>
            )}
        </AnimatePresence>
    )
}

const NotificationItem = ({ item, onDelete }: { item: Notification, onDelete: () => void }) => (
    <motion.div
        layout
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.95 }}
        className="group relative p-3.5 bg-white/[0.02] border border-white/[0.03] rounded-2xl hover:bg-white/[0.04] transition-all"
    >
        <div className="flex justify-between items-start gap-3">
            <div className="space-y-1">
                <h4 className="text-xs font-bold text-white tracking-tight">{item.title}</h4>
                <p className="text-[11px] text-gray-500 leading-snug">{item.message}</p>
                <div className="flex items-center gap-1 text-[9px] text-gray-600 font-bold pt-1 uppercase">
                    <Clock className="w-2.5 h-2.5" />
                    {item.time}
                </div>
            </div>
            <motion.button
                whileHover={{ scale: 1.2, color: '#f87171' }}
                onClick={onDelete}
                className="opacity-0 group-hover:opacity-100 p-1 text-gray-700 transition-all"
            >
                <X className="w-3 h-3" />
            </motion.button>
        </div>
        {item.isNew && <div className="absolute top-4 right-4 w-1 h-1 bg-purple-500 rounded-full shadow-[0_0_8px_rgba(168,85,247,0.8)]" />}
    </motion.div>
)