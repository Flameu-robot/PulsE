export interface PostData {
    id: string
    user: {
        name: string
        avatar: string
        username: string
        online?: boolean
        isVerified?: boolean
    }
    content: string
    image?: string
    likes: number
    comments: number
    shares: number
    timestamp: string
    liked: boolean
}