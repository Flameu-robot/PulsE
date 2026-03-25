export interface Notification {
    id: string
    title: string
    message: string
    time: string
    isNew: boolean
}

export interface NotificationsModalProps {
    isOpen: boolean
    onClose: () => void
}