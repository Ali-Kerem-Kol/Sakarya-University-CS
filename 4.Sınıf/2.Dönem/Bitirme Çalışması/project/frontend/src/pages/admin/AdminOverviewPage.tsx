import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { downloadFile } from '../../api/client';
import { fetchApplicantOverview, fetchUserDocuments } from '@/api/admin';
import { ApplicantOverviewResponse, ProjectDocument } from '../../types';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow
} from '@/components/ui/table';
import { Card, CardHeader, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
    Search,
    Download,
    CheckCircle2,
    XCircle,
    Clock,
    ChevronLeft,
    ChevronRight,
    ExternalLink,
    FileText,
    ArrowUpRight
} from 'lucide-react';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue
} from '@/components/ui/select';
import {
    Drawer,
    DrawerContent,
    DrawerDescription,
    DrawerHeader,
    DrawerTitle,
} from "@/components/ui/drawer";
import { cn } from '@/lib/utils';
import { Link } from 'react-router-dom';

const AdminOverviewPage: React.FC = () => {
    const [page, setPage] = useState(0);
    const [q, setQ] = useState('');
    const [status, setStatus] = useState<string>('ALL');
    const [positionKey, setPositionKey] = useState('');
    const [hasCv, setHasCv] = useState<string>('ALL');

    const [selectedApplicant, setSelectedApplicant] = useState<ApplicantOverviewResponse | null>(null);

    const { data, isLoading } = useQuery({
        queryKey: ['admin-overview', page, q, status, positionKey, hasCv],
        queryFn: () => {
            const params: any = { page };
            if (q) params.q = q;
            if (status !== 'ALL') params.status = status;
            if (positionKey) params.positionKey = positionKey;
            if (hasCv !== 'ALL') params.hasCv = hasCv === 'true';

            return fetchApplicantOverview(params);
        },
    });

    const getStatusBadge = (status?: string) => {
        if (!status) return <Badge variant="secondary">NONE</Badge>;
        const variants: Record<string, string> = {
            SUBMITTED: 'bg-blue-100 text-blue-700',
            IN_REVIEW: 'bg-amber-100 text-amber-700',
            APPROVED: 'bg-green-100 text-green-700',
            REJECTED: 'bg-red-100 text-red-700',
        };
        return <Badge className={cn("font-medium", variants[status])}>{status}</Badge>;
    };

    const handleExport = async () => {
        const params: Record<string, string | number | boolean | undefined> = {};
        if (q) params.q = q;
        if (status !== 'ALL') params.status = status;
        if (positionKey) params.positionKey = positionKey;
        if (hasCv !== 'ALL') params.hasCv = hasCv === 'true';

        await downloadFile('/admin/overview/applicants/export', 'overview-export.csv', params);
    };

    return (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-2 duration-500">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Applicant Overview</h1>
                    <p className="text-slate-500 text-sm mt-1">Monitor all candidates and their application status.</p>
                </div>
                <Button onClick={handleExport} variant="outline" className="bg-white">
                    <Download className="w-4 h-4 mr-2" />
                    Export CSV
                </Button>
            </div>

            <Card className="border-slate-200 shadow-sm overflow-hidden">
                <CardHeader className="bg-white border-b border-slate-100 py-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                        <div className="relative col-span-1 lg:col-span-2">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                            <Input
                                placeholder="Search by name or email..."
                                className="pl-10 bg-slate-50/50 border-slate-200 focus:bg-white"
                                value={q}
                                onChange={(e) => setQ(e.target.value)}
                            />
                        </div>
                        <Input
                            placeholder="Position key..."
                            className="bg-slate-50/50 border-slate-200 focus:bg-white"
                            value={positionKey}
                            onChange={(e) => setPositionKey(e.target.value)}
                        />
                        <Select value={status} onValueChange={setStatus}>
                            <SelectTrigger className="bg-slate-50/50 border-slate-200">
                                <SelectValue placeholder="Status Filter" />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="ALL">All Statuses</SelectItem>
                                <SelectItem value="SUBMITTED">Submitted</SelectItem>
                                <SelectItem value="IN_REVIEW">In Review</SelectItem>
                                <SelectItem value="APPROVED">Approved</SelectItem>
                                <SelectItem value="REJECTED">Rejected</SelectItem>
                            </SelectContent>
                        </Select>

                        <TriStateSelect label="CV" value={hasCv} onChange={setHasCv} />
                    </div>
                </CardHeader>
                <CardContent className="p-0">
                    <Table>
                        <TableHeader className="bg-slate-50/50">
                            <TableRow className="hover:bg-transparent border-slate-100">
                                <TableHead className="w-[250px] font-semibold text-slate-700">Applicant</TableHead>
                                <TableHead className="font-semibold text-slate-700">Latest Status</TableHead>
                                <TableHead className="font-semibold text-slate-700">Position</TableHead>
                                <TableHead className="font-semibold text-slate-700">Documents</TableHead>
                                <TableHead className="text-right font-semibold text-slate-700">Action</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {isLoading ? (
                                <TableRow>
                                    <TableCell colSpan={5} className="text-center py-20">
                                        <div className="flex flex-col items-center gap-2 text-slate-400">
                                            <Clock className="w-8 h-8 animate-pulse" />
                                            <span className="text-sm font-medium">Loading participants...</span>
                                        </div>
                                    </TableCell>
                                </TableRow>
                            ) : data?.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={5} className="text-center py-20 text-slate-500">
                                        No applicants found matching your criteria.
                                    </TableCell>
                                </TableRow>
                            ) : (
                                data?.map((applicant) => (
                                    <TableRow
                                        key={applicant.userId}
                                        className="group hover:bg-blue-50/30 border-slate-100 transition-colors cursor-pointer"
                                        onClick={() => setSelectedApplicant(applicant)}
                                    >
                                        <TableCell>
                                            <div className="flex flex-col">
                                                <span className="font-bold text-slate-900">{applicant.firstName} {applicant.lastName}</span>
                                                <span className="text-xs text-slate-500">{applicant.email}</span>
                                            </div>
                                        </TableCell>
                                        <TableCell>{getStatusBadge(applicant.latestStatus)}</TableCell>
                                        <TableCell>
                                            <span className="text-sm text-slate-600 font-medium">
                                                {applicant.latestPositionKey || '—'}
                                            </span>
                                        </TableCell>
                                        <TableCell>
                                            <div className="flex gap-1.5">
                                                {applicant.hasCv ? (
                                                    <CheckCircle2 className="w-4 h-4 text-green-500" />
                                                ) : (
                                                    <XCircle className="w-4 h-4 text-slate-200" />
                                                )}
                                                <span className="text-[10px] uppercase font-bold text-slate-400">CV</span>
                                            </div>
                                        </TableCell>
                                        <TableCell className="text-right">
                                            <Button variant="ghost" size="icon" className="opacity-0 group-hover:opacity-100 transition-opacity">
                                                <ExternalLink className="w-4 h-4 text-blue-500" />
                                            </Button>
                                        </TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </CardContent>
                <div className="p-4 border-t border-slate-100 flex items-center justify-between bg-white">
                    <p className="text-xs font-medium text-slate-500 uppercase tracking-widest">
                        Showing {data?.number ? (data.number * (data.size ?? 0)) + 1 : 1} - {Math.min(((data?.number ?? 0) + 1) * (data?.size ?? 0), data?.totalElements ?? 0)} of {data?.totalElements ?? 0}
                    </p>
                    <div className="flex gap-2">
                        <Button
                            variant="outline"
                            size="sm"
                            disabled={page === 0}
                            onClick={() => setPage(p => p - 1)}
                            className="h-8 border-slate-200"
                        >
                            <ChevronLeft className="w-4 h-4" />
                        </Button>
                        <Button
                            variant="outline"
                            size="sm"
                            disabled={page >= (data?.totalPages || 1) - 1}
                            onClick={() => setPage(p => p + 1)}
                            className="h-8 border-slate-200"
                        >
                            <ChevronRight className="w-4 h-4" />
                        </Button>
                    </div>
                </div>
            </Card>

            <ApplicantDrawer
                applicant={selectedApplicant}
                onClose={() => setSelectedApplicant(null)}
            />
        </div>
    );
};

const TriStateSelect = ({ label, value, onChange }: { label: string, value: string, onChange: (v: string) => void }) => (
    <Select value={value} onValueChange={onChange}>
        <SelectTrigger className="bg-slate-50/50 border-slate-200">
            <SelectValue placeholder={`${label}: All`} />
        </SelectTrigger>
        <SelectContent>
            <SelectItem value="ALL">{label}: All</SelectItem>
            <SelectItem value="true">{label}: Yes</SelectItem>
            <SelectItem value="false">{label}: No</SelectItem>
        </SelectContent>
    </Select>
);

const ApplicantDrawer = ({ applicant, onClose }: { applicant: ApplicantOverviewResponse | null, onClose: () => void }) => {
    const { data: details, isLoading } = useQuery({
        queryKey: ['applicant-details', applicant?.userId],
        queryFn: async () => {
            if (!applicant) return null;
            const docs = await fetchUserDocuments(applicant.userId);
            return { documents: docs };
        },
        enabled: !!applicant
    });

    return (
        <Drawer open={!!applicant} onOpenChange={(open) => !open && onClose()}>
            <DrawerContent className="max-h-[90vh]">
                <div className="mx-auto w-full max-w-2xl overflow-y-auto p-6 pt-0">
                    <DrawerHeader className="px-0">
                        <div className="flex items-center justify-between">
                            <div>
                                <DrawerTitle className="text-2xl">{applicant?.firstName} {applicant?.lastName}</DrawerTitle>
                                <DrawerDescription>{applicant?.email}</DrawerDescription>
                            </div>
                            {applicant?.latestApplicationId && (
                                <Button asChild size="sm" className="gap-2">
                                    <Link to={`/admin/applications/${applicant.latestApplicationId}`}>
                                        View Application
                                        <ArrowUpRight className="w-4 h-4" />
                                    </Link>
                                </Button>
                            )}
                        </div>
                    </DrawerHeader>

                    <div className="space-y-8 py-4">
                        <section>
                            <h3 className="text-sm font-bold uppercase tracking-widest text-slate-400 mb-4 flex items-center gap-2">
                                <FileText className="w-4 h-4" />
                                Documents
                            </h3>
                            {isLoading ? (
                                <div className="animate-pulse space-y-2">
                                    <div className="h-10 bg-slate-100 rounded-lg" />
                                    <div className="h-10 bg-slate-100 rounded-lg" />
                                </div>
                            ) : details?.documents.length === 0 ? (
                                <p className="text-sm text-slate-500 italic">No documents uploaded.</p>
                            ) : (
                                <div className="grid gap-2">
                                    {details?.documents.map((doc: ProjectDocument) => (
                                        <div key={doc.id} className="flex items-center justify-between p-3 bg-slate-50 rounded-xl border border-slate-100">
                                            <div className="flex items-center gap-3">
                                                <Badge variant="outline" className="text-[10px]">{doc.documentType}</Badge>
                                                <span className="text-sm font-medium truncate max-w-[200px]">{doc.fileName}</span>
                                            </div>
                                            <Button variant="ghost" size="sm" onClick={() => downloadFile(`/admin/documents/${doc.id}/download`, doc.fileName)}>
                                                <Download className="w-4 h-4" />
                                            </Button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </section>
                    </div>
                </div>
            </DrawerContent>
        </Drawer>
    );
};

export default AdminOverviewPage;
