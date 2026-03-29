'use client'

import Sidebar from "./components/navigation/Sidebar";

export default function MainLayout({
                                       children,
                                   }: {
    children: React.ReactNode;
}) {
    return (
        <div className="flex h-screen bg-[#09090b] text-white overflow-hidden font-sans selection:bg-purple-500/30">
            <Sidebar />
            <main className="flex-1 pl-20 lg:pl-64 relative overflow-hidden h-full">
                {children}
            </main>
        </div>
    );
}