import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

export function middleware(request: NextRequest) {
    const token = request.cookies.get('accessToken')?.value
    const { pathname } = request.nextUrl


    if (!token && pathname.startsWith('/main')) {
        return NextResponse.redirect(new URL('/', request.url))
    }

    if (token && pathname === '/') {
        return NextResponse.redirect(new URL('/main', request.url))
    }

    return NextResponse.next()
}

export const config = {
    matcher: ['/((?!api|_next/static|_next/image|favicon.ico).*)']
}