export interface Message {
    id: string;
    text: string;
    sender: 'me' | 'them';
    time: string;
}

export type ChatCategory = 'all' | 'unread' | 'friends' | 'strangers';

export interface Chat {
    id: number;
    user: string;
    avatar: string;
    lastMsg: string;
    time: string;
    online: boolean;
    unread: number;
    isGroup?: boolean;
    category: ChatCategory;
    description?: string;
    isPublic?: boolean;
    inviteLink?: string;
}