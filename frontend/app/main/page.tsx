'use client'

import { motion } from 'framer-motion'
import Feed from './components/post/feed/Feed'
import Suggestions from './components/rightPanel/Suggestions'

export default function FeedPage() {
    return (
        <div className="flex w-full h-full">
            <div className="flex-1 max-w-4xl mx-auto overflow-y-auto no-scrollbar pb-20">
                <Feed />
            </div>

            <motion.aside
                initial={{ x: 50, opacity: 0 }}
                animate={{ x: 0, opacity: 1 }}
                transition={{ duration: 0.8, delay: 0.4, ease: "circOut" }}
                className="hidden xl:block w-[350px] p-8 border-l border-white/[0.03]"
            >
                <Suggestions />
            </motion.aside>
        </div>
    )
}