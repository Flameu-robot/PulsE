import { useState } from 'react'
import { Notification } from './types'

const initialNotifications: Notification[] = [
    { id: '1', title: 'New Follower', message: 'Sarah started following you', time: '2m ago', isNew: true },
    { id: '2', title: 'Achievement', message: 'You reached top 10 this week!', time: '1h ago', isNew: true },
    { id: '3', title: 'System', message: 'Your profile was updated successfully', time: '2d ago', isNew: false },
]

export const useNotifications = () => {
    const [notifications, setNotifications] = useState<Notification[]>(initialNotifications)

    const deleteAll = () => setNotifications([])
    const deleteOne = (id: string) => setNotifications(prev => prev.filter(n => n.id !== id))

    const newItems = notifications.filter(n => n.isNew)
    const oldItems = notifications.filter(n => !n.isNew)

    return {
        notifications,
        deleteAll,
        deleteOne,
        newItems,
        oldItems
    }
}