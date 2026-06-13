import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { fetchPublicPostings, fetchPublicAnnouncements } from '@/api/postings';
import { createAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/admin';
import { PostingCategory, Announcement } from '@/types';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Skeleton } from '@/components/ui/skeleton';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from "@/hooks/use-toast";
import { Calendar, Briefcase, ChevronRight, Megaphone, Plus, Edit, Trash2 } from 'lucide-react';
import { useAuth } from '@/auth/AuthContext';

const LandingPage: React.FC = () => {
    const { user, isAuthenticated } = useAuth();
    const isAdmin = isAuthenticated && user?.role === 'ADMIN';
    const queryClient = useQueryClient();
    const { toast } = useToast();

    // --- Postings State ---
    const [selectedCategory, setSelectedCategory] = useState<PostingCategory | 'ALL'>('ALL');

    // --- Announcement State ---
    const [isAnnounceDialogOpen, setIsAnnounceDialogOpen] = useState(false);
    const [editingAnnouncement, setEditingAnnouncement] = useState<Announcement | null>(null);
    const [announceTitle, setAnnounceTitle] = useState('');
    const [announceContent, setAnnounceContent] = useState('');

    // --- Queries ---
    const { data: postings, isLoading: loadingPostings, error: errorPostings } = useQuery({
        queryKey: ['publicPostings', selectedCategory],
        queryFn: () => fetchPublicPostings(selectedCategory === 'ALL' ? undefined : selectedCategory),
    });

    const { data: announcements, isLoading: loadingAnnouncements } = useQuery({
        queryKey: ['publicAnnouncements'],
        queryFn: fetchPublicAnnouncements,
    });

    // --- Announcement Mutations (Admin) ---
    const createAnnounceMutation = useMutation({
        mutationFn: createAnnouncement,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['publicAnnouncements'] });
            setIsAnnounceDialogOpen(false);
            toast({ title: "Created", description: "Announcement published." });
            resetAnnounceForm();
        },
        onError: () => toast({ variant: "destructive", title: "Failed", description: "Could not create announcement." })
    });

    const updateAnnounceMutation = useMutation({
        mutationFn: (data: Partial<Announcement>) => updateAnnouncement(editingAnnouncement!.id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['publicAnnouncements'] });
            setIsAnnounceDialogOpen(false);
            toast({ title: "Updated", description: "Announcement updated." });
            resetAnnounceForm();
        },
        onError: () => toast({ variant: "destructive", title: "Failed", description: "Could not update announcement." })
    });

    const deleteAnnounceMutation = useMutation({
        mutationFn: deleteAnnouncement,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['publicAnnouncements'] });
            toast({ title: "Deleted", description: "Announcement removed." });
        },
        onError: () => toast({ variant: "destructive", title: "Failed", description: "Could not delete announcement." })
    });

    const resetAnnounceForm = () => {
        setEditingAnnouncement(null);
        setAnnounceTitle('');
        setAnnounceContent('');
    };

    const openCreateAnnounce = () => {
        resetAnnounceForm();
        setIsAnnounceDialogOpen(true);
    };

    const openEditAnnounce = (ann: Announcement) => {
        setEditingAnnouncement(ann);
        setAnnounceTitle(ann.title);
        setAnnounceContent(ann.content);
        setIsAnnounceDialogOpen(true);
    };

    const handleAnnounceSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        const payload = { title: announceTitle, content: announceContent, isPublished: true };
        if (editingAnnouncement) {
            updateAnnounceMutation.mutate(payload);
        } else {
            createAnnounceMutation.mutate(payload);
        }
    };

    const categoryMap: Record<string, string> = {
        ALL: 'All Roles',
        BACKEND: 'Backend',
        FRONTEND: 'Frontend',
        MOBILE: 'Mobile',
        FULLSTACK: 'Fullstack',
    };

    return (
        <div className="animate-in space-y-12 fade-in duration-700">
            {/* --- Hero Section --- */}
            <section className="relative overflow-hidden rounded-[2rem] border border-border/70 bg-gradient-to-br from-blue-700 via-indigo-700 to-violet-700 py-16 text-center text-white shadow-2xl shadow-black/20 dark:from-slate-800 dark:via-blue-900 dark:to-indigo-900">
                <div className="absolute top-0 left-0 h-full w-full bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-15"></div>
                <div className="relative z-10 px-6">
                    <h1 className="mb-6 text-5xl font-extrabold tracking-tight text-white/95 drop-shadow-sm md:text-7xl">
                        Launch Your Career
                    </h1>
                    <p className="mx-auto mb-10 max-w-3xl text-xl font-medium leading-relaxed text-white/80 md:text-2xl">
                        Connect with top engineering projects and start building the future today.
                    </p>
                    <div className="flex justify-center gap-4">
                        <Button size="lg" variant="secondary" className="rounded-full border-0 bg-white/90 px-8 font-bold text-indigo-900 hover:bg-white dark:bg-slate-100 dark:text-indigo-950" onClick={() => document.getElementById('postings')?.scrollIntoView({ behavior: 'smooth' })}>
                            Explore Positions
                        </Button>
                        {!isAuthenticated && (
                            <Link to="/register">
                                <Button size="lg" variant="outline" className="rounded-full border-white/70 bg-transparent px-8 text-white hover:bg-white/10">
                                    Join Now
                                </Button>
                            </Link>
                        )}
                    </div>
                </div>
            </section>

            {/* --- Announcements Feed --- */}
            <section className="max-w-5xl mx-auto">
                <div className="flex items-center justify-between mb-6 px-2">
                    <div className="flex items-center gap-3">
                        <div className="rounded-full border border-amber-200/70 bg-amber-100 p-2 text-amber-700 dark:border-amber-400/20 dark:bg-amber-400/10 dark:text-amber-200">
                            <Megaphone className="w-6 h-6" />
                        </div>
                        <h2 className="text-2xl font-bold text-foreground">Latest Announcements</h2>
                    </div>
                    {isAdmin && (
                        <Button onClick={openCreateAnnounce} size="sm" className="gap-2">
                            <Plus className="w-4 h-4" /> New Announcement
                        </Button>
                    )}
                </div>

                {loadingAnnouncements ? (
                    <div className="grid gap-4 md:grid-cols-2">
                        <Skeleton className="h-32 w-full rounded-xl" />
                        <Skeleton className="h-32 w-full rounded-xl" />
                    </div>
                ) : announcements && announcements.length > 0 ? (
                    <div className="grid gap-4 md:grid-cols-2">
                        {announcements.map((ann: Announcement) => (
                            <Card key={ann.id} className="border-l-4 border-l-amber-400 transition-shadow hover:shadow-md dark:border-l-amber-300/70">
                                <CardContent className="pt-6 relative">
                                    <div className="flex justify-between items-start mb-2">
                                        <h3 className="line-clamp-1 text-lg font-semibold text-foreground">{ann.title}</h3>
                                        <Badge variant="outline" className="bg-muted/50 text-xs text-muted-foreground">{new Date(ann.createdAt).toLocaleDateString()}</Badge>
                                    </div>
                                    <p className="line-clamp-2 text-sm leading-relaxed text-muted-foreground">{ann.content}</p>

                                    {isAdmin && (
                                        <div className="absolute bottom-2 right-2 flex gap-1">
                                            <Button size="icon" variant="ghost" className="h-8 w-8 text-blue-600 hover:bg-blue-50 dark:text-blue-300 dark:hover:bg-blue-500/10" onClick={() => openEditAnnounce(ann)}>
                                                <Edit className="w-3 h-3" />
                                            </Button>
                                            <Button size="icon" variant="ghost" className="h-8 w-8 text-red-600 hover:bg-red-50 dark:text-red-300 dark:hover:bg-red-500/10" onClick={() => deleteAnnounceMutation.mutate(ann.id)}>
                                                <Trash2 className="w-3 h-3" />
                                            </Button>
                                        </div>
                                    )}
                                </CardContent>
                            </Card>
                        ))}
                    </div>
                ) : (
                    <div className="rounded-xl border border-dashed border-border bg-card/70 py-8 text-center">
                        <p className="text-muted-foreground">No active announcements</p>
                    </div>
                )}
            </section>

            {/* --- Postings Section --- */}
            <div id="postings" className="flex flex-col items-center space-y-8 pt-4">
                <Tabs defaultValue="ALL" onValueChange={(val) => setSelectedCategory(val as PostingCategory | 'ALL')} className="w-full max-w-4xl">
                    <TabsList className="grid h-auto w-full grid-cols-2 rounded-2xl border border-border/70 bg-muted/55 p-1.5 md:grid-cols-5">
                        {Object.entries(categoryMap).map(([key, label]) => (
                            <TabsTrigger
                                key={key}
                                value={key}
                                className="rounded-xl py-2.5 font-bold transition-all data-[state=active]:bg-card data-[state=active]:text-primary data-[state=active]:shadow-md"
                            >
                                {label}
                            </TabsTrigger>
                        ))}
                    </TabsList>
                </Tabs>

                {loadingPostings ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 w-full">
                        {[1, 2, 3].map((i) => (
                            <Card key={i} className="h-80"><Skeleton className="h-full w-full" /></Card>
                        ))}
                    </div>
                ) : errorPostings ? (
                    <div className="text-center py-10 text-red-500">Failed to load postings.</div>
                ) : postings?.length === 0 ? (
                    <div className="w-full rounded-3xl border border-dashed border-border bg-card/70 py-24 text-center">
                        <Briefcase className="mx-auto mb-4 h-16 w-16 text-muted-foreground/60" />
                        <h3 className="text-xl font-bold text-foreground">No positions found</h3>
                        <p className="text-muted-foreground">Check back later for new opportunities.</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 w-full">
                        {postings?.map((posting) => (
                            <Card key={posting.id} className="group flex flex-col overflow-hidden rounded-2xl border-border/80 transition-all duration-300 hover:-translate-y-1 hover:shadow-xl">
                                <CardHeader className="border-b border-border/60 bg-muted/25 pb-4">
                                    <div className="flex justify-between items-start gap-4 mb-2">
                                        <Badge variant={posting.status === 'CLOSED' ? 'destructive' : 'secondary'} className="font-semibold">
                                            {posting.status}
                                        </Badge>
                                        <Badge variant="outline" className="bg-card font-medium text-muted-foreground">
                                            {posting.category}
                                        </Badge>
                                    </div>
                                    <CardTitle className="text-xl font-bold line-clamp-1 group-hover:text-primary transition-colors">
                                        {posting.title}
                                    </CardTitle>
                                    <CardDescription className="line-clamp-1 font-medium text-muted-foreground">
                                        {posting.projectName}
                                    </CardDescription>
                                </CardHeader>
                                <CardContent className="pt-6 flex-1">
                                    <p className="line-clamp-3 text-sm leading-relaxed text-muted-foreground">
                                        {posting.description}
                                    </p>
                                </CardContent>
                                <CardFooter className="border-t border-border/60 bg-muted/20 pt-4">
                                    <div className="flex w-full items-center justify-between text-sm text-muted-foreground">
                                        <div className="flex items-center gap-1.5 font-medium">
                                            <Calendar className="h-4 w-4 text-muted-foreground" />
                                            <span>{new Date(posting.createdAt).toLocaleDateString()}</span>
                                        </div>
                                        <Link to={`/postings/${posting.id}`}>
                                            <Button size="sm" className="gap-1 rounded-lg group-hover:bg-primary group-hover:text-white transition-colors">
                                                View Details <ChevronRight className="w-4 h-4" />
                                            </Button>
                                        </Link>
                                    </div>
                                </CardFooter>
                            </Card>
                        ))}
                    </div>
                )}
            </div>

            {/* --- Admin Announcement Dialog --- */}
            <Dialog open={isAnnounceDialogOpen} onOpenChange={setIsAnnounceDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>{editingAnnouncement ? 'Edit Announcement' : 'New Announcement'}</DialogTitle>
                    </DialogHeader>
                    <form onSubmit={handleAnnounceSubmit} className="space-y-4">
                        <div className="space-y-2">
                            <Label>Title</Label>
                            <Input value={announceTitle} onChange={e => setAnnounceTitle(e.target.value)} required placeholder="Important update..." />
                        </div>
                        <div className="space-y-2">
                            <Label>Content</Label>
                            <textarea
                                className="flex min-h-[100px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                                value={announceContent}
                                onChange={e => setAnnounceContent(e.target.value)}
                                required
                                placeholder="Details..."
                            />
                        </div>
                        <Button type="submit" className="w-full" disabled={createAnnounceMutation.isPending || updateAnnounceMutation.isPending}>
                            {editingAnnouncement ? 'Update Announcement' : 'Publish Announcement'}
                        </Button>
                    </form>
                </DialogContent>
            </Dialog>
        </div>
    );
};

export default LandingPage;
