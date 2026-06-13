import { useEffect } from 'react';
import { useForm, SubmitHandler } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Link } from 'react-router-dom';
import { useAuthMutations } from '../auth/useAuthMutations';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { mapBackendErrorToMessage } from '@/api/client';
import { isPdfFile } from '@/api/multipart';

const parseLocalizedNumber = (value: unknown) => {
    if (typeof value === 'number') return Number.isFinite(value) ? value : undefined;
    if (typeof value !== 'string') return undefined;
    const normalized = value.trim().replace(',', '.');
    if (!normalized) return undefined;
    const parsed = Number(normalized);
    return Number.isFinite(parsed) ? parsed : undefined;
};

const baseSchema = z.object({
    firstName: z.string().min(2, 'First Name is required'),
    lastName: z.string().min(2, 'Last Name is required'),
    email: z.string().email('Invalid email address'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
    confirmPassword: z.string(),
    department: z.string().min(2, 'Department is required'),
    classYear: z.number().int('Class Year must be an integer').min(1).max(4),
    gpa: z.number().min(0).max(4),
    englishLevel: z.string().min(1, 'English Level is required'),
    cv: z.instanceof(FileList)
        .refine((files) => files.length > 0, 'CV (PDF) is required')
        .refine((files) => isPdfFile(files[0]), 'Only PDF files are allowed')
        .refine((files) => files[0]?.size <= 50 * 1024 * 1024, 'File size must be less than 50MB'),
});

const registerSchema = baseSchema.refine((data) => data.password === data.confirmPassword, {
    message: "Passwords don't match",
    path: ['confirmPassword'],
});

type RegisterFormValues = z.infer<typeof registerSchema>;

const RegisterPage: React.FC = () => {
    const { registerMutation } = useAuthMutations();
    const {
        register,
        handleSubmit,
        setError,
        formState: { errors },
    } = useForm<RegisterFormValues>({
        resolver: zodResolver(registerSchema),
    });

    const onSubmit: SubmitHandler<RegisterFormValues> = (data) => {
        const normalizedClassYear = Number(data.classYear);
        const normalizedGpa = Number(data.gpa);
        if (!Number.isFinite(normalizedClassYear)) {
            setError('classYear', { type: 'manual', message: 'Class Year must be a valid integer.' });
            return;
        }
        if (!Number.isFinite(normalizedGpa)) {
            setError('gpa', { type: 'manual', message: 'GPA must be a valid number (e.g. 3.25).' });
            return;
        }

        const payload = {
            data: {
                email: data.email,
                password: data.password,
                firstName: data.firstName,
                lastName: data.lastName,
                classYear: normalizedClassYear,
                department: data.department,
                englishLevel: data.englishLevel,
                gpa: normalizedGpa,
            },
            cv: data.cv[0],
        };

        if (import.meta.env.DEV) {
            console.debug('[register] payload', payload.data);
        }

        registerMutation.mutate(payload);
    };

    useEffect(() => {
        if (!import.meta.env.DEV || !registerMutation.isError) return;
        const error = registerMutation.error as {
            response?: { status?: number; data?: unknown };
            message?: string;
        };
        console.error('[register] failed response', {
            status: error?.response?.status,
            body: error?.response?.data,
            message: error?.message,
        });
    }, [registerMutation.isError, registerMutation.error]);

    const registerErrorMessage = (() => {
        if (!registerMutation.isError) return '';

        const error = registerMutation.error as {
            response?: { status?: number; data?: { message?: string; errorCode?: string; code?: string } };
            message?: string;
        };

        const code = String(error?.response?.data?.errorCode ?? error?.response?.data?.code ?? '').toUpperCase();
        const backendMessage = String(error?.response?.data?.message ?? '').trim();

        if (error?.response?.status === 409 || code === 'CONFLICT') {
            return 'Bu email zaten kayitli. Giris yapin veya sifre sifirlayin.';
        }

        if (error?.response?.status === 400 && code === 'INVALID_MULTIPART_JSON') {
            return 'Form verisi hatali gonderildi. GPA/ClassYear gibi alanlari kontrol edin.';
        }

        const mapped = mapBackendErrorToMessage(error, 'Kayit istegi basarisiz.');
        if (code && (backendMessage || mapped)) {
            return `[${code}] ${backendMessage || mapped}`;
        }

        return backendMessage || mapped;
    })();

    return (
        <div className="flex min-h-screen items-center justify-center bg-background px-4 py-10">
            <Card className="w-full max-w-2xl border-border/70 bg-card/95 shadow-xl shadow-black/15">
                <CardHeader>
                    <CardTitle className="text-2xl font-bold">Student Registration</CardTitle>
                    <CardDescription>Register with your @ogr.sakarya.edu.tr email to apply for internships.</CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <Label>First Name</Label>
                                <Input {...register('firstName')} className={errors.firstName ? 'border-destructive' : ''} />
                                {errors.firstName && <p className="text-xs text-destructive">{errors.firstName.message}</p>}
                            </div>
                            <div className="space-y-2">
                                <Label>Last Name</Label>
                                <Input {...register('lastName')} className={errors.lastName ? 'border-destructive' : ''} />
                                {errors.lastName && <p className="text-xs text-destructive">{errors.lastName.message}</p>}
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label>Email</Label>
                            <Input
                                {...register('email')}
                                type="email"
                                placeholder="student@ogr.sakarya.edu.tr"
                                className={errors.email ? 'border-destructive' : ''}
                            />
                            {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
                            <p className="text-xs text-muted-foreground">Use your Sakarya University student email.</p>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <Label>Password</Label>
                                <Input {...register('password')} type="password" className={errors.password ? 'border-destructive' : ''} />
                                {errors.password && <p className="text-xs text-destructive">{errors.password.message}</p>}
                            </div>
                            <div className="space-y-2">
                                <Label>Confirm Password</Label>
                                <Input
                                    {...register('confirmPassword')}
                                    type="password"
                                    className={errors.confirmPassword ? 'border-destructive' : ''}
                                />
                                {errors.confirmPassword && <p className="text-xs text-destructive">{errors.confirmPassword.message}</p>}
                            </div>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <Label>Department</Label>
                                <Input
                                    {...register('department')}
                                    placeholder="Computer Engineering"
                                    className={errors.department ? 'border-destructive' : ''}
                                />
                                {errors.department && <p className="text-xs text-destructive">{errors.department.message}</p>}
                            </div>
                            <div className="space-y-2">
                                <Label>Class Year</Label>
                                <Input
                                    {...register('classYear', {
                                        setValueAs: (value) => parseLocalizedNumber(value) ?? Number.NaN,
                                    })}
                                    type="number"
                                    min="1"
                                    max="4"
                                    className={errors.classYear ? 'border-destructive' : ''}
                                />
                                {errors.classYear && <p className="text-xs text-destructive">{errors.classYear.message}</p>}
                            </div>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <Label>GPA</Label>
                                <Input
                                    {...register('gpa', {
                                        setValueAs: (value) => parseLocalizedNumber(value) ?? Number.NaN,
                                    })}
                                    type="text"
                                    inputMode="decimal"
                                    placeholder="3.50"
                                    className={errors.gpa ? 'border-destructive' : ''}
                                />
                                {errors.gpa && <p className="text-xs text-destructive">{errors.gpa.message}</p>}
                            </div>
                            <div className="space-y-2">
                                <Label>English Level</Label>
                                <Input
                                    {...register('englishLevel')}
                                    placeholder="B1, B2, etc."
                                    className={errors.englishLevel ? 'border-destructive' : ''}
                                />
                                {errors.englishLevel && <p className="text-xs text-destructive">{errors.englishLevel.message}</p>}
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label>CV (PDF)</Label>
                            <Input {...register('cv')} type="file" accept="application/pdf,.pdf" className={errors.cv ? 'border-destructive' : ''} />
                            {errors.cv && <p className="text-xs text-destructive">{errors.cv.message}</p>}
                            <p className="text-xs text-muted-foreground">Upload your CV in PDF format (Max 50MB).</p>
                        </div>

                        {registerMutation.isError && (
                            <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700 dark:border-red-500/30 dark:bg-red-500/10 dark:text-red-300">Registration failed. {registerErrorMessage}</div>
                        )}

                        <Button type="submit" className="w-full" disabled={registerMutation.isPending}>
                            {registerMutation.isPending ? 'Registering...' : 'Register'}
                        </Button>
                    </form>
                </CardContent>
                <CardFooter className="justify-center">
                    <p className="text-sm text-muted-foreground">
                        Already have an account?{' '}
                        <Link to="/login" className="text-primary hover:underline">
                            Login
                        </Link>
                    </p>
                </CardFooter>
            </Card>
        </div>
    );
};

export default RegisterPage;
