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