import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
    title: 'PulsE - Modern Messenger',
    description: 'Connect with friends and family instantly with PulsE messenger',
}

export default function RootLayout({
                                       children,
                                   }: {
    children: React.ReactNode
}) {
    return (
        <html lang="en" className="dark">
        <body
            className={`${inter.className} bg-[#02040a] text-white antialiased`}
            suppressHydrationWarning
        >
        {children}
        </body>
        </html>
    )
}