import { useState, useEffect, useRef } from 'react'
import { PostData, MockFriend } from './types'

export const MOCK_FRIENDS: MockFriend[] = [
    { id: 1, name: 'Sarah Chen', avatar: 'https://i.pravatar.cc/150?u=2' },
    { id: 2, name: 'Mike Ross', avatar: 'https://i.pravatar.cc/150?u=3' },
    { id: 3, name: 'Harvey Specter', avatar: 'https://i.pravatar.cc/150?u=4' },
]

export const usePostCard = (post: PostData, onDelete?: (id: string) => void) => {
    const [isLiked, setIsLiked] = useState(post.liked || false)
    const [likesCount, setLikesCount] = useState(post.likes)
    const [isSaved, setIsSaved] = useState(false)
    const [isImageOpen, setIsImageOpen] = useState(false)
    const [showMoreMenu, setShowMoreMenu] = useState(false)
    const [showShareModal, setShowShareModal] = useState(false)
    const [commentsDisabled, setCommentsDisabled] = useState(false)
    const [showComments, setShowComments] = useState(false)
    const [mounted, setMounted] = useState(false)
    const menuRef = useRef<HTMLDivElement>(null)

    useEffect(() => {
        setMounted(true)
        const handleClickOutside = (e: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
                setShowMoreMenu(false)
            }
        }
        document.addEventListener('mousedown', handleClickOutside)
        return () => document.removeEventListener('mousedown', handleClickOutside)
    }, [])

    const handleCopyLink = () => {
        if (typeof window !== 'undefined') {
            navigator.clipboard.writeText(`${window.location.origin}/post/${post.id}`)
        }
        setShowMoreMenu(false)
    }

    const handleLikeToggle = () => {
        setIsLiked(!isLiked)
        setLikesCount(prev => (isLiked ? prev - 1 : prev + 1))
    }

    const handleToggleCommentsDisabled = () => {
        setCommentsDisabled(!commentsDisabled)
        setShowMoreMenu(false)
    }

    const handleDelete = () => {
        onDelete?.(post.id)
        setShowMoreMenu(false)
    }

    return {
        isLiked,
        likesCount,
        isSaved,
        setIsSaved,
        isImageOpen,
        setIsImageOpen,
        showMoreMenu,
        setShowMoreMenu,
        showShareModal,
        setShowShareModal,
        commentsDisabled,
        showComments,
        setShowComments,
        mounted,
        menuRef,
        handleCopyLink,
        handleLikeToggle,
        handleToggleCommentsDisabled,
        handleDelete
    }
}