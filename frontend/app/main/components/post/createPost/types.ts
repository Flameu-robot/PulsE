export interface Attachment {
    file: File
    url: string
    type: string
}

export interface CreatePostProps {
    onPublish?: (content: string, attachments: Attachment[]) => void
}