import React from 'react';
import { NavLink, Outlet, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { Button } from '@/components/ui/button';
import {
    Briefcase,
    LogOut,
    ChevronRight,
    UserCircle,
    FileText,
    CheckSquare,
    MessageCircle,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import BrandWordmark from '@/components/BrandWordmark';

const UserLayout: React.FC = () => {
    const { logout, user } = useAuth();
    const navigate = useNavigate();

    const menuItems = [
        { icon: UserCircle, label: 'Hesap', path: '/user/profile' },
        { icon: CheckSquare, label: 'Gorev Grafigi', path: '/user/tasks' },
        { icon: MessageCircle, label: 'Sorularim', path: '/user/questions' },
        { icon: Briefcase, label: 'My Submissions', path: '/user/applications' },
        { icon: FileText, label: 'Documents', path: '/user/documents' },
    ];

    return (
        <div className="flex h-screen bg-background text-foreground">
            <aside className="hidden w-72 flex-col border-r border-border/80 bg-card/85 backdrop-blur md:flex">
                <div className="px-6 pb-6 pt-7">
                    <Link to="/" className="group inline-block">
                        <h1 className="flex items-center gap-3 text-2xl font-bold text-foreground transition-colors group-hover:text-primary">
                            <BrandWordmark imageClassName="h-7" />
                            <span className="text-base text-muted-foreground">Portal</span>
                        </h1>
                    </Link>
                    <p className="mt-2 text-xs text-muted-foreground">Kariyer ve gorev takip merkezi</p>
                </div>

                <nav className="flex-1 space-y-1.5 px-4">
                    {menuItems.map((item) => (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            className={({ isActive }) => cn(
                                'group flex items-center gap-3 rounded-xl px-4 py-3 transition-all duration-200',
                                isActive
                                    ? 'bg-primary text-primary-foreground shadow-lg shadow-primary/30'
                                    : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                            )}
                        >
                            <item.icon className="h-5 w-5" />
                            <span className="font-medium">{item.label}</span>
                            <div className="ml-auto opacity-0 group-[.active]:opacity-100">
                                <ChevronRight className="h-4 w-4" />
                            </div>
                        </NavLink>
                    ))}
                </nav>

                <div className="mt-auto border-t border-border/80 p-4">
                    <div className="mb-4 rounded-xl border border-border/70 bg-background/70 px-4 py-3">
                        <p className="truncate text-xs font-medium text-foreground">{user?.email}</p>
                        <p className="text-[10px] uppercase tracking-wider text-muted-foreground">Candidate</p>
                    </div>
                    <Button
                        variant="ghost"
                        className="w-full justify-start rounded-xl text-muted-foreground hover:bg-accent hover:text-accent-foreground"
                        onClick={() => {
                            logout();
                            navigate('/login');
                        }}
                    >
                        <LogOut className="mr-3 h-5 w-5" />
                        Cikis Yap
                    </Button>
                </div>
            </aside>

            <main className="flex-1 flex flex-col overflow-hidden">
                <header className="border-b border-border/80 bg-card/80 px-5 py-3 backdrop-blur md:px-8">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-primary">Kullanici</p>
                            <h2 className="text-lg font-semibold">Workspace</h2>
                        </div>
                        <div className="flex items-center gap-2">
                            <Button variant="outline" size="icon" className="md:hidden" onClick={() => logout()}>
                                <LogOut className="h-5 w-5" />
                            </Button>
                        </div>
                    </div>
                </header>

                <div className="soft-grid flex-1 overflow-y-auto p-6 lg:p-10">
                    <div className="max-w-5xl mx-auto">
                        <Outlet />
                    </div>
                </div>
            </main>
        </div>
    );
};

export default UserLayout;

