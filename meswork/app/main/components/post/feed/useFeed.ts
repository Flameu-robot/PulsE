import { useState } from 'react'
import { PostData } from './types'

export const MY_USERNAME = '@alexj'
export const MY_NAME = 'Alex Johnson'
export const MY_AVATAR = 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100'

const initialPosts: PostData[] = [
    {
        id: '1',
        user: {
            name: 'Sarah Chen',
            avatar: 'https://i.pravatar.cc/150?u=2',
            username: '@sarah_c',
            online: true,
            isVerified: true
        },
        content: 'Just launched our new feature! 🚀',
        image: 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800',
        likes: 234,
        comments: 42,
        shares: 18,
        timestamp: '2 hours ago',
        liked: false
    }
]

export const useFeed = () => {
    const [posts, setPosts] = useState<PostData[]>(initialPosts)

    const handlePublish = (content: string, attachments: any[]) => {
        const newPost: PostData = {
            id: Date.now().toString(),
            user: {
                name: MY_NAME,
                avatar: MY_AVATAR,
                username: MY_USERNAME,
                online: true
            },
            content,
            image: attachments.length > 0 ? attachments[0].url : undefined,
            likes: 0,
            comments: 0,
            shares: 0,
            timestamp: 'Just now',
            liked: false
        }
        setPosts([newPost, ...posts])
    }

    const handleDelete = (id: string) => {
        setPosts(posts.filter(p => p.id !== id))
    }

    return {
        posts,
        handlePublish,
        handleDelete,
        MY_USERNAME
    }
}