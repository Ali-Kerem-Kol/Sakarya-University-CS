import React from 'react';

interface CommitEdgeProps {
    edgeId: string;
    kind?: 'vertical' | 'fork' | 'curve';
    fromX: number;
    fromY: number;
    toX: number;
    toY: number;
    color: string;
}

export const CommitEdge: React.FC<CommitEdgeProps> = ({ edgeId, kind = 'curve', fromX, fromY, toX, toY, color }) => {
    if (kind === 'fork') {
        return <line key={edgeId} x1={fromX} y1={fromY} x2={toX} y2={toY} stroke={color} strokeWidth={2.1} opacity={0.95} />;
    }

    if (fromX === toX) {
        return <line key={edgeId} x1={fromX} y1={fromY} x2={toX} y2={toY} stroke={color} strokeWidth={2} opacity={0.94} />;
    }

    if (kind === 'vertical') {
        return <line key={edgeId} x1={fromX} y1={fromY} x2={toX} y2={toY} stroke={color} strokeWidth={2} opacity={0.94} />;
    }

    const distanceY = toY - fromY;
    const curveY = fromY + distanceY / 2;
    const path = `M ${fromX} ${fromY} C ${fromX} ${curveY}, ${toX} ${curveY}, ${toX} ${toY}`;

    return <path key={edgeId} d={path} fill="none" stroke={color} strokeWidth={2} strokeDasharray="4 3" opacity={0.92} />;
};
