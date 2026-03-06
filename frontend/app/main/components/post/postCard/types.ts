export interface PostUser {
    name: string
    avatar: string
    username: string
    online: boolean
    isVerified?: boolean
}

export interface PostData {
    id: string
    user: PostUser
    content: string
    image?: string
    likes: number
    comments: number
    shares?: number
    time: string
    liked?: boolean
}

export interface PostProps {
    post: PostData
    isOwnPost: boolean
    onDelete?: (id: string) => void
}

export interface MockFriend {
    id: number
    name: string
    avatar: string
}