import React from 'react';

interface TimelineProps<T> {
    items: T[];
    getKey: (item: T) => string;
    renderItem: (item: T) => React.ReactNode;
    emptyMessage?: string;
}

export function Timeline<T>({ items, getKey, renderItem, emptyMessage = 'No activity yet' }: TimelineProps<T>) {
    if (items.length === 0) {
        return (
            <div className="rounded-md border border-dashed border-slate-300 bg-white p-8 text-center text-sm text-slate-500">
                {emptyMessage}
            </div>
        );
    }

    return (
        <div className="relative pl-8">
            <div className="absolute bottom-0 left-3 top-1 w-px bg-slate-200" />
            <div className="space-y-4">
                {items.map((item) => (
                    <div key={getKey(item)} className="relative">
                        <span className="absolute -left-[23px] top-5 h-3 w-3 rounded-full border border-blue-200 bg-blue-500" />
                        {renderItem(item)}
                    </div>
                ))}
            </div>
        </div>
    );
}
