import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { fetchPostingDetail } from '@/api/postings';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import ProjectTaskGraphManager from '@/components/ProjectTaskGraphManager';

const AdminPostingDetailPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const [activeTab, setActiveTab] = useState('task-history');

    if (!id) return <div>Invalid Posting ID</div>;

    const { data: posting, isLoading } = useQuery({
        queryKey: ['posting', id],
        queryFn: () => fetchPostingDetail(id),
    });

    if (isLoading) return <div>Loading...</div>;
    if (!posting) return <div>Posting not found</div>;

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold">Gorev Gecmisi: {posting.title}</h1>
                    <p className="text-sm text-muted-foreground">{posting.projectName} - {posting.category}</p>
                </div>
                <Button variant="outline" asChild>
                    <Link to="/admin/postings">Back to List</Link>
                </Button>
            </div>

            <Tabs value={activeTab} onValueChange={setActiveTab}>
                <TabsList>
                    <TabsTrigger value="task-history">Gorev Grafigi</TabsTrigger>
                    <TabsTrigger value="applications">Applications</TabsTrigger>
                </TabsList>

                <TabsContent value="task-history" className="space-y-4">
                    <ProjectTaskGraphManager projectId={id} title="Gorev Gecmisi Yonetimi" />
                </TabsContent>

                <TabsContent value="applications" className="space-y-4">
                    <div className="p-4 bg-yellow-50 text-yellow-800 rounded">
                        Use the main <Link to={`/admin/submissions?postingId=${id}`} className="underline font-bold">Submissions Page</Link> for full controls, or view accepted members here.
                    </div>
                </TabsContent>
            </Tabs>
        </div>
    );
};

export default AdminPostingDetailPage;
