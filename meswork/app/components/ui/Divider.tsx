'use client'

interface DividerProps {
    text?: string
}

export default function Divider({ text }: DividerProps) {
    return (
        <div className="relative my-6">
            <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-border"></div>
            </div>
            {text && (
                <div className="relative flex justify-center text-xs uppercase">
          <span className="bg-background px-4 text-muted-foreground font-medium">
            {text}
          </span>
                </div>
            )}
        </div>
    )
}