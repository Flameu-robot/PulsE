export interface UpdateProfileDto {
    username: string;
    avatarUrl: string | null;
    bio: string | null;
}

export interface UserProfileModalProps {
    isOpen: boolean;
    onClose: () => void;
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

