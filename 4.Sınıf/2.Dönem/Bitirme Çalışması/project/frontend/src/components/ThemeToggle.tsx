import React from 'react';
import { Moon, Sun } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { useTheme } from '@/theme/ThemeProvider';

interface ThemeToggleProps {
    floating?: boolean;
}

const ThemeToggle: React.FC<ThemeToggleProps> = ({ floating = false }) => {
    const { theme, toggleTheme } = useTheme();

    return (
        <Button
            type="button"
            variant="outline"
            size="icon"
            onClick={toggleTheme}
            title={theme === 'dark' ? 'Acik temaya gec' : 'Koyu temaya gec'}
            className={cn(
                'rounded-full border-border/80 bg-card/90 text-foreground shadow-sm backdrop-blur hover:bg-accent',
                floating && 'fixed bottom-5 right-5 z-[70] h-11 w-11 border-border shadow-lg shadow-black/15',
            )}
        >
            {theme === 'dark' ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
        </Button>
    );
};

export default ThemeToggle;
