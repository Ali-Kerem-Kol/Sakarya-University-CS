import React, { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { createMailJobByCategory, createMailJobByPosting, createMailJobAllStudents, fetchAdminPostings, MailPayload } from '@/api/admin';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { useToast } from "@/hooks/use-toast";
import { Loader2, Send } from 'lucide-react';
import { PostingCategory } from '@/types';

const AdminMailPage: React.FC = () => {
    const { toast } = useToast();
    const [activeTab, setActiveTab] = useState('category');

    // Form States
    const [subject, setSubject] = useState('');
    const [body, setBody] = useState('');
    const [category, setCategory] = useState<PostingCategory | string>('BACKEND');
    const [postingId, setPostingId] = useState('');
    const [files, setFiles] = useState<FileList | null>(null);

    const { data: postings } = useQuery({
        queryKey: ['admin-postings'],
        queryFn: () => fetchAdminPostings({})
    });

    const mailMutation = useMutation({
        mutationFn: async () => {
            const fileArray = files ? Array.from(files) : undefined;
            const payload: MailPayload = { subject, body, files: fileArray };

            if (!subject || !body) throw new Error("Subject and Body are required.");

            if (activeTab === 'category') {
                await createMailJobByCategory(category, payload);
            } else if (activeTab === 'posting') {
                if (!postingId) throw new Error("Please select a posting.");
                await createMailJobByPosting(postingId, payload);
            } else if (activeTab === 'all') {
                await createMailJobAllStudents(payload);
            }
        },
        onSuccess: () => {
            toast({ title: "Mail Job Created", description: "Emails are being queued." });
            setSubject('');
            setBody('');
            setFiles(null);
        },
        onError: (err: any) => {
            const msg = err.message || err.response?.data?.message || "Could not create mail job.";
            toast({ variant: "destructive", title: "Failed", description: msg });
        }
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        mailMutation.mutate();
    };

    return (
        <div className="max-w-4xl mx-auto space-y-6">
            <h1 className="text-3xl font-bold">Mail Operations</h1>
            <p className="text-muted-foreground">Send bulk emails to candidates.</p>

            <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                    <CardTitle>Compose Message</CardTitle>
                    <CardDescription>Select audience and write your message.</CardDescription>
                </CardHeader>
                <CardContent>
                    <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
                        <TabsList className="grid w-full grid-cols-3 mb-6">
                            <TabsTrigger value="category">By Category</TabsTrigger>
                            <TabsTrigger value="posting">By Posting</TabsTrigger>
                            <TabsTrigger value="all">All Students</TabsTrigger>
                        </TabsList>

                        <form onSubmit={handleSubmit} className="space-y-6">
                            <TabsContent value="category" className="mt-0">
                                <div className="space-y-2">
                                    <Label>Category</Label>
                                    <Select value={category} onValueChange={setCategory}>
                                        <SelectTrigger><SelectValue /></SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="BACKEND">Backend</SelectItem>
                                            <SelectItem value="FRONTEND">Frontend</SelectItem>
                                            <SelectItem value="MOBILE">Mobile</SelectItem>
                                            <SelectItem value="FULLSTACK">Fullstack</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>
                            </TabsContent>

                            <TabsContent value="posting" className="mt-0">
                                <div className="space-y-2">
                                    <Label>Select Posting</Label>
                                    <Select value={postingId} onValueChange={setPostingId}>
                                        <SelectTrigger><SelectValue placeholder="Choose a job..." /></SelectTrigger>
                                        <SelectContent>
                                            {postings?.map(p => (
                                                <SelectItem key={p.id} value={p.id}>{p.title}</SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                </div>
                            </TabsContent>

                            <TabsContent value="all" className="mt-0">
                                <div className="p-3 bg-amber-50 text-amber-800 rounded-md text-sm border border-amber-200">
                                    Warning: This will send an email to every registered student in the system. Use wisely.
                                </div>
                            </TabsContent>

                            {/* Common Fields */}
                            <div className="space-y-2">
                                <Label>Subject</Label>
                                <Input value={subject} onChange={e => setSubject(e.target.value)} required placeholder="Invitation for interview..." />
                            </div>

                            <div className="space-y-2">
                                <Label>Body</Label>
                                <textarea
                                    className="flex min-h-[150px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                                    value={body}
                                    onChange={e => setBody(e.target.value)}
                                    required
                                    placeholder="Dear candidate..."
                                />
                            </div>

                            <div className="space-y-2">
                                <Label>Attachments (Optional)</Label>
                                <Input type="file" multiple onChange={e => setFiles(e.target.files)} />
                            </div>

                            <Button type="submit" className="w-full" disabled={mailMutation.isPending}>
                                {mailMutation.isPending ? <Loader2 className="animate-spin mr-2" /> : <Send className="w-4 h-4 mr-2" />}
                                Send Emails
                            </Button>
                        </form>
                    </Tabs>
                </CardContent>
            </Card>
        </div>
    );
};

export default AdminMailPage;
