import { useMemo } from 'react'
import { Friend } from './types'

const initialFriends: Friend[] = [
    { id: '1', name: 'Sarah Chen', avatar: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150&h=150&fit=crop', online: true },
    { id: '2', name: 'Mike Ross', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop', online: true },
    { id: '3', name: 'Emma Watson', avatar: 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=150&h=150&fit=crop', online: false, lastActive: '5m ago' },
    { id: '4', name: 'James Wilson', avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&h=150&fit=crop', online: false, lastActive: '2h ago' },
]

const initialTopics: string[] = [
    '#TechNews',
    '#Photography',
    '#Travel',
    '#Music',
    '#Art'
]

export const useRightPanel = () => {
    const friends = initialFriends
    const topics = initialTopics

    const onlineCount = useMemo(() => friends.filter(f => f.online).length, [friends])

    return {
        friends,
        topics,
        onlineCount
    }
}