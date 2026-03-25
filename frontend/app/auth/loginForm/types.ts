export interface LoginFormData {
    login: string
    password: string
}

export interface AuthResponse {
    accessToken: string
    refreshToken: string
}