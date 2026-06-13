import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { useAuth } from './AuthContext';
import { loginUser, registerUser, verifyUser, forgotPassword, resetPassword } from '../api/auth';

export const useAuthMutations = () => {
    const { login: setAuth } = useAuth();
    const navigate = useNavigate();

    const loginMutation = useMutation({
        mutationFn: loginUser,
        onSuccess: (data) => {
            setAuth(data);
            if (data.role === 'ADMIN') {
                navigate('/admin/overview');
            } else {
                navigate('/user/home');
            }
        },
    });

    const registerMutation = useMutation({
        mutationFn: (payload: { data: any, cv: File }) => registerUser(payload.data, payload.cv),
        onSuccess: () => {
            navigate('/login', { state: { message: 'Registration successful! Please check your email to verify your account.' } });
        },
    });

    const verifyMutation = useMutation({
        mutationFn: verifyUser,
    });

    const forgotPasswordMutation = useMutation({
        mutationFn: forgotPassword,
    });

    const resetPasswordMutation = useMutation({
        mutationFn: resetPassword,
        onSuccess: () => {
            navigate('/login', { state: { message: 'Password reset successful. Please login.' } });
        },
    });

    return {
        loginMutation,
        registerMutation,
        verifyMutation,
        forgotPasswordMutation,
        resetPasswordMutation
    };
};
