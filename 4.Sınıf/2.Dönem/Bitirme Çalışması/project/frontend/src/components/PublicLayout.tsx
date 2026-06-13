import React from 'react';
import { Link, Outlet } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/auth/AuthContext';
import { Sparkles } from 'lucide-react';
import BrandWordmark from '@/components/BrandWordmark';

const PublicLayout: React.FC = () => {
    const { isAuthenticated, user } = useAuth();

    return (
        <div className="flex min-h-screen flex-col bg-background text-foreground">
            <header className="sticky top-0 z-50 border-b border-border/80 bg-card/85 backdrop-blur">
                <div className="container mx-auto px-4 py-2">
                    <div className="flex h-14 items-center justify-between">
                        <div className="flex items-center gap-6">
                            <Link to="/" className="flex items-center gap-3 text-xl font-bold text-foreground transition-colors hover:text-primary">
                                <BrandWordmark imageClassName="h-6" />
                                <span className="text-base text-muted-foreground">Talent</span>
                            </Link>
                            <nav className="flex items-center gap-4">
                                <Link to="/" className="text-sm font-medium text-muted-foreground transition-colors hover:text-primary">Ilanlar</Link>
                            </nav>
                        </div>

                        <nav className="flex items-center gap-2">
                            {isAuthenticated ? (
                                <Link to={user?.role === 'ADMIN' ? '/admin/profile' : '/user/profile'}>
                                    <Button variant="outline" className="rounded-full">Hesap</Button>
                                </Link>
                            ) : (
                                <div className="flex gap-2">
                                    <Link to="/login">
                                        <Button variant="ghost" className="rounded-full">Giris</Button>
                                    </Link>
                                    <Link to="/register">
                                        <Button className="rounded-full">Kayit Ol</Button>
                                    </Link>
                                </div>
                            )}
                        </nav>
                    </div>
                </div>
            </header>

            <main className="flex-1">
                <div className="container mx-auto px-4 py-8">
                    <div className="mb-6 flex items-center gap-2 text-xs font-semibold uppercase tracking-[0.2em] text-primary">
                        <Sparkles className="h-4 w-4" />
                        32bit Experience
                    </div>
                    <Outlet />
                </div>
            </main>

            <footer className="border-t border-border/80 bg-card/90 py-8 text-muted-foreground">
                <div className="container mx-auto px-4 text-center text-sm">
                    <p>&copy; {new Date().getFullYear()} 32bit Talent Network. Sakarya University.</p>
                </div>
            </footer>
        </div>
    );
};

export default PublicLayout;
