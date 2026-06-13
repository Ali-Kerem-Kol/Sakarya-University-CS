import { Component, ErrorInfo, ReactNode } from 'react';
import { Button } from '@/components/ui/button';
import { AlertCircle } from 'lucide-react';

interface Props {
    children: ReactNode;
}

interface State {
    hasError: boolean;
    error: Error | null;
}

export class ErrorBoundary extends Component<Props, State> {
    public state: State = {
        hasError: false,
        error: null,
    };

    public static getDerivedStateFromError(error: Error): State {
        return { hasError: true, error };
    }

    public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
        console.error('Uncaught error:', error, errorInfo);
    }

    public render() {
        if (this.state.hasError) {
            return (
                <div className="min-h-screen flex flex-col items-center justify-center p-6 bg-slate-50 text-center">
                    <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mb-4">
                        <AlertCircle className="w-8 h-8 text-red-600" />
                    </div>
                    <h1 className="text-2xl font-bold text-slate-900 mb-2">Something went wrong</h1>
                    <p className="text-slate-500 max-w-md mb-6">
                        We encountered an unexpected error while rendering this page.
                    </p>
                    <div className="bg-white p-4 rounded-lg border border-slate-200 shadow-sm max-w-lg w-full text-left overflow-auto mb-6">
                        <p className="text-xs font-mono text-red-500 whitespace-pre-wrap">
                            {this.state.error?.message}
                        </p>
                    </div>
                    <Button onClick={() => window.location.reload()} variant="outline">
                        Reload Page
                    </Button>
                </div>
            );
        }

        return this.props.children;
    }
}
