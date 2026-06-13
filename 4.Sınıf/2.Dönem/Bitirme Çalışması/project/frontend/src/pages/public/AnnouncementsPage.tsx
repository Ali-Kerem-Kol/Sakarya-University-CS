import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchPublicAnnouncements } from '@/api/postings';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Loader2, Calendar } from 'lucide-react';
import { Badge } from '@/components/ui/badge';

const AnnouncementsPage: React.FC = () => {
    const { data: announcements, isLoading } = useQuery({
        queryKey: ['public-announcements'],
        queryFn: fetchPublicAnnouncements
    });

    if (isLoading) return <div className="flex justify-center p-20"><Loader2 className="animate-spin w-8 h-8 text-primary" /></div>;

    return (
        <div className="container mx-auto py-12 px-4 space-y-8">
            <div className="text-center space-y-4">
                <h1 className="text-4xl font-extrabold tracking-tight text-foreground sm:text-5xl">Announcements</h1>
                <p className="mx-auto max-w-2xl text-lg text-muted-foreground">Latest updates and news from the team.</p>
            </div>

            <div className="grid gap-6 md:grid-cols-1 lg:grid-cols-2 max-w-4xl mx-auto">
                {announcements?.length === 0 ? (
                    <div className="col-span-full rounded-lg bg-card/70 py-12 text-center text-muted-foreground">
                        <p>No announcements yet.</p>
                    </div>
                ) : (
                    announcements?.map((ann) => (
                        <Card key={ann.id} className="hover:shadow-lg transition-shadow border-l-4 border-l-primary/50">
                            <CardHeader>
                                <div className="flex justify-between items-start mb-2">
                                    <Badge variant="secondary" className="text-xs">
                                        <Calendar className="w-3 h-3 mr-1" />
                                        {new Date(ann.createdAt).toLocaleDateString()}
                                    </Badge>
                                </div>
                                <CardTitle className="text-xl font-bold text-foreground">{ann.title}</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="whitespace-pre-wrap text-muted-foreground">{ann.content}</p>
                            </CardContent>
                        </Card>
                    ))
                )}
            </div>
        </div>
    );
};

export default AnnouncementsPage;
