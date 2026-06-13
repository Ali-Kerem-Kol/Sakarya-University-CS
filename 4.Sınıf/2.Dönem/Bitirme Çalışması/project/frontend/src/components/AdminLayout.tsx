import { LogOut, Files, Users, Mail, UserCircle } from 'lucide-react';
import React from 'react';
import { NavLink, Outlet, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import BrandWordmark from '@/components/BrandWordmark';

const AdminLayout: React.FC = () => {
    const { logout, user } = useAuth();
    const navigate = useNavigate();

    const menuItems = [
        { icon: Files, label: 'Projeler', path: '/admin/postings' },
        { icon: Users, label: 'Kullanicilar', path: '/admin/admin-users' },
        { icon: Mail, label: 'Mail Operations', path: '/admin/mail' },
        { icon: UserCircle, label: 'My Profile', path: '/admin/profile' },
    ];

    return (
        <div className="flex h-screen bg-background text-foreground">
            <aside className="hidden w-72 flex-col border-r border-border/80 bg-card/85 backdrop-blur md:flex">
                <div className="px-6 pb-6 pt-7">
                    <Link to="/" className="group inline-block">
                        <h1 className="flex items-center gap-3 text-xl font-bold text-foreground transition-colors group-hover:text-primary">
                            <BrandWordmark imageClassName="h-6" />
                            <span className="text-base text-muted-foreground">Admin Hub</span>
                        </h1>
                    </Link>
                    <p className="mt-2 text-xs text-muted-foreground">Merkezi operasyon paneli</p>
                </div>

                <nav className="flex-1 space-y-1.5 px-4">
                    {menuItems.map((item) => (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            className={({ isActive }) => cn(
                                'flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition-all duration-200',
                                isActive
                                    ? 'bg-primary text-primary-foreground shadow-lg shadow-primary/30'
                                    : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                            )}
                        >
                            <item.icon className="h-4 w-4" />
                            <span>{item.label}</span>
                        </NavLink>
                    ))}
                </nav>

                <div className="border-t border-border/80 p-4">
                    <div className="mb-3 flex items-center gap-3 rounded-xl border border-border/70 bg-background/70 px-4 py-3">
                        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-xs font-bold text-primary-foreground">
                            {user?.email[0].toUpperCase()}
                        </div>
                        <div className="overflow-hidden">
                            <p className="truncate text-xs font-semibold text-foreground">{user?.email}</p>
                            <p className="text-[10px] uppercase tracking-wide text-muted-foreground">Administrator</p>
                        </div>
                    </div>
                    <Button
                        variant="ghost"
                        className="w-full justify-start rounded-xl text-muted-foreground hover:bg-accent hover:text-accent-foreground"
                        onClick={() => {
                            logout();
                            navigate('/login');
                        }}
                    >
                        <LogOut className="mr-3 h-4 w-4" />
                        Cikis Yap
                    </Button>
                </div>
            </aside>

            <main className="flex-1 flex flex-col overflow-hidden">
                <header className="border-b border-border/80 bg-card/80 px-5 py-3 backdrop-blur md:px-8">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-primary">Yonetim</p>
                            <h2 className="text-lg font-semibold text-foreground">Admin Workspace</h2>
                        </div>
                        <div />
                    </div>
                </header>

                <div className="soft-grid flex-1 overflow-y-auto">
                    <div className="p-5 md:p-8">
                        <Outlet />
                    </div>
                </div>
            </main>
        </div>
    );
};

export default AdminLayout;

