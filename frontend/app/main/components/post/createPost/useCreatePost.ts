import { useState, useRef, ChangeEvent } from 'react'
import { Attachment } from './types'

export const EMOJIS = ['🔥', '🚀', '✨', '💻', '🎨', '🙌', '❤️', '👍', '📍', '✅']

export const useCreatePost = (onPublish?: (content: string, attachments: Attachment[]) => void) => {
    const [isExpanded, setIsExpanded] = useState(false)
    const [text, setText] = useState('')
    const [attachments, setAttachments] = useState<Attachment[]>([])
    const [showEmojis, setShowEmojis] = useState(false)
    const fileInputRef = useRef<HTMLInputElement>(null)

    const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
        const files = Array.from(e.target.files || [])
        const newAttachments = files.map(file => ({
            file,
            url: URL.createObjectURL(file),
            type: file.type
        }))
        setAttachments([...attachments, ...newAttachments])
        if (!isExpanded) setIsExpanded(true)
    }

    const removeAttachment = (index: number) => {
        const newFiles = [...attachments]
        URL.revokeObjectURL(newFiles[index].url)
        newFiles.splice(index, 1)
        setAttachments(newFiles)
    }

    const addEmoji = (emoji: string) => {
        setText(prev => prev + emoji)
        setShowEmojis(false)
    }

    const handlePublish = () => {
        if (!text.trim() && attachments.length === 0) return

        if (onPublish) {
            onPublish(text, attachments)
        }

        setText('')
        setAttachments([])
        setIsExpanded(false)
    }

    return {
        isExpanded,
        setIsExpanded,
        text,
        setText,
        attachments,
        showEmojis,
        setShowEmojis,
        fileInputRef,
        handleFileChange,
        removeAttachment,
        addEmoji,
        handlePublish
    }
}