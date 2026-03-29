'use client'

import { motion } from 'framer-motion'
import Image from 'next/image'
import AuthCard from './auth/AuthCard'
import { MessageSquare, Shield, Zap, Heart, ShieldCheck } from 'lucide-react'

export default function Home() {
  const userAvatars = [
    'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&h=150&fit=crop',
    'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop',
    'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&h=150&fit=crop',
    'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=150&h=150&fit=crop',
    'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150&h=150&fit=crop'
  ]

  const conversationImages = [
    {
      src: 'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=500&h=600&fit=crop',
      delay: 0.5, rotate: -10, x: -100,
      badge: <Heart className="w-5 h-5 text-pink-500" />
    },
    {
      src: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&h=600&fit=crop',
      delay: 0.7, rotate: 0, x: 0,
      badge: <MessageSquare className="w-5 h-5 text-purple-400" />
    },
    {
      src: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=500&h=600&fit=crop',
      delay: 0.9, rotate: 10, x: 100,
      badge: <ShieldCheck className="w-5 h-5 text-blue-400" />
    }
  ]

  const itemVariants = {
    hidden: { y: 30, opacity: 0 },
    visible: { y: 0, opacity: 1, transition: { duration: 0.6, ease: "easeOut" } }
  }

  return (
      <main className="flex min-h-screen bg-[#02040a] overflow-hidden">
        <div className="hidden lg:flex w-1/2 relative flex-col justify-center px-16 xl:px-24 bg-[#02040a] border-r border-white/5 z-10">
          <div className="absolute inset-0 pointer-events-none">
            <div className="absolute top-[-5%] left-[-5%] w-[60%] h-[60%] bg-purple-600/10 blur-[130px] rounded-full" />
            <div className="absolute bottom-[5%] right-[-5%] w-[40%] h-[40%] bg-indigo-600/5 blur-[100px] rounded-full" />
          </div>

          <motion.div
              initial="hidden"
              animate="visible"
              variants={{ visible: { transition: { staggerChildren: 0.1 }}}}
              className="relative z-10 space-y-14"
          >
            <motion.div variants={itemVariants} className="relative h-[380px] w-full flex items-center justify-center perspective-[1200px]">
              {conversationImages.map((img, i) => (
                  <motion.div
                      key={i}
                      initial={{ opacity: 0, scale: 0.8, y: 50 }}
                      animate={{ opacity: 1, scale: 1, x: img.x, rotateZ: img.rotate, y: 0 }}
                      transition={{ delay: img.delay, duration: 0.8, type: "spring", stiffness: 50 }}
                      whileHover={{ scale: 1.05, rotateZ: 0, zIndex: 100, y: -20, transition: { duration: 0.2 } }}
                      className="absolute w-56 h-72 rounded-3xl border border-white/10 overflow-hidden cursor-pointer group shadow-[0_30px_70px_rgba(0,0,0,0.8)] bg-[#02040a]"
                      style={{ zIndex: i }}
                  >
                    <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent opacity-40 group-hover:opacity-100 transition-opacity z-10" />
                    <Image src={img.src} alt="Presentation" fill className="object-cover transition-transform group-hover:scale-110 duration-1000" />
                  </motion.div>
              ))}
            </motion.div>

            <div className="space-y-6">
              <motion.h1 variants={itemVariants} className="text-6xl xl:text-7xl font-black text-white leading-[0.9] tracking-tighter">
                Connect with <br />
                <span className="text-transparent bg-clip-text bg-gradient-to-r from-purple-400 via-pink-400 to-indigo-400">
                PulsE Messager.
              </span>
              </motion.h1>
              <motion.p variants={itemVariants} className="text-xl text-gray-400 max-w-lg font-light leading-relaxed">
                The next generation of messaging. Secure, fast, and elegantly designed for your privacy.
              </motion.p>
            </div>

            <motion.div variants={itemVariants} className="flex items-center gap-6">
              <div className="flex -space-x-4">
                {userAvatars.map((url, i) => (
                    <motion.div
                        key={i}
                        whileHover={{ y: -10, scale: 1.2, zIndex: 20 }}
                        transition={{ type: "spring", stiffness: 400, damping: 12 }}
                        className="relative w-12 h-12 rounded-full border-[3px] border-[#02040a] bg-gray-900 overflow-hidden cursor-pointer shadow-xl"
                    >
                      <Image src={url} alt="User" fill className="object-cover" />
                    </motion.div>
                ))}
              </div>
              <div className="h-10 w-px bg-white/10" />
              <div className="flex flex-col">
                <span className="text-white font-bold text-base">2M+ active users</span>
                <span className="text-xs text-purple-400/80 font-medium uppercase tracking-widest">Growing every day</span>
              </div>
            </motion.div>
          </motion.div>
        </div>

        <div className="w-full lg:w-1/2 flex items-center justify-center p-8 bg-[#0a0c12] relative border-l border-white/5 z-20">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_var(--tw-gradient-stops))] from-purple-500/5 via-transparent to-transparent pointer-events-none" />
          <motion.div
              initial={{ opacity: 0, x: 30 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.6, delay: 0.2 }}
              className="w-full max-w-[460px] relative z-30"
          >
            <AuthCard />
          </motion.div>
        </div>
      </main>
  )
}