'use client'

import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import Stories from '../../story/stories/Stories'
import CreatePost from '../createPost/CreatePost'
import PostCard from '../postCard/PostCard'

const INITIAL_POSTS = [
    {
        id: '1',
        user: { name: 'Alex Rivera', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150', online: true, isVerified: true },
        content: 'Just launched the new PulsE interface! What do you guys think? 🚀 #uidesign #messenger',
        image: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800',
        likes: 124,
        comments: 18,
        time: '2h ago'
    },
    {
        id: '2',
        user: { name: 'Sarah Chen', avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150', online: false },
        content: 'Late night coding sessions are the best. 💻✨',
        likes: 89,
        comments: 5,
        time: '5h ago'
    }
]

export default function Feed() {
    const [posts, setPosts] = useState(INITIAL_POSTS)

    const handlePublish = (content: string, attachments: any[]) => {
        const newPost = {
            id: Date.now().toString(),
            user: {
                name: 'Current User',
                avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100',
                online: true,
                isVerified: false
            },
            content,
            image: attachments.length > 0 ? attachments[0].url : undefined,
            likes: 0,
            comments: 0,
            time: 'Just now'
        }
        setPosts([newPost, ...posts])
    }

    return (
        <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
        >
            <Stories />
            <CreatePost onPublish={handlePublish} />
            <div className="space-y-6 mt-8">
                <AnimatePresence mode="popLayout">
                    {posts.map((post) => (
                        <motion.div
                            key={post.id}
                            layout
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, scale: 0.95 }}
                            transition={{ duration: 0.3 }}
                        >
                            <PostCard post={post} />
                        </motion.div>
                    ))}
                </AnimatePresence>
            </div>
        </motion.div>
    )
}