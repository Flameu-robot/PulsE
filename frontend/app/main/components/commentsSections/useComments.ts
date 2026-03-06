import { useState, useMemo, useCallback, FormEvent } from 'react'
import { Comment } from './types'

const initialComments: Comment[] = [
    {
        id: '1',
        user: { name: 'Mike Ross', avatar: 'https://i.pravatar.cc/150?u=3', username: '@mross' },
        content: 'This looks absolutely incredible! Love the UI/UX details here. 🔥 The glassmorphism effect is subtle but very effective.',
        timestamp: '1h ago',
        likes: 24,
        isLiked: false,
        replies: [
            {
                id: '101',
                user: { name: 'Sarah Chen', avatar: 'https://i.pravatar.cc/150?u=2', username: '@sarah_c' },
                content: 'Totally agree! How did you achieve that blur effect on the borders?',
                timestamp: '45m ago',
                likes: 5,
                isLiked: true
            }
        ]
    },
    {
        id: '2',
        user: { name: 'Harvey Specter', avatar: 'https://i.pravatar.cc/150?u=4', username: '@harvey' },
        content: 'Clean work. Minimalist and functional. Expect nothing less.',
        timestamp: '2h ago',
        likes: 18,
        isLiked: false
    }
]

export const useComments = () => {
    const [comments, setComments] = useState<Comment[]>(initialComments)
    const [newComment, setNewComment] = useState('')
    const [replyTo, setReplyTo] = useState<{ id: string, name: string } | null>(null)

    const topComment = useMemo(() => {
        const flatComments = comments.flatMap(c => [c, ...(c.replies || [])])
        if (flatComments.length === 0) return null
        return [...flatComments].sort((a, b) => b.likes - a.likes)[0]
    }, [comments])

    const handleLike = useCallback((id: string) => {
        const updateLikes = (items: Comment[]): Comment[] => {
            return items.map(c => {
                if (c.id === id) {
                    return { ...c, isLiked: !c.isLiked, likes: c.isLiked ? c.likes - 1 : c.likes + 1 }
                }
                if (c.replies) {
                    return { ...c, replies: updateLikes(c.replies) }
                }
                return c
            })
        }
        setComments(prev => updateLikes(prev))
    }, [])

    const handleReplyClick = useCallback((id: string, name: string) => {
        setReplyTo({ id, name })
    }, [])

    const handleSubmit = (e: FormEvent) => {
        e.preventDefault()
        if (!newComment.trim()) return

        const comment: Comment = {
            id: Date.now().toString(),
            user: {
                name: 'Alex Johnson',
                avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100',
                username: '@alexj'
            },
            content: newComment,
            timestamp: 'Just now',
            likes: 0,
            isLiked: false
        }

        if (replyTo) {
            setComments(prev => prev.map(c => {
                if (c.id === replyTo.id) return { ...c, replies: [...(c.replies || []), comment] }
                if (c.replies) return { ...c, replies: c.replies.map(r => r.id === replyTo.id ? { ...r, replies: [...(r.replies || []), comment] } : r) }
                return c
            }))
            setReplyTo(null)
        } else {
            setComments([comment, ...comments])
        }
        setNewComment('')
    }

    return {
        comments,
        newComment,
        setNewComment,
        replyTo,
        setReplyTo,
        topComment,
        handleLike,
        handleReplyClick,
        handleSubmit
    }
}