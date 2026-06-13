import { TaskGraphBranch, TaskGraphEdge, TaskGraphNode } from '@/api/adminTimeline';

export interface BranchLane {
    branchKey: string;
    branchName: string;
    ownerUserId: number | null;
    branchIndex: number;
    lane: number;
    x: number;
}

export interface CommitPoint {
    node: TaskGraphNode;
    x: number;
    y: number;
    index: number;
}

export interface GraphLayoutResult {
    width: number;
    height: number;
    centerX: number;
    rowHeight: number;
    paddingY: number;
    lanes: BranchLane[];
    commits: CommitPoint[];
    commitByNodeId: Map<string, CommitPoint>;
    nodeByTaskId: Map<string, TaskGraphNode>;
    edges: TaskGraphEdge[];
}

export interface GraphLayoutOptions {
    rowHeight?: number;
    laneGap?: number;
    paddingX?: number;
    paddingY?: number;
    newestOnTop?: boolean;
}

const DEFAULT_OPTIONS: Required<GraphLayoutOptions> = {
    rowHeight: 88,
    laneGap: 92,
    paddingX: 48,
    paddingY: 20,
    newestOnTop: true,
};

const normalizeNumber = (value: unknown): number | null => {
    if (value === null || value === undefined) return null;
    if (typeof value === 'number' && Number.isFinite(value)) return value;
    if (typeof value === 'string' && value.trim()) {
        const parsed = Number(value);
        return Number.isFinite(parsed) ? parsed : null;
    }
    return null;
};

const normalizeBranch = (branch: TaskGraphBranch): TaskGraphBranch => ({
    branchKey: String(branch.branchKey ?? 'MAIN').trim().toUpperCase() || 'MAIN',
    branchName: String(branch.branchName ?? branch.branchKey ?? 'MAIN').trim() || 'MAIN',
    ownerUserId: normalizeNumber(branch.ownerUserId),
});

const zigzagLane = (index: number): number => {
    const step = Math.floor(index / 2) + 1;
    return index % 2 === 0 ? step : -step;
};

const sortUsers = (a: TaskGraphBranch, b: TaskGraphBranch) => {
    const aUser = a.ownerUserId ?? Number.MAX_SAFE_INTEGER;
    const bUser = b.ownerUserId ?? Number.MAX_SAFE_INTEGER;
    if (aUser !== bUser) return aUser - bUser;
    return a.branchKey.localeCompare(b.branchKey, 'tr');
};

const ensureMainFirst = (branches: TaskGraphBranch[]) => {
    const normalized = new Map<string, TaskGraphBranch>();
    branches.forEach((branch) => {
        const row = normalizeBranch(branch);
        normalized.set(row.branchKey, row);
    });

    if (!normalized.has('MAIN')) {
        normalized.set('MAIN', {
            branchKey: 'MAIN',
            branchName: 'Main',
            ownerUserId: null,
        });
    }

    const all = Array.from(normalized.values());
    const main = all.find((branch) => branch.branchKey === 'MAIN');
    const users = all.filter((branch) => branch.branchKey !== 'MAIN').sort(sortUsers);
    return {
        main,
        users,
    };
};

const sortNodesByTimeline = (nodes: TaskGraphNode[], newestOnTop: boolean) => {
    const sorted = [...nodes];
    sorted.sort((a, b) => {
        const aTime = new Date(a.createdAt ?? 0).getTime();
        const bTime = new Date(b.createdAt ?? 0).getTime();
        if (aTime !== bTime) {
            return newestOnTop ? bTime - aTime : aTime - bTime;
        }
        return String(a.taskId).localeCompare(String(b.taskId), 'tr');
    });
    return sorted;
};

export const buildGraphLayout = (
    nodes: TaskGraphNode[],
    edges: TaskGraphEdge[],
    branches: TaskGraphBranch[],
    options?: GraphLayoutOptions,
): GraphLayoutResult => {
    const config = { ...DEFAULT_OPTIONS, ...(options ?? {}) };

    const branchSource = [...branches];
    const knownBranchKeys = new Set(branchSource.map((branch) => String(branch.branchKey ?? 'MAIN').trim().toUpperCase() || 'MAIN'));
    nodes.forEach((node) => {
        const branchKey = String(node.branchKey ?? 'MAIN').trim().toUpperCase() || 'MAIN';
        if (!knownBranchKeys.has(branchKey)) {
            knownBranchKeys.add(branchKey);
            branchSource.push({
                branchKey,
                branchName: branchKey,
                ownerUserId: normalizeNumber(node.assignedToUserId),
            });
        }
    });

    const { main, users } = ensureMainFirst(branchSource);
    const lanes: BranchLane[] = [];

    if (main) {
        lanes.push({
            branchKey: main.branchKey,
            branchName: main.branchName,
            ownerUserId: normalizeNumber(main.ownerUserId),
            branchIndex: 0,
            lane: 0,
            x: 0,
        });
    }

    users.forEach((branch, index) => {
        lanes.push({
            branchKey: branch.branchKey,
            branchName: branch.branchName,
            ownerUserId: normalizeNumber(branch.ownerUserId),
            branchIndex: index + 1,
            lane: zigzagLane(index),
            x: 0,
        });
    });

    const maxAbsLane = lanes.length > 0 ? Math.max(...lanes.map((lane) => Math.abs(lane.lane))) : 0;
    const centerX = config.paddingX + maxAbsLane * config.laneGap;

    lanes.forEach((lane) => {
        lane.x = centerX + lane.lane * config.laneGap;
    });

    const laneByBranchKey = new Map<string, BranchLane>();
    lanes.forEach((lane) => laneByBranchKey.set(lane.branchKey, lane));

    const sortedNodes = sortNodesByTimeline(nodes, config.newestOnTop);
    const commits: CommitPoint[] = sortedNodes.map((node, index) => {
        const branchKey = String(node.branchKey ?? 'MAIN').trim().toUpperCase() || 'MAIN';
        const lane = laneByBranchKey.get(branchKey);
        return {
            node,
            x: lane?.x ?? centerX,
            y: config.paddingY + index * config.rowHeight + config.rowHeight / 2,
            index,
        };
    });

    const commitByNodeId = new Map<string, CommitPoint>();
    commits.forEach((commit) => commitByNodeId.set(String(commit.node.nodeId), commit));

    const nodeByTaskId = new Map<string, TaskGraphNode>();
    sortedNodes.forEach((node) => nodeByTaskId.set(String(node.taskId), node));

    const width = config.paddingX * 2 + Math.max(1, maxAbsLane * 2) * config.laneGap;
    const height = Math.max(config.rowHeight + config.paddingY * 2, sortedNodes.length * config.rowHeight + config.paddingY * 2);

    return {
        width,
        height,
        centerX,
        rowHeight: config.rowHeight,
        paddingY: config.paddingY,
        lanes,
        commits,
        commitByNodeId,
        nodeByTaskId,
        edges,
    };
};
