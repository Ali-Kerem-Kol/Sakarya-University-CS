import React from 'react';
import { cn } from '@/lib/utils';
import wordmarkLight from '@/assets/32bit-wordmark-light.png';
import wordmarkDark from '@/assets/32bit-wordmark-dark.png';

interface BrandWordmarkProps {
    className?: string;
    imageClassName?: string;
}

const BrandWordmark: React.FC<BrandWordmarkProps> = ({ className, imageClassName }) => {
    return (
        <span className={cn('inline-flex items-center', className)}>
            <img src={wordmarkDark} alt="32bit" className={cn('h-7 w-auto dark:hidden', imageClassName)} />
            <img src={wordmarkLight} alt="32bit" className={cn('hidden h-7 w-auto dark:block', imageClassName)} />
        </span>
    );
};

export default BrandWordmark;

