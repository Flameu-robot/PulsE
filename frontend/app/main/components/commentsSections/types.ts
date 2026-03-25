export interface Comment {
    id: string
    user: {
        name: string
        avatar: string
        username: string
    }
    content: string
    timestamp: string
    likes: number
    isLiked: boolean
    replies?: Comment[]
}

export interface CommentsSectionProps {
    postId: string
    isExpanded: boolean
}

export interface CommentItemProps {
    item: Comment
    isReply?: boolean
    isTop?: boolean
    canReply: boolean
    onLike: (id: string) => void
    onReplyClick: (id: string, name: string) => void
}