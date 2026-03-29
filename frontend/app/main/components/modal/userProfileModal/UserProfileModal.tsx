'use client';

import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Settings, LogOut, Zap, Star, Camera, Check, ArrowLeft, AlertCircle } from 'lucide-react';
import { UserProfileModalProps } from './types';
import { useUserProfile } from './useUserProfile';

// Компонент CurrentlyPlaying оставлен без изменений
const CurrentlyPlaying = () => {
    return (
        <div className="mb-6 px-1">
            <div className="relative group p-3 bg-white/[0.03] border border-white/[0.05] rounded-[24px] sm:rounded-[28px] flex items-center gap-4 overflow-hidden">
                <div className="absolute inset-0 bg-gradient-to-r from-purple-500/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
                <div className="relative">
                    <div className="w-10 h-10 sm:w-12 sm:h-12 rounded-xl sm:rounded-2xl overflow-hidden border border-white/10">
                        <img
                            src="https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=200"
                            alt="Album art"
                            className="w-full h-full object-cover"
                        />
                    </div>
                </div>
                <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                        <span className="text-[9px] sm:text-[10px] font-black text-purple-400 uppercase tracking-[0.2em]">Now Playing</span>
                        <div className="flex gap-0.5 items-end h-2">
                            {[0.4, 0.8, 0.6, 0.9].map((h, i) => (
                                <motion.div
                                    key={i}
                                    animate={{ height: ['20%', '100%', '20%'] }}
                                    transition={{ repeat: Infinity, duration: 0.5, delay: i * 0.1 }}
                                    className="w-0.5 bg-purple-500/50 rounded-full"
                                    style={{ height: `${h * 100}%` }}
                                />
                            ))}
                        </div>
                    </div>
                    <h4 className="text-white text-xs sm:text-sm font-bold truncate">Starboy</h4>
                    <p className="text-gray-500 text-[10px] sm:text-[11px] font-medium truncate">The Weeknd • 02:45</p>
                </div>
            </div>
        </div>
    );
};

const UsersIcon = (props: any) => (
    <svg {...props} xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
        <circle cx="9" cy="7" r="4"/>
        <path d="M22 21v-2a4 4 0 0 0-3-3.87"/>
        <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
    </svg>
);

