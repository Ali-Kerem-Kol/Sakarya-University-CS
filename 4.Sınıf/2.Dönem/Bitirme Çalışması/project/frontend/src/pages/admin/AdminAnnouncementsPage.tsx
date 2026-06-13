import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchAdminAnnouncements, createAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/admin';
import { Announcement } from '@/types';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from "@/hooks/use-toast";
import { Plus, Edit, Trash2 } from 'lucide-react';

const AdminAnnouncementsPage: React.FC = () => {
    const queryClient = useQueryClient();
    const { toast } = useToast();
    const [selectedAnnouncement, setSelectedAnnouncement] = useState<Announcement | null>(null);
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');

    const { data: announcements, isLoading } = useQuery({
        queryKey: ['admin-announcements'],
        queryFn: fetchAdminAnnouncements
    });

    const createMutation = useMutation({
        mutationFn: createAnnouncement,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-announcements'] });
            setIsDialogOpen(false);
            toast({ title: "Created", description: "Announcement added." });
        }
    });

    const updateMutation = useMutation({
        mutationFn: (data: Partial<Announcement>) => updateAnnouncement(selectedAnnouncement!.id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-announcements'] });
            setIsDialogOpen(false);
            toast({ title: "Updated", description: "Announcement updated." });
        }
    });

    const deleteMutation = useMutation({
        mutationFn: deleteAnnouncement,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-announcements'] });
            toast({ title: "Deleted", description: "Announcement removed." });
        }
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        const payload = { title, content, isPublished: true };
        if (selectedAnnouncement) {
            updateMutation.mutate(payload);
        } else {
            createMutation.mutate(payload);
        }
    };

    const openCreate = () => {
        setSelectedAnnouncement(null);
        setTitle('');
        setContent('');
        setIsDialogOpen(true);
    };

    const openEdit = (ann: Announcement) => {
        setSelectedAnnouncement(ann);
        setTitle(ann.title);
        setContent(ann.content);
        setIsDialogOpen(true);
    };

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold">Announcements</h1>
                <Button onClick={openCreate}><Plus className="w-4 h-4 mr-2" /> Add Announcement</Button>
            </div>

            <div className="bg-white rounded-md border shadow-sm">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Title</TableHead>
                            <TableHead>Content</TableHead>
                            <TableHead className="text-right">Created At</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? <TableRow><TableCell colSpan={4} className="text-center p-4">Loading...</TableCell></TableRow> :
                            announcements?.length === 0 ? <TableRow><TableCell colSpan={4} className="text-center p-8 text-muted-foreground">No announcements found.</TableCell></TableRow> :
                                announcements?.map(ann => (
                                    <TableRow key={ann.id}>
                                        <TableCell className="font-medium">{ann.title}</TableCell>
                                        <TableCell className="truncate max-w-[300px]">{ann.content}</TableCell>
                                        <TableCell className="text-right text-xs text-muted-foreground">{new Date(ann.createdAt).toLocaleDateString()}</TableCell>
                                        <TableCell className="text-right space-x-2">
                                            <Button size="icon" variant="ghost" onClick={() => openEdit(ann)}><Edit className="w-4 h-4" /></Button>
                                            <Button size="icon" variant="ghost" className="text-red-500" onClick={() => deleteMutation.mutate(ann.id)}><Trash2 className="w-4 h-4" /></Button>
                                        </TableCell>
                                    </TableRow>
                                ))}
                    </TableBody>
                </Table>
            </div>

            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>{selectedAnnouncement ? 'Edit Announcement' : 'New Announcement'}</DialogTitle>
                    </DialogHeader>
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div className="space-y-2">
                            <Label>Title</Label>
                            <Input value={title} onChange={e => setTitle(e.target.value)} required />
                        </div>
                        <div className="space-y-2">
                            <Label>Content</Label>
                            <textarea
                                className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                                value={content}
                                onChange={e => setContent(e.target.value)}
                                required
                                rows={5}
                            />
                        </div>
                        <Button type="submit" className="w-full" disabled={createMutation.isPending || updateMutation.isPending}>
                            {selectedAnnouncement ? 'Update' : 'Create'}
                        </Button>
                    </form>
                </DialogContent>
            </Dialog>
        </div>
    );
};

export default AdminAnnouncementsPage;
