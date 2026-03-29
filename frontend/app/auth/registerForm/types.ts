export interface RegisterFormData {
    username: string
    email: string
    password: string
    confirmPassword: string
}

export interface AuthResponse {
    accessToken: string
    refreshToken: string
}

export interface VerifyConfirmData {
    code: string
}