export const UserProfileModal = ({ isOpen, onClose }: UserProfileModalProps) => {
    const {
        isEditing,
        setIsEditing,
        fileInputRef,
        formik,
        handleAvatarChange,
        handleCancel,
        inputClasses,
        isLoading,
        userInitial,
    } = useUserProfile();

    return (
        <AnimatePresence>
            {isOpen && (
                <>
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        onClick={!isEditing ? onClose : undefined}
                        className="fixed inset-0 bg-[#000]/80 backdrop-blur-md z-[100]"
                    />

                    <motion.div
                        initial={{ opacity: 0, scale: 0.95, y: 20 }}
                        animate={{ opacity: 1, scale: 1, y: 0 }}
                        exit={{ opacity: 0, scale: 0.95, y: 20 }}
                        className="fixed left-0 right-0 bottom-0 sm:left-1/2 sm:top-1/2 sm:bottom-auto sm:-translate-x-1/2 sm:-translate-y-1/2 w-full max-w-lg bg-[#0d0d0f] border-t sm:border border-white/5 rounded-t-[32px] sm:rounded-[40px] overflow-hidden z-[101] shadow-[0_0_50px_rgba(168,85,247,0.15)] max-h-[90vh] overflow-y-auto no-scrollbar"
                    >
                        {/* остальной код компонента без изменений */}
                        <div className="relative h-32 sm:h-48 overflow-hidden">
                            <motion.div
                                animate={{ backgroundPosition: ['0% 0%', '100% 100%'] }}
                                transition={{ duration: 10, repeat: Infinity, repeatType: 'mirror' }}
                                className="absolute inset-0 bg-[url('https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800')] bg-cover bg-center scale-110"
                            />
                            <div className="absolute inset-0 bg-gradient-to-t from-[#0d0d0f] via-[#0d0d0f]/20 to-transparent" />
                            {!isEditing && (
                                <motion.button
                                    whileHover={{ scale: 1.1 }}
                                    whileTap={{ scale: 0.9 }}
                                    onClick={onClose}
                                    className="absolute top-4 right-4 sm:top-6 sm:right-6 p-2 bg-black/40 hover:bg-white/10 backdrop-blur-xl border border-white/10 rounded-full text-white transition-all z-10"
                                >
                                    <X className="w-5 h-5" />
                                </motion.button>
                            )}
                        </div>

                        <div className="px-6 sm:px-10 pb-8 sm:pb-10 relative">
                            <div className="flex flex-col items-center text-center -mt-20 sm:-mt-28 mb-6">
                                <div className="relative group w-24 sm:w-32 mb-4">
                                    <div className="w-24 h-24 sm:w-32 sm:h-32 rounded-[28px] sm:rounded-[35px] border-[4px] sm:border-[6px] border-[#0d0d0f] overflow-hidden shadow-2xl relative bg-[#1a1a1e] flex items-center justify-center">
                                        {formik.values.avatarUrl ? (
                                            <img
                                                src={formik.values.avatarUrl}
                                                className={`w-full h-full object-cover transition-transform duration-700 ${isLoading ? 'animate-pulse opacity-50' : 'opacity-100'}`}
                                                alt="Avatar"
                                            />
                                        ) : (
                                            <div className="text-5xl sm:text-7xl font-black text-white/20 select-none">
                                                {isLoading ? '...' : userInitial}
                                            </div>
                                        )}
                                        {isEditing && (
                                            <button
                                                type="button"
                                                onClick={() => fileInputRef.current?.click()}
                                                className="absolute inset-0 bg-black/60 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity backdrop-blur-sm"
                                            >
                                                <Camera className="w-8 h-8 text-white" />
                                            </button>
                                        )}
                                    </div>
                                    {!isEditing && <div className="absolute bottom-1 right-1 w-4 h-4 sm:w-6 sm:h-6 bg-green-500 border-2 sm:border-4 border-[#0d0d0f] rounded-full" />}
                                    <input type="file" ref={fileInputRef} className="hidden" accept="image/*" onChange={handleAvatarChange} />
                                </div>

                                <h2 className="text-3xl sm:text-4xl font-black text-white tracking-tighter">
                                    {isLoading ? '...' : formik.values.username}
                                </h2>
                            </div>

                            <AnimatePresence mode="wait">
                                {!isEditing ? (
                                    <motion.div
                                        key="view"
                                        initial={{ opacity: 0, x: -10 }}
                                        animate={{ opacity: 1, x: 0 }}
                                        exit={{ opacity: 0, x: 10 }}
                                    >
                                        {formik.values.bio && (
                                            <div className="mb-6 p-4 bg-white/[0.02] border border-white/[0.05] rounded-2xl text-left">
                                                <p className="text-gray-400 text-xs sm:text-sm leading-relaxed">
                                                    {formik.values.bio}
                                                </p>
                                            </div>
                                        )}

                                        <div className="grid grid-cols-3 gap-2 sm:gap-3 mb-6 sm:mb-8">
                                            {[
                                                { label: 'Followers', count: '12.4k', icon: UsersIcon },
                                                { label: 'Ranking', count: '#12', icon: Star },
                                                { label: 'Reach', count: '84k', icon: Zap },
                                            ].map((stat) => (
                                                <div
                                                    key={stat.label}
                                                    className="relative group overflow-hidden p-3 sm:p-4 bg-white/[0.02] border border-white/[0.05] rounded-2xl sm:rounded-3xl transition-all hover:bg-white/[0.04]"
                                                >
                                                    <div className="text-white text-sm sm:text-lg font-black mb-0.5">{stat.count}</div>
                                                    <div className="text-[8px] sm:text-[9px] text-gray-500 uppercase font-bold tracking-widest">
                                                        {stat.label}
                                                    </div>
                                                </div>
                                            ))}
                                        </div>

                                        <CurrentlyPlaying />

                                        <div className="grid grid-cols-2 gap-3">
                                            <motion.button
                                                whileHover={{ scale: 1.02 }}
                                                whileTap={{ scale: 0.98 }}
                                                onClick={() => setIsEditing(true)}
                                                disabled={isLoading}
                                                className="flex items-center justify-center gap-2 p-3 sm:p-4 bg-white/[0.02] border border-white/[0.05] rounded-[20px] sm:rounded-[24px] text-gray-400 hover:text-white hover:bg-white/[0.05] disabled:opacity-50 transition-all font-bold text-sm"
                                            >
                                                <Settings className="w-4 h-4" /> Edit
                                            </motion.button>

                                            <motion.button
                                                whileHover={{ scale: 1.02 }}
                                                whileTap={{ scale: 0.98 }}
                                                className="flex items-center justify-center gap-2 p-3 sm:p-4 bg-red-500/5 border border-red-500/10 rounded-[20px] sm:rounded-[24px] text-red-500/80 hover:bg-red-500/10 hover:text-red-500 transition-all font-bold text-sm"
                                            >
                                                <LogOut className="w-4 h-4" /> Exit
                                            </motion.button>
                                        </div>
                                    </motion.div>
                                ) : (
                                    <motion.form
                                        key="edit"
                                        initial={{ opacity: 0, x: 10 }}
                                        animate={{ opacity: 1, x: 0 }}
                                        exit={{ opacity: 0, x: -10 }}
                                        onSubmit={formik.handleSubmit}
                                        className="space-y-4"
                                    >
                                        <div className="space-y-1">
                                            <label className="text-[10px] uppercase font-black text-gray-500 ml-1">Username</label>
                                            <div className="relative">
                                                <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500 text-sm">@</span>
                                                <input
                                                    name="username"
                                                    className={`${inputClasses('username')} pl-7`}
                                                    {...formik.getFieldProps('username')}
                                                />
                                            </div>
                                            {formik.touched.username && formik.errors.username && (
                                                <motion.div
                                                    initial={{ opacity: 0, y: -5 }}
                                                    animate={{ opacity: 1, y: 0 }}
                                                    className="flex items-center gap-1.5 text-red-400 text-[10px] font-bold ml-1"
                                                >
                                                    <AlertCircle className="w-3 h-3" /> {formik.errors.username as string}
                                                </motion.div>
                                            )}
                                        </div>

                                        <div className="space-y-1">
                                            <label className="text-[10px] uppercase font-black text-gray-500 ml-1">Bio</label>
                                            <textarea
                                                name="bio"
                                                className={inputClasses('bio')}
                                                rows={3}
                                                {...formik.getFieldProps('bio')}
                                            />
                                            {formik.touched.bio && formik.errors.bio && (
                                                <motion.div
                                                    initial={{ opacity: 0, y: -5 }}
                                                    animate={{ opacity: 1, y: 0 }}
                                                    className="flex items-center gap-1.5 text-red-400 text-[10px] font-bold ml-1"
                                                >
                                                    <AlertCircle className="w-3 h-3" /> {formik.errors.bio as string}
                                                </motion.div>
                                            )}
                                        </div>

                                        <div className="grid grid-cols-2 gap-3 pt-2">
                                            <motion.button
                                                type="button"
                                                whileHover={{ scale: 1.02 }}
                                                whileTap={{ scale: 0.98 }}
                                                onClick={handleCancel}
                                                className="flex items-center justify-center gap-2 p-4 bg-white/[0.02] border border-white/5 rounded-2xl text-gray-400 font-bold text-sm"
                                            >
                                                <ArrowLeft className="w-4 h-4" /> Cancel
                                            </motion.button>

                                            <motion.button
                                                type="submit"
                                                whileHover={{ scale: 1.02, backgroundColor: '#7e22ce' }}
                                                whileTap={{ scale: 0.98 }}
                                                disabled={!formik.isValid || !formik.dirty || isLoading}
                                                className="flex items-center justify-center gap-2 p-4 bg-purple-600 disabled:opacity-50 disabled:cursor-not-allowed rounded-2xl text-white font-bold text-sm shadow-lg shadow-purple-600/20 transition-all"
                                            >
                                                <Check className="w-4 h-4" /> {isLoading ? 'Saving...' : 'Save'}
                                            </motion.button>
                                        </div>
                                    </motion.form>
                                )}
                            </AnimatePresence>
                        </div>
                    </motion.div>
                </>
            )}
        </AnimatePresence>
    );
};