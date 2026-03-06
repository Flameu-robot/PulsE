'use client'
import { useState } from 'react'
import { QrCode, Copy, Check, Link as LinkIcon, UserPlus, ArrowUpRight } from 'lucide-react'

export const InvitePanel = () => {
    const [copied, setCopied] = useState(false)
    const MY_INVITE_CODE = "PULSE-8821-X9"

    const handleCopy = () => {
        navigator.clipboard.writeText(MY_INVITE_CODE)
        setCopied(true)
        setTimeout(() => setCopied(false), 2000)
    }

    return (
        <div className="hidden xl:flex flex-col w-[400px] h-full p-8 bg-[#0a0a0c] space-y-8">
            <div className="space-y-4">
                <h2 className="text-[10px] font-black text-gray-500 uppercase tracking-[0.25em] px-2 flex items-center justify-between">
                    Your Invite Code <span className="h-px flex-1 bg-white/5 ml-4" />
                </h2>
                <div className="bg-[#121215] border border-white/5 rounded-[2.5rem] p-8 text-center relative overflow-hidden group shadow-2xl">
                    <div className="absolute -top-10 -right-10 w-32 h-32 bg-purple-600/10 blur-[50px] rounded-full group-hover:bg-purple-600/20 transition-all duration-700" />
                    <div className="inline-flex p-4 bg-purple-600/10 rounded-[2rem] mb-6 relative">
                        <QrCode className="w-8 h-8 text-purple-500" />
                        <div className="absolute -top-1 -right-1 w-3 h-3 bg-purple-500 rounded-full animate-pulse" />
                    </div>
                    <div className="space-y-2">
                        <p className="text-[10px] font-bold text-gray-500 uppercase tracking-[0.2em]">Personal Code</p>
                        <div onClick={handleCopy} className="cursor-pointer py-4 bg-white/5 border border-white/5 rounded-2xl flex items-center justify-center gap-3 hover:bg-white/10 transition-all group/code">
                            <span className="text-xl font-mono font-black tracking-widest text-white group-hover/code:text-purple-400">{MY_INVITE_CODE}</span>
                            {copied ? <Check className="w-4 h-4 text-green-500" /> : <Copy className="w-4 h-4 text-gray-600" />}
                        </div>
                    </div>
                </div>
            </div>

            <div className="space-y-4">
                <h2 className="text-[10px] font-black text-gray-500 uppercase tracking-[0.25em] px-2 flex items-center justify-between">
                    Invite Actions <span className="h-px flex-1 bg-white/5 ml-4" />
                </h2>
                <div className="grid grid-cols-1 gap-3">
                    <button className="flex items-center gap-4 p-5 bg-[#121215] hover:bg-[#1a1a1e] border border-white/5 rounded-[2rem] transition-all group">
                        <div className="w-12 h-12 bg-blue-500/10 rounded-2xl flex items-center justify-center text-blue-400 group-hover:scale-110 transition-transform">
                            <LinkIcon className="w-5 h-5" />
                        </div>
                        <div className="text-left flex-1 min-w-0">
                            <p className="text-sm font-bold text-white uppercase tracking-tight">Profile Link</p>
                            <p className="text-[10px] text-gray-500 truncate">pulse.io/invite/alex_j</p>
                        </div>
                        <ArrowUpRight className="w-4 h-4 text-gray-700 group-hover:text-white transition-colors" />
                    </button>
                    <div className="p-5 bg-[#121215] border border-white/5 rounded-[2rem] space-y-4">
                        <div className="flex items-center gap-3 text-gray-400">
                            <UserPlus className="w-4 h-4 text-purple-500" />
                            <span className="text-xs font-bold uppercase tracking-wider">Add by ID</span>
                        </div>
                        <div className="flex gap-2">
                            <input type="text" placeholder="XXXX-0000" className="flex-1 bg-[#0a0a0c] border border-white/5 rounded-xl py-3 px-4 text-xs font-mono focus:outline-none focus:border-purple-500/30 transition-all" />
                            <button className="px-5 bg-purple-600 hover:bg-purple-500 rounded-xl font-bold text-xs transition-all shadow-lg shadow-purple-600/20">ADD</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}