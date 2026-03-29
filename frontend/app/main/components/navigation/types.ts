import { LucideIcon } from 'lucide-react'

export interface NavItem {
    id: string
    icon: LucideIcon
    label: string
    href: string
}

export interface UserProfile {
    id: number;
    username: string;
    email: string;
    phone: string;
    role: string;
    status: string;
    avatarUrl: string | null;
    bio: string | null;
    createdAt: string;
    lastLoginAt: string;
}

