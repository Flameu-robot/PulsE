import { useState, useEffect, MouseEvent } from 'react'
import { Story } from './types'

export const useStoryModal = (
    stories: Story[],
    initialIndex: number,
    onClose: () => void,
    onStoryChange: (id: number | string) => void
) => {
    const [currentIndex, setCurrentIndex] = useState(initialIndex)
    const [progress, setProgress] = useState(0)
    const [isPaused, setIsPaused] = useState(false)
    const [liked, setLiked] = useState(false)

    const currentStory = stories[currentIndex]

    const handleNext = () => {
        if (currentIndex < stories.length - 1) {
            setCurrentIndex(prev => prev + 1)
            setProgress(0)
            setLiked(false)
            onStoryChange(stories[currentIndex + 1].id)
        } else {
            onClose()
        }
    }

    const handlePrev = () => {
        if (currentIndex > 0) {
            setCurrentIndex(prev => prev - 1)
            setProgress(0)
            setLiked(false)
            onStoryChange(stories[currentIndex - 1].id)
        }
    }

    useEffect(() => {
        if (isPaused) return
        const timer = setInterval(() => {
            setProgress(prev => {
                if (prev >= 100) {
                    handleNext()
                    return 0
                }
                return prev + 1
            })
        }, 50)
        return () => clearInterval(timer)
    }, [currentIndex, isPaused])

    const handleBackdropClick = (e: MouseEvent) => {
        if (e.target === e.currentTarget) onClose()
    }

    return {
        currentIndex,
        progress,
        isPaused,
        setIsPaused,
        liked,
        setLiked,
        currentStory,
        handleNext,
        handlePrev,
        handleBackdropClick
    }
}