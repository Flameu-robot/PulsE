import { useRef, useEffect, useState, ChangeEvent } from 'react'
import { Story } from './types'

export const MOCK_STORIES: Story[] = [
    { id: 1, username: 'Alex Rivera', avatar: 'https://i.pravatar.cc/150?u=1', content: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800', type: 'image', likes: 12, shares: 2, views: 45 },
    { id: 2, username: 'Sarah Chen', avatar: 'https://i.pravatar.cc/150?u=2', content: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800', type: 'image', likes: 45, shares: 8, views: 120 },
    { id: 3, username: 'Mike Ross', avatar: 'https://i.pravatar.cc/150?u=3', content: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800', type: 'image', likes: 5, shares: 0, views: 32 },
]

export const useStories = () => {
    const scrollRef = useRef<HTMLDivElement>(null)
    const fileInputRef = useRef<HTMLInputElement>(null)
    const [showLeftArrow, setShowLeftArrow] = useState(false)
    const [showRightArrow, setShowRightArrow] = useState(true)
    const [viewedStories, setViewedStories] = useState<(number | string)[]>([])
    const [activeStoryIndex, setActiveStoryIndex] = useState<number | null>(null)
    const [myStories, setMyStories] = useState<Story[]>([])

    const allStories = [...myStories, ...MOCK_STORIES]

    const updateArrows = () => {
        if (scrollRef.current) {
            const { scrollLeft, scrollWidth, clientWidth } = scrollRef.current
            setShowLeftArrow(scrollLeft > 5)
            setShowRightArrow(scrollLeft < scrollWidth - clientWidth - 5)
        }
    }

    useEffect(() => {
        const el = scrollRef.current
        if (!el) return

        updateArrows()

        const handleWheel = (e: WheelEvent) => {
            if (Math.abs(e.deltaY) < Math.abs(e.deltaX)) return
            e.preventDefault()
            el.scrollLeft += e.deltaY * 1.3
        }

        el.addEventListener('wheel', handleWheel, { passive: false })
        el.addEventListener('scroll', updateArrows)
        window.addEventListener('resize', updateArrows)

        return () => {
            el.removeEventListener('wheel', handleWheel)
            el.removeEventListener('scroll', updateArrows)
            window.removeEventListener('resize', updateArrows)
        }
    }, [])

    const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0]
        if (file) {
            const url = URL.createObjectURL(file)
            const isVideo = file.type.startsWith('video')
            const newStory: Story = {
                id: `my-${Date.now()}`,
                username: 'You',
                avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100',
                content: url,
                type: isVideo ? 'video' : 'image',
                isOwn: true,
                likes: 2,
                shares: 1,
                views: 5
            }
            setMyStories([newStory, ...myStories])
        }
    }

    const scrollLeft = () => scrollRef.current?.scrollBy({ left: -300, behavior: 'smooth' })
    const scrollRight = () => scrollRef.current?.scrollBy({ left: 300, behavior: 'smooth' })

    const markAsViewed = (id: number | string) => {
        if (!viewedStories.includes(id)) {
            setViewedStories(p => [...p, id])
        }
    }

    const handleStoryClick = (index: number, id: number | string) => {
        setActiveStoryIndex(index)
        markAsViewed(id)
    }

    return {
        scrollRef,
        fileInputRef,
        showLeftArrow,
        showRightArrow,
        viewedStories,
        activeStoryIndex,
        setActiveStoryIndex,
        allStories,
        handleFileChange,
        scrollLeft,
        scrollRight,
        markAsViewed,
        handleStoryClick
    }
}