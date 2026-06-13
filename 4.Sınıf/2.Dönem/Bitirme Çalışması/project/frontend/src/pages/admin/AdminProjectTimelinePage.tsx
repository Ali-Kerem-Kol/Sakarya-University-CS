import React, { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { fetchAdminPostings } from '@/api/admin';
import { getTimelineAssignmentDetail, listProjectTimeline, ProjectTimelineEvent } from '@/api/adminTimeline';
import { openDocument } from '@/api/client';
import { Timeline } from '@/components/Timeline';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

const formatDate = (value?: string | null) => (value ? new Date(value).toLocaleString() : '-');

const StatsBadge: React.FC<{ label: string; value: number }> = ({ label, value }) => (
    <Badge variant="outline" className="text-xs">
        {label}: {String(value)}
    </Badge>
);

const AdminProjectTimelinePage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const [expandedTaskId, setExpandedTaskId] = useState<string>('');

    const { data: projects } = useQuery({
        queryKey: ['admin-postings'],
        queryFn: () => fetchAdminPostings(),
    });

    const { data: timeline, isLoading } = useQuery({
        queryKey: ['admin-project-timeline', id],
        queryFn: () => listProjectTimeline(String(id)),
        enabled: Boolean(id),
    });

    const expandedEvent = (timeline ?? []).find((item) => item.id === expandedTaskId);
    const { data: expandedDetail } = useQuery({
        queryKey: ['admin-project-timeline-detail', expandedEvent?.latestAssignmentId],
        queryFn: () => getTimelineAssignmentDetail(String(expandedEvent?.latestAssignmentId ?? '')),
        enabled: Boolean(expandedEvent?.latestAssignmentId),
    });

    const project = (projects ?? []).find((item) => String(item.id) === String(id));

    const renderCard = (event: ProjectTimelineEvent) => {
        const isExpanded = expandedTaskId === event.id;
        return (
            <Card className="bg-white">
                <CardHeader className="pb-2">
                    <div className="flex flex-wrap items-start justify-between gap-2">
                        <CardTitle className="text-base">{event.taskTitle}</CardTitle>
                        <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => setExpandedTaskId(isExpanded ? '' : event.id)}
                        >
                            {isExpanded ? 'Hide details' : 'Show details'}
                        </Button>
                    </div>
                    <p className="text-xs text-slate-500">
                        {event.actor} | {formatDate(event.eventAt)} | Assign: {event.assignType}
                    </p>
                </CardHeader>
                <CardContent className="space-y-3">
                    <div className="flex flex-wrap gap-2">
                        <StatsBadge label="assigned" value={event.stats.assigned} />
                        <StatsBadge label="approved" value={event.stats.approved} />
                        <StatsBadge label="rejected" value={event.stats.rejected} />
                        <StatsBadge label="pending" value={event.stats.pending} />
                    </div>

                    {isExpanded && (
                        <div className="space-y-3 border-t pt-3">
                            <div className="grid gap-3 md:grid-cols-2 text-sm">
                                <div>
                                    <p className="font-medium text-slate-700">Tamamlayanlar</p>
                                    {event.completedUsers.length === 0 ? (
                                        <p className="text-slate-500">Yok</p>
                                    ) : (
                                        <p className="text-slate-600">{event.completedUsers.join(', ')}</p>
                                    )}
                                </div>
                                <div>
                                    <p className="font-medium text-slate-700">Bekleyenler</p>
                                    {event.waitingUsers.length === 0 ? (
                                        <p className="text-slate-500">Yok</p>
                                    ) : (
                                        <p className="text-slate-600">{event.waitingUsers.join(', ')}</p>
                                    )}
                                </div>
                            </div>

                            {(expandedDetail?.taskAttachments ?? []).length > 0 && (
                                <div className="space-y-2">
                                    <p className="text-sm font-medium text-slate-700">Attachments</p>
                                    {expandedDetail?.taskAttachments.map((file) => (
                                        <div key={file.id} className="flex items-center justify-between rounded border p-2 text-sm">
                                            <span className="truncate">?? {file.fileName}</span>
                                            <div className="flex gap-2">
                                                <Button size="sm" variant="outline" onClick={() => openDocument(file.downloadUrl, { mode: 'view', contentType: file.contentType || 'application/pdf' })}>View</Button>
                                                <Button size="sm" variant="outline" onClick={() => openDocument(file.downloadUrl, { mode: 'download', defaultFileName: file.fileName })}>Download</Button>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    )}
                </CardContent>
            </Card>
        );
    };

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-bold">Admin Project Timeline</h1>
                    <p className="text-sm text-slate-500">{project?.title ?? `Project #${String(id ?? '-')}`}</p>
                </div>
                <Link to="/admin/tasks">
                    <Button variant="outline">Back to Gorevler</Button>
                </Link>
            </div>

            {isLoading ? (
                <div className="text-sm text-slate-500">Yukleniyor...</div>
            ) : (
                <Timeline
                    items={timeline ?? []}
                    getKey={(item) => item.id}
                    renderItem={renderCard}
                    emptyMessage="No activity yet"
                />
            )}
        </div>
    );
};

export default AdminProjectTimelinePage;
