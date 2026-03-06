export interface Story {
    id: number | string
    username: string
    avatar: string
    content: string
    type: 'image' | 'video'
    isOwn?: boolean
    likes?: number
    shares?: number
    views?: number
}

export interface StoryModalProps {
    stories: Story[]
    initialIndex: number
    onClose: () => void
    onStoryChange: (id: number | string) => void
}