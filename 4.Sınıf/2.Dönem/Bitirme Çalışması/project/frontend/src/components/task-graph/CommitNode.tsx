import React from 'react';
import { TaskGraphNode } from '@/api/adminTimeline';
import { getStatusNodeStyle } from '@/lib/branchColor';
import { cn } from '@/lib/utils';

interface CommitNodeProps {
    node: TaskGraphNode;
    x: number;
    y: number;
    selected: boolean;
    colorOverride?: {
        fill: string;
        stroke: string;
        strokeWidth?: number;
    };
    statusMarkerColor?: string;
    onClick: () => void;
    onHover: (event: React.MouseEvent<HTMLButtonElement>, node: TaskGraphNode) => void;
    onLeave: () => void;
}

export const CommitNode: React.FC<CommitNodeProps> = ({
    node,
    x,
    y,
    selected,
    colorOverride,
    statusMarkerColor,
    onClick,
    onHover,
    onLeave,
}) => {
    const baseStyle = getStatusNodeStyle(node.status, node.branchKey);
    const style = colorOverride
        ? {
            fill: colorOverride.fill,
            stroke: colorOverride.stroke,
            strokeWidth: colorOverride.strokeWidth ?? baseStyle.strokeWidth,
        }
        : baseStyle;

    return (
        <button
            type="button"
            className={cn(
                'absolute -translate-x-1/2 -translate-y-1/2 h-6 w-6 rounded-full transition-all overflow-visible',
                selected
                    ? 'shadow-lg'
                    : 'hover:ring-2 hover:ring-offset-2 hover:ring-slate-300 hover:shadow-md hover:ring-offset-background dark:hover:ring-slate-500/70'
            )}
            style={{
                left: x,
                top: y,
                backgroundColor: style.fill,
                borderColor: style.stroke,
                borderWidth: style.strokeWidth,
            }}
            onClick={onClick}
            onMouseEnter={(event) => onHover(event, node)}
            onMouseMove={(event) => onHover(event, node)}
            onMouseLeave={onLeave}
            aria-label={node.title}
        >
            {selected && (
                <span
                    aria-hidden
                    className="pointer-events-none absolute -inset-[6px] rounded-md border-2 border-sky-400/90"
                />
            )}
            {statusMarkerColor && (
                <span
                    aria-hidden
                    className="pointer-events-none absolute -inset-1 rounded-full border-2"
                    style={{ borderColor: statusMarkerColor, opacity: 0.68 }}
                />
            )}
        </button>
    );
};
