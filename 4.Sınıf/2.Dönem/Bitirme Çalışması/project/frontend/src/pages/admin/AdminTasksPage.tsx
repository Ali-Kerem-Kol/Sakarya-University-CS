import React, { useEffect, useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { fetchAdminPostings } from '@/api/admin';
import ProjectTaskGraphManager from '@/components/ProjectTaskGraphManager';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

const AdminTasksPage: React.FC = () => {
    const [category, setCategory] = useState('');
    const [projectId, setProjectId] = useState('');

    const { data: projects, isLoading } = useQuery({
        queryKey: ['admin-postings'],
        queryFn: () => fetchAdminPostings(),
    });

    const categories = useMemo(() => {
        const unique = new Set<string>();
        (projects ?? []).forEach((project) => {
            const key = String(project.category ?? '').trim();
            if (key) unique.add(key);
        });
        return Array.from(unique);
    }, [projects]);

    const filteredProjects = useMemo(() => {
        if (!category) return projects ?? [];
        return (projects ?? []).filter((project) => String(project.category ?? '').trim() === category);
    }, [category, projects]);

    useEffect(() => {
        if (!category && categories.length > 0) {
            setCategory(categories[0]);
        }
    }, [categories, category]);

    useEffect(() => {
        if (filteredProjects.length === 0) {
            setProjectId('');
            return;
        }

        if (!filteredProjects.some((project) => String(project.id) === projectId)) {
            setProjectId(String(filteredProjects[0].id));
        }
    }, [filteredProjects, projectId]);

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold">Gorev Gecmisi Yonetimi</h1>
                <p className="text-sm text-slate-500">Gorevleri grafik uzerinden olusturabilir, duzenleyebilir, silebilir ve degerlendirebilirsiniz.</p>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Project Selection</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                    <div className="max-w-sm space-y-2">
                        <Label>Category</Label>
                        <Select value={category} onValueChange={setCategory}>
                            <SelectTrigger>
                                <SelectValue placeholder={isLoading ? 'Loading...' : 'Select category'} />
                            </SelectTrigger>
                            <SelectContent>
                                {categories.map((categoryValue) => (
                                    <SelectItem key={categoryValue} value={categoryValue}>
                                        {categoryValue}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>

                    <div className="max-w-sm space-y-2">
                        <Label>Project</Label>
                        <Select value={projectId} onValueChange={setProjectId}>
                            <SelectTrigger>
                                <SelectValue placeholder={isLoading ? 'Loading...' : 'Select project'} />
                            </SelectTrigger>
                            <SelectContent>
                                {filteredProjects.map((project) => (
                                    <SelectItem key={String(project.id)} value={String(project.id)}>
                                        {project.title}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>

                    {projectId && (
                        <Button variant="outline" asChild>
                            <Link to={`/admin/postings/${projectId}/manage`}>Detayli Gorev Ekranini Ac</Link>
                        </Button>
                    )}
                </CardContent>
            </Card>

            {projectId ? (
                <ProjectTaskGraphManager projectId={projectId} />
            ) : (
                <Card>
                    <CardContent className="py-10 text-center text-slate-500">Gorev gecmisini yonetmek icin bir proje secin.</CardContent>
                </Card>
            )}
        </div>
    );
};

export default AdminTasksPage;
