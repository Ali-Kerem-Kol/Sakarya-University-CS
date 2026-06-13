import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getAdminProjectTaskGraph, getMyTaskGraph } from '@/api/adminTimeline';
import { buildGraphLayout } from '@/lib/graphLayout';

interface UseTaskGraphAdminArgs {
    mode: 'admin';
    projectId: string | number;
}

interface UseTaskGraphUserArgs {
    mode: 'user';
    projectId?: string | number;
}

type UseTaskGraphArgs = UseTaskGraphAdminArgs | UseTaskGraphUserArgs;

export const useTaskGraph = (args: UseTaskGraphArgs) => {
    const query = useQuery({
        queryKey: args.mode === 'admin'
            ? ['task-graph', 'admin', String(args.projectId)]
            : ['task-graph', 'user', args.projectId ? String(args.projectId) : 'current'],
        queryFn: () => {
            if (args.mode === 'admin') {
                return getAdminProjectTaskGraph(args.projectId);
            }
            return getMyTaskGraph(args.projectId);
        },
        enabled: args.mode === 'admin' ? Boolean(args.projectId) : true,
    });

    const layout = useMemo(() => {
        if (!query.data) {
            return buildGraphLayout([], [], []);
        }

        return buildGraphLayout(query.data.nodes, query.data.edges, query.data.branches, {
            newestOnTop: true,
        });
    }, [query.data]);

    return {
        ...query,
        graph: query.data,
        layout,
    };
};
