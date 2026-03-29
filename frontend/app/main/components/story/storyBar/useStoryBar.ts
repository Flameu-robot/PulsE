import { Story } from './types'

const initialStories: Story[] = [
    { id: '1', user: 'Your story', avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&h=150&fit=crop', viewed: false },
    { id: '2', user: 'Sarah', avatar: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150&h=150&fit=crop', viewed: false },
    { id: '3', user: 'Mike', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop', viewed: true },
    { id: '4', user: 'Emma', avatar: 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=150&h=150&fit=crop', viewed: false },
    { id: '5', user: 'James', avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&h=150&fit=crop', viewed: true },
]

export const useStoryBar = () => {
    const stories = initialStories

    return {
        stories
    }
